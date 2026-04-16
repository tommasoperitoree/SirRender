import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VecTest() {
	
	val vecA = Vec(1f, 2f, 3f)
	val vecB = Vec(1f, 1f, 1f)
	
	@Test
	fun `test isClose`() {
		assertTrue(vecA.isClose(Vec(1f, 2f, 3f)))
		assertFalse(vecB.isClose(Vec(1f, 2f, 3f)))
	}
	
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
	fun `test operator times Scalar`() {
		val vecC = vecA * 2f
		assertTrue(vecC.isClose(Vec(2f, 4f, 6f)))
	}
	
	@Test
	fun `test squaredNorm function`() {
		val squaredNormA: Float = vecA.squaredNorm()
		assertTrue(areClose(squaredNormA, 14f))
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
	
	/** With vector and floats it is convenient to use the fun isClose instead of ==. */
	@Test
	fun `test point toVec`() {
		val vecRes = pointB.toVec()
		assertTrue(vecRes.isClose(vecB))
	}
	
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
}

class NormalTest() {
	val nA = Normal(1f, 1f, 1f)
	val nB = Normal(1f, 2f, 3f)
	val vecB = Vec(1f, 1f, 1f)
	
	@Test
	fun `test isClose`() {
		assertTrue(nB.isClose(Normal(1f, 2f, 3f)))
		assertFalse(nA.isClose(Normal(1f, 2f, 3f)))
	}
	
	@Test
	fun `test times Scalar`() {
		val nC = nA * 3f
		assertTrue(nC.isClose(Normal(3f, 3f, 3f)))
		assertFalse(nC.isClose(Normal(3f, 1f, 3f)))
	}
	
	@Test
	fun `test unaryMinus`() {
		val nN = -nA
		assertTrue(nN.isClose(Normal(-1f, -1f, -1f)))
	}
	
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
	
}




