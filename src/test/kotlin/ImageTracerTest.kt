import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImageTracerTest {
	val image = HDRImage(4, 2)
	val camera = PerspectiveCamera(aspectRatio = 2f)
	val tracer = ImageTracer(image, camera)
	
	@Test
	fun `test ImageTracer fireRay`() {
		val ray1 = tracer.fireRay(0, 0, 2.5f, 1.5f)
		val ray2 = tracer.fireRay(2, 1, .5f, .5f)
		assertTrue(ray1.isClose(ray2))
	}
	
	@Test
	fun `test ImageTracer fireAllRays`() {
		tracer.fireAllRays { ray -> Color(1f, 2f, 3f) }
		for (row in 0 until tracer.image.height) {
			for (col in 0 until tracer.image.width) {
				assertTrue(tracer.image.getPixel(col, row).isClose(Color(1f, 2f, 3f)))
			}
		}
	}
}