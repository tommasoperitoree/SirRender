package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.ulong
import core.ImageTracer
import core.OrthogonalCamera
import core.PathTracer
import core.PerspectiveCamera
import core.PointLight
import core.PointLightRenderer
import core.ProgressBar
import core.World
import geometry.Plane
import geometry.Sphere
import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.HDRImage
import materials.Material
import materials.UniformPigment
import math.PCG
import math.Point
import math.Vec
import math.rotationY
import math.rotationZ
import math.scaling
import math.translation
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * CLI command that renders a predefined demo scene using a path tracer.
 *
 * The generated HDR image is saved as a PFM file. Optionally, the command
 * can also convert and save the result as a PNG image with the command `-r`.
 *
 */
class Demo : CliktCommand(
	name = "demo"
) {
	override fun help(context: Context) = "Generate a demo image"
	
	val baseName: String? by option(
		"--name", help = "Output file base name (default: timestamp)"
	)
	val width: Int by option(
		"--width", "-w", help = "Image width in pixels"
	).int().default(1280)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(720)
	val camera: String by option(
		"--camera", "-c", help = "Camera type (projection): Orthogonal or Perspective"
	).choice("Orthogonal", "Perspective", ignoreCase = true).default("Perspective")
	val outputDir: String by option(
		"--output-dir", "-o", help = "Output directory for images"
	).default("./outputs/scenes")
	val observerZAngle: Float by option(
		"--observer-angle", "-i", help = "Starting observer angle in degrees"
	).float().default(0f)
	val renderImage: Boolean by option(
		"--render", "-r", help = "Also convert output to PNG"
	).flag(default = false)
	val factor: Float by option(
		"--factor", "-f", help = "Luminosity scaling factor"
	).float().default(0.2f)
	val gamma: Float by option(
		"--gamma", "-g", help = "Gamma correction value"
	).float().default(1f)
	val initState: ULong by option(
		"--initState", help = "Initial state number for random generation"
	).ulong().default(42uL)
	val initSeq: ULong by option(
		"--initSeq", help = "Initial sequence number for random generation"
	).ulong().default(54uL)
	
	override fun run() {
		
		val world = buildDemoWorld()
		
		val img = HDRImage(width, height)
		
		val screenCenter = Vec(-1f, 0f, 0f)
		val observerYAngle = 45f // angle to rotate above plane (around y-axis)
		
		// concatenation of transformations: first move away from scene,
		// then rotate upwards around y-axis, and finally gradually move around the scene (z-axis)
		val camTransformation = rotationZ(observerZAngle) *
				rotationY(observerYAngle) *
				translation(screenCenter)
		
		val cam = when (camera.lowercase()) {
			"orthogonal" -> OrthogonalCamera(transformation = camTransformation)
			"perspective" -> PerspectiveCamera(transformation = camTransformation)
			else -> throw IllegalStateException("No camera found for $camera.")
		}
		
		// Run the ray-tracer
		println("Starting render...\n")
		val pathTracer = ImageTracer(img, cam, antialiasing = 3, pcg = PCG())
		
		//print("Using a path tracer")
		print("Using a Point-light tracer")
		
		//val renderer = PathTracer(
		//	world,
		//	Color(),
		//	PCG(initState, initSeq),
		//	numRays = 4,
		//	maxRayDepth = 6,
		//	russianRouletteLimit = 4
		//)
		
		val renderer = PointLightRenderer(world, Color.black)
		
		
		// Run the ray-tracer with ProgressBar
		val samplesPerPixel =
			if (pathTracer.antialiasing > 1) pathTracer.antialiasing * pathTracer.antialiasing else 1
		val totalPixels = img.width.toLong() * img.height.toLong()
		val totalSamples = totalPixels * samplesPerPixel
		val progressBar = ProgressBar(totalSamples)
		var done = 0L
		pathTracer.fireAllRays { ray ->
			val color = renderer(ray)
			
			done++
			progressBar.update(done)
			
			color
		}
		
		
		// If name is empty, create a safe, sortable timestamp (e.g., 2026-06-22_16-16-33)
		val name = baseName ?: LocalDateTime.now().format(
			DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
		)
		val pfmPath = "$outputDir/$name.pfm"
		
		img.writePFMFile(pfmPath)
		println("Saved PFM → $pfmPath")
		
		
		if (renderImage) img.savePng("$outputDir/$name.png", factor, gamma)
	}
}

/**
 * Builds the hardcoded demo scene: eight small diffuse spheres at the vertices of a
 * unit cube, plus two emissive spheres on two faces to break symmetry.
 *
 * All spheres are scaled by 1/10 so the scene fits within a unit bounding box.
 * Used exclusively by the [Demo] command.
 *
 * @return a [World] containing all demo shapes, ready to pass to [core.ImageTracer].
 */
private fun buildDemoWorld(): World {
	
	val world = World()
	val scale = 1 / 10f
	val scaling = scaling(Vec(scale, scale, scale))
	val coords = listOf(-0.5f, 0.5f)
	
	val sphereMaterial = Material(
		brdf = DiffuseBRDF(UniformPigment(Color.white)),
		CheckeredPigment(Color.white, Color(1f, 1f, 0f), 4)
	)
	val sphereMaterial1 = Material(
		brdf = DiffuseBRDF(UniformPigment(Color.black)),
		UniformPigment(Color(1f, 0f, 0f))
	)
	
	// spheres in every vertex of a cube centered in origin with edge 1, scaled 1/10
	for (x in coords) for (y in coords) for (z in coords)
		world.addShape(Sphere(translation(Vec(x, y, z)) * scaling, sphereMaterial))
	
	
	// two more spheres in middle of two faces, gives asymmetry to scene
	world.addShape(Sphere(translation(Vec(0f, 0f, -0.5f)) * scaling, sphereMaterial1))
	world.addShape(Sphere(translation(Vec(0f, 0.5f, 0f)) * scaling, sphereMaterial1))
	
	return world
}