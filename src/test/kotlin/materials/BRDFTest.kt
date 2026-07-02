package materials

import math.Normal
import math.PCG
import math.Point
import math.Vec
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BRDFTest {
	
	val pcg = PCG()
	val incomingDir = Vec(0f, 0f, -1f)
	val normal = Normal(0f, 0f, 1f).normalize()
	val intPoint = Point(0f, 0f, 3f)
	val depth = 5
	
	@Test
	fun `ScatterRay DiffuseBRDF test`() {
		val diffuseBRDF = DiffuseBRDF()
		var ray = diffuseBRDF.scatterRay(pcg, incomingDir, intPoint, normal, depth)
		assertEquals(intPoint, ray.origin)
		assertEquals(1e-3f, ray.tMin)
		assertEquals(Float.POSITIVE_INFINITY, ray.tMax)
		assertEquals(depth, ray.depth)
		
		//test it for numerous rays
		repeat(1000) {
			ray = diffuseBRDF.scatterRay(pcg, incomingDir, intPoint, normal, depth)
			assertTrue(areClose(ray.dir.squaredNorm(), 1f))
			assertTrue(ray.dir.dot(normal.toVec()) >= 0f)
			assertTrue(incomingDir.dot(normal.toVec()) <= 0f)
		}
	}
	
	@Test
	fun `ScatterRay SpecularBRDF test`() {
		val specularBRDF = SpecularBRDF()
		val ray = specularBRDF.scatterRay(pcg, incomingDir, intPoint, normal, depth)
		
		assertEquals(intPoint, ray.origin)
		assertTrue(areClose(ray.dir.squaredNorm(), 1f))
		assertTrue(ray.dir.dot(normal.toVec()) >= 0f)
		assertEquals(1e-3f, ray.tMin)
		assertEquals(Float.POSITIVE_INFINITY, ray.tMax)
		assertEquals(depth, ray.depth)
		// for incoming ray form (0,0,-1) the reflected ray has dir (0,0,1)
		assertTrue(ray.dir.isClose(Vec(0f, 0f, 1f)))
	}
}
