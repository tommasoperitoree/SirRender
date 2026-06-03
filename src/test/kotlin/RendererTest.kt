import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RendererTest {
	
	@Test
	fun `furnace test`() {
		val pcg = PCG()
		
		repeat(10) {
			val emittedRadiance = pcg.randomFloat()
			val reflectance = pcg.randomFloat() * 0.9f // Avoid numbers too close to 1
			
			val world = World()
			val enclosureMaterial = Material(
				DiffuseBRDF(UniformPigment(Color.white * reflectance)),
				UniformPigment(Color.white * emittedRadiance)
			)
			world.addShape(Sphere(material = enclosureMaterial))
			
			val pathTracer = PathTracer(world, Color.black, pcg, 1, 1000, 501)
			
			val ray = Ray(Point(0f, 0f, 0f), Vec(1f, 0f, 0f))
			val color = pathTracer(ray)
			
			val expectedColor = (emittedRadiance / (1f - reflectance)).let { Color(it, it, it) }
			
			println("Expected: $expectedColor")
			println("Got:      $color")
			
			assertTrue(
				color.isClose(expectedColor, 1e-3f),
				"Furnace test failed! Expected $expectedColor but got $color"
			)
		}
	}
}