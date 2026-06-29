package cli


import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import materials.HDRImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.FileImageOutputStream


/**
 * Assembles a directory of `.pfm` HDR frames into an animated GIF.
 *
 * Frames are sorted alphabetically, tone-mapped via [HDRImage.normalizeImage] and
 * [HDRImage.clampImage], then written one at a time to keep memory usage constant
 * regardless of frame count. Use [scripts/animateRender.sh] or [scripts/animateDemo.sh] to produce
 * the PFM frames first.
 */
class PFMtoGIF : CliktCommand(
	name = "pfm-to-gif"
) {
	override fun help(context: Context) = "Assemble a folder of PFM frames into an animated GIF"
	
	val inputDir: String by option(
		"--input-dir", "-i", help = "Directory containing PFM frame files, sorted alphabetically"
	).default("./outputs/animateDemo")
	val outputFileName: String by option(
		"--output", "-o", help = "Output GIF file path"
	).default("./outputs/animateDemo.gif")
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
		
		println(
			"""
            Assembling GIF
              Input  : $inputDir (${pfmFiles.size} frames)
              Output : $outputFileName
              Delay  : ${delayTime}cs (~${100 / delayTime} fps)
              Tone   : factor=$factor  gamma=$gamma
            """.trimIndent()
		)
		
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
				
				// Convert the modified XML tree back to binary for metadata
				metadata.setFromTree(formatName, root)
				
				// 4. Write frame directly to disk, freeing RAM for the next iteration
				writer.writeToSequence(IIOImage(bImg, null, metadata), imageWriteParam)
			}
			
			writer.endWriteSequence()
		} // output is automatically closed here
		
		println("GIF saved → $outputFileName  (${pfmFiles.size} frames)")
	}
}