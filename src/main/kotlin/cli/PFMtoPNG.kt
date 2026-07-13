package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.float
import materials.HDRImage
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * CLI command that converts a `.pfm` HDR image into an LDR image format.
 *
 * The output format is determined by the extension of the output file,
 * such as PNG or JPEG.
 */
class PFMtoPNG : CliktCommand(
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