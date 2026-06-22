package parsing

import core.PerspectiveCamera
import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.SpecularBRDF
import materials.UniformPigment
import math.HomogMatr4x4
import math.Transformation
import math.Vec
import math.rotationY
import math.translation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {
	
	@Test
	fun `parser Scene`() {
		val stream = ("float clock(150)\n" +
				"        material sky_material(\n" +
				"            diffuse(uniform((0, 0, 0))),\n" +
				"            uniform((0.7, 0.5, 1))\n" +
				"        )\n" +
				"    \n" +
				"        material ground_material(\n" +
				"            diffuse(checkered((0.3, 0.5, 0.1),\n" +
				"                              (0.1, 0.2, 0.5), 4)),\n" +
				"            uniform((0, 0, 0))\n" +
				"        )\n" +
				"    \n" +
				"        material sphere_material(\n" +
				"            specular(uniform((0.5, 0.5, 0.5))),\n" +
				"            uniform((0, 0, 0))\n" +
				"        )\n" +
				"    \n" +
				"        plane (sky_material, translation((0, 0, 100)) * rotation_y(clock))\n" +
				"        plane (ground_material, identity)\n" +
				"    \n" +
				"        sphere(sphere_material, translation((0, 0, 1)))\n" +
				"    \n" +
				"        camera(perspective, rotation_z(30) * translation((-4, 0, 1)), 1.0, 2.0)").reader()
		
		
		val inputFile = SceneInputStream(stream)
		val scene = parseScene(inputFile)
		
		//check float variables
		assertEquals(1, scene.floatVariables.size)
		val clock = scene.floatVariables["clock"]
		assert("clock" in scene.floatVariables)
		assertEquals(150.0f, scene.floatVariables["clock"])
		
		//check material variables
		assertEquals(3, scene.materials.size)
		
		assert("sky_material" in scene.materials)
		assert("ground_material" in scene.materials)
		assert("sphere_material" in scene.materials)
		
		val skyMaterial = scene.materials["sky_material"]
		val groundMaterial = scene.materials["ground_material"]
		val sphereMaterial = scene.materials["sphere_material"]
		
		//sky
		assertNotNull(skyMaterial) //must check if is noNull
		assertIs<DiffuseBRDF>(skyMaterial.brdf)
		assertIs<UniformPigment>(skyMaterial.brdf.pigment)
		assertTrue(skyMaterial.brdf.pigment.color.isClose(Color.black))
		assertIs<UniformPigment>(skyMaterial.emittedRadiance)
		assertTrue(skyMaterial.emittedRadiance.color.isClose(Color(0.7f, 0.5f, 1f)))
		
		//ground
		assertNotNull(groundMaterial)
		assertIs<DiffuseBRDF>(groundMaterial.brdf)
		assertIs<CheckeredPigment>(groundMaterial.brdf.pigment)
		assertTrue(groundMaterial.brdf.pigment.color1.isClose(Color(0.3f, 0.5f, 0.1f)))
		assertTrue(groundMaterial.brdf.pigment.color2.isClose(Color(0.1f, 0.2f, 0.5f)))
		assertEquals(groundMaterial.brdf.pigment.numSteps, 4)
		assertIs<UniformPigment>(groundMaterial.emittedRadiance)
		assertTrue(groundMaterial.emittedRadiance.color.isClose(Color.black))
		
		//sphere
		assertNotNull(sphereMaterial)
		assertIs<SpecularBRDF>(sphereMaterial.brdf)
		assertIs<UniformPigment>(sphereMaterial.brdf.pigment)
		assertTrue(sphereMaterial.brdf.pigment.color.isClose(Color(0.5f, 0.5f, 0.5f)))
		assertIs<UniformPigment>(sphereMaterial.emittedRadiance)
		assertTrue(sphereMaterial.emittedRadiance.color.isClose(Color.black))
		
		//Check shapes
		assertEquals(3, scene.world.shapes.size)
		
		val plane = scene.world.shapes[0]
		assertNotNull(plane)
		assertTrue(plane.transformation.isClose(translation(Vec(0f, 0f, 100f)) * rotationY(angleDeg = clock!!)))
		
		val planeGround = scene.world.shapes[1]
		assertNotNull(planeGround)
		assertTrue(planeGround.transformation.isClose(Transformation(HomogMatr4x4.identity())))
		
		val sphere = scene.world.shapes[2]
		assertNotNull(sphere)
		assertTrue(sphere.transformation.isClose(translation(Vec(0f, 0f, 1f))))
		
		//Check camera
		val cameraP = scene.camera
		assertNotNull(cameraP)
		assertIs<PerspectiveCamera>(cameraP)
	}
}