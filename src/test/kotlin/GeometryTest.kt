import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs


class PointTest() {
	
	val pointA = Point(1.0f, 2.0f, 3.0f)
	val pointB = Point(1.0f, 1.0f, 1.0f)
	val pointC = Point()
	
	val vecB = Vec(1.0f, 1.0f, 1.0f)
	
	@Test
	fun `test isClosePoint`() {
		assertTrue(pointA.isPointClose(Point(1.0f, 2.0f, 3.0f)))
		assertFalse(pointB.isPointClose(Point(1.0f, 2.0f, 3.0f)))
	}
	
	@Test
	fun `test pointToVec`() {
		assertTrue(actual = pointB.pointToVec() == vecB)
	}
	
}

