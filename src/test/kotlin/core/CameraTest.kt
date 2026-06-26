package core

import materials.areClose
import math.Point
import math.rotationZ
import math.translation
import math.vecY
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CameraTest {
	
	@Test
	fun `test OrthogonalCamera`() {
		
		val cam = OrthogonalCamera(2f)
		
		val ray1 = cam.fireRay(0f, 0f)
		val ray2 = cam.fireRay(1f, 0f)
		val ray3 = cam.fireRay(0f, 1f)
		val ray4 = cam.fireRay(1f, 1f)
		
		// verify that the rays are parallel by verifying that cross products vanish
		Assertions.assertTrue(areClose(0f, (ray1.dir cross ray2.dir).squaredNorm()))
		Assertions.assertTrue(areClose(0f, (ray1.dir cross ray3.dir).squaredNorm()))
		Assertions.assertTrue(areClose(0f, (ray1.dir cross ray4.dir).squaredNorm()))
		
		// verify that the ray hitting the corners have the right coordinates
		Assertions.assertTrue(ray1.at(1f).isClose(Point(0f, 2f, -1f)))
		Assertions.assertTrue(ray2.at(1f).isClose(Point(0f, -2f, -1f)))
		Assertions.assertTrue(ray3.at(1f).isClose(Point(0f, 2f, 1f)))
		Assertions.assertTrue(ray4.at(1f).isClose(Point(0f, -2f, 1f)))
	}
	
	@Test
	fun `test OrthogonalCamera transform`() {
		val cam = OrthogonalCamera(transformation = (translation(-vecY() * 2f)) * rotationZ(90f))
		val ray = cam.fireRay(0.5f, 0.5f)
		Assertions.assertTrue(ray.at(1f).isClose(Point(0f, -2f, 0f)))
	}
	
	@Test
	fun `test PerspectiveCamera`() {
		val cam = PerspectiveCamera(1f, 2f)
		
		val ray1 = cam.fireRay(0f, 0f)
		val ray2 = cam.fireRay(1f, 0f)
		val ray3 = cam.fireRay(0f, 1f)
		val ray4 = cam.fireRay(1f, 1f)
		
		// Verify that all the rays depart from the same point
		Assertions.assertTrue(ray1.origin.isClose(ray2.origin))
		Assertions.assertTrue(ray1.origin.isClose(ray3.origin))
		Assertions.assertTrue(ray1.origin.isClose(ray4.origin))
		
		// Verify that the ray hitting the corners have the right coordinates
		Assertions.assertTrue(ray1.at(1f).isClose(Point(0f, 2f, -1f)))
		Assertions.assertTrue(ray2.at(1f).isClose(Point(0f, -2f, -1f)))
		Assertions.assertTrue(ray3.at(1f).isClose(Point(0f, 2f, 1f)))
		Assertions.assertTrue(ray4.at(1f).isClose(Point(0f, -2f, 1f)))
	}
	
}