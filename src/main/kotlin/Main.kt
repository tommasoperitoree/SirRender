import java.io.File

data class Parameters(
	val inputFileName: String,
	val factor: Float,
	val gamma: Float,
	val outputFileName: String
)
fun main(args: Array<String>) {
	
	try {
		require(args.size==4){"Usage: [inputFileName], [factor], [gamma], outputFileName]"}
		val factor= args[1].toFloatOrNull()
			?: throw IllegalArgumentException("Invalid facto: '${args[1]}' must be a float number")
		val gamma= args[2].toFloatOrNull()
			?: throw IllegalArgumentException("Invalid facto: '${args[2]}' must be a float number")
		val params = Parameters(args[0], factor, gamma, args[3])
		
		println("Parametri caricati: $params")
	} catch (e:IllegalArgumentException) {
		println(e.message)
	}
}


