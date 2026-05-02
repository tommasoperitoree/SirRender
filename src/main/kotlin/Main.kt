import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import java.io.FileInputStream
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.imageio.IIOImage
import javax.imageio.stream.FileImageOutputStream
import java.io.File
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
		"--input",
		"Input PFM file path"
	).file(mustExist = true, canBeDir = false).convert { it.path }
	val outputFileName: String by argument(
		"--output",
		"Output image file path (extension determines format)"
	).default("./src/main/kotlin/resources/image.png")
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
	).choice("orthogonal", "perspective", ignoreCase = true).default("Orthogonal")
	val observerAngle: Int by option(
		"--observer-angle", "-i",
	).int().default(30)
	val numFrames: Int by option(
		"--num-frames", "-n", help = "Number of frames for the animation"
	).int().default(36)
	val outputFileName: String by option(
		"--output", "-o",
		help = "Output image file name - extension determines format; default dir ./src/main/resources/"
	).default("demo.png")
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
		val frames = mutableListOf<BufferedImage>()
		val angleStep = 360f / numFrames
		
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
		
		
		for (frameIndex in 0 until numFrames) {
			val img = HDRImage(width, height)
			val currAngle = observerAngle.toFloat() + (frameIndex * angleStep)
			
			// Per orbitare, prima trasliamo indietro la camera e poi la ruotiamo
			//da capire come fare a ruotare la camera per non far si che le sfere si sovrappongano
			val camTrans = rotationZ(currAngle) * translation(Vec(-2f, 0f, 0.5f))
			val cam = when (camera.lowercase()) {
				"orthogonal" -> OrthogonalCamera(transformation = camTrans)
				//"orthogonal" -> OrthogonalCamera(transformation = translation(screenCenter))
				"perspective" -> PerspectiveCamera(transformation = camTrans)
				else -> throw IllegalStateException("No camera  found for $camera.")
			}
			val tracer = ImageTracer(img, cam)
			tracer.fireAllRays { ray -> world.rayIntersection(ray)?.let { white() } ?: black() }
			
			img.normalizeImage(factor)
			img.clampImage()
			//using this it save all of frame, afterwords use a null stream ByteArrayOutputStream
			val bImage = img.writeLDRImage(java.io.ByteArrayOutputStream(), "png", gamma)
			frames.add(bImage)
		}
		/*
		val outputFilePath = "./src/main/resources/$outputFileName"
		val format = outputFilePath.substringAfterLast(".").lowercase()
		FileOutputStream(outputFilePath).use { img.writeLDRImage(it, format, gamma) }
		
		println("Saved $outputFilePath")
		*/
		/**
		 * Here all the frame are put together to create a gif using [writer]
		 * For each frame it has to create an object typo [IIOImage] and pass it to [writer]
		 * In order to see the gif open file directly with app or browser
		 */
		
		val gifFile = File("./src/main/resources/animation.gif")
		val output = FileImageOutputStream(gifFile)
		val writer = ImageIO.getImageWritersByFormatName("gif").next()
		writer.output = output
		
		writer.prepareWriteSequence(null) // Start the sequence
		
		for (frame in frames) {
			val imageWriteParam = writer.defaultWriteParam
			val metadata = writer.getDefaultImageMetadata(
				javax.imageio.ImageTypeSpecifier.createFromRenderedImage(frame),
				imageWriteParam
			)
			
			//setting time delay in order to observe the animation
			val formatName = "javax_imageio_gif_image_1.0"
			val root = metadata.getAsTree("javax_imageio_gif_image_1.0") as javax.imageio.metadata.IIOMetadataNode
			val gce =
				root.getElementsByTagName("GraphicControlExtension").item(0) as? javax.imageio.metadata.IIOMetadataNode
					?: javax.imageio.metadata.IIOMetadataNode("GraphicControlExtension")
			gce.setAttribute("delayTime", "100")
			if (root.getElementsByTagName("GraphicControlExtension").length == 0) {
				root.appendChild(gce)
			}
			
			// Converte l'albero XML modificato di nuovo nel formato binario dei metadati
			metadata.setFromTree(formatName, root)
			
			// Scrive il frame con i suoi metadati specifici
			writer.writeToSequence(javax.imageio.IIOImage(frame, null, metadata), imageWriteParam)
		}
		writer.endWriteSequence()
	}
}
// --- Entry point ---

fun main(args: Array<String>) = SirRender()
	.subcommands(Pfm2Png(), Demo())
	.main(args)

// ./gradlew run --args="demo --width=480 --height=480 --output demo.png --num-frames=72"