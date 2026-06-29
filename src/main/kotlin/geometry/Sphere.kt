package geometry

import materials.Material
import math.Normal
import math.Point
import math.SurfaceVec
import math.Transformation
import math.Vec
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt


/** Calculation of [Sphere]'s [Normal] at intersection [point] */
fun sphereNormal(point: Point, rayDir: Vec): Normal {
	val result = Normal(point.x, point.y, point.z)
	return if ((point.toVec() dot rayDir) < 0f) result else -result
}

/** Calculation of intersection [point] on the geometry.Sphere's surface, in (u,v) coordinates*/
fun spherePointToUV(point: Point): SurfaceVec {
	// To understand if 0.5f shift is needed
	// val u = 0.5f + atan2(point.y, point.x) / (2f * PI.toFloat())
	val u = atan2(point.y, point.x) / (2f * PI.toFloat())
	return SurfaceVec(
		if (u >= 0f) u else u + 1f, acos(point.z.coerceIn(-1f, 1f)) / PI.toFloat()
	)
}

/** A 3D unitary sphere centered at the origin. */
class Sphere(
	override val transformation: Transformation = Transformation(), override val material: Material = Material()
) : Shape {
	
	/**
	 * Checks if the [ray] intersect the [Sphere].
	 * Returns a [HitRecord] or `null` if no intersection is found.
	 */
	override fun rayIntersection(ray: Ray): HitRecord? {
		
		val invRay: Ray = ray.transform(transformation.inverse())
		val o: Vec = invRay.origin.toVec()
		val d: Vec = invRay.dir
		
		val od: Float = o dot d // negative if ray moves toward sphere center, positive if moving away
		val dSq = d.squaredNorm()
		val oSq = o.squaredNorm()
		
		// if (oSq > 1f && od > 0f) return null
		
		val deltaRid: Float = od * od - dSq * (oSq - 1f)
		
		if (deltaRid <= 0f) return null // negative discriminant: ray misses the sphere
		val sqr = sqrt(deltaRid)
		
		val t1: Float = (-od - sqr) / dSq
		val t2: Float = (-od + sqr) / dSq
		val tFirstHit = if (t1 > invRay.tMin && t1 < invRay.tMax) {
			t1
		} else if (t2 > invRay.tMin && t2 < invRay.tMax) {
			t2
		} else {
			return null
		}
		val hitPoint = invRay.at(tFirstHit)
		val worldPoint = transformation * hitPoint
		val normal = transformation * sphereNormal(hitPoint, rayDir = d)
		val surfacePoint = spherePointToUV(hitPoint)
		val t = tFirstHit
		
		return HitRecord(
			worldPoint,
			normal,
			surfacePoint,
			t,
			ray,
			this
		)
	}
}