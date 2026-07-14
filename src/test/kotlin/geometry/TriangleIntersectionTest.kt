package geometry

import materials.Color
import materials.DiffuseBRDF
import materials.Material
import materials.UniformPigment
import math.Point
import math.Transformation
import math.Vec
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TriangleIntersectionTest {
	
	// Minimal Shape stub, since triangleHitRecord only needs a Shape reference
	// to populate HitRecord.shape — no rayIntersection logic of its own is exercised.
	private val dummyMaterial = Material(DiffuseBRDF(UniformPigment(Color.white)))
	private val dummyShape = object : Shape {
		override val transformation = Transformation()
		override val material = dummyMaterial
	}
	
	// Triangle in the XY plane: v0=(0,0,0), v1=(1,0,0), v2=(0,1,0).
	// Winding order (v0,v1,v2) gives edge1=(1,0,0), edge2=(0,1,0),
	// normal = edge1 x edge2 = (0,0,1) — fixed "front" direction is +Z.
	private val v0 = Point(0f, 0f, 0f)
	private val v1 = Point(1f, 0f, 0f)
	private val v2 = Point(0f, 1f, 0f)
	
	@Test
	fun `ray hitting the triangle interior returns a hit`() {
		val ray = Ray(Point(0.2f, 0.2f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNotNull(hit)
		assertTrue(kotlin.math.abs(hit.t - 1f) < 1e-4f)
	}
	
	@Test
	fun `ray missing the triangle outside its edges returns null`() {
		val ray = Ray(Point(5f, 5f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNull(hit)
	}
	
	@Test
	fun `ray hitting exactly the centroid is a hit`() {
		// centroid of (0,0,0),(1,0,0),(0,1,0) is (1/3, 1/3, 0)
		val ray = Ray(Point(1f / 3f, 1f / 3f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNotNull(hit)
	}
	
	@Test
	fun `ray parallel to the triangle plane returns null`() {
		// direction lies within the XY plane itself -> determinant near zero
		val ray = Ray(Point(0.2f, 0.2f, 0f), Vec(1f, 0f, 0f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNull(hit)
	}
	
	@Test
	fun `ray behind the triangle (negative t) returns null`() {
		// triangle is at z=0, ray travels away from it in +z starting past it
		val ray = Ray(Point(0.2f, 0.2f, 1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNull(hit)
	}
	
	@Test
	fun `degenerate triangle with zero area returns null`() {
		// v2 collapsed onto the line through v0-v1: edge1 x edge2 is a zero vector,
		// determinant with any direction not in-plane still trends toward zero/undefined
		val degenerateV2 = Point(2f, 0f, 0f) // co-linear with v0, v1
		val ray = Ray(Point(0.5f, 0.5f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, degenerateV2, ray, ray, dummyShape, Transformation())
		assertNull(hit)
	}
	
	@Test
	fun `hit respects ray tMin, rejecting near self-intersections`() {
		// origin sits essentially on the triangle plane; requesting a hit with a
		// realistic tMin should reject the near-zero t this would otherwise produce
		val ray = Ray(Point(0.2f, 0.2f, -1e-6f), Vec(0f, 0f, 1f), tMin = 1e-3f)
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNull(hit)
	}
	
	@Test
	fun `hit respects ray tMax, rejecting far intersections`() {
		val ray = Ray(Point(0.2f, 0.2f, -10f), Vec(0f, 0f, 1f), tMax = 5f)
		// triangle is at t=10 from this origin, beyond tMax=5
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNull(hit)
	}
	
	@Test
	fun `front face hit returns a normal facing the ray`() {
		val ray = Ray(Point(0.2f, 0.2f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNotNull(hit)
		assertTrue(hit.normal.z > 0f)
	}
	
	@Test
	fun `back face hit returns the SAME fixed normal direction (one-sided by design)`() {
		// Triangles are intentionally one-sided: the normal is fixed by winding
		// order and does NOT flip based on which side the ray approaches from,
		// unlike Sphere or Plane. This test locks that design decision in place.
		val frontRay = Ray(Point(0.2f, 0.2f, -1f), Vec(0f, 0f, 1f))
		val backRay = Ray(Point(0.2f, 0.2f, 1f), Vec(0f, 0f, -1f))
		
		val frontHit = triangleHitRecord(v0, v1, v2, frontRay, frontRay, dummyShape, Transformation())
		val backHit = triangleHitRecord(v0, v1, v2, backRay, backRay, dummyShape, Transformation())
		
		assertNotNull(frontHit)
		assertNotNull(backHit)
		// Both hits report the SAME normal direction (+Z), regardless of approach side.
		assertTrue(frontHit.normal.z > 0f)
		assertTrue(backHit.normal.z > 0f)
	}
	
	@Test
	fun `surface UV coordinates match the barycentric hit location`() {
		// hit at the point (1,0,0) == v1 exactly should give barycentric u close to 1, v close to 0
		val ray = Ray(Point(0.999f, 0.0005f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, ray, ray, dummyShape, Transformation())
		assertNotNull(hit)
		assertTrue(hit.surfacePoint.u > 0.95f)
		assertTrue(hit.surfacePoint.v < 0.05f)
	}
	
	@Test
	fun `world point is correctly transformed when a non-identity transformation is applied`() {
		val transform = math.translation(Vec(10f, 0f, 0f))
		// invRay: ray expressed in the SAME local space as v0...v2 (untransformed)
		val invRay = Ray(Point(0.2f, 0.2f, -1f), Vec(0f, 0f, 1f))
		val hit = triangleHitRecord(v0, v1, v2, invRay, invRay, dummyShape, transform)
		assertNotNull(hit)
		// worldPoint should reflect the +10 x-translation applied on top of the local hit at x=0.2
		assertTrue(kotlin.math.abs(hit.worldPoint.x - 10.2f) < 1e-3f)
	}
}
