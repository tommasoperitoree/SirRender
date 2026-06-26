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
	return if ((point.toVec() dot rayDir) < 0f) result else result.unaryMinus()
}

/** Calculation of intersection [point] on the Sphere's surface, in (u,v) coordinates*/
fun spherePointToUV(point: Point): Vec2d {
	val u = atan2(point.y, point.x) / (2f * PI.toFloat())
	val v = acos(point.z) / PI.toFloat()
	return Vec2d(
		if (u >= 0f) u else u + 1f, v
	)
}

/**
 * A generic 3D shape.
 * Interface. Each concrete [Shape] should override the [rayIntersection] method.
 */
interface Shape {
	
	val transformation: Transformation
	val material: Material
	
	/** Compute the intersection between a [ray] and this [Shape] */
	fun rayIntersection(ray: Ray): HitRecord? = throw NotImplementedError("Shape.rayIntersection() is abstract")
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
			transformation * sphereNormal(hitPoint, rayDir = invRay.dir),
			spherePointToUV(hitPoint),
			tFirstHit,
			ray,
			this
		)
	}
}


/** A 3D infinite plane parallel to the x and y axes and passing through the origin. */
class Plane(
	override val transformation: Transformation = Transformation(),
	override val material: Material = Material()
) : Shape {
	
	fun planePointToUV(point: Point): Vec2d {
		
		val u = point.x - floor(point.x)
		val v = point.y - floor(point.y)
		return Vec2d(u, v)
	}
	
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
			Vec2d(hitPoint.x, hitPoint.y),//floor was already in checkered
			t,
			ray,
			this
		)
		
	}
	
}


class Cube(
	override val transformation: Transformation = Transformation(),
	override val material: Material = Material()
) : Shape {
	
	//0=x, 1=y, 2=z
	
	fun cubePointToUV(point: Point, axis: Int, sign: Float): Vec2d {
		return when (axis) {
			0 -> Vec2d((point.y + 1f) / 2f, (point.z + 1f) / 2f)
			1 -> Vec2d((point.x + 1f) / 2f, (point.z + 1f) / 2f)
			else -> Vec2d((point.x + 1f) / 2f, (point.y + 1f) / 2f)
		}
	}
	
	fun findExitAxis(point: Point): Pair<Int, Float> {
		val coords = floatArrayOf(point.x, point.y, point.z)
		for (axis in 0..2) if (areClose(abs(coords[axis]), 1f)) {
			return Pair(axis, if (coords[axis] > 0) 1f else -1f)
		}
		return Pair(0, 1f) //fallback
	}
	
	//unitary cube centered in the origin with length 2 [-1,1]
	override fun rayIntersection(ray: Ray): HitRecord? {
		val invRay: Ray = ray.transform(transformation.inverse())
		if (invRay.dir.norm() < 1e-5f) {
			return null
		}
		var hitAxis = -1
		var hitSign = 0f
		var tNear = Float.NEGATIVE_INFINITY
		var tFar = Float.POSITIVE_INFINITY
		
		val origins = floatArrayOf(invRay.origin.x, invRay.origin.y, invRay.origin.z)
		val dirs = floatArrayOf(invRay.dir.x, invRay.dir.y, invRay.dir.z)
		for (axis in 0..2) {
			val o = origins[axis]
			val d = dirs[axis]
			if (areClose(d, 0f)) { // if the ray is orthogonal to the face on axis
				if (o < -1f || o > 1f) return null // if the origin of ray is out of length range it won't hit the cube
				else continue
			}
			//ray is describe by the parametric eq x(t)=o+t*d
			var t1 = (-1 - o) / d //enter
			var t2 = (1 - o) / d //exit
			var sign1 = -1f
			var sign2 = 1f
			
			//condition t2 is in enter and t1 in exit, it depends on sign of d
			if (t1 > t2) {
				val temp = t1
				t1 = t2
				t2 = temp
				val tempS = sign1
				sign1 = sign2
				sign2 = tempS
			}
			
			if (t1 > tNear) {
				tNear = t1
				hitAxis = axis
				hitSign = sign1
			}
			
			if (t2 < tFar) {
				tFar = t2
			}
			if (tNear > tFar) return null
		}
		//choose first valid t, if tNear>0 is t1 else is tFar
		val tHit = when {
			tNear in invRay.tMin..invRay.tMax -> tNear
			tFar in invRay.tMin..invRay.tMax -> tFar
			else -> return null
		}
		
		val hitPoint = invRay.at(tHit)
		val (axis, sign) = if (tHit == tNear) Pair(hitAxis, hitSign)
		else findExitAxis(hitPoint)
		
		val normalVec = when (axis) {
			0 -> Vec(sign, 0f, 0f)
			1 -> Vec(0f, sign, 0f)
			else -> Vec(0f, 0f, sign)
		}
		
		val worldPoint = transformation * hitPoint
		val worldNormal = (transformation * normalVec.toNormal())
		val uv = cubePointToUV(hitPoint, axis, sign)
		
		
		return HitRecord(
			worldPoint = worldPoint,
			normal = worldNormal,
			surfacePoint = uv,
			t = tHit,
			ray = ray,
			shape = this
		)
	}
}

