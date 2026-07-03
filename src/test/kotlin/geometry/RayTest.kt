package geometry

import math.Point
import math.Vec
import math.rotationX
import math.translation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RayTest {
	
	val ray1 = Ray(Point(1.0f, 2.0f, 3.0f), Vec(5.0f, 4.0f, -1.0f))
	val ray2 = Ray(Point(1.0f, 2.0f, 3.0f), Vec(5.0f, 4.0f, -1.0f))
	val ray3 = Ray(Point(5.0f, 1.0f, 0.0f), Vec(3.0f, 9.0f, 4.0f))
	
	@Test
	fun `test isClose`() {
		Assertions.assertTrue(ray1.isClose(ray2))
		Assertions.assertFalse(ray1.isClose(ray3))
	}
	
	@Test
	fun `test at Ray`() {
		Assertions.assertTrue(ray1.at(0.0f).isClose(Point(1.0f, 2.0f, 3.0f)))
		Assertions.assertTrue(ray1.at(1.0f).isClose(Point(6.0f, 6.0f, 2.0f)))
		Assertions.assertTrue(ray1.at(2.0f).isClose(Point(11.0f, 10.0f, 1.0f)))
	}
	
	@Test
	fun `test Ray transform`() {
		val ray = Ray(Point(1.0f, 2.0f, 3.0f), Vec(6.0f, 5.0f, 4.0f))
		val transformation = translation(Vec(10.0f, 11.0f, 12.0f)) * rotationX(90.0f)
		val transformed = ray.transform(transformation)
		
		Assertions.assertTrue(transformed.origin.isClose(Point(11.0f, 8.0f, 14.0f)))
		Assertions.assertTrue(transformed.dir.isClose(Vec(6.0f, -4.0f, 5.0f)))
		Assertions.assertEquals(1e-3f, transformed.tMin)
		Assertions.assertEquals(Float.POSITIVE_INFINITY, transformed.tMax)
		Assertions.assertEquals(0, transformed.depth)
	}
	
}