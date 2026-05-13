import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PCGTest {
	
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test random`() {
		val pcg = PCG()
		
		assertEquals(1753877967969059832u, pcg.state)
		assertEquals(109u, pcg.inc)
		
		for (expected in uintArrayOf(
			2707161783u, 2068313097u,
			3122475824u, 2211639955u,
			3215226955u, 3421331566u
		)
		) assertEquals(pcg.random(), expected)
	}
}
