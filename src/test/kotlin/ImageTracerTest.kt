import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ImageTracerTest {
	val image=HDRImage(2,4)
	val camera= PerspectiveCamera(1.0f,2.0f)
	val tracer = ImageTracer(image,camera)
	
	/**
	 * the two [ray] should be the same, because the first exceed from the [pixel] 0,0,
	 * indeed u&v should be a float nuber between (0,1), deafault is 0,5 in the center
	 */
	val ray1=tracer.fireRay(0,0,2.5f,1.5f)
	val ray2=tracer.fireRay(2,1,0.5f,0.5f)
	
	@Test
	fun `fireRay testt`() {
		assertTrue(ray1.isClose(ray2))
	}
	
	//this test should be fail for now
	@Test
	fun `fireAllRays testt`() {
		val color=Color(1.0f,2.0f,3.0f)
		for (row in 0 until image.height) {
			for (col in 0 until image.width) {
				assertTrue(color.isCloseColor(image.getPixel(col,row)))
			}
		}
	}
}