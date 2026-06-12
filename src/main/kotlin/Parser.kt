import java.io.File

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
	fun expectKeyword(s: SceneInputStream, keyword: List<Keyword>): Keyword {
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file")
		
		if (token !is KeywordToken) throw GrammarError(token.location, "got $token instead of $keyword")
		//use !in if keyword now is a list of Keywords
		if (token.keyword !in keyword) throw GrammarError(
			token.location,
			"expected on of keywords in $Keyword instead of $token"
		)
		return token.keyword
	}
	
	fun expectNumber(s: SceneInputStream, scene: Scene): Float {
		val token = s.readToken() ?: throw GrammarError(s.location, "unexpected end of file")
		
		if (token is NumberToken) return token.value
		if (token is IdentifierToken) {
			val name = token.identifier
			//use !in instead of !is because it works only on object not map
			if (name !in scene.floatVariables) throw GrammarError(token.location, "unknown variable $token")
			return scene.floatVariables[name]!! // !! is a NoNull Assert, the variable can't be null
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
		expectSymbol(s, ",")
		val g = expectNumber(s, scene)
		expectSymbol(s, ",")
		val b = expectNumber(s, scene)
		expectSymbol(s, ")")
		
		return Color(r, g, b)
	}
	
	fun parseVec(s: SceneInputStream, scene: Scene): Vec {
		
		expectSymbol(s, "(")
		val x = expectNumber(s, scene)
		expectSymbol(s, ",")
		val y = expectNumber(s, scene)
		expectSymbol(s, ",")
		val z = expectNumber(s, scene)
		expectSymbol(s, ")")
		
		return Vec(x, y, z)
	}
	
	fun parsePigment(s: SceneInputStream, scene: Scene): Pigment? {
		var keyword = expectKeyword(s, listOf(Keyword.UNIFORM, Keyword.CHECKERED, Keyword.IMAGE))
		var pigment: Pigment? = null
		
		expectSymbol(s, "(")
		
		if (keyword == Keyword.UNIFORM) {
			val color = parseColor(s, scene)
			pigment = UniformPigment(color)
		}
		
		if (keyword == Keyword.CHECKERED) {
			val color1 = parseColor(s, scene)
			expectSymbol(s, ",")
			val color2 = parseColor(s, scene)
			expectSymbol(s, ",")
			val numStep = (expectNumber(s, scene)).toInt()
			pigment = CheckeredPigment(color1, color2, numStep)
		}
		
		//instead of give a HDRImage, from Scene, the compiler read the name of the PFM file,
		// then parser will open it e and close right after (.use)
		if (keyword == Keyword.IMAGE) {
			val fileName = expectString(s)
			val image = File(fileName).inputStream().use { HDRImage.fromPFMStream(it) }
			pigment = ImagePigment(image)
		}
		
		expectSymbol(s, ")")
		return pigment
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
	
	fun parseCamera(s: SceneInputStream, scene: Scene): Camera {
		TODO()
	}
	
}