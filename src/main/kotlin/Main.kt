import java.io.FileInputStream
import java.io.FileOutputStream

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