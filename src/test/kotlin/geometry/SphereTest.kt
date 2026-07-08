package geometry

import materials.areClose
import math.Normal
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

class SphereTest {
	
	val sphere = Sphere()
	val t = Vec(10f, 0f, 0f)
	val sphere1 = Sphere(translation(t))
	val s = Vec(10f, 10f, 10f)
	val sphere2 = Sphere(scaling(s))
	val s3 = Vec(2f, 1f, 1f)
	val sphere3 = Sphere(scaling(s3))
	
	@Test
	fun `test sphereNormal`() {
		val ray = Ray(Point(12f, 12f, 0f), Vec(-1f, -1f, 0f))
		val invRay: Ray = ray.transform(scaling(s3).inverse())
		val normal = Normal(1f, 4f, 0f).normalize()
		val hit = sphere3.rayIntersection(ray)!!
		val hitPoint = invRay.at(hit.t)
		val actual = (scaling(s3) * sphereNormal(hitPoint, invRay.dir)).normalize()
		assertTrue(actual.isClose(normal))
	}
	
	@Test
	fun `test spherePointToUV`() {
		val point1 = Point(1f, 0f, 0f)
		val uv1 = SurfaceVec(0f, 0.5f)
		val point2 = Point(0.577f, 0.577f, 0.577f) // point on diagonal, first quadrant
		val uv2 = SurfaceVec(0.125f, 0.30422f)
		
		assertTrue(spherePointToUV(point1).isClose(uv1))
		assertTrue(spherePointToUV(point2).isClose(uv2))
	}
	
	@Test
	fun `test rayIntersection z direction`() {
		val ray1 = Ray(Point(0f, 0f, 2f), -vecZ())
		val uv1 = SurfaceVec(0f, 0f)
		val hit1 = sphere.rayIntersection(ray1)
		
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
		val hit2 = sphere.rayIntersection(ray2)
		val uv2 = SurfaceVec(0f, 1 / 2f)
		
		assertNotNull(hit2)
		assertTrue(
			hit2.normal.isClose(vecX().toNormal()) &&
					hit2.surfacePoint.isClose(uv2) &&
					areClose(hit2.t, 2f)
		)
	}
	
	@Test
	fun `test rayIntersection inside sphere`() {
		val ray3 = Ray(Point(0f, 0f, 0f), vecX())
		val hit3 = sphere.rayIntersection(ray3)
		val uv3 = SurfaceVec(0f, 1 / 2f)
		
		assertNotNull(hit3)
		assertTrue(
			hit3.normal.isClose(-vecX().toNormal()) &&
					hit3.surfacePoint.isClose(uv3) &&
					areClose(hit3.t, 1f)
		)
	}
	
	/**
	 * Verify [Sphere.rayIntersection] with a sphere that has been translated on one axis, the intersection is verified in z & x
	 * NB uv are valuated in the coordinate system of the sphere
	 */
	@Test
	fun `test rayIntersection with translation`() {
		val ray = Ray(Point(10f, 0f, 2f), -vecZ())
		val hit = sphere1.rayIntersection(ray)
		val uv = SurfaceVec(0f, 0f)
		
		val ray2 = Ray(Point(13f, 0f, 0f), -vecX())
		val hit2 = sphere1.rayIntersection(ray2)
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
		val hit = sphere2.rayIntersection(ray)
		val uv = SurfaceVec(0f, 0f)
		//if there is a scaling remember always to normalize()
		
		assertNotNull(hit)
		assertTrue(
			hit.normal.normalize().isClose(vecZ().toNormal()) &&
					hit.surfacePoint.isClose(uv) &&
					areClose(hit.t, 5f)
		)
	}
	
	/**
	 * Verify that the [Ray] used in `test rayIntersection z direction` no longer hits [sphere1]
	 */
	@Test
	fun `test noIntersection`() {
		val ray1 = Ray(Point(0f, 0f, 2f), -vecZ())
		val hit1 = sphere1.rayIntersection(ray1)
		
		val ray2 = Ray(Point(-10f, 0f, 0f), -vecZ())
		val hit2 = sphere1.rayIntersection(ray2)
		
		assertNull(hit1)
		assertNull(hit2)
	}
	
	@Test
	fun `test rayIntersectionShape`() {
		val sphere = Sphere()
		val ray = Ray(Point(-2f, 0f, 0f), vecX())
		val hits = sphere.rayIntersectionShape(ray)
		
		assertEquals(2, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
		assertEquals(3f, hits[1].t, 1e-5f)
	}
	
	@Test
	fun `test rayIntersectionShape inside sphere`() {
		val sphere = Sphere()
		val ray = Ray(Point(0f, 0f, 0f), vecX())
		
		val hits = sphere.rayIntersectionShape(ray)
		
		assertEquals(1, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
	}
	
	@Test
	fun `test contains`() {
		val sphere = Sphere(translation(Vec(2f, 0f, 0f)))
		
		assertTrue(sphere.contains(Point(2f, 0f, 0f)))
		assertTrue(sphere.contains(Point(2.5f, 0f, 0f)))
		assertFalse(sphere.contains(Point(0f, 0f, 0f)))
		assertFalse(sphere.contains(Point(3.5f, 0f, 0f)))
	}
}