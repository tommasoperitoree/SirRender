import java.io.Reader

// ------------------------------
//  Source Location
// ------------------------------

data class SourceLocation(
	val fileName: String = "",
	var lineNum: Int = 1,
	var colNum: Int = 1
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

/** An [Exception] found by the lexer/parser while reading a scene file

The fields of this type are the following:

- [SourceLocation.fileName]: the name of the file, or the empty string if there is no real file
- [SourceLocation.line] the line number where the error was discovered (starting from 1)
- [SourceLocation.column]: the column number where the error was discovered (starting from 1)
- `message`: a user-frendly error message**/
class GrammarError(
	val location: SourceLocation,
	override val message: String
) : Exception(message)

// ------------------------------
//      Input Stream wrapper
// ------------------------------
//Inputstream can be confused with a library of Kotlin
class SceneInputStream(
	private val stream: Reader,
	fileName: String = "",
	val tabulations: Int = 4,
) {
	var location = SourceLocation(fileName, lineNum = 1, colNum = 1)
	var savedLocation = location.copy()
	
	var savedChar: Char? = null
	var savedToken: Token? = null
	
	/** Update [location] after having read [ch] from the stream. */
	fun updatePos(ch: Char?) {
		if (ch == null) return
		
		when (ch) {
			'\n' -> {
				location.lineNum += 1
				location.colNum = 1
			}
			
			'\t' -> {
				location.colNum += tabulations
			}
			
			else -> {
				location.colNum += 1
			}
		}
	}
	
	/** Read a bew character from the stream. */
	fun readChar(): Char? {
		var ch: Char?
		if (savedChar != null) { // recover the unread character and return it
			ch = savedChar
			savedChar = null
		} else { // read a new character from the stream
			ch = stream.read().toChar()
		}
		
		savedLocation = location.copy()
		updatePos(ch)
		return ch
	}
	
	/** Push a character back to the stream. */
	fun unreadChar(ch: Char?) {
		assert(savedChar == null)
		savedChar = ch
		location = savedLocation.copy()
	}
	
	/** Keep reading characters until a non-whitespace/non-comment character is found. */
	fun skipWhitespacesAndComments() {
		var ch = readChar()
		while (ch != null) {
			when (ch) {
				in WHITESPACE -> {}
				'/' -> {
					//if the next character is / it means that there is // so it is a comment, skip until /n
					val next = readChar()
					if (next == '/') {
						var c = readChar()
						while (c != '\n' && c != null) c = readChar()
						if (c == '\n') unreadChar(c) //replace '\n' in the buffer, and it will be seen as a character of WHITESPACE
					} else {
						unreadChar(next)
						return
						//it wasn't a comment, and it has to be put back
					}
				}
				else -> {
					unreadChar(ch)
					return
				}
			}
			ch=readChar()
		}
	}
	
	/**
	 * If it is a symbol (comma, parenthesis, etc.), it returns a SymbolToken;
	 * If it is a digit, it returns a LiteralNumberToken;
	 * If it is "", it returns a LiteralStringToken;
	 * If it is a sequence of characters a…z, it returns a KeywordToken if the sequence is a keyword, IdentifierToken otherwise;
	 * If the file is finished, it returns StopToken.
	 */
	fun readToken() {
		TODO()
	}
}