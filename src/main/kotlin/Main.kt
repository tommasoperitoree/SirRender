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
import com.github.ajalt.clikt.parameters.types.inputStream
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.ulong
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
import kotlin.math.sqrt


/** Create elements of the demo scene. */
private fun buildDemoWorld(): World {
	
	val world = World()
	
	val skyMaterial = Material(
		brdf = DiffuseBRDF(pigment = UniformPigment(Color())),
		emittedRadiance = UniformPigment(Color(0.35f, 0.75f, 1.0f))
	)
	
	val groundMaterial = Material(
		brdf = DiffuseBRDF(
			pigment = CheckeredPigment(
				//color1 = Color.white,
				color1 = Color(1f, 0.3f, 0f), // orange
				//color1 = Color(0.8f, 0.05f, 0.2f),
				color2 = Color.black,
				numSteps = 5
			)
			//pigment = UniformPigment(Color(1f, 0.3f, 0f))
		)
	)
	
	
	val sunMaterial = Material(
		brdf = DiffuseBRDF(
			pigment = UniformPigment(Color(1.0f, 0.4f, 0f))
		),
		emittedRadiance = UniformPigment(Color(10.0f, 4f, 0f))
	)
	
	//RED
	val sphereMaterial = Material(
		brdf = DiffuseBRDF(
			pigment = UniformPigment(Color(0.8f, 0f, 0f))
		)
	)
	
	val cubeMaterial = Material(
		brdf = DiffuseBRDF(
			UniformPigment(Color(0f, 0f, 1f))
		)
	)
	val mirrorMaterial = Material(
		brdf = SpecularBRDF(
			UniformPigment(Color(0.753f, 0.753f, 0.753f))
		)
	)
	
	//Pavement
	world.addShape(Plane(transformation = Transformation(), groundMaterial))
	//use a sphere instead of two plane for the sky
	world.addShape(
		Sphere(
			transformation = scaling(Vec(50f, 50f, 50f)),
			material = skyMaterial
		)
	)
	
	/*
	//Sky blu, rotate it to prevent the sky and the ground overlapping, the second plane is put in order to cover all the angles
	world.addShape(
		Plane(
			transformation = translation(
				Vec(10f, 0f, 0f)
			) * rotationY(90f),
			skyMaterial
		)
	)
	world.addShape(
		Plane(
			transformation = translation(
				Vec(-10f, 0f, 0f)
			) * rotationY(90f),
			skyMaterial
		)
	)
	
	
	//Sun in the sky in (-0.5,-3,6)
	world.addShape(
		Sphere(
			transformation = scaling(
				Vec(0.3f, 0.3f, 0.3f)
			) * translation(Vec(-0.4f, 0f, 5f)),
			material = sunMaterial
		)
	)
	
	//first sphere in (-2,1,1) red
	world.addShape(
		Sphere(
			transformation = scaling(
				Vec(0.4f, 0.4f, 0.4f)
			) * translation(Vec(-2f, 1f, 1f)),
			material = sphereMaterial
		)
	)
	
	//second sphere in (-4,-1,1) silver that reflect the first sphere
	world.addShape(
		Sphere(
			scaling(
				Vec(0.2f, 0.2f, 0.2f)
			) * translation(Vec(-6f, -1f, 1f)),
			mirrorMaterial
		)
	)
	*/
	world.addShape(
		Cube(
			scaling(Vec(0.2f, 0.2f, 0.2f)) * translation(Vec(-3f, -1f, 1f)),
			cubeMaterial
		)
	)
	return world
}

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
	).int().default(640)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(480)
	val camera: String by option(
		"--camera", "-c", help = "Camera type (projection): Orthogonal or Perspective"
	).choice("Orthogonal", "Perspective", ignoreCase = true).default("Perspective")
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
	
	override fun run() {
		
		val cameraDir = "$outputDir/$camera"
		File(cameraDir).mkdirs() // create output dir if it doesn't exist
		
		val world = buildDemoWorld()
		val angleStep = if (numFrames == 1) 0f else 360f / numFrames
		
		for (frameIndex in 0 until numFrames) {
			val angle = 90f
			//val angle = observerAngle + (frameIndex * angleStep)
			val angleNNN = "%03d".format(frameIndex)
			
			val img = HDRImage(width, height)
			
			val screenCenter = Vec(-1f, 0f, 0f)
			val verticalAngle = 35f // angle to rotate above plane (around y-axis)
			// concatenation of transformations: first move away from scene,
			// then rotate upwards around y-axis, and finally gradually move around the scene (z-axis)
			val camTransformation = rotationZ(angle) *
					rotationY(verticalAngle) *
					translation(screenCenter)
			
			val cam = when (camera.lowercase()) {
				"orthogonal" -> OrthogonalCamera(transformation = camTransformation)
				"perspective" -> PerspectiveCamera(transformation = camTransformation)
				else -> throw IllegalStateException("No camera found for $camera.")
			}
			
			//Run the ray-tracer
			println("Starting render...\n")
			val pathTracer = ImageTracer(img, cam, antialiasing = 2, pcg = PCG())
			
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
			progressBar.update(totalPixels, force = true)
			
			val baseName = "demo"
			val pfmPath = "$cameraDir/$baseName.pfm"
			img.writePFMFile(pfmPath)
			println("Saved PFM → $pfmPath")
			
			
			if (renderImage) {
				//img.normalizeImage(factor)
				img.clampImage()
				
				val pngPath = "$cameraDir/$baseName.png"
				FileOutputStream(pngPath).use { img.writeLDRImage(it, "png", gamma) }
				println("Saved PNG → $pngPath")
			}
		}
	}
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
	).int().default(640)
	val height: Int by option(
		"--height", "-h", help = "Image height in pixels"
	).int().default(480)
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
				numRays = 2,
				maxRayDepth = 5,
				russianRouletteLimit = 4
			)
			
			// Inserisci questo in Render:
			val samplesPerPixel = if (pathTracer.antialiasing > 1) pathTracer.antialiasing * pathTracer.antialiasing else 1
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
			progressBar.update(totalPixels, force = true)
			
			val baseName = "${inputFile.nameWithoutExtension}"
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