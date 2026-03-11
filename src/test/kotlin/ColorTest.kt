import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ColorTest {
	
	val colorA = Color(1.0f, 2.0f, 3.0f)
	val colorB = Color(5.0f, 6.0f, 7.0f)
	val scalar = 3.0f
	
	@Test
	fun `test sum of two Colors`() {
		val sum = colorA + colorB
		assertTrue { sum.isCloseColor(Color(6.0f, 8.0f, 10.0f)) }
	}
	
	@Test
	fun `test multiplication of Color by scalar`() {
		val colorC = colorA * scalar
		assertTrue { colorC.isCloseColor(Color(3.0f, 6.0f, 9.0f)) }
	}
	
	@Test
	fun `test multiplication of Color by Color`() {
		val colorC = colorA * colorB
		assertTrue { colorC.isCloseColor(Color(5.0f, 12.0f, 21.0f)) }
	}
	
	@Test
	fun `test isColorClose function`() {
		assertFalse { colorA.isCloseColor(Color(11.0f, 2.0f, 3.0f)) }
	}
	
	
}