package geometry

import materials.Color
import materials.DiffuseBRDF
import materials.Material
import materials.UniformPigment
import math.Point
import math.Transformation
import math.Vec
import math.translation
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MeshTest {
	
	private val material = Material(DiffuseBRDF(UniformPigment(Color.white)))
	
	// A simple "quad" made of two triangles in the XY plane, spanning [0,2] x [0,1]:
	//   v0=(0,0,0)  v1=(1,0,0)  v2=(1,1,0)  v3=(0,1,0)  v4=(2,0,0)  v5=(2,1,0)
	// Triangle A: v0,v1,v2   Triangle B: v0,v2,v3   Triangle C: v1,v4,v5 (further out)
	private val vertices = listOf(
		Point(0f, 0f, 0f), // 0
		Point(1f, 0f, 0f), // 1
		Point(1f, 1f, 0f), // 2
		Point(0f, 1f, 0f), // 3
		Point(2f, 0f, 0f), // 4
		Point(2f, 1f, 0f), // 5
	)
	private val indices = listOf(
		Triple(0, 1, 2),
		Triple(0, 2, 3),
		Triple(1, 4, 5),
	)
	
	private fun buildMesh(transform: Transformation = Transformation()) =
		Mesh(vertices, indices, transformation = transform, material = material)
	
	@Test
	fun `ray hitting a triangle within the mesh returns a hit`() {
		val mesh = buildMesh()
		val ray = Ray(Point(0.5f, 0.3f, -1f), Vec(0f, 0f, 1f))
		val hit = mesh.rayIntersection(ray)
		assertNotNull(hit)
	}
	
	@Test
	fun `ray missing the mesh's bounding box entirely returns null quickly`() {
		val mesh = buildMesh()
		val ray = Ray(Point(50f, 50f, -1f), Vec(0f, 0f, 1f))
		val hit = mesh.rayIntersection(ray)
		assertNull(hit)
	}
	
	@Test
	fun `ray inside the mesh AABB but missing every triangle returns null`() {
		val mesh = buildMesh()
		// (1.5, 0.9, z) is within the mesh's overall bounding box [0,2]x[0,1]
		// but does not land inside any of the three defined triangles
		val ray = Ray(Point(1.5f, 0.9f, -1f), Vec(0f, 0f, 1f))
		val hit = mesh.rayIntersection(ray)
		assertNull(hit)
	}
	
	@Test
	fun `closest triangle hit is selected when multiple triangles could be hit`() {
		// Build a mesh with two overlapping-in-XY triangles at different depths,
		// to confirm the closer one (smaller t) wins.
		val nearFar = listOf(
			Point(-1f, -1f, 0f), // 0: near triangle, z=0
			Point(1f, -1f, 0f),  // 1
			Point(0f, 1f, 0f),   // 2
			Point(-1f, -1f, 5f), // 3: far triangle, z=5
			Point(1f, -1f, 5f),  // 4
			Point(0f, 1f, 5f),   // 5
		)
		val bothTriangles = listOf(Triple(0, 1, 2), Triple(3, 4, 5))
		val mesh = Mesh(nearFar, bothTriangles, material = material)
		
		val ray = Ray(Point(0f, 0f, -10f), Vec(0f, 0f, 1f))
		val hit = mesh.rayIntersection(ray)
		assertNotNull(hit)
		// near triangle is at z=0 -> t=10; far triangle is at z=5 -> t=15
		assertTrue(kotlin.math.abs(hit.t - 10f) < 1e-3f)
	}
	
	@Test
	fun `shared vertices between adjacent triangles produce a seamless surface`() {
		// Triangles A (0,1,2) and B (0,2,3) share the edge v0-v2. A ray aimed
		// exactly along that shared edge should hit one triangle or the other,
		// never fall through a seam.
		val mesh = buildMesh()
		val ray = Ray(Point(0.5f, 0.5f, -1f), Vec(0f, 0f, 1f)) // on the diagonal v0-v2
		val hit = mesh.rayIntersection(ray)
		assertNotNull(hit)
	}
	
	@Test
	fun `mesh transformation is applied to the resulting world-space hit point`() {
		val transform = translation(Vec(0f, 0f, 100f))
		val mesh = buildMesh(transform)
		// shoot the ray toward where the mesh will be AFTER translation
		val ray = Ray(Point(0.5f, 0.3f, 99f), Vec(0f, 0f, 1f))
		val hit = mesh.rayIntersection(ray)
		assertNotNull(hit)
		assertTrue(kotlin.math.abs(hit.worldPoint.z - 100f) < 1e-2f)
	}
	
	@Test
	fun `aabb is computed from object-space vertices, unaffected by transformation`() {
		val transform = translation(Vec(1000f, 0f, 0f))
		val mesh = buildMesh(transform)
		// the aabb should still reflect the RAW vertex extent [0,2]x[0,1]x[0,0],
		// not the world-translated position
		assertTrue(mesh.aabb.pMin.x == 0f && mesh.aabb.pMax.x == 2f)
	}
}
