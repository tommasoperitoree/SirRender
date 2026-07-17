package parsing

import core.PerspectiveCamera
import core.PointLight
import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.SpecularBRDF
import materials.UniformPigment
import geometry.Sphere
import geometry.Cube
import geometry.CSG
import geometry.Mesh
import math.HomogMatr4x4
import math.Transformation
import math.Vec
import math.Point
import math.rotationY
import math.translation
import org.junit.jupiter.api.Test
import java.io.File
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {
	
	@Test
	fun `parser Scene`() {
		
		val testSceneFile = "./src/test/resources/testScene.txt"
		val stream = File(testSceneFile).reader()
		
		val inputFile = SceneInputStream(stream)
		val scene = parseScene(inputFile)
		
		//check float variables
		assertEquals(1, scene.floatVariables.size)
		val clock = scene.floatVariables["clock"]
		assert("clock" in scene.floatVariables)
		assertEquals(150.0f, scene.floatVariables["clock"])
		
		//check material variables
		assertEquals(3, scene.materials.size)
		
		assert("skyMaterial" in scene.materials)
		assert("groundMaterial" in scene.materials)
		assert("sphereMaterial" in scene.materials)
		
		val skyMaterial = scene.materials["skyMaterial"]
		val groundMaterial = scene.materials["groundMaterial"]
		val sphereMaterial = scene.materials["sphereMaterial"]
		
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
		assertEquals(6, scene.world.shapes.size)
		
		val plane = scene.world.shapes[0]
		assertNotNull(plane)
		assertTrue(plane.transformation.isClose(translation(Vec(0f, 0f, 100f)) * rotationY(angleDeg = clock!!)))
		
		val planeGround = scene.world.shapes[1]
		assertNotNull(planeGround)
		assertTrue(planeGround.transformation.isClose(Transformation(HomogMatr4x4.identity())))
		
		val sphere = scene.world.shapes[2]
		assertNotNull(sphere)
		assertTrue(sphere.transformation.isClose(translation(Vec(0f, 0f, 1f))))
		
		
		val cube = scene.world.shapes[3]
		assertNotNull(cube)
		assertTrue(cube.transformation.isClose(translation(Vec(5f, 0f, 1f))))
		
		val cylinder = scene.world.shapes[4]
		assertNotNull(cylinder)
		assertTrue(cylinder.transformation.isClose(translation(Vec(0f, 3f, 1f))))
		
		val csg = scene.world.shapes[5]
		assertNotNull(csg)
		assertIs<CSG>(csg)
		
		//Check lights
		assertEquals(1, scene.world.lights.size)
		
		val light = scene.world.lights[0]
		assertTrue(light.position.isClose(Point(1f, 2f, 3f)))
		assertTrue(light.color.isClose(Color.white))
		assertEquals(2f, light.linearRadius)
		
		assertEquals(CSG.Operation.DIFFERENCE, csg.operation)
		assertIs<Cube>(csg.firstShape)
		assertIs<Sphere>(csg.secondShape)
		
		assertTrue(csg.secondShape.transformation.isClose(translation(Vec(0.5f, 0f, 0f))))
		
		//Check camera
		val cameraP = scene.camera
		assertNotNull(cameraP)
		assertIs<PerspectiveCamera>(cameraP)
	}
}

class ParserMeshTest {
	
	private fun writeTempObj(content: String): String {
		val file = File.createTempFile("parsermeshtest", ".obj")
		file.deleteOnExit()
		file.writeText(content)
		return file.path
	}
	
	private fun parse(sceneText: String): Scene =
		parseScene(SceneInputStream(StringReader(sceneText)))
	
	@Test
	fun `mesh keyword parses a complete scene-level shape declaration`() {
		val objPath = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val sceneText = """
            material pawnMaterial(
                diffuse(uniform((0.8, 0, 0))),
                uniform((0, 0, 0))
            )
            mesh(pawnMaterial, file("$objPath"), identity)
        """.trimIndent()
		
		val scene = parse(sceneText)
		assertEquals(1, scene.world.shapes.size)
		val mesh = scene.world.shapes.first() as Mesh
		assertEquals(3, mesh.vertices.size)
		assertEquals(1, mesh.triangleIndices.size)
	}
	
