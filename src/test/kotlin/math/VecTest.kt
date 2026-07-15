package math

import materials.areClose
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

//da decidere isClose lo teniamo sempre in alto
// oppure lo inseriamo nelle utulity functions?
class VecTest {

	val vecA = Vec(1f, 2f, 3f)
	val vecB = Vec(1f, 1f, 1f)
	val eps = 10e-5f
	
	@Test
	fun `test isClose`() {
		assertTrue(vecA.isClose(Vec(1f, 2f, 3f)))
		assertFalse(vecB.isClose(Vec(1f, 2f, 3f)))
	}
	
	// --- Operator overloading ---

	@Test
	fun `test operator plus`() {
		val vecC = vecA + vecB
		assertTrue(vecC.isClose(Vec(2f, 3f, 4f)))
	}
	
	@Test
	fun `test operator minus`() {
		val vecC = vecA - vecB
		assertTrue(vecC.isClose(Vec(0f, 1f, 2f)))
	}
	
	@Test
	fun `test operator unaryMinus`() {
		val vecC = vecA.unaryMinus()
		assertTrue(vecC.isClose(Vec(-1f, -2f, -3f)))
	}
	
	@Test
	fun `test operator times Scalar`() {
		val vecC = vecA * 2f
		assertTrue(vecC.isClose(Vec(2f, 4f, 6f)))
	}
	
	// --- Utility functions ---

	@Test
	fun `test squaredNorm function`() {
		val squaredNormA: Float = vecA.squaredNorm()
		assertTrue(areClose(squaredNormA, 14f))
	}
	
	@Test
	fun `test Norm function`() {
		val normA = vecA.norm()
		assertTrue(areClose(normA, sqrt(14.0f)))
	}
	
	@Test
	fun `test dot product Vec`() {
		val dot = vecA dot vecB
		assertTrue(areClose(dot, 6f))
	}
	
	@Test
	fun `test cross product Vec`() {
		val cross: Normal = vecA cross vecB
		assertTrue(cross.isClose(Normal(-1f, 2f, -1f)))
	}
	
	@Test
	fun `test normalize`() {
		val vecC = vecA.normalize()
		assertEquals(vecC.norm(), 1f, eps)
	}
	
	@Test
	fun `test toNormal`() {
		val nA = vecA.toNormal()
		assertEquals(vecA.x, nA.x, eps)
		assertEquals(vecA.y, nA.y, eps)
		assertEquals(vecA.z, nA.z, eps)
	}
	
	@Test
	fun `test toString`() {
		assertEquals("Vec(1.0, 2.0, 3.0)", vecA.toString())
	}
}
