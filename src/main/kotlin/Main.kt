import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KParameter

data class Parameters(
	val inputFileName: String = "",
	val factor: Float = 0.2f,
	val gamma: Float = 1.0f,
	val outputFileName: String = ""
) {
	companion object {
		/**
		 * Parses command line [args] into a [Parameters] instance.
		 *
		 * @throws IllegalArgumentException if the arguments are invalid
		 */
		fun fromArgs(args: Array<String>): Parameters {
			require(args.size == 4) { "Usage: <inputFile> <factor> <gamma> <outputFile>" }
			
			val factor = args[1].toFloatOrNull()
				?: throw IllegalArgumentException("Invalid factor: '${args[1]}' must be a float")
			val gamma = args[2].toFloatOrNull()
				?: throw IllegalArgumentException("Invalid gamma: '${args[2]}' must be a float")
			
			return Parameters(args[0], factor, gamma, args[3])
		}
	}
}

fun main(args: Array<String>) {
	
	val params = Parameters.fromArgs(args)
	
	try {
		Parameters.fromArgs(args)
		println("Loaded parameters: $params\n")
	} catch (e: IllegalArgumentException) {
		println("Error: ${e.message}")
	}
	
	val img = FileInputStream(params.inputFileName).use { input ->
		HDRImage.fromPFMStream(input)
	}
	
	img.normalizeImage(params.factor)
	img.clampImage()
	
	FileOutputStream(params.outputFileName).use { output ->
		img.writeLDRImage(output, "png", params.gamma)
	}
	
	println("File ${params.outputFileName} has been created")
	
}