	@Test
	fun `mesh keyword applies the given transformation`() {
		val objPath = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val sceneText = """
            material m(
                diffuse(uniform((1, 1, 1))),
                uniform((0, 0, 0))
            )
            mesh(m, file("$objPath"), translation((5, 0, 0)))
        """.trimIndent()
		
		val scene = parse(sceneText)
		val mesh = scene.world.shapes.first() as Mesh
		// The Mesh stores the raw local-space vertices; the transformation itself is
		// what places it in the world — verify it was captured, not silently dropped.
		assertTrue(mesh.transformation.isClose(math.translation(math.Vec(5f, 0f, 0f))))
	}
	
	@Test
	fun `mesh keyword supports chained transformations`() {
		val objPath = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val sceneText = """
            material m(
                diffuse(uniform((1, 1, 1))),
                uniform((0, 0, 0))
            )
            mesh(m, file("$objPath"), scaling((2, 2, 2)) * translation((1, 0, 0)))
        """.trimIndent()
		
		val scene = parse(sceneText)
		assertEquals(1, scene.world.shapes.size)
	}
	
	@Test
	fun `mesh with unknown material name throws a GrammarError`() {
		val objPath = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val sceneText = """
            mesh(nonexistentMaterial, file("$objPath"), identity)
        """.trimIndent()
		
		assertFailsWith<GrammarError> { parse(sceneText) }
	}
	
	@Test
	fun `mesh missing the file keyword throws a GrammarError`() {
		val sceneText = """
            material m(
                diffuse(uniform((1, 1, 1))),
                uniform((0, 0, 0))
            )
            mesh(m, "not/wrapped/in/file/keyword.obj", identity)
        """.trimIndent()
		
		assertFailsWith<GrammarError> { parse(sceneText) }
	}
	
	@Test
	fun `mesh referencing a nonexistent obj file throws rather than silently producing an empty mesh`() {
		val sceneText = """
            material m(
                diffuse(uniform((1, 1, 1))),
                uniform((0, 0, 0))
            )
            mesh(m, file("this/path/does/not/exist.obj"), identity)
        """.trimIndent()
		
		// loadObj uses File(path).forEachLine, which throws if the file doesn't exist —
		// confirms this propagates as a real exception rather than an empty/silent mesh.
		assertFailsWith<java.io.FileNotFoundException> { parse(sceneText) }
	}
	
	@Test
	fun `multiple meshes in one scene are all added to the world`() {
		val objPath = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val sceneText = """
            material m(
                diffuse(uniform((1, 1, 1))),
                uniform((0, 0, 0))
            )
            mesh(m, file("$objPath"), identity)
            mesh(m, file("$objPath"), translation((3, 0, 0)))
        """.trimIndent()
		
		val scene = parse(sceneText)
		assertEquals(2, scene.world.shapes.size)
	}
	
	@Test
	fun `mesh can coexist with other shape types in the same scene`() {
		val objPath = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val sceneText = """
            material m(
                diffuse(uniform((1, 1, 1))),
                uniform((0, 0, 0))
            )
            sphere(m, identity)
            mesh(m, file("$objPath"), identity)
            plane(m, identity)
        """.trimIndent()
		
		val scene = parse(sceneText)
		assertEquals(3, scene.world.shapes.size)
	}
	
	@Test
	fun `mesh file keyword accepts an optional axis-order string`() {
		val objPath = writeTempObj("v 1.0 2.0 3.0\nv 0.0 0.0 0.0\nv 1.0 0.0 0.0\nf 1 2 3")
		val sceneText = """
        material m(
            diffuse(uniform((1, 1, 1))),
            uniform((0, 0, 0))
        )
        mesh(m, file("$objPath", "xzy"), identity)
    """.trimIndent()
		
		val scene = parse(sceneText)
		val mesh = scene.world.shapes.first() as geometry.Mesh
		// "xzy" swaps columns 2 and 3: OBJ (1,2,3) -> SirRender (1,3,2)
		assertTrue(mesh.vertices[0].isClose(math.Point(1f, 3f, 2f)))
	}
	
	@Test
	fun `mesh file keyword defaults to xyz order when omitted`() {
		val objPath = writeTempObj("v 1.0 2.0 3.0\nv 0.0 0.0 0.0\nv 1.0 0.0 0.0\nf 1 2 3")
		val sceneText = """
        material m(
            diffuse(uniform((1, 1, 1))),
            uniform((0, 0, 0))
        )
        mesh(m, file("$objPath"), identity)
    """.trimIndent()
		
		val scene = parse(sceneText)
		val mesh = scene.world.shapes.first() as geometry.Mesh
		assertTrue(mesh.vertices[0].isClose(math.Point(1f, 2f, 3f)))
	}
}