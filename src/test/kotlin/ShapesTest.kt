import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShapesTest {
	val sphere = Sphere()
	val ray1 = Ray(Point(0f, 0f, 3f), -vecZ())
	
	@Test
	fun `test rayIntersection`() {
		val hit = sphere.rayIntersection(ray1)
		assertTrue(
			hit?.worldPoint?.isClose(Point(0f, 0f, 1f)) ?: false &&
					hit.normal.isClose(vecZ().toNormal())
		)
		// other tests with different rays
		// also missing (u,v) coordinates and t value check
	}
	
}