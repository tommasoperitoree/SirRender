import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.Reader
import java.util.stream.Stream


class LexerTest {
	
	/** Set of test of [SceneInputStream] **/
	@Test
	fun `test SceneInputStream`() {
		//need reader() in order to read a char
		val stream = SceneInputStream("abc /nd/nef".reader())
		
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
		
	}
}