package math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ONBTest {
	
	val pcg = PCG()
	val eps = 1e-5f
	
	@Test
	fun `test Vec2d isClose`() {
		val a = SurfaceVec(1f, 2f)
		
		assertTrue(a.isClose(SurfaceVec(1f, 2f)))
		assertFalse(a.isClose(SurfaceVec(3f, 2f)))
	}
	
	@Test
	fun `test ONB with negative normal`() {
		val normal = Normal(0f, 0f, -1f)
		
		val (e1, e2, e3) = createOnbFromZ(normal)
		
		assertTrue(e3.isClose(Vec(0f, 0f, -1f)))
		
		assertEquals(0f, e1 dot e2, eps)
		assertEquals(0f, e2 dot e3, eps)
		assertEquals(0f, e3 dot e1, eps)
		
		assertEquals(1f, e1.squaredNorm(), eps)
		assertEquals(1f, e2.squaredNorm(), eps)
		assertEquals(1f, e3.squaredNorm(), eps)
	}
	
	
	@Test
	fun `random testing PCG`() {
		repeat(1000) {
			val (x, y, z) = List(3) { pcg.randomFloat() * 2f - 1f } // test also negative normal
			val normal = Normal(x, y, z).normalize()
			val (e1, e2, e3) = createOnbFromZ(normal)
			
			// verify z-axis is aligned with normal
			assertTrue(e3.isClose(normal.toVec()))
			
			// verify that the base is orthogonal
			assertEquals(0f, e1 dot e2, eps)
			assertEquals(0f, e2 dot e3, eps)
			assertEquals(0f, e3 dot e1, eps)
			
			// verify that each component is normalized
			assertEquals(1f, e1.squaredNorm(), eps)
			assertEquals(1f, e2.squaredNorm(), eps)
			assertEquals(1f, e3.squaredNorm(), eps)
		}
	}
}
