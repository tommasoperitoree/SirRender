import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2

import kotlin.math.pow
import kotlin.math.sqrt

interface Shape {
	// if we need to have shape already have method Transformation, then interface does not work
	
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("Shape.rayIntersection() is abstract")
}

class Sphere(
	val transformation: Transformation = Transformation()
) {
	fun rayIntersection(ray: Ray): HitRecord? {
		val invRay: Ray = ray.transform(transformation.inverse())
		val o: Vec = ray.origin.toVec()
		val d: Vec = ray.dir
		val delta4: Float = (o dot d).pow(2f) - d.squaredNorm().times(o.squaredNorm() - 1f)
		val t1: Float = (-(o dot d) - sqrt(delta4)) / d.squaredNorm()
		val t2: Float = (-(o dot d) + sqrt(delta4)) / d.squaredNorm()
		var tFirstHit = t1
		
		tFirstHit = if (t1 > invRay.tMin && t1 < invRay.tMax) {
			t1
		} else if (t2 > invRay.tMin && t2 < invRay.tMax) {
			t2
		} else {
			return null
		}
		
		val hitPoint = invRay.at(tFirstHit)
		return HitRecord(
			worldPoint = transformation * hitPoint,
			normal = transformation * sphereNormal(hitPoint, rayDir = ray.dir),
			spherePointToUV(hitPoint),
			tFirstHit,
			ray
		)
	}
	
	/** Calculation of [Sphere]'s [Normal] at intersection [point] */
	fun sphereNormal(point: Point, rayDir: Vec): Normal {
		val result = Normal(point.x, point.y, point.z)
		return if ((point.toVec() dot rayDir) < 0f) result else -result
	}
	
	/** Calculation of intersection [point] on the Sphere's surface, in (u,v) coordinates*/
	fun spherePointToUV(point: Point): Vec2d {
		val u = atan2(point.x, point.y) / (2f * PI.toFloat())
		val v = acos(point.z / PI.toFloat())
		return Vec2d(
			if (u >= 0f) u else u + 1f,
			v
		)
	}
	
}

class Plane(

) {

}