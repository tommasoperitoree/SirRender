package materials

import math.SurfaceVec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PigmentTest {
	
	val uv: SurfaceVec = SurfaceVec()
	
	@Test
	fun `getColor UniformPigment test`() {
		val color = Color(1.0f, 0.5f, 0.25f)
		val pigment = UniformPigment(color)
		assertEquals(color, pigment.getColor(uv))
	}
	
	@Test
	fun `getColor CheckeredPigment test`() {
		val color1 = Color(0.2f, 0.2f, 0.2f) // dark gray
		val color2 = Color(0.8f, 0.8f, 0.8f) // light gray
		val step = 2
		val pigment = CheckeredPigment(color1, color2, step)
		
		assertEquals(color1, pigment.getColor(SurfaceVec(0.25f, 0.25f)))
		assertEquals(color2, pigment.getColor(SurfaceVec(0.25f, 0.75f)))
		assertEquals(color2, pigment.getColor(SurfaceVec(0.75f, 0.25f)))
		assertEquals(color1, pigment.getColor(SurfaceVec(0.75f, 0.75f)))
	}
	
	@Test
	fun `getColor ImagePigment test`() {
		val image = HDRImage(2, 2)
		image.setPixel(0, 0, Color(0.0f, 0.0f, 0.0f))
		image.setPixel(0, 1, Color(1.0f, 0.0f, 0.0f))
		image.setPixel(1, 0, Color(0.0f, 1.0f, 0.0f))
		image.setPixel(1, 1, Color(0.0f, 0.0f, 1.0f))
		
		val pigment = ImagePigment(image)
		assertEquals(Color(0.25f, 0.25f, 0.25f), pigment.getColor(SurfaceVec(0.25f, 0.25f)))
	}
}
