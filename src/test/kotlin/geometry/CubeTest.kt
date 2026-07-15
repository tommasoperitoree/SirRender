package geometry

import materials.areClose
import math.Point
import math.Vec
import math.vecX
import math.vecY
import math.vecZ
import math.translation
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CubeTest {
	val cube = Cube()
	
	@Test
	fun `test cubePointToUV`() {
		val point = Point(0f, 0f, 1f)
		val uv = cubePointToUV(point, axis = 2, sign = 1f)
		
		assertTrue(areClose(uv.u, 0.5f))
		assertTrue(areClose(uv.v, 0.5f))
	}
	
	@Test
	fun `test rayIntersection with cube`() {
		val ray = Ray(Point(0f, 0f, 2f), -vecZ())
		val hit = cube.rayIntersection(ray)
		
		assertNotNull(hit)
	}
	
	@Test
	fun `test rayIntersection inside cube`() {
		val ray = Ray(Point(0f, 0f, 0f), vecX())
		val hit = cube.rayIntersection(ray)
		
		assertNotNull(hit)
		assertTrue(areClose(hit.worldPoint.x, 1f))
		assertTrue(areClose(hit.normal.x, 1f))
	}
	
	@Test
	fun `test no rayIntersection`() {
		val ray = Ray(Point(3f, 2f, 6f), -vecZ())
		val hit = cube.rayIntersection(ray)
		
		assertNull(hit)
	}
	
	@Test
	fun `test rayIntersection translated cube`() {
		val transCube = Cube(translation(Vec(5f, 0f, 0f)))
		
		val ray = Ray(Point(5f, 0f, 5f), -vecZ())
		val hit = transCube.rayIntersection(ray)
		
		assertNotNull(hit)
		assertTrue(areClose(hit.worldPoint.x, 5f))
		assertTrue(areClose(hit.worldPoint.y, 0f))
		assertTrue(areClose(hit.worldPoint.z, 1f))
		
		assertTrue(areClose(hit.normal.x, 0f))
		assertTrue(areClose(hit.normal.y, 0f))
		assertTrue(areClose(hit.normal.z, 1f))
	}
	
	@Test
	fun `test rayIntersectionShape`() {
		val cube = Cube()
		val ray = Ray(Point(-2f, 0f, 0f), vecX())
		
		val hits = cube.rayIntersectionShape(ray)
		
		assertEquals(2, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
		assertEquals(3f, hits[1].t, 1e-5f)
	}
	
	@Test
	fun `test rayIntersectionShape inside cube`() {
		val cube = Cube()
		val ray = Ray(Point(0f, 0f, 0f), vecX())
		
		val hits = cube.rayIntersectionShape(ray)
		
		assertEquals(1, hits.size)
		assertEquals(1f, hits[0].t, 1e-5f)
	}
	
	@Test
	fun `test contains`() {
		val cube = Cube(translation(Vec(2f, 0f, 0f)))
		
		assertTrue(cube.contains(Point(2f, 0f, 0f)))
		assertTrue(cube.contains(Point(2.5f, 0f, 0f)))
		assertFalse(cube.contains(Point(0f, 0f, 0f)))
		assertFalse(cube.contains(Point(3.5f, 0f, 0f)))
	}
}