import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.floor

import kotlin.math.pow
import kotlin.math.sqrt

/** Calculation of [Sphere]'s [Normal] at intersection [point] */
fun sphereNormal(point: Point, rayDir: Vec): Normal {
	val result = Normal(point.x, point.y, point.z)
	return if ((point.toVec() dot rayDir) < 0f) result else -result
}

/** Calculation of intersection [point] on the Sphere's surface, in (u,v) coordinates*/
fun spherePointToUV(point: Point): Vec2d {
	val u = atan2(point.x, point.y) / (2f * PI.toFloat())
	val v = acos(point.z) / PI.toFloat()
	return Vec2d(
		if (u >= 0f) u else u + 1f,
		v
	)
}

/**
 * A generic 3D shape.
 * Interface. Each concrete [Shape] should override the [rayIntersection] method.
 */
interface Shape {
	/** Compute the intersection between a [ray] and this [Shape] */
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("Shape.rayIntersection() is abstract")
}


/** A 3D unitary sphere centered at the origin. */
class Sphere(
	val transformation: Transformation = Transformation()
) : Shape {
	
	/**
	 * Checks if the [ray] intersect the [Sphere].
	 * Returns a [HitRecord] or `null` if no intersection is found.
	 */
	override fun rayIntersection(ray: Ray): HitRecord? {
		
		val invRay: Ray = ray.transform(transformation.inverse())
		val o: Vec = invRay.origin.toVec()
		val d: Vec = invRay.dir
		val delta4: Float = (o dot d).pow(2f) - d.squaredNorm().times(o.squaredNorm() - 1f)
		val t1: Float = (-(o dot d) - sqrt(delta4)) / d.squaredNorm()
		val t2: Float = (-(o dot d) + sqrt(delta4)) / d.squaredNorm()
		
		val tFirstHit = if (t1 > invRay.tMin && t1 < invRay.tMax) {
			t1
		} else if (t2 > invRay.tMin && t2 < invRay.tMax) {
			t2
		} else {
			return null
		}
		val hitPoint = invRay.at(tFirstHit)
		
		return HitRecord(
			transformation * hitPoint,
			transformation * sphereNormal(hitPoint, rayDir = ray.dir),
			spherePointToUV(hitPoint),
			tFirstHit,
			ray
		)
	}
}


/** A 3D infinite plane parallel to the x and y axes and passing through the origin. */
class Plane(
	val transformation: Transformation = Transformation()
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
			Vec2d(hitPoint.x - floor(hitPoint.x), hitPoint.y - floor(hitPoint.y)),
			t,
			ray
		)
		
	}
}