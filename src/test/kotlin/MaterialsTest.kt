import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class PigmentTest {
	val uv: Vec2d = Vec2d()
	
	@Test
	fun `getColor UniformPigment test`() {
		val color = Color(1.0f, 0.5f, 0.25f)
		val pigment = UniformPigment(color)
		assertEquals(color, pigment.getColor(uv))
	}
	
	@Test
	fun `getColor CheckeredPigment test`() {
		val color1 = Color(0.2f, 0.2f, 0.2f) // grigio scuro
		val color2 = Color(0.8f, 0.8f, 0.8f) // grigio chiaro
		val step = 2
		val pigment = CheckeredPigment(color1, color2, step)
		
		assertEquals(color1, pigment.getColor(Vec2d(0.25f, 0.25f)))
		assertEquals(color2, pigment.getColor(Vec2d(0.25f, 0.75f)))
		assertEquals(color2, pigment.getColor(Vec2d(0.75f, 0.25f)))
		assertEquals(color1, pigment.getColor(Vec2d(0.75f, 0.75f)))
	}
	
	@Test
	fun `getColor ImagePigment test`() {
		val image = HDRImage(2, 2)
		image.setPixel(0, 0, Color(0.0f, 0.0f, 0.0f))
		image.setPixel(0, 1, Color(1.0f, 0.0f, 0.0f))
		image.setPixel(1, 0, Color(0.0f, 1.0f, 0.0f))
		image.setPixel(1, 1, Color(0.0f, 0.0f, 1.0f))
		
		val pigment = ImagePigment(image)
		assertEquals(Color(0.25f, 0.25f, 0.25f), pigment.getColor(Vec2d(0.25f, 0.25f)))
	}
}


class BRDFTest {
	val pcg = PCG()
	val incomingDir = Vec(0f, 0f, -1f)
	val normal = Normal(0f, 0f, 1f).normalize()
	val intPoint = Point(0f, 0f, 3f)
	val depth = 5
	val isSpecular = true
	
	@Test
	fun `ScatterRay DiffuseBRDF test`() {
		val diffuseBRDF = DiffuseBRDF()
		val ray = diffuseBRDF.scatterRay(pcg, incomingDir, intPoint, normal, depth)
		assertEquals(intPoint, ray.origin)
		assertEquals(1e-5f, ray.tMin)
		assertEquals(Float.POSITIVE_INFINITY, ray.tMax)
		assertEquals(depth, ray.depth)
		assertFalse(ray.isSpecular) //after diffusion flag must be false
		
		//test it for numerays
		repeat(1000) {
			assertTrue(areClose(ray.dir.squaredNorm(), 1f))
			assertTrue(ray.dir.dot(normal.toVec()) >= 0f)
			assertTrue(incomingDir.dot(normal.toVec()) <= 0f)
		}
	}
	
	@Test
	fun `ScatterRay SpecularBRDF test`() {
		val specularBRDF = SpecularBRDF()
		val ray = specularBRDF.scatterRay(pcg, incomingDir, intPoint, normal, depth)
		
		assertEquals(intPoint, ray.origin)
		assertTrue(areClose(ray.dir.squaredNorm(), 1f))
		assertTrue(ray.dir.dot(normal.toVec()) >= 0f)
		assertEquals(1e-5f, ray.tMin)
		assertEquals(Float.POSITIVE_INFINITY, ray.tMax)
		assertEquals(depth, ray.depth)
		assertTrue(ray.isSpecular)
		//for incoming ray form (0,0,-1) the reflected ray has dir (0,0,1)
		assertTrue(ray.dir.isClose(Vec(0f, 0f, 1f)))
	}
}

