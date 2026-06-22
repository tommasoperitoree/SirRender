package cli

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.ulong
import core.ImageTracer
import core.OrthogonalCamera
import core.PathTracer
import core.PerspectiveCamera
import core.ProgressBar
import core.World
import geometry.Sphere
import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.HDRImage
import materials.Material
import materials.UniformPigment
import math.PCG
import math.Vec
import math.rotationY
import math.rotationZ
import math.scaling
import math.translation
import parsing.Scene
import parsing.SceneInputStream
import parsing.parseScene
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import javax.imageio.ImageIO
import javax.imageio.IIOImage
import javax.imageio.stream.FileImageOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadataNode


// --- Parent command ---

class SirRender : CliktCommand() {
	override fun help(context: Context) = "SirRender: a ray tracer CLI"
	override fun run() = Unit
}


// --- Subcommand 1: pfm2png ---
class Pfm2Png : CliktCommand(
	name = "pfm2png",
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
		"--factor", "-f", help = "Luminosity scaling factor"
	).float().default(0.2f)
	
	val gamma: Float by option(
		"--gamma", "-g", help = "Gamma correction value"
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
	name = "demo"
) {
	override fun help(context: Context) = "Generate a demo image"
	
	val width: Int by option(
		"--width", "-w", help = "Image width in pixels"
	).int().default(1280)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(720)
	val camera: String by option(
		"--camera", "-c", help = "Camera type (projection): Orthogonal or Perspective"
	).choice("Orthogonal", "Perspective", ignoreCase = true).default("Perspective")
	val numFrames: Int by option(
		"--num-frames", "-n", help = "Number of frames (angles) to generate"
	).int().default(1)
	val outputDir: String by option(
		"--output-dir", "-o", help = "Output directory for images"
	).default("./src/main/resources/frames")
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
		val observerYAngle = 35f // angle to rotate above plane (around y-axis)
		
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
		
		print("Using a path tracer")
		
		val renderer = PathTracer(
			world,
			Color(),
			PCG(initState, initSeq),
			numRays = 4,
			maxRayDepth = 6,
			russianRouletteLimit = 4
		)
		
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
		
		
		// Create a safe, sortable timestamp (e.g., 2026-06-22_16-16-33)
		val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
		val timestamp = LocalDateTime.now().format(formatter)
		
		val pfmPath = "$outputDir/$timestamp.pfm"
		img.writePFMFile(pfmPath)
		println("Saved PFM → $pfmPath")
		
		
		if (renderImage) {
			img.normalizeImage(factor)
			img.clampImage()
			val pngPath = "$outputDir/$timestamp.png"
			FileOutputStream(pngPath).use { img.writeLDRImage(it, "png", gamma) }
			println("Saved PNG → $pngPath")
		}
		
	}
}


/** Create elements of the demo scene. */
private fun buildDemoWorld(): World {
	
	val world = World()
	val scale = 1 / 10f
	val scaling = scaling(Vec(scale, scale, scale))
	val coords = listOf(-0.5f, 0.5f)
	
	val sphere_material= Material(
		brdf = DiffuseBRDF(UniformPigment(Color.white)),
		CheckeredPigment(Color.white,Color(1f,1f,0f),4)
	)
	val sphere_material1= Material(
		brdf = DiffuseBRDF(UniformPigment(Color.black)),
		UniformPigment(Color(1f,0f,0f))
	)
	
	// spheres in every vertex of a cube centered in origin with edge 1, scaled 1/10
	for (x in coords) for (y in coords) for (z in coords)
		world.addShape(Sphere(translation(Vec(x, y, z)) * scaling,sphere_material))
	
	// two more spheres in middle of two faces, gives asymmetry to scene
	world.addShape(Sphere(translation(Vec(0f, 0f, -0.5f)) * scaling,sphere_material1))
	world.addShape(Sphere(translation(Vec(0f, 0.5f, 0f)) * scaling,sphere_material1))
	
	
	return world
}

// --- Subcommand 3: animation ---

