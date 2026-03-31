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