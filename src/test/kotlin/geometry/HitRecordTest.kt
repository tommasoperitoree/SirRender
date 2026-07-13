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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HitRecordTest {
	val ray = Ray(Point(0f, 0f, 0f), Vec(0f, 0f, 1f))
	val shape = Sphere()
	val normal = Normal(0f, 0f, 1f)
	val surfacePoint = SurfaceVec(0.5f, 0.5f)
	val t = 3f
	
	
	@Test
	fun `test HitRecord isClose`() {
		val point1 = Point(1f, 1f, 1f)
		val point2 = Point(1f, 1.000001f, 1f)
		
		val hit1 = HitRecord(point1, normal, surfacePoint, t, ray, shape)
		val hit2 = HitRecord(point2, normal, surfacePoint, t, ray, shape)
		
		assertTrue(hit1.isClose(hit2))
	}
}