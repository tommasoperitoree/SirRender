import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import java.io.FileInputStream
import java.io.FileOutputStream

// --- Parent command ---

class SirRender : CliktCommand() {
	override fun help(context: Context) = "SirRender: a ray tracer CLI"
	override fun run() = Unit
}

// --- Subcommand 1: pfm2png ---

class Pfm2Png : CliktCommand(
	"pfm2png",
) {
	override fun help(context: Context) = "Convert a PFM HDR image to LDR format (PNG, JPEG, WebP, ...)"
	
	val inputFileName: String by argument(
		"INPUT",
		"Input PFM file path"
	).file(mustExist = true, canBeDir = false).convert { it.path }
	val outputFileName: String by argument(
		"OUTPUT",
		"Output image file path (extension determines format)"
	)
	val factor: Float by option(
		"--factor", "-f",
		help = "Luminosity scaling factor"
	).float().default(0.2f)
	val gamma: Float by option(
		"--gamma", "-g",
		help = "Gamma correction value"
	).float().default(1f)
	
	override fun run() {
		println("Converting $inputFileName → $outputFileName (factor=$factor, gamma=$gamma)")
		
		val img = FileInputStream(inputFileName).use { HDRImage.fromPFMStream(it) }
		img.normalizeImage(factor)
		img.clampImage()
		
		val format = outputFileName.substringAfterLast(".").lowercase()
		FileOutputStream(outputFileName).use { img.writeLDRImage(it, format, gamma) }
		
		println("Saved $outputFileName")
	}
}

// --- Subcommand 2: demo ---

class Demo : CliktCommand(
	"demo"
) {
	override fun help(context: Context) = "Generate a demo image"
	
	val width: Int by option(
		"--width", "-w",
		help = "Image width in pixels"
	).int().default(480)
	val height: Int by option(
		"--height", "-h",
		help = "Image height in pixels"
	).int().default(480)
	val camera: String by option(
		"--camera", "-c",
		help = "Camera type (projection): Orthogonal or Perspective"
	).default("Orthogonal")
	val observerAngle: Int by option(
		"--observer-angle", "-i",
	).int().default(0)
	val outputFileName: String by option(
		"--output", "-o",
		help = "Output image file path (extension determines format)"
	).choice("orthogonal", "perspective", ignoreCase = true).default("demo.png")
	val factor: Float by option(
		"--factor", "-f",
		help = "Luminosity scaling factor"
	).float().default(0.2f)
	val gamma: Float by option(
		"--gamma", "-g",
		help = "Gamma correction value"
	).float().default(1f)
	
	override fun run() {
		println("Generating demo image (${width}x${height}) → $outputFileName with $camera projection")
		
		// --- Scene creation ---
		val world = World()
		val scale = 1 / 10f
		val scaling = scaling(Vec(scale, scale, scale))
		val coords = listOf(-0.5f, 0.5f)
		for (x in coords) {
			for (y in coords) {
				for (z in coords) {
					// spheres in every vertex of a cube centered in origin with edge 1, scaled 1/10
					world.addShape(Sphere(translation(Vec(x, y, z)) * scaling))
				}
			}
		}
		// two more spheres in middle of two faces, gives asymmetry to scene
		world.addShape(Sphere(translation(Vec(0f, 0f, -0.5f)) * scaling))
		world.addShape(Sphere(translation(Vec(0f, 0.5f, 0f)) * scaling))
		
		val img = HDRImage(width, height)
		val screenCenter = Vec(-1f, 0f, 0f)
		val cam = when (camera.lowercase()) {
			"orthogonal" -> OrthogonalCamera(transformation = translation(screenCenter))
			"perspective" -> PerspectiveCamera(transformation = translation(screenCenter))
			else -> throw IllegalStateException("No camera  found for $camera.")
		}
		val tracer = ImageTracer(img, cam)
		tracer.fireAllRays { ray -> world.rayIntersection(ray)?.let { white() } ?: black() }
		
		img.normalizeImage(factor)
		img.clampImage()
		val format = outputFileName.substringAfterLast(".").lowercase()
		FileOutputStream(outputFileName).use { img.writeLDRImage(it, format, gamma) }
		
		println("Saved $outputFileName")
		
		// TODO: generate demo image content here
	}
}

// --- Entry point ---

fun main(args: Array<String>) = SirRender()
	.subcommands(Pfm2Png(), Demo())
	.main(args)

// ./gradlew run --args="demo --width=480 --height=480 --output demo.pfm"