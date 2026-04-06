import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs



class PointTest(){
	
	val pointA=Point( x = 1.0f, y = 2.0f, z = 3.0f)
	val pointB=Point(x=1.0f,y=1.0f,z=1.0f)
	val pointC=Point()
	
	val vecB=Vec(x=1.0f,y=1.0f,z=1.0f)
	
	@Test
	fun `test isClosePoint`(){
		assertTrue(pointA.isClosePoint(Point(1.0f, 2.0f, 3.0f)))
		assertFalse(pointB.isClosePoint(Point(1.0f, 2.0f, 3.0f)))
	}
	
	@Test
	fun `test pointToVec`(){
		assertTrue(actual = pointB.pointToVec()== vecB)
	}
	
}

