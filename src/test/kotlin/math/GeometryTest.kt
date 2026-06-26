import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

//da decidere isClose lo teniamo sempre in alto
// oppure lo inseriamo nelle utulity functions?
class VecTest() {
	
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
	fun `test toString`(){
		assertEquals("Vec(1.0, 2.0, 3.0)", vecA.toString())
	}
}

class PointTest() {
	
	val pointA = Point(1f, 2f, 3f)
	val pointB = Point(1f, 1f, 1f)
	val vecB = Vec(1f, 1f, 1f)
	
	@Test
	fun `test isClose`() {
		assertTrue(pointA.isClose(Point(1f, 2f, 3f)))
		assertFalse(pointB.isClose(Point(1f, 2f, 3f)))
	}
	
	// --- Operator overloading ---
	@Test
	fun `test operator plus Vec`() {
		val c = pointA + vecB
		assertTrue(c.isClose(Point(2f, 3f, 4f)))
	}
	
	/** Difference between two [Point]s returns a [Vec]. */
	@Test
	fun `test operator minus Vec`() {
		val d = pointA - pointB
		assertTrue(d.isClose(Vec(0f, 1f, 2f)))
	}
	
	/** Difference between a [Point] & a [Vec] returns a [Point]. */
	@Test
	fun `test minus Point`() {
		val e = pointB.minus(vecB)
		assertTrue(e.isClose(Point(0f, 0f, 0f)))
		assertFalse(e.isClose(pointA))
	}
	
	// --- Utility functions ---
	
	/** With vector and floats it is convenient to use the fun isClose instead of ==. */
	@Test
	fun `test point toVec`() {
		val vecRes = pointB.toVec()
		assertTrue(vecRes.isClose(vecB))
	}
	
	@Test
	fun `test toString`(){
		assertEquals("Point(1.0, 2.0, 3.0)", pointA.toString())
	}
}

class NormalTest() {
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
	fun `test toString`(){
		assertEquals("Normal(1.0, 1.0, 1.0)", nA.toString())
	}
}

class ONBTest() {
	
	val pcg = PCG()
	val eps = 1e-5f
	
	@Test
	fun `test Vec2d isClose`() {
		val a = Vec2d(1f, 2f)
		
		assertTrue(a.isClose(Vec2d(1f, 1f)))
		assertFalse(a.isClose(Vec2d(3f, 2f)))
	}
	
	@Test
	fun `random testing PCG`() {
		repeat(1000) {
			val (x, y, z) = List(3) { pcg.randomFloat() }
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