import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ColorTest {
	
	val colorA = Color(1.0f, 2.0f, 3.0f)
	val colorB = Color(5.0f, 6.0f, 7.0f)
	val scalar = 3.0f
	
	@Test
	fun plus() {
		val sum = colorA + colorB
		assertTrue { sum.areColorsClose(Color(6.0f, 8.0f, 10.0f)) }
	}
	
	@Test
	fun times() {
		val colorC = colorA * scalar
		assertTrue { colorC.areColorsClose(Color(3.0f, 6.0f, 9.0f)) }
	}
	
	@Test
	fun testTimes() {
		val colorC = colorA * colorB
		assertTrue { colorC.areColorsClose(Color(5.0f, 12.0f, 21.0f)) }
	}
	
	@Test
	fun areClose() {
		assertFalse { colorA.areColorsClose(Color(11.0f, 2.0f, 3.0f)) }
	}
	
	
}