package geometry

import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.Material
import math.Point
import math.Vec
import math.scaling
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlaneTest {
	
	val groundMaterial = Material(
		brdf = DiffuseBRDF(
			pigment = CheckeredPigment(
				color1 = Color.white,
				color2 = Color.black,
				numSteps = 2
			)
		)
	)
	val plane = Plane(material = groundMaterial)
	
	@Test
	fun `test intersection`() {
		val s = scaling(Vec(-1f, 1f, 1f)) // invert on x-axis
		val planes = Plane(s, groundMaterial)
		val point = Point(0.25f, 0.25f)
		val point1 = Point(0.25f, 0.75f)
		
		val uvPlane = planePointToUV(point)
		val uvPlane1 = planePointToUV(point1)
		
		// to the plane with transformation we need to pass the inverse transformation on the point
		val localPoint = planes.transformation.inverse() * point
		val localPoint1 = planes.transformation.inverse() * point1
		val uvPlanes = planePointToUV(localPoint)
		val uvPlanes1 = planePointToUV(localPoint1)
		
		//without scaling
		assertEquals(plane.material.brdf.pigment.getColor(uvPlane), Color.white)
		assertEquals(plane.material.brdf.pigment.getColor(uvPlane1), Color.black)
		//with scaling
		assertEquals(planes.material.brdf.pigment.getColor(uvPlanes1), Color.white)
		assertEquals(planes.material.brdf.pigment.getColor(uvPlanes), Color.black)
	}
}
