import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class LexerTest {
	
	/** Set of test of [SceneInputStream] **/
	@Test
	fun `test sceneInputStream`() {
		//need reader() in order to read a char
		val stream = SceneInputStream("abc \nd //comment \ne\tf".reader())
		
		assertEquals(1, stream.location.lineNum)
		assertEquals(1, stream.location.colNum)
		
		//NB 'a' is a char typo, meanwhile "a" is a String typo
		assertEquals('a', stream.readChar())
		assertEquals(1, stream.location.lineNum)
		assertEquals(2, stream.location.colNum)
		
		//Overwrite a with A, then readChar read A and then the location goes to (1,2)
		stream.unreadChar('A')
		assertEquals(1, stream.location.lineNum)
		assertEquals(1, stream.location.colNum)
		
		assertEquals('A', stream.readChar())
		assertEquals(1, stream.location.lineNum)
		assertEquals(2, stream.location.colNum)
		
		assertEquals('b', stream.readChar())
		assertEquals(1, stream.location.lineNum)
		assertEquals(3, stream.location.colNum)
		
		assertEquals('c', stream.readChar())
		assertEquals(1, stream.location.lineNum)
		assertEquals(4, stream.location.colNum)
		
		// *** TEST skipWhitespaceAndComments (whitespace)*** //
		stream.skipWhitespacesAndComments()
		
		//*** TEST updatePos() after \n ***//
		assertEquals(2, stream.location.lineNum)
		assertEquals(1, stream.location.colNum)
		
		assertEquals('d', stream.readChar())
		assertEquals(2, stream.location.lineNum)
		assertEquals(2, stream.location.colNum)
		
		stream.skipWhitespacesAndComments() //test comment//
		
		assertEquals(3, stream.location.lineNum)
		assertEquals(1, stream.location.colNum)
		
		assertEquals('e', stream.readChar())
		assertEquals(3, stream.location.lineNum)
		assertEquals(2, stream.location.colNum)
		
		stream.skipWhitespacesAndComments() //test \t//
		//*** TEST updatePos() after \t ***//
		assertEquals(3, stream.location.lineNum)
		assertEquals(6, stream.location.colNum)
		
		assertEquals('f', stream.readChar())
		assertEquals(3, stream.location.lineNum)
		assertEquals(7, stream.location.colNum)
		
		assertEquals(expected = null, actual = stream.readChar())
	}
}