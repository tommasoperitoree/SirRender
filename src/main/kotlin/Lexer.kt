class SourceLocation(
	val fileName: String,
	val line: Long,
	val column: Long
) {

}

sealed class Token {
	class SymbolToken(val value: String) : Token()
	class LiteralNumberToken(val value: Long) : Token()
	class LiteralStringToken(val value: String) : Token()
	class KeywordToken(val keyword: String) : Token()
	class IdentifierToken(val identifier: String) : Token()
	class StopToken(val stop: String) : Token()
}

/**
 * If it is a symbol (comma, parenthesis, etc.), it returns a SymbolToken;
 * If it is a digit, it returns a LiteralNumberToken;
 * If it is ", it returns a LiteralStringToken;
 * If it is a sequence of characters a…z, it returns a KeywordToken if the sequence is a keyword, IdentifierToken otherwise;
 * If the file is finished, it returns StopToken.
 */