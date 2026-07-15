package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.ulong
import com.github.ajalt.clikt.parameters.types.choice
import core.ImageTracer
import core.OrthogonalCamera
import core.PathTracer
import core.PerspectiveCamera
import core.ProgressBar
import core.PointLightRenderer
import core.Renderer
import materials.Color
import materials.HDRImage
import math.PCG
import parsing.Scene
import parsing.SceneInputStream
import parsing.parseScene
import java.io.File

/**
 * CLI command that renders a scene described in an external scene file.
 *
 * The command parses the scene, renders it using a path tracer, and saves
 * the resulting HDR image as a PFM file. It can optionally generate a
 * tone-mapped PNG version of the rendered image.
 */
class Render : CliktCommand("render") {
	override fun help(context: Context) = "Render a scene file to a PFM image, with optional PNG conversion"
	
	// formats: FullHD (1920x1080) ; 720p (1280x720) ; 480p (854x480) ; 360p (640x360)
	val width: Int by option(
		"--width", "-w", help = "Image width in pixels"
	).int().default(640)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(360)
	val outputDir: String by option(
		"--output-dir", "-o", help = "Output directory for PFM frames"
	).default("./outputs/scenes")
	val baseName: String? by option(
		"--name", help = "Output file base name (default: timestamp)"
	)
	val renderImage: Boolean by option(
		"--render", "-r", help = "Also convert output to PNG"
	).flag(default = false)
	val rendererType: String by option(
		"--renderer-type", "-rt", help = "Renderer type: path or point-light"
	).choice("path-tracer", "point-light", "auto", ignoreCase = true).default("auto")
	val factor: Float by option(
		"--factor", "-f", help = "Luminosity scaling factor"
	).float().default(0.2f)
	val gamma: Float by option(
		"--gamma", "-g", help = "Gamma correction value"
	).float().default(1f)
	val rays: Int by option(
		"--num-rays", "-n", help = "Num rays"
	).int().default(8)
	val depth: Int by option(
		"--depth", "-d", help = "Depth scaling factor"
	).int().default(5)
	val roulette: Int by option(
		"--roulette", "-rou", help = "Russian Roulette maximum depth factor"
	).int().default(3)
	val clock: Float? by option(
		"--clock", help = "Override the 'clock' float variable in the scene file (used for animation)"
	).float()
	val initState: ULong by option(
		"--initState", help = "Initial state number for random generation"
	).ulong().default(42uL)
	val initSeq: ULong by option(
		"--initSeq", help = "Initial sequence number for random generation"
	).ulong().default(54uL)
	val antialiasing: Int by option(
		"--antialiasing", "-a", help = "Antialiasing value"
	).int().default(1)
	val inputFile: File by option(
		"--input-file", "-inp", help = "Input file path"
	).file(mustExist = true, canBeDir = false, mustBeReadable = true)
		.default(File("SceneR/sceneFile.txt"))
	
	
	override fun run() {
		
		File(outputDir).mkdirs()
		
		val parsedScene: Scene = inputFile.reader().use { reader ->
			val sceneStream = SceneInputStream(reader)
			val variables = if (clock != null) mapOf("clock" to clock!!) else emptyMap()
			parseScene(sceneStream, variables)
		}
		
		val baseCamera = parsedScene.camera
			?: throw IllegalArgumentException("No camera found in scene file")
		val img = HDRImage(width, height)
		
		val cameraType = when (baseCamera) {
			is OrthogonalCamera -> "Orthogonal"
			is PerspectiveCamera -> "Perspective (distance=${baseCamera.distance})"
			else -> "Unknown"
		}
		
		val cam = when (baseCamera) {
			is OrthogonalCamera -> OrthogonalCamera(
				baseCamera.aspectRatio,
				transformation = baseCamera.transformation
			)
			
			is PerspectiveCamera -> PerspectiveCamera(
				baseCamera.distance,
				baseCamera.aspectRatio,
				transformation = baseCamera.transformation
			)
			
			else -> throw IllegalStateException("Unsupported camera type: $baseCamera")
		}
		
		// ── Startup summary ──────────────────────────────────────────────────────
		val samplesPerPixel = if (antialiasing > 1) antialiasing * antialiasing else 1
		val totalPixels = width.toLong() * height.toLong()
		val totalSamples = totalPixels * samplesPerPixel
		val totalRays = totalSamples * rays.toLong()
		
		println(
			"""
        ┌─ SirRender — Render ──────────────────────────────────────┐
        │  Scene       : ${inputFile.path}
        │  Resolution  : $width × $height  (${totalPixels.toSci()} px)
        │  Camera      : $cameraType  (aspect ${baseCamera.aspectRatio})
        │  Antialiasing: $antialiasing × $antialiasing  (${samplesPerPixel} samples/px)
        │
        │  Path tracer
        │    Rays/sample      : $rays
        │    Max depth        : $depth
        │    RR limit         : $roulette
        │    PCG seed (state) : $initState  seq: $initSeq
        │
        │  Total samples : ${totalSamples.toSci()}
        │  Total rays    : ~${totalRays.toSci()}  (excl. Russian roulette)
        │
        │  Output dir  : $outputDir
        │  Tone-mapping: factor=$factor  gamma=$gamma  → PNG: $renderImage
        └───────────────────────────────────────────────────────────┘
            """.trimIndent()
		)
		// ────────────────────────────────────────────────────────────────────────
		
		val pathTracer = ImageTracer(img, cam, antialiasing = antialiasing, pcg = PCG())
		
		val renderer: Renderer = when (rendererType.lowercase()) {
			"auto" -> {
				if (parsedScene.world.lights.isNotEmpty()) {
					PointLightRenderer(
						world = parsedScene.world,
						backgroundColor = Color.black
					)
				} else {
					PathTracer(
						parsedScene.world,
						Color(),
						PCG(initState, initSeq),
						numRays = rays,
						maxRayDepth = depth,
						russianRouletteLimit = roulette,
					)
				}
			}
			
			"path" -> PathTracer(
				parsedScene.world,
				Color(),
				PCG(initState, initSeq),
				numRays = rays,
				maxRayDepth = depth,
				russianRouletteLimit = roulette,
			)
			
			"point-light" -> PointLightRenderer(
				world = parsedScene.world,
				backgroundColor = Color.black
			)
			
			else -> throw IllegalArgumentException("Unknown renderer type: $rendererType")
		}
		
		
		val progressBar = ProgressBar(totalSamples)
		var done = 0L
		pathTracer.fireAllRays { ray ->
			done++
			progressBar.update(done)
			renderer(ray)
		}
		
		val name = baseName ?: inputFile.nameWithoutExtension
		val pfmPath = "$outputDir/$name.pfm"
		img.writePFMFile(pfmPath)
		println("\nSaved PFM → $pfmPath")
		
		if (renderImage) img.savePng("$outputDir/$name.png", factor, gamma)
	}
}