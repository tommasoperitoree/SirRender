import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class Parameters(
	val inputFileName: String,
	val factor: Float,
	val gamma: Float,
	val outputFileName: String
)

fun main(args: Array<String>) {
	
	try {
		require(args.size == 4) { "Usage: [inputFileName], [factor], [gamma], outputFileName]" }
		val factor = args[1].toFloatOrNull()
			?: throw IllegalArgumentException("Invalid factor: '${args[1]}' must be a float number")
		val gamma = args[2].toFloatOrNull()
			?: throw IllegalArgumentException("Invalid factor: '${args[2]}' must be a float number")
		val params = Parameters(args[0], factor, gamma, args[3])
		
		println("Parameters loaded: $params")
		
		val hdrImage = HDRImage.fromPFMStream(FileInputStream(params.inputFileName))
		hdrImage.normalizeImage(factor)
		hdrImage.clampImage()
		
		FileOutputStream(params.outputFileName).use { outStream ->
			hdrImage.writeLDRImage(
				outStream,
				format = "PNG",
				gamma
			)
		}
		println("Image saved in ${params.outputFileName}")
		
		
	} catch (e: IllegalArgumentException) {
		println(e.message)
	}
}



