import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
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
	name = "pfm2png",
) {
	override fun help(context: Context) = "Convert a PFM HDR image to LDR format (PNG, JPEG, WebP, ...)"
	
	val inputFileName: String by argument(
		name = "INPUT",
		help = "Input PFM file path"
	).file(mustExist = true, canBeDir = false).convert { it.path }
	val outputFileName: String by argument(
		name = "OUTPUT",
		help = "Output image file path (extension determines format)"
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
	name = "demo"
) {
	override fun help(context: Context) = "Generate a demo image"
	
	val width: Int by option(
		"--width", "-W",
		help = "Image width in pixels"
	).int().default(480)
	val height: Int by option(
		"--height", "-H",
		help = "Image height in pixels"
	).int().default(480)
	val outputFileName: String by option(
		"--output", "-o",
		help = "Output PFM file path"
	).default("demo.pfm")
	
	override fun run() {
		println("Generating demo image (${width}x${height}) → $outputFileName")
		
		val img = HDRImage(width, height)
		// TODO: generate demo image content here
		
		img.writePFMFile(outputFileName)
		println("Saved $outputFileName")
	}
}

// --- Entry point ---

fun main(args: Array<String>) = SirRender()
	.subcommands(Pfm2Png(), Demo())
	.main(args)