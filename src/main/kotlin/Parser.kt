/** A scene read from a scene file */
data class Scene(
	val materials: MutableMap<String, Material> = mutableMapOf(),
	val world: World = World(),
	var camera: Camera? = null,
	val floatVariables: MutableMap<String, Float> = mutableMapOf(),
	val overriddenVariables: MutableSet<String> = mutableSetOf()
) {
	
	// --- Check expected functions ---
	
	/** Read a [Token] from [s] and check that it matches the given [symbol]. */
	fun expectSymbol(s: SceneInputStream, symbol: String) {
		TODO()
	}
	
	fun expectKeyword(s: SceneInputStream, keyword: String) {
		TODO()
	}
	
	fun expectNumber(s: SceneInputStream, scene: Scene): Float {
		TODO()
	}
	
	fun expectString(s: SceneInputStream): String {
		TODO()
	}
	
	fun expectIdentifier(s: SceneInputStream): String {
		TODO()
	}
	
	
	// --- Parsing functions ---
	
	
}