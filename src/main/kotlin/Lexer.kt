import java.io.InputStream

// ------------------------------
//  Source Location
// ------------------------------

class SourceLocation(
	val fileName: String = "",
	val line: Int = 0,
	val column: Int = 0
)


// ------------------------------
//  Constants, enum Keywords
// ------------------------------

val WHITESPACE = " \t\n\r"
val SYMBOLS = "()<>[],*"

enum class Keyword(val lexeme: String) {
	NEW("new"),
	MATERIAL("material"),
	PLANE("plane"),
	SPHERE("sphere"),
	DIFFUSE("diffuse"),
	SPECULAR("specular"),
	UNIFORM("uniform"),
	CHECKERED("checkered"),
	IMAGE("image"),
	IDENTITY("identity"),
	TRANSLATION("translation"),
	ROTATION_X("rotation_x"),
	ROTATION_Y("rotation_y"),
	ROTATION_Z("rotation_z"),
	SCALING("scaling"),
	CAMERA("camera"),
	ORTHOGONAL("orthogonal"),
	PERSPECTIVE("perspective"),
	FLOAT("float"),
	POINT_LIGHT("point_light");
	
	companion object {
		// Builds the dictionary automatically at startup: {"new": NEW, "material": MATERIAL, ...}
		private val lookupMap = entries.associateBy { it.lexeme }
		
		/** Returns the Keyword if the string matches, or null if it doesn't. */
		fun fromString(value: String): Keyword? = lookupMap[value]
	}
}


// ------------------------------
//  Tokens
// ------------------------------

/** A lexical token, used when parsing a scene file. */
sealed class Token {
	abstract val location: SourceLocation
}

/** A [Token] containing a symbol (i.e., a variable name). */
data class SymbolToken(val symbol: Char, override val location: SourceLocation) : Token() {
	override fun toString(): String = symbol.toString()
}

/** A [Token] containing a literal number ([Float]) */
data class NumberToken(val value: Float, override val location: SourceLocation) : Token() {
	override fun toString() = value.toString()
}

/** A [Token] containing a literal [String]. */
data class StringToken(val value: String, override val location: SourceLocation) : Token() {
	override fun toString() = value
}

/** A [Token] containing an identifier. */
data class KeywordToken(val keyword: Keyword, override val location: SourceLocation) : Token() {
	override fun toString() = keyword.name
}

/** A [Token] containing a keyword. */
data class IdentifierToken(val identifier: String, override val location: SourceLocation) : Token() {
	override fun toString() = identifier
}

/** A [Token] signaling the end of a file. */
data class StopToken(override val location: SourceLocation) : Token()


// ------------------------------
//      Input Stream wrapper
// ------------------------------

class inputStream(
	val stream: InputStream,
	fileName: String = "",
	val tabulations: Int = 4,
) {
	val location: SourceLocation = SourceLocation()
	var savedChar: Char? = null
	var savedLocation: SourceLocation? = null
	var savedToken: Token? = null
	
	init {
		var location = SourceLocation(fileName, line = 1, column = 1)
	}
	
	/**
	 * If it is a symbol (comma, parenthesis, etc.), it returns a SymbolToken;
	 * If it is a digit, it returns a LiteralNumberToken;
	 * If it is "", it returns a LiteralStringToken;
	 * If it is a sequence of characters a…z, it returns a KeywordToken if the sequence is a keyword, IdentifierToken otherwise;
	 * If the file is finished, it returns StopToken.
	 */
	fun readToken() {
	
	}
}


var str = InputStream.nullInputStream()
var inp = inputStream(fileName = "", stream = str, tabulations = 1)

var loc = inp.location