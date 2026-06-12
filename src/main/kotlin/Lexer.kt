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

const val WHITESPACE = " \t\n\r"
const val SYMBOLS = "()<>[],*"

enum class Keyword(val lexeme: String) {
	NEW("new"),
	SKY("sky"),
	GROUND("ground"),
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
	FLOAT("float");
	
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

/** A [Token] containing a keyword. */
data class KeywordToken(val keyword: Keyword, override val location: SourceLocation) : Token() {
	override fun toString() = keyword.name
}

/** A [Token] containing an identifier */
data class IdentifierToken(val identifier: String, override val location: SourceLocation) : Token() {
	override fun toString() = identifier
}

/** A [Token] signaling the end of a file EOF. */
data class StopToken(override val location: SourceLocation) : Token()


/** An [Exception] found by the lexer/parser while reading a scene file.
 * The fields of this type are the following:
 * - [SourceLocation.fileName] the name of the file, or the empty string if there is no real file
 * - [SourceLocation.lineNum] the line number where the error was discovered (starting from 1)
 * - [SourceLocation.colNum] the column number where the error was discovered (starting from 1)
 * - [message]: a user-friendly error message
 */
class GrammarError(
	val location: SourceLocation,
	override val message: String
) : Exception(message)


// ------------------------------
//      Input Stream wrapper
// ------------------------------

/**
 * A high-level wrapper around a stream, used to parse scene files
 * This class implements a wrapper around a stream, with the following additional capabilities:
 * - It tracks the line number and column number;
 * - It permits to "un-read" characters and tokens.
 */
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
		} else { // byte = -1 is when char is null, at the end of the stream
			val byte = stream.read()
			ch = if (byte == -1) null else byte.toChar()
		}
		
		savedLocation = location.copy()
		updatePos(ch)
		return ch
	}
	
	/** Push a character back to the stream. */
	fun unreadChar(ch: Char) {
		check(savedChar == null) { "Cannot unread a char when one is already saved!" }
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
					// if the next character is / it means that there is // so it is a comment, skip until /n
					val next = readChar()
					if (next == '/') {
						var c = readChar()
						while (c != '\n' && c != null) c = readChar()
						if (c == '\n') unreadChar(c) // replace '\n' in the buffer, and it will be seen as a character of WHITESPACE
					} else { // it wasn't a comment, and it has to be put back
						if (next != null) unreadChar(next)
						// do we need to put this back?
						unreadChar('/') // put back original '/'
						return
					}
				}
				
				else -> {
					unreadChar(ch)
					return
				}
			}
			ch = readChar()
		}
	}
	
	/** Interprets a series of [Char]s as a [String]. Returns corresponding [Token]. */
	fun parseStringToken(tokenLoc: SourceLocation): StringToken {
		val token = buildString {
			while (true) {
				val ch = readChar() ?: throw GrammarError(tokenLoc, "Undetermined string")
				if (ch == '"') break
				append(ch)
			}
		}
		return StringToken(value = token, location = tokenLoc)
	}
	
	/** Interprets a series of [Char]s as a literal number, type [Float]. Returns corresponding [Token]. */
	fun parseFloatToken(firstChar: Char, tokenLoc: SourceLocation): NumberToken {
		val token = buildString {
			append(firstChar)
			while (true) {
				val ch = readChar() ?: break // if readChar() returns null (EOF) break
				
				if (ch.isDigit() || ch in "+-.eE") {
					append(ch)
				} else {
					unreadChar(ch)
					break
				}
			}
		}
		val value = token.toFloatOrNull()
			?: throw GrammarError(tokenLoc, "'$token' is an invalid floating-point number")
		return NumberToken(value, tokenLoc)
	}
	
	/**
	 * Interpret a series of [Char] as either a [KeywordToken], if the [String] is found in the [Keyword] dictionary,
	 * or as an [IdentifierToken] otherwise. The corresponding [Token] is then returned.
	 */
	fun parseKeywordOrIdentifierToken(firstChar: Char, tokenLoc: SourceLocation): Token {
		val token = buildString {
			append(firstChar)
			
			while (true) {
				val ch = readChar() ?: break
				
				if (ch.isLetterOrDigit() || ch == '_') {
					append(ch)
				} else {
					unreadChar(ch)
					break
				}
			}
		}
		
		val keyword = Keyword.fromString(token)
		return if (keyword != null) {
			KeywordToken(keyword, tokenLoc)
		} else {
			IdentifierToken(token, tokenLoc)
		}
	}
	
	/**
	 * Read a [Token] within the [SceneInputStream]
	 * - If it is a symbol (comma, parenthesis, etc.), it returns a [SymbolToken];
	 * - If it is a digit, it returns a [NumberToken];
	 * - If it is "", it returns a [StringToken];
	 * - If it is a sequence of characters a…z, it returns a [KeywordToken] if the sequence is a keyword, [IdentifierToken] otherwise;
	 * - If the file is finished, it returns [StopToken].
	 */
	fun readToken(): Token {
		
		// if savedToken is not null, call it 'token', clear the saved state, and return it
		savedToken?.let { token ->
			savedToken = null
			return token
		}
		
		skipWhitespacesAndComments() // at this point we're sure that ch does *not* contain a whitespace character
		
		val ch = readChar() ?: return StopToken(location = location.copy())  // no more characters in the file, so return a StopToken
		
		val tokenLoc = location.copy() // the position in the stream
		
		return when {
			// one-character symbol, like '(' or ','
			ch in SYMBOLS -> {
				SymbolToken(ch, tokenLoc)
			}
			
			// a literal string (used for file names)
			ch == '"' -> {
				parseStringToken(tokenLoc)
			}
			
			// a floating-point number
			ch.isDigit() || ch in "+-." -> {
				parseFloatToken(ch, tokenLoc)
			}
			
			// since it begins with an alphabetic character, it must either be a keyword or an identifier
			ch.isLetter() || ch == '_' -> {
				parseKeywordOrIdentifierToken(ch, tokenLoc)
			}
			
			// we got some weird character, like '@' or '&'
			else -> throw GrammarError(tokenLoc, "Invalid character '$ch'")
		}
		
	}
	
	/** Makes as if [token] were never read from the stream. */
	fun unreadToken(token: Token) {
		// throw IllegalStateException if no savedToken to be unread
		check(savedToken == null) { "Cannot unread a token when one is already saved!" }
		savedToken = token
	}
}