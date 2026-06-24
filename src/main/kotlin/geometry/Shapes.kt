package geometry

import materials.Material
import materials.areClose
import math.Normal
import math.Point
import math.Transformation
import math.Vec
import math.Vec2d
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.floor

import kotlin.math.sqrt
import kotlin.ranges.rangeTo


/**
 * A generic 3D shape.
 * Interface. Each concrete [Shape] should override the [rayIntersection] method.
 */
interface Shape {
	
	val transformation: Transformation
	val material: Material
	
	/** Compute the intersection between a [ray] and this [Shape] */
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("geometry.Shape.rayIntersection() is abstract")
}


/** Calculation of [Sphere]'s [Normal] at intersection [point] */
fun sphereNormal(point: Point, rayDir: Vec): Normal {
	val result = Normal(point.x, point.y, point.z)
	return if ((point.toVec() dot rayDir) < 0f) result else -result
}

/** Calculation of intersection [point] on the geometry.Sphere's surface, in (u,v) coordinates*/
fun spherePointToUV(point: Point): Vec2d {
	// To understand if 0.5f shift is needed
	// val u = 0.5f + atan2(point.y, point.x) / (2f * PI.toFloat())
	val u = atan2(point.y, point.x) / (2f * PI.toFloat())
	return Vec2d(
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
<<<<<<< HEAD
		//val delta4: Float = (o dot d).pow(2f) - d.squaredNorm().times(o.squaredNorm() - 1f)
		//try to use vectorial formula in slides, it's still ok but could be a problem if o is perpendicular to d
		// so that t1 & t2 are too small and rejected
		val a = d.squaredNorm()
		val bHalf = o dot d
		val cross = o cross d
		val delta4: Float = a - (cross).squaredNorm()
		if (delta4 == 0f) return null //adding this check if there are no intersection
		val t1: Float = (-bHalf - sqrt(delta4)) / a
		val t2: Float = (-bHalf + sqrt(delta4)) / a
		
=======
		val od: Float = o dot d // sign tells if ray is moving towards or away from ray's origin
		val dSq = d.squaredNorm()
		val oSq = o.squaredNorm()
		
		// if (oSq > 1f && od > 0f) return null
		
		val deltaRid: Float = od * od - dSq * (oSq - 1f)
		
		if (deltaRid <= 0f) return null // adding this check if there are no intersection
		
		val sqr = sqrt(deltaRid)
		
		val t1: Float = (-od - sqr) / dSq
		val t2: Float = (-od + sqr) / dSq
>>>>>>> 3b2a0f9d9f4c2f54a19092099ac2c658ace958c7
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
			transformation * sphereNormal(hitPoint, rayDir = d),
			spherePointToUV(hitPoint),
			tFirstHit,
			ray,
			this
		)
	}
}


fun planePointToUV(point: Point): Vec2d =
	Vec2d(point.x - floor(point.x), point.y - floor(point.y))

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


fun cubePointToUV(point: Point, axis: Int, sign: Float): Vec2d {
	val u = when (axis) {
		0 -> if (sign > 0) point.y else -point.y
		1 -> if (sign > 0) point.x else -point.x
		else -> if (sign > 0) point.x else -point.x
	}
	val v = when (axis) {
		0 -> point.z
		1 -> point.z
		else -> if (sign > 0) point.y else -point.y
	}
	// Shift to [0, 1] range and strictly coerce to prevent bounds crashing
	return Vec2d(
		((u + 1f) / 2f).coerceIn(0f, 1f),
		((v + 1f) / 2f).coerceIn(0f, 1f)
	)
}

fun findExitAxis(point: Point): Pair<Int, Float> {
	val coords = floatArrayOf(point.x, point.y, point.z)
	for (axis in 0..2) if (areClose(abs(coords[axis]), 1f)) {
		return Pair(axis, if (coords[axis] > 0) 1f else -1f)
	}
	return Pair(0, 1f) //fallback
}

/**
 *
 */
class Cube(
	override val transformation: Transformation = Transformation(),
	override val material: Material = Material()
) : Shape {
	// 0=x, 1=y, 2=z
	
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

