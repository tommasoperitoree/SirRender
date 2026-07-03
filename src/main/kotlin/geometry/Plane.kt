package geometry

import materials.Material
import math.Normal
import math.Point
import math.SurfaceVec
import math.Transformation
import kotlin.math.abs
import kotlin.math.floor


fun planePointToUV(point: Point): SurfaceVec =
	SurfaceVec(point.x - floor(point.x), point.y - floor(point.y))

/** A 3D infinite plane parallel to the x and y axes and passing through the origin. */
class Plane(
	override val transformation: Transformation = Transformation(),
	override val material: Material = Material()
) : Shape {
	
	/**
	 * Checks if the [ray] intersect the [Plane].
	 * Returns a [HitRecord] or `null` if no intersection is found.
	 */
	override fun rayIntersection(ray: Ray): HitRecord? {
		
		val invRay: Ray = ray.transform(transformation.inverse())
		if (abs(invRay.dir.z) < 1e-5f) {
			return null
		}
		
		val t = -invRay.origin.z / invRay.dir.z
		if (t <= invRay.tMin || t >= invRay.tMax) {
			return null
		}
		
		val hitPoint = invRay.at(t)
		return HitRecord(
			transformation * hitPoint,
			transformation * Normal(0f, 0f, if (invRay.dir.z < 0f) 1f else -1f),
			planePointToUV(hitPoint),
			t,
			ray,
			this
		)
		
	}
	
}