import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VecTest() {
	
}

class PointTest() {
	
	val pointA = Point(1.0f, 2.0f, 3.0f)
	val pointB = Point(1.0f, 1.0f, 1.0f)
	val pointC = Point()
	
	val vecB = Vec(1.0f, 1.0f, 1.0f)
	
	@Test
	fun `test isClose`() {
		assertTrue(pointA.isClose(Point(1.0f, 2.0f, 3.0f)))
		assertFalse(pointB.isClose(Point(1.0f, 2.0f, 3.0f)))
	}
	
	/** With vector and floats it is convenient to use the fun isClose instead of ==. */
	@Test
	fun `test point fun toVec`() {
		val vecRes = pointB.toVec()
		assertTrue(vecRes.isClose(vecB))
	}
	
	@Test
	fun `test sum`() {
		val c = pointA.plus(vecB)
		assertTrue(c.isClose(Point(2.0f, 3.0f, 4.0f)))
	}
	
	/** Difference between two [Point]s returns a [Vec]. */
	@Test
	fun `test minus Vec`() {
		val d = pointA.minus(pointB)
		assertTrue(d.isClose(Vec(0.0f, 1.0f, 2.0f)))
	}
	
	/** Difference between a [Point] & a [Vec] returns a [Point]. */
	@Test
	fun `test minus Point`() {
		val e = pointB.minus(vecB)
		assertTrue(e.isClose(Point(0.0f, 0.0f, 0.0f)))
		assertFalse(e.isClose(pointA))
	}
}

class NormalTest() {
	val nA = Normal(1.0f, 1.0f, 1.0f)
	val nB = Normal(1.0f, 2.0f, 3.0f)
	val vecB = Vec(1.0f, 1.0f, 1.0f)
	val scalar = 3.0f
	
	@Test
	fun `test isClose`() {
		assertTrue(nB.isClose(Normal(1.0f, 2.0f, 3.0f)))
		assertFalse(nA.isClose(Normal(1.0f, 2.0f, 3.0f)))
	}
	
	@Test
	fun `test times`() {
		val nC = nA * scalar
		assertTrue(nC.isClose(Normal(3.0f, 3.0f, 3.0f)))
		assertFalse(nC.isClose(Normal(3.0f, 1.0f, 3.0f)))
	}
	
	@Test
	fun `test unaryMinus`() {
		val nN = nA.unaryMinus()
		assertTrue(nN.isClose(Normal(-1.0f, -1.0f, -1.0f)))
	}
	
	@Test
	fun `test dot`() {
		val sc = nA dot vecB
		assertTrue(areClose(sc, 3.0f))
	}
	
}




