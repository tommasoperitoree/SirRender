import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShapesTest {
	val sphere = Sphere()
	val t = Vec(10f, 0f, 0f)
	val sphere1 = Sphere(translation(t))
	
	@Test
	fun `test rayIntersection z direction`() {
		val ray1 = Ray(Point(0f, 0f, 2f), -vecZ())
		val uv1 = Vec2d(0f, 0f)
		val hit1 = sphere.rayIntersection(ray1)
		
		assertTrue(
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
		val uv2 = Vec2d(1 / 4f, 1 / 2f)
		
		assertTrue(
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
		val uv3 = Vec2d(1 / 4f, 1 / 2f)
		
	}
	
	/**
	 * Verify [Sphere.rayIntersection] with a sphere that has been translated on one axis, the intersection is verified in z & x
	 * NB uv are valuated in the coordinate sistem of the sphere
	 */
	@Test
	fun `test rayIntersection with translation`() {
		val ray = Ray(Point(10f, 0f, 2f), -vecZ())
		val hit = sphere1.rayIntersection(ray)
		val uv = Vec2d(0f, 0f)
		
		val ray2 = Ray(Point(13f, 0f, 0f), -vecX())
		val hit2 = sphere1.rayIntersection(ray2)
		val uv2 = Vec2d(1 / 4f, 1 / 2f)
		
		
		assertTrue(
			hit?.worldPoint?.isClose(Point(10f, 0f, 1f)) ?: false &&
					hit.normal.isClose(vecZ().toNormal()) &&
					hit.surfacePoint.isClose(uv)&&
					areClose(hit.t,1f)
		)
		
		assertTrue(
			hit2?.worldPoint?.isClose(Point(11f, 0f, 0f)) ?: false &&
					hit2.normal.isClose(vecX().toNormal()) &&
					hit2.surfacePoint.isClose(uv2) &&
					areClose(hit2.t,2f)
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
		
		assertFalse(
			hit1?.worldPoint?.isClose(Point(10f, 0f, 1f)) ?: false &&
					hit1.normal.isClose(vecZ().toNormal())
		)
		assertFalse(
			hit2?.worldPoint?.isClose(Point(10f, 0f, 1f)) ?: false &&
					hit2.normal.isClose(vecZ().toNormal())
		)
	}
}