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
		val token=s.readToken()
		if (token !is SymbolToken || token.symbol.toString() != symbol) {
			throw GrammarError(token.location, "got $token instead of $symbol" )
		}
	}
	/**Read a [Token] from [s]  and check that it is one of the keywords in [Keyword]*/
	fun expectKeyword(s: SceneInputStream, keyword: String): String{
		val token=s.readToken()
		if (token !is KeywordToken) throw GrammarError(token.location, "got $token instead of $keyword" )
		if (token.keyword.toString() != keyword) throw GrammarError(token.location, "expected on of keywords in $Keyword instead of $token")
		return token.keyword.toString()
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