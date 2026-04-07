import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestTemplate
import kotlin.math.abs


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
	
	@Test
			/**
			 * with vector and floats is convenient to use the fun isClose instead of ==
			 */
	fun `test pointToVec`() {
		val vecRes=pointB.pointToVec()
		assertTrue(vecRes.isClose(vecB)) //questa va cambiata in isClose di tommi?
	}
	
	@Test
	fun `test sum`() {
		val c=pointA.plus(vecB)
		assertTrue(c.isClose(Point(2.0f,3.0f,4.0f)))
	}
	
	@Test
			/**
			 * difference between two [Point] returns a [Vec]
			 */
	fun `test minus Vec`(){
		val d=pointA.minus(pointB)
		assertTrue(d.isClose(Vec(0.0f,1.0f,2.0f)))
	}
	
	@Test
			/**
			 * difference between a [Point] & [Vec] return [Point]
			 */
	fun`test minus Point`(){
		val e=pointB.minus(vecB)
		assertTrue (e.isClose(Point(0.0f,0.0f,0.0f)))
		assertFalse (e.isClose(pointA))
	}
	
}

class NormalTest(){
	val nA=Normal(1.0f,1.0f,1.0f)
	val nB=Normal(1.0f,2.0f,3.0f)
	val vecB = Vec(1.0f, 1.0f, 1.0f)
	val scalar=3.0f
	
	@Test
	fun `test isClose`() {
		assertTrue(nB.isClose(Normal(1.0f, 2.0f, 3.0f)))
		assertFalse(nA.isClose(Normal(1.0f, 2.0f, 3.0f)))
	}
	
	@Test
	fun `test times`() {
		val nC=nA.times(scalar)
		assertTrue(nC.isClose(Normal(3.0f, 3.0f, 3.0f)))
		assertFalse(nC.isClose(Normal(3.0f, 1.0f, 3.0f)))
	}
	
	@Test
	fun`test unaryMinus`(){
		val nN=nA.unaryMinus()
		assertTrue(nN.isClose(Normal(-1.0f,-1.0f,-1.0f)))
	}
	
	@Test
	fun`test dot`() {
		val sc=nA.dot(nB)
		assertTrue(areClose(sc,6.0f))
		assertFalse(areClose(sc,5.0f))
	}
	
	@Test
	fun`test cross`() {
		val cV=nA.cross(vecB)
		assertTrue(cV.isClose(Vec()))
		assertFalse (cV.isClose(Vec(1.0f,1.0f,1.0f)))
	}
}




