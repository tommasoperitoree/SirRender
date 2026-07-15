package geometry

import materials.areClose
import math.Normal
import math.Point
import math.SurfaceVec
import math.Vec
import math.scaling
import math.translation
import math.vecX
import math.vecZ
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class CSGTest {
	
	val sphere1 = Sphere()
	val sphere2 = Sphere(translation(Vec(0.5f, 0f, 0f)))
	
	val ray = Ray(Point(-2f, 0f, 0f), vecX())
	
	val union = CSG(sphere1, sphere2, operation = CSG.Operation.UNION)
	val difference = CSG(sphere1, sphere2, operation = CSG.Operation.DIFFERENCE)
	val intersection = CSG(sphere1, sphere2, operation = CSG.Operation.INTERSECTION)
	
	@Test
	fun `test contains`() {
		
		assertTrue(union.contains(Point(0f, 0f, 0f)))
		assertTrue(union.contains(Point(1.25f, 0f, 0f)))
		
		assertTrue(difference.contains(Point(-0.75f, 0.5f, 0f)))
		assertFalse(difference.contains(Point(0f, 0f, 0f)))
		
		assertTrue(intersection.contains(Point(0f, 0f, 0f)))
		assertFalse(intersection.contains(Point(-0.75f, 0f, 0f)))
	}
	
	@Test
	fun `test union`() {
		val hits = union.rayIntersectionShape(ray)
		
		assertEquals(4, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
		assertEquals(1.5f, hits[1].t, 1e-5f)
		assertEquals(3f, hits[2].t, 1e-5f)
		assertEquals(3.5f, hits[3].t, 1e-5f)
	}
	
	@Test
	fun `test difference`() {
		val hits = difference.rayIntersectionShape(ray)
		
		assertEquals(2, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
		assertEquals(1.5f, hits[1].t, 1e-5f)
	}
	
	@Test
	fun `test intersection`() {
		val hits = intersection.rayIntersectionShape(ray)
		
		assertEquals(2, hits.size)
		assertEquals(1.5f, hits[0].t, 1e-5f)
		assertEquals(3f, hits[1].t, 1e-5f)
	}
}