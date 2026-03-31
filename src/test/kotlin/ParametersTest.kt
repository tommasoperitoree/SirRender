import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ParametersTest {
	
	@Test
	fun `test valid args`() {
		val p = Parameters.fromArgs(arrayOf("input.pfm", "0.2", "1.0", "output.png"))
		assertEquals("input.pfm", p.inputFileName)
		assertEquals(0.2f, p.factor)
		assertEquals(1.0f, p.gamma)
		assertEquals("output.png", p.outputFileName)
	}
	
	@Test
	fun `test wrong number of args`() {
		assertThrows(IllegalArgumentException::class.java) {
			Parameters.fromArgs(arrayOf("input.pfm", "0.2"))
		}
	}
	
	@Test
	fun `test invalid types`() {
		assertThrows(IllegalArgumentException::class.java) {
			Parameters.fromArgs(arrayOf("input.pfm", "abc", "1.0", "output.png")) // wrong factor type
		}
		assertThrows(IllegalArgumentException::class.java) {
			Parameters.fromArgs(arrayOf("input.pfm", "0.2", "abc", "output.png")) // wrong gamma type
		}
	}
}