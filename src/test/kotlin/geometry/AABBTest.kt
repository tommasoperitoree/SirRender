package geometry

import math.Point
import math.Vec
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AABBTest {
	
	private val unitBox = AABB(Point(-1f, -1f, -1f), Point(1f, 1f, 1f))
	
	@Test
	fun `ray straight through the box hits`() {
		val ray = Ray(Point(0f, 0f, -5f), Vec(0f, 0f, 1f))
		assertTrue(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `ray missing the box on all axes misses`() {
		val ray = Ray(Point(5f, 5f, -5f), Vec(0f, 0f, 1f))
		assertFalse(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `ray aimed away from the box misses`() {
		// box is behind the ray's origin, direction points further away
		val ray = Ray(Point(0f, 0f, -5f), Vec(0f, 0f, -1f))
		assertFalse(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `ray origin inside the box hits`() {
		val ray = Ray(Point(0f, 0f, 0f), Vec(0f, 0f, 1f))
		assertTrue(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `ray parallel to a face but outside the slab misses`() {
		// direction has zero x-component; origin.x is outside [-1,1] so the
		// x-slab interval is empty (t1x, t2x are both +Infinity or both -Infinity)
		val ray = Ray(Point(5f, 0f, -5f), Vec(0f, 0f, 1f))
		assertFalse(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `ray parallel to a face and inside the slab range hits`() {
		// direction has zero x-component; origin.x IS inside [-1,1], so the ray
		// effectively slides along the face and still passes through the box
		val ray = Ray(Point(0.5f, 0f, -5f), Vec(0f, 0f, 1f))
		assertTrue(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `ray starting exactly on a face and pointing inward hits`() {
		val ray = Ray(Point(-1f, 0f, 0f), Vec(1f, 0f, 0f))
		assertTrue(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `hit beyond ray tMax is rejected`() {
		val ray = Ray(Point(0f, 0f, -5f), Vec(0f, 0f, 1f), tMax = 2f)
		// box entry is at t=4, which is beyond tMax=2
		assertFalse(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `hit before ray tMin is rejected`() {
		// box entry point is at t=4; setting tMin above that should reject it
		val ray = Ray(Point(0f, 0f, -5f), Vec(0f, 0f, 1f), tMin = 10f)
		assertFalse(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `grazing ray touching near a corner still counts as a hit`() {
		val ray = Ray(Point(0.999f, 0.999f, -5f), Vec(0f, 0f, 1f))
		assertTrue(unitBox.quickRayIntersection(ray))
	}
	
	@Test
	fun `fromPoints computes the tight bounding box of a point list`() {
		val points = listOf(
			Point(0f, 0f, 0f),
			Point(2f, -1f, 3f),
			Point(-1f, 4f, 1f)
		)
		val box = AABB.fromPoints(points)
		assertTrue(box.pMin.x == -1f && box.pMin.y == -1f && box.pMin.z == 0f)
		assertTrue(box.pMax.x == 2f && box.pMax.y == 4f && box.pMax.z == 3f)
	}
	
	@Test
	fun `fromPoints throws on an empty list`() {
		var threw = false
		try {
			AABB.fromPoints(emptyList())
		} catch (_: IllegalArgumentException) {
			threw = true
		}
		assertTrue(threw)
	}
}
