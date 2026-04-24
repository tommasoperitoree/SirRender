import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2

interface Shape {
	// if we need to have shape already have method Transformation, then interface does not work
	
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("Shape.rayIntersection() is abstract")
}

class Sphere(
	val transformation: Transformation = Transformation()
) {
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
	
	fun rayIntersection(ray: Ray): HitRecord? {
	
	}
}

class Plane(

) {

}