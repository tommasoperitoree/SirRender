import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImageTracerTest {
	val image = HDRImage(2, 4)
	val camera = PerspectiveCamera(1.0f, 2.0f)
	val tracer = ImageTracer(image, camera)
	
	val ray1 = tracer.fireRay(0, 0, 2.5f, 1.5f)
	val ray2 = tracer.fireRay(2, 1, 0.5f, 0.5f)
	
	// the two rays should be the same, because the first jumps beyond the pixel(0,0),
	// since u & v should be floats numbers in [0,1], with default 0.5 in the center
	@Test
	fun `fireRay test`() {
		assertTrue(ray1.isClose(ray2))
	}
	
	@Test
	fun `test ImageTracer fireAllRays`() {
		tracer.fireAllRays { _ -> Color(1f, 2f, 3f) }
		for (row in 0 until tracer.image.height) {
			for (col in 0 until tracer.image.width) {
				assertTrue(tracer.image.getPixel(col, row).isClose(Color(1f, 2f, 3f)))
			}
		}
	}
}