class Animation : CliktCommand(
	name = "animation"
) {
	override fun help(context: Context) = "Assemble PFM frames from a folder into a GIF"
	
	val inputDir: String by option(
		"--input-dir", "-i", help = "Directory containing PFM frame files"
	).default("./src/main/resources/frames")
	val outputFileName: String by option(
		"--output", "-o", help = "Output GIF file path"
	).default("./src/main/resources/animation.gif")
	val factor: Float by option(
		"--factor", "-f", help = "Luminosity scaling factor"
	).float().default(0.2f)
	val gamma: Float by option(
		"--gamma", "-g", help = "Gamma correction value"
	).float().default(1f)
	val delayTime: Int by option(
		"--delay", "-d", help = "Frame delay in centiseconds (100 = 1s)"
	).int().default(4)  // ~25fps
	
	override fun run() {
		
		// collect and sort PFM files from input dir
		val pfmFiles = File(inputDir)
			.listFiles { f -> f.extension.lowercase() == "pfm" }
			?.sortedBy { it.name }
			?: error("No PFM files found in $inputDir")
		println("Found ${pfmFiles.size} PFM files in $inputDir")
		
		val gifFile = File(outputFileName)
		gifFile.parentFile?.mkdirs()
		
		// Wrap the GIF output stream in a .use block to prevent file locking on crash
		FileImageOutputStream(gifFile).use { output ->
			val writer = ImageIO.getImageWritersByFormatName("gif").next()
			writer.output = output
			writer.prepareWriteSequence(null) // Start the sequence
			
			// Process one frame at a time to prevent OutOfMemory errors
			for ((i, file) in pfmFiles.withIndex()) {
				println("Processing frame ${i + 1}/${pfmFiles.size} → ${file.name}")
				
				// 1. Read and tone-map the HDR image
				val img = file.inputStream().use { HDRImage.fromPFMStream(it) }
				img.normalizeImage(factor)
				img.clampImage()
				
				// 2. Safely capture the byte stream and convert to a BufferedImage
				val byteOut = ByteArrayOutputStream()
				img.writeLDRImage(byteOut, "png", gamma)
				val bImg = ImageIO.read(ByteArrayInputStream(byteOut.toByteArray()))
				
				// 3. Setup GIF metadata
				val imageWriteParam = writer.defaultWriteParam
				val metadata = writer.getDefaultImageMetadata(
					ImageTypeSpecifier.createFromRenderedImage(bImg),
					imageWriteParam
				)
				val formatName = "javax_imageio_gif_image_1.0"
				val root = metadata.getAsTree(formatName) as IIOMetadataNode
				val gce = root.getElementsByTagName("GraphicControlExtension").item(0)
						as? IIOMetadataNode
					?: IIOMetadataNode("GraphicControlExtension").also {
						root.appendChild(it)
					}
				
				// Set time delay in order to observe the animation
				gce.setAttribute("delayTime", delayTime.toString())
				if (root.getElementsByTagName("GraphicControlExtension").length == 0) {
					root.appendChild(gce)
				}
				
				// Convert the modified XML tree back to binary for metadata
				metadata.setFromTree(formatName, root)
				
				// 4. Write frame directly to disk, freeing RAM for the next iteration
				writer.writeToSequence(IIOImage(bImg, null, metadata), imageWriteParam)
			}
			
			writer.endWriteSequence()
		} // output is automatically closed here
		
		println("GIF saved → $outputFileName")
	}
}

class Render : CliktCommand("render") {
	override fun help(context: Context) = "Generate a scene image"
	
	val width: Int by option(
		"--width", "-w", help = "Image width in pixels"
	).int().default(1280)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(720)
	val numFrames: Int by option(
		"--num-frames", "-n", help = "Number of frames (angles) to generate"
	).int().default(1)
	val outputDir: String by option(
		"--output-dir", "-o", help = "Output directory for PFM frames"
	).default("./src/main/resources/frames")
	val observerAngle: Float by option(
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
	val antialiasing: Int by option(
		"--antialiasing", "-a", help = "Antialiasing value"
	).int().default(1)
	val inputFile: File by option("--input-file", "-inp", help = "Input file path")
		.file(mustExist = true, canBeDir = false, mustBeReadable = true)
		.default(File("SceneR/sceneFile.txt"))
	
	
	override fun run() {
		val parsedScene: Scene = inputFile.reader().use { reader ->
			val sceneStream = SceneInputStream(reader)
			parseScene(sceneStream)
		}
		
		val sceneName = inputFile.nameWithoutExtension
		
		File(outputDir).mkdirs()
		
		val baseCamera = parsedScene.camera ?: throw IllegalArgumentException("No camera found")
		
		val angleStep = if (numFrames == 1) 0f else 360f / numFrames
		
		for (frameIndex in 0 until numFrames) {
			val angle = 90f
			//val angle = observerAngle + (frameIndex * angleStep)
			val angleNNN = "%03d".format(frameIndex)
			
			val img = HDRImage(width, height)
			
			val screenCenter = Vec(-1f, 0f, 0f)
			
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
				
				else -> throw IllegalStateException("No camera found for $baseCamera")
			}
			
			//Run the ray-tracer
			
			val pathTracer = ImageTracer(img, cam, antialiasing = antialiasing, pcg = PCG())
			
			print("Using a path tracer")
			
			val renderer = PathTracer(
				parsedScene.world,
				Color(),
				PCG(initState, initSeq),
				numRays = 4,
				maxRayDepth = 6,
				russianRouletteLimit = 4
			)
			
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
			
			
			val baseName = inputFile.nameWithoutExtension
			val pfmPath = "$outputDir/$baseName.pfm"
			img.writePFMFile(pfmPath)
			println("Saved PFM → $pfmPath")
			
			
			if (renderImage) {
				//img.normalizeImage(factor)
				img.clampImage()
				
				val pngPath = "$outputDir/$baseName.png"
				FileOutputStream(pngPath).use { img.writeLDRImage(it, "png", gamma) }
				println("Saved PNG → $pngPath")
			}
		}
	}
}
// --- Entry point ---

fun main(args: Array<String>) =
	SirRender(
	).subcommands(
		Pfm2Png(), Demo(), Animation(), Render()
	).main(args)

// ./gradlew run --args="demo -w 640 -h 480 -c "Orthogonal" -o demo.png"
// ./gradlew run --args="animation --width=480 --height=480 --output demo.png --num-frames=72"

// ./gradlew run --args="demo -r -c "Orthogonal" "-f0.01" -w 1280 -h 720 -o src/main/resources/Ortho_demo"


//./gradlew run --args="render -r -inp SceneR/cube.txt -w 1280 -f0.5 -h 720 -a 3 -o output"
