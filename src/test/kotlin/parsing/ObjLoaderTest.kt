package parsing

import geometry.Ray
import geometry.triangleHitRecord
import math.Point
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ObjLoaderTest {
	
	/** Writes [content] to a temp .obj file and returns its path, deleting it on JVM exit. */
	private fun writeTempObj(content: String): String {
		val file = File.createTempFile("ObjloaderTest", ".obj")
		file.deleteOnExit()
		file.writeText(content)
		return file.path
	}
	
	@Test
	fun `loads a simple triangle with bare vertex-only face syntax`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val (vertices, indices) = loadObj(path)
		assertEquals(3, vertices.size)
		assertEquals(listOf(Triple(0, 1, 2)), indices)
		assertEquals(Point(0f, 0f, 0f), vertices[0])
		assertEquals(Point(1f, 0f, 0f), vertices[1])
		assertEquals(Point(0f, 1f, 0f), vertices[2])
	}
	
	@Test
	fun `loads a face with vertex-texture syntax, ignoring texture indices`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1/1 2/2 3/3
            """.trimIndent()
		)
		val (_, indices) = loadObj(path)
		assertEquals(listOf(Triple(0, 1, 2)), indices)
	}
	
	@Test
	fun `loads a face with vertex-normal syntax (no texture), ignoring normal indices`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1//1 2//2 3//3
            """.trimIndent()
		)
		val (_, indices) = loadObj(path)
		assertEquals(listOf(Triple(0, 1, 2)), indices)
	}
	
	@Test
	fun `loads a face with full vertex-texture-normal syntax`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1/1/1 2/2/2 3/3/3
            """.trimIndent()
		)
		val (_, indices) = loadObj(path)
		assertEquals(listOf(Triple(0, 1, 2)), indices)
	}
	
	@Test
	fun `triangulates a quad face via fan triangulation`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3 4
            """.trimIndent()
		)
		val (_, indices) = loadObj(path)
		assertEquals(listOf(Triple(0, 1, 2), Triple(0, 2, 3)), indices)
	}
	
	@Test
	fun `triangulates a pentagon face via fan triangulation`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.5 1.0 0.0
            v 0.5 1.5 0.0
            v -0.5 1.0 0.0
            f 1 2 3 4 5
            """.trimIndent()
		)
		val (_, indices) = loadObj(path)
		assertEquals(
			listOf(Triple(0, 1, 2), Triple(0, 2, 3), Triple(0, 3, 4)),
			indices
		)
	}
	
	@Test
	fun `shared vertices between faces are stored only once`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            f 1 3 4
            """.trimIndent()
		)
		val (vertices, indices) = loadObj(path)
		assertEquals(4, vertices.size) // not 6 — vertex 1 and 3 are shared, not duplicated
		assertEquals(2, indices.size)
	}
	
	@Test
	fun `default axis order xyz is an identity mapping`() {
		val path = writeTempObj("v 1.0 2.0 3.0")
		val (vertices, _) = loadObj(path, order = "xyz")
		assertEquals(Point(1f, 2f, 3f), vertices[0])
	}
	
	@Test
	fun `axis order remaps OBJ file columns to SirRender axes`() {
		val path = writeTempObj("v 1.0 2.0 3.0")
		// "xzy" means: 1st column -> x, 2nd column -> z, 3rd column -> y
		val (vertices, _) = loadObj(path, order = "xzy")
		assertEquals(Point(1f, 3f, 2f), vertices[0])
	}
	
	@Test
	fun `invalid axis order string throws`() {
		val path = writeTempObj("v 1.0 2.0 3.0")
		assertFailsWith<IllegalArgumentException> {
			loadObj(path, order = "xxy") // not a permutation of xyz
		}
	}
	
	@Test
	fun `comment lines are ignored`() {
		val path = writeTempObj(
			"""
            # this is a comment
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            # another comment before the face
            f 1 2 3
            """.trimIndent()
		)
		val (vertices, indices) = loadObj(path)
		assertEquals(3, vertices.size)
		assertEquals(1, indices.size)
	}
	
	@Test
	fun `vt, vn, o, g, usemtl, and s lines are silently ignored without error`() {
		val path = writeTempObj(
			"""
            o MyObject
            g Group1
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            vt 0.0 0.0
            vt 1.0 0.0
            vt 0.0 1.0
            vn 0.0 0.0 1.0
            usemtl Material1
            s 1
            f 1/1/1 2/2/1 3/3/1
            """.trimIndent()
		)
		val (vertices, indices) = loadObj(path)
		assertEquals(3, vertices.size)
		assertEquals(1, indices.size)
	}
	
	@Test
	fun `blank lines are ignored`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0

            v 1.0 0.0 0.0

            v 0.0 1.0 0.0
            f 1 2 3
            """.trimIndent()
		)
		val (vertices, indices) = loadObj(path)
		assertEquals(3, vertices.size)
		assertEquals(1, indices.size)
	}
	
	@Test
	fun `face with fewer than 3 vertices throws`() {
		val path = writeTempObj(
			"""
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            f 1 2
            """.trimIndent()
		)
		assertFailsWith<IllegalArgumentException> { loadObj(path) }
	}
	
	@Test
	fun `a realistic mixed-content obj file loads correctly end to end`() {
		// A unit square split into 2 triangles, with typical exporter metadata present.
		val path = writeTempObj(
			"""
            # Exported by SomeTool
            o Square
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 1.0 1.0 0.0
            v 0.0 1.0 0.0
            vt 0.0 0.0
            vt 1.0 0.0
            vt 1.0 1.0
            vt 0.0 1.0
            vn 0.0 0.0 1.0
            usemtl Default
            s off
            f 1/1/1 2/2/1 3/3/1
            f 1/1/1 3/3/1 4/4/1
            """.trimIndent()
		)
		val (vertices, indices) = loadObj(path)
		assertEquals(4, vertices.size)
		assertEquals(listOf(Triple(0, 1, 2), Triple(0, 2, 3)), indices)
	}
	
	@Test
	fun `negative relative face indices resolve correctly`() {
		val path = writeTempObj(
			"""
        v 0.0 0.0 0.0
        v 1.0 0.0 0.0
        v 0.0 1.0 0.0
        f -3 -2 -1
        """.trimIndent()
		)
		val (_, indices) = loadObj(path)
		assertEquals(listOf(Triple(0, 1, 2)), indices)
	}
	
	@Test
	fun `face index 0 throws a clear error`() {
		val path = writeTempObj("v 0.0 0.0 0.0\nv 1.0 0.0 0.0\nv 0.0 1.0 0.0\nf 0 1 2")
		assertFailsWith<IllegalArgumentException> { loadObj(path) }
	}
	
	@Test
	fun `out of range face index throws with a clear message instead of crashing later`() {
		val path = writeTempObj("v 0.0 0.0 0.0\nv 1.0 0.0 0.0\nv 0.0 1.0 0.0\nf 1 2 99")
		val ex = assertFailsWith<IllegalArgumentException> { loadObj(path) }
		assertTrue(ex.message!!.contains("out of range"))
	}
	
	
}