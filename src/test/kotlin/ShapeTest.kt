import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShapeTest {
	val sphere = Sphere()
	val ray1 = Ray(Point(0f, 0f, 3f), -vecZ())
	
	@Test
	fun `test rayIntersection`() {
		val hit = sphere.rayIntersection(ray1)
		assertTrue(
			hit?.worldPoint?.isClose(Point(0f, 0f, 1f)) ?: false &&
					hit.normal.isClose(vecZ().toNormal()) ?: false
		)
	}
}