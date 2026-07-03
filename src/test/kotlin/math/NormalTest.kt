package math

import materials.areClose
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NormalTest {
	
	val nA = Normal(1f, 1f, 1f)
	val nB = Normal(1f, 2f, 3f)
	val vecB = Vec(1f, 1f, 1f)
	val eps = 10e-5f
	
	@Test
	fun `test isClose`() {
		assertTrue(nB.isClose(Normal(1f, 2f, 3f)))
		assertFalse(nA.isClose(Normal(1f, 2f, 3f)))
	}
	
	// --- Operator overloading ---

	@Test
	fun `test unaryMinus`() {
		val nN = -nA
		assertTrue(nN.isClose(Normal(-1f, -1f, -1f)))
	}
	
	@Test
	fun `test times Scalar`() {
		val nC = nA * 3f
		assertTrue(nC.isClose(Normal(3f, 3f, 3f)))
		assertFalse(nC.isClose(Normal(3f, 1f, 3f)))
	}
	
	// ---Utility functions ---
	
	@Test
	fun `test dot`() {
		val sc = nA dot vecB
		assertTrue(areClose(sc, 3f))
	}
	
	@Test
	fun `test squaredNorm function`() {
		val squaredNormA: Float = nB.squaredNorm()
		assertTrue(areClose(squaredNormA, 14f))
	}
	
	@Test
	fun `test normalize`() {
		val nC = nA.normalize()
		assertEquals(nC.norm(), 1f, eps)
	}
	
	@Test
	fun `test toVec`() {
		val vecA = nA.toVec()
		assertEquals(vecA.x, nA.x, eps)
		assertEquals(vecA.y, nA.y, eps)
		assertEquals(vecA.z, nA.z, eps)
	}
	
	@Test
	fun `test toString`() {
		assertEquals("Normal(1.0, 1.0, 1.0)", nA.toString())
	}
}
