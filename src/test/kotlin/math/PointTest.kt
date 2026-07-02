package math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PointTest {
	
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
	fun `test toString`() {
		assertEquals("Point(1.0, 2.0, 3.0)", pointA.toString())
	}
}
