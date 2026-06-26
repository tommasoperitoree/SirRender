package cli

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
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
	
	// formats: FullHD (1920x1080) ; 720p (1280x720) ; 480p (854x480) ; 360p (640x360)
	val width: Int by option(
		"--width", "-w", help = "Image width in pixels"
	).int().default(640)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(360)
	val outputDir: String by option(
		"--output-dir", "-o", help = "Output directory for PFM frames"
	).default("./src/main/resources/frames")
	val renderImage: Boolean by option(
		"--render", "-r", help = "Also convert output to PNG"
	).flag(default = false)
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
			parseScene(sceneStream)
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
		
		val renderer = PathTracer(
			parsedScene.world,
			Color(),
			PCG(initState, initSeq),
			numRays = rays,     // ← was hardcoded 7
			maxRayDepth = depth,    // ← was hardcoded 6
			russianRouletteLimit = roulette, // ← was hardcoded 3
		)
		
		val progressBar = ProgressBar(totalSamples)
		var done = 0L
		pathTracer.fireAllRays { ray ->
			done++
			progressBar.update(done)
			renderer(ray)
		}
		
		val baseName = inputFile.nameWithoutExtension
		val pfmPath = "$outputDir/$baseName.pfm"
		img.writePFMFile(pfmPath)
		println("\nSaved PFM → $pfmPath")
		
		if (renderImage) {
			img.normalizeImage(factor)
			img.clampImage()
			val pngPath = "$outputDir/$baseName.png"
			FileOutputStream(pngPath).use { img.writeLDRImage(it, "png", gamma) }
			println("Saved PNG → $pngPath")
		}
	}
}

fun Long.toSci(): String = "%.2e".format(this.toDouble())