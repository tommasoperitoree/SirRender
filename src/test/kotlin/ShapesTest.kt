import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShapesTest {
	val sphere = Sphere()
	
	@Test
	fun `test rayIntersectionDirZ`() {
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
	fun `test rayIntersectionDirX`() {
		
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
	fun `test rayInsideSphereIntersection`() {
		val ray3 = Ray(Point(0f, 0f, 0f), vecX())
		val hit3 = sphere.rayIntersection(ray3)
		val uv3 = Vec2d(1 / 4f, 1 / 2f)
		
		assertTrue(
			hit3?.worldPoint?.isClose(Point(1f, 0f, 0f)) ?: false &&
					hit3.normal.isClose(-vecX().toNormal()) &&
					hit3.surfacePoint.isClose(uv3) &&
					areClose(hit3.t, 1f)
		)
	}
}