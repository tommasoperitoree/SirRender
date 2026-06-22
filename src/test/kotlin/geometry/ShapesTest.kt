package geometry

import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.Material
import materials.areClose
import math.Normal
import math.Point
import math.Vec
import math.Vec2d
import math.scaling
import math.translation
import math.vecX
import math.vecZ
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ShapesTest {
	
	//Test sphere
	val sphere = Sphere()
	val t = Vec(10f, 0f, 0f)
	val sphere1 = Sphere(translation(t))
	val s = Vec(10f, 10f, 10f)
	val sphere2 = Sphere(scaling(s))
	
	@Test
	fun `test sphereNormal`() {
		val normal = Normal(1f, 0f, 0f)
		val ray = Ray(Point(5f, 0f, 0f), -vecX())
		assertEquals(sphereNormal(Point(1f, 0f, 0f), ray.dir), normal)
		Assertions.assertTrue(sphereNormal(Point(-1f, 0f, 0f), ray.dir).isClose(normal))
	}
	
	@Test
	fun `test spherePointToUV`() {
		val point1 = Point(1f, 0f, 0f)
		val uv1 = Vec2d(0f, 0.5f)
		val point2 = Point(0.577f, 0.577f, 0.577f) // point on diagonal, first quadrant
		val uv2 = Vec2d(0.125f, 0.30422f)
		
		Assertions.assertTrue(spherePointToUV(point1).isClose(uv1))
		Assertions.assertTrue(spherePointToUV(point2).isClose(uv2))
	}
	
	@Test
	fun `test rayIntersection z direction`() {
		val ray1 = Ray(Point(0f, 0f, 2f), -vecZ())
		val uv1 = Vec2d(0f, 0f)
		val hit1 = sphere.rayIntersection(ray1)
		
		Assertions.assertTrue(
			hit1?.worldPoint?.isClose(Point(0f, 0f, 1f)) ?: false &&
					hit1.normal.isClose(vecZ().toNormal()) &&
					hit1.surfacePoint.isClose(uv1) &&
					areClose(hit1.t, 1f)
		)
	}
	
	@Test
	fun `test rayIntersection x direction`() {
		
		val ray2 = Ray(Point(3f, 0f, 0f), -vecX())
		val hit2 = sphere.rayIntersection(ray2)
		val uv2 = Vec2d(0f, 1 / 2f)
		
		Assertions.assertTrue(
			hit2?.worldPoint?.isClose(Point(1f, 0f, 0f)) ?: false &&
					hit2.normal.isClose(vecX().toNormal()) &&
					hit2.surfacePoint.isClose(uv2) &&
					areClose(hit2.t, 2f)
		)
		
	}
	
	@Test
	fun `test rayIntersection inside sphere`() {
		val ray3 = Ray(Point(0f, 0f, 0f), vecX())
		val hit3 = sphere.rayIntersection(ray3)
		val uv3 = Vec2d(0f, 1 / 2f)
		
		TODO()
	}
	
	/**
	 * Verify [Sphere.rayIntersection] with a sphere that has been translated on one axis, the intersection is verified in z & x
	 * NB uv are valuated in the coordinate system of the sphere
	 */
	@Test
	fun `test rayIntersection with translation`() {
		val ray = Ray(Point(10f, 0f, 2f), -vecZ())
		val hit = sphere1.rayIntersection(ray)
		val uv = Vec2d(0f, 0f)
		
		val ray2 = Ray(Point(13f, 0f, 0f), -vecX())
		val hit2 = sphere1.rayIntersection(ray2)
		val uv2 = Vec2d(0f, 1 / 2f)
		
		
		Assertions.assertTrue(
			hit?.worldPoint?.isClose(Point(10f, 0f, 1f)) ?: false &&
					hit.normal.isClose(vecZ().toNormal()) &&
					hit.surfacePoint.isClose(uv) &&
					areClose(hit.t, 1f)
		)
		
		Assertions.assertTrue(
			hit2?.worldPoint?.isClose(Point(11f, 0f, 0f)) ?: false &&
					hit2.normal.isClose(vecX().toNormal()) &&
					hit2.surfacePoint.isClose(uv2) &&
					areClose(hit2.t, 2f)
		)
	}
	
	@Test
	fun `test scaling rayIntersection`() {
		val ray = Ray(Point(0f, 0f, 15f), -vecZ())
		val hit = sphere2.rayIntersection(ray)
		val uv = Vec2d(0f, 0f)
		//if there is a scaling remember always to normalize()
		Assertions.assertTrue(
			hit?.worldPoint?.isClose(Point(0f, 0f, 10f)) ?: false &&
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
		
		Assertions.assertFalse(
			hit1?.worldPoint?.isClose(Point(10f, 0f, 1f)) ?: false &&
					hit1.normal.isClose(vecZ().toNormal())
		)
		Assertions.assertFalse(
			hit2?.worldPoint?.isClose(Point(10f, 0f, 1f)) ?: false &&
					hit2.normal.isClose(vecZ().toNormal())
		)
	}
	
	
	// test Plane
	val groundMaterial = Material(
		brdf = DiffuseBRDF(
			pigment = CheckeredPigment(
				color1 = Color.white,
				color2 = Color.black,
				numSteps = 2
			)
		)
	)
	val plane = Plane(material = groundMaterial)
	
	@Test
	fun `test intersection`() {
		val s = scaling(Vec(-1f, 1f, 1f)) // invert on x-axis
		val planes = Plane(s, groundMaterial)
		val point = Point(0.25f, 0.25f)
		val point1 = Point(0.25f, 0.75f)
		
		val uvPlane = plane.planePointToUV(point)
		val uvPlane1 = plane.planePointToUV(point1)
		
		// to the plane with transformation we need to pass the inverse transformation on the point
		val localPoint = planes.transformation.inverse() * point
		val localPoint1 = planes.transformation.inverse() * point1
		val uvPlanes = planes.planePointToUV(localPoint)
		val uvPlanes1 = planes.planePointToUV(localPoint1)
		
		//without scaling
		assertEquals(plane.material.brdf.pigment.getColor(uvPlane), Color.white)
		assertEquals(plane.material.brdf.pigment.getColor(uvPlane1), Color.black)
		//with scaling
		assertEquals(planes.material.brdf.pigment.getColor(uvPlanes1), Color.white)
		assertEquals(planes.material.brdf.pigment.getColor(uvPlanes), Color.black)
	}
}