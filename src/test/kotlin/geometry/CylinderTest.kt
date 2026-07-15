package geometry

import materials.areClose
import math.Point
import math.SurfaceVec
import math.Vec
import math.scaling
import math.translation
import math.vecX
import math.vecZ
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CylinderTest {
	
	val cylinder = Cylinder()
	val t = Vec(10f, 0f, 0f)
	val cylinder1 = Cylinder(translation(t))
	val c = Vec(10f, 10f, 10f)
	val cylinder2 = Cylinder(scaling(c))
	val c3 = Vec(2f, 1f, 1f)
	val cylinder3 = Cylinder(scaling(c3))
	
	@Test
	fun `test cylinderNormal`() {
		val ray = Ray(Point(3f, 0f, 0f), -vecX())
		val invRay: Ray = ray.transform(scaling(c3).inverse())
		val normal = vecX().toNormal()
		val hit = cylinder3.rayIntersection(ray)!!
		val hitPoint = invRay.at(hit.t)
		val actual = (scaling(c3) * cylinderNormal(hitPoint, invRay.dir)).normalize()
		assertTrue(actual.isClose(normal))
	}
	
	@Test
	fun `test cylinderPointToUV`() {
		val point1 = Point(1f, 0f, 0f)
		val uv1 = SurfaceVec(0f, 0.5f)
		val point2 = Point(0.707f, 0.707f, 0f)
		val uv2 = SurfaceVec(0.125f, 0.5f)
		val point3 = Point(0f, 0f, 1f)
		val uv3 = SurfaceVec(0.5f, 0.5f)
		
		assertTrue(cylinderPointToUV(point1).isClose(uv1))
		assertTrue(cylinderPointToUV(point2).isClose(uv2))
		assertTrue(cylinderPointToUV(point3).isClose(uv3))
	}
	
	@Test
	fun `test rayIntersection z direction`() {
		val ray1 = Ray(Point(0f, 0f, 2f), -vecZ())
		val uv1 = SurfaceVec(0.5f, 0.5f)
		val hit1 = cylinder.rayIntersection(ray1)
		
		assertNotNull(hit1)
		assertTrue(
			hit1.normal.isClose(vecZ().toNormal()) &&
					hit1.surfacePoint.isClose(uv1) &&
					areClose(hit1.t, 1f)
		)
	}
	
	@Test
	fun `test rayIntersection x direction`() {
		val ray2 = Ray(Point(3f, 0f, 0f), -vecX())
		val hit2 = cylinder.rayIntersection(ray2)
		val uv2 = SurfaceVec(0f, 1 / 2f)
		
		assertNotNull(hit2)
		assertTrue(
			hit2.normal.isClose(vecX().toNormal()) &&
					hit2.surfacePoint.isClose(uv2) &&
					areClose(hit2.t, 2f)
		)
	}
	
	@Test
	fun `test rayIntersection inside cylinder`() {
		val ray3 = Ray(Point(0f, 0f, 0f), vecX())
		val hit3 = cylinder.rayIntersection(ray3)
		val uv3 = SurfaceVec(0f, 1 / 2f)
		
		assertNotNull(hit3)
		assertTrue(
			hit3.normal.isClose(-vecX().toNormal()) &&
					hit3.surfacePoint.isClose(uv3) &&
					areClose(hit3.t, 1f)
		)
	}
	
	@Test
	fun `test rayIntersection with translation`() {
		val ray = Ray(Point(10f, 0f, 2f), -vecZ())
		val hit = cylinder1.rayIntersection(ray)
		val uv = SurfaceVec(0.5f, 0.5f)
		
		val ray2 = Ray(Point(13f, 0f, 0f), -vecX())
		val hit2 = cylinder1.rayIntersection(ray2)
		val uv2 = SurfaceVec(0f, 1 / 2f)
		
		assertNotNull(hit)
		assertNotNull(hit2)
		
		assertTrue(
			hit.normal.isClose(vecZ().toNormal()) &&
					hit.surfacePoint.isClose(uv) &&
					areClose(hit.t, 1f)
		)
		
		assertTrue(
			hit2.normal.isClose(vecX().toNormal()) &&
					hit2.surfacePoint.isClose(uv2) &&
					areClose(hit2.t, 2f)
		)
	}
	
	@Test
	fun `test scaling rayIntersection`() {
		val ray = Ray(Point(0f, 0f, 15f), -vecZ())
		val hit = cylinder2.rayIntersection(ray)
		val uv = SurfaceVec(0.5f, 0.5f)
		
		assertNotNull(hit)
		assertTrue(
			hit.normal.normalize().isClose(vecZ().toNormal()) &&
					hit.surfacePoint.isClose(uv) &&
					areClose(hit.t, 5f)
		)
	}
	
	/**
	 * Verify that the [Ray] used in `test rayIntersection z direction` no longer hits [cylinder1]
	 */
	@Test
	fun `test noIntersection`() {
		val ray1 = Ray(Point(0f, 0f, 2f), -vecZ())
		val hit1 = cylinder1.rayIntersection(ray1)
		
		val ray2 = Ray(Point(-10f, 0f, 0f), -vecZ())
		val hit2 = cylinder1.rayIntersection(ray2)
		
		assertNull(hit1)
		assertNull(hit2)
	}
	
	@Test
	fun `test rayIntersectionShape lateral surface`() {
		val ray = Ray(Point(-2f, 0f, 0f), vecX())
		val hits = cylinder.rayIntersectionShape(ray)
		
		assertEquals(2, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
		assertEquals(3f, hits[1].t, 1e-5f)
	}
	
	@Test
	fun `test rayIntersectionShape caps`() {
		val ray = Ray(Point(0f, 0f, 2f), -vecZ())
		val hits = cylinder.rayIntersectionShape(ray)
		
		assertEquals(2, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
		assertEquals(3f, hits[1].t, 1e-5f)
	}
	
	@Test
	fun `test rayIntersectionShape inside cylinder`() {
		val ray = Ray(Point(0f, 0f, 0f), vecX())
		
		val hits = cylinder.rayIntersectionShape(ray)
		
		assertEquals(1, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
	}
	
	@Test
	fun `test contains`() {
		val cylinder = Cylinder(translation(Vec(2f, 0f, 0f)))
		
		assertTrue(cylinder.contains(Point(2f, 0f, 0f)))
		assertTrue(cylinder.contains(Point(2.5f, 0f, 0f)))
		assertFalse(cylinder.contains(Point(0f, 0f, 0f)))
		assertFalse(cylinder.contains(Point(3.5f, 0f, 0f)))
		assertFalse(cylinder.contains(Point(2f, 0f, 1.5f)))
	}
}