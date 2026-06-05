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
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file") //analogue to StopToken
		
		if (token !is SymbolToken || token.symbol.toString() != symbol) throw GrammarError(
			token.location,
			"got $token instead of $symbol"
		)
		
	}
	
	/**Read a [Token] from [s]  and check that it is one of the keywords in [Keyword]*/
	fun expectKeyword(s: SceneInputStream, keyword: String): String {
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file")
		
		if (token !is KeywordToken) throw GrammarError(token.location, "got $token instead of $keyword")
		if (token.keyword.toString() != keyword) throw GrammarError(
			token.location,
			"expected on of keywords in $Keyword instead of $token"
		)
		return token.keyword.toString()
	}
	
	fun expectNumber(s: SceneInputStream, scene: Scene): Float {
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file")
		
		if (token is NumberToken) return token.value
		if (token is IdentifierToken) {
			val name = token.identifier
			//use !in instead of !is because it works only on object not map
			if (name !in scene.floatVariables) throw GrammarError(token.location, "unknown variable $token")
			return scene.floatVariables[name]!!
		}
		throw GrammarError(token.location, "got $token instead of number")
	}
	
	fun expectString(s: SceneInputStream): String {
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file")
		
		if (token !is StringToken) throw GrammarError(token.location, "got $token instead of string")
		return token.toString()
	}
	
	fun expectIdentifier(s: SceneInputStream): String {
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file")
		
		if (token !is IdentifierToken) throw GrammarError(token.location, "got $token instead of identifier")
		return token.identifier
	}
	
	
	// --- Parsing functions ---
	fun parseColor(s: SceneInputStream, scene: Scene): Color {
		
		expectSymbol(s, "(")
		val r = expectNumber(s, scene)
		expectKeyword(s, ",")
		val g = expectNumber(s, scene)
		expectSymbol(s, ",")
		val b = expectNumber(s, scene)
		expectSymbol(s, ")")
		
		return Color(r, g, b)
	}
	
	fun parseVec(s: SceneInputStream, scene: Scene): Vec {
		TODO()
	}
	
	fun parsePigment(s: SceneInputStream, secene: Scene): Pigment {
		TODO()
	}
	
	fun parseBRDF(s: SceneInputStream, scene: Scene): BRDF {
		TODO()
	}
	
	fun parseMaterial(s: SceneInputStream, scene: Scene): Material {
		TODO()
	}
	
	fun parseTransformation(s: SceneInputStream, scene: Scene): Transformation {
		TODO()
	}
	
	fun parseSphere(s: SceneInputStream, scene: Scene): Sphere {
		TODO()
	}
	
	fun parsePlane(s: SceneInputStream, scene: Scene): Plane {
		TODO()
	}
	
	fun parseCamera(s:SceneInputStream,scene: Scene):Camera{
		TODO()
	}
	
}