import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImageTracerTest {
	val image = HDRImage(2, 4)
	val camera = PerspectiveCamera(1f, 2f)
	val tracer = ImageTracer(image, camera)
	
	val ray1 = tracer.fireRay(0, 0, 2.5f, 1.5f)
	val ray2 = tracer.fireRay(2, 1, .5f, .5f)
	
	
	@Test
	fun `test orientation`() {
		val topLeftRay = tracer.fireRay(0, 0, 0f, 0f)
		assertTrue(Point(0f, 2f, 1f).isClose(topLeftRay.at(1f)))
		
		val bottomRightRay = tracer.fireRay(1, 3, 1f, 1f)
		assertTrue(Point(0f, -2f, -1f).isClose(bottomRightRay.at(1f)))
	}
	
	// the two rays should be the same, because the first jumps beyond the pixel(0,0),
	// since u & v should be floats numbers in [0,1], with default 0.5 in the center
	@Test
	fun `test uv sub mapping`() {
		assertTrue(ray1.isClose(ray2))
	}
	
	@Test
	fun `test image coverage with ImageTracer fireAllRays`() {
		tracer.fireAllRays { _ -> Color(1f, 2f, 3f) }
		for (row in 0 until tracer.image.height) {
			for (col in 0 until tracer.image.width) {
				assertTrue(tracer.image.getPixel(col, row).isClose(Color(1f, 2f, 3f)))
			}
		}
	}
}