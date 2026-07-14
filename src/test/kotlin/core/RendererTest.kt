package core

import geometry.Ray
import geometry.Sphere
import materials.Color
import materials.DiffuseBRDF
import materials.Material
import materials.UniformPigment
import math.PCG
import math.Point
import math.Vec
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
			
			val pathTracer = PathTracer(world, Color.black, pcg, 1, 1000, 1001)
			
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
	
	@Test
	fun `no intersection pointLightRenderer test`() {
		val world = World()
		val renderer = PointLightRenderer(
			world = world,
			backgroundColor = Color(1f, 0f, 0f)
		)
		
		val ray = Ray(
			origin = Point(0f, 0f, 2f),
			dir = Vec(0f, 0f, 1f)
		)
		
		val color = renderer(ray)
		
		assertEquals(Color(1f, 0f, 0f), color)
	}
	
	@Test
	fun `intersection pointLightRenderer test`() {
		val world = World()
		val sphere = Sphere(
			material = Material(
				brdf = DiffuseBRDF(UniformPigment(Color.white))
			)
		)
		
		world.addShape(sphere)
		world.addLight(PointLight(position = Point(0f, 0f, 3f), color = Color.white))
		
		val renderer = PointLightRenderer(world = world, backgroundColor = Color.black)
		val ray = Ray(origin = Point(0f, 0f, 2f), dir = Vec(0f, 0f, -1f))
		val color = renderer(ray)
		
		assertTrue(color.r > 0f)
		assertTrue(color.g > 0f)
		assertTrue(color.b > 0f)
	}
}