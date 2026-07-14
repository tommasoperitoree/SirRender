package geometry

import materials.Material
import materials.areClose
import math.Point
import math.SurfaceVec
import math.Transformation
import math.Vec
import kotlin.math.abs

/** Converts a [point] on a cube face into UV coordinates. */
fun cubePointToUV(point: Point, axis: Int, sign: Float): SurfaceVec {
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
	return SurfaceVec(
		((u + 1f) / 2f).coerceIn(0f, 1f),
		((v + 1f) / 2f).coerceIn(0f, 1f)
	)
}

/** Determines the cube face containing [point]. */
fun findExitAxis(point: Point): Pair<Int, Float> {
	
	val coords = floatArrayOf(point.x, point.y, point.z)
	for (axis in 0..2) if (areClose(abs(coords[axis]), 1f)) {
		return Pair(axis, if (coords[axis] > 0) 1f else -1f)
	}
	return Pair(0, 1f)
	// Fallback: float imprecision prevents any face matching areClose()
	// Returning axis 0 / sign +1 is a best-guess that avoids a crash;
	// the resulting normal may be slightly wrong for extreme grazing rays.
}


/**
 * An axis-aligned unit cube occupying [−1, 1]³ in object space.
 *
 * The six faces are identified by axis (0=X, 1=Y, 2=Z) and sign (±1).
 * UV coordinates are computed per-face via [cubePointToUV].
 * Apply a [Transformation] to scale, rotate, or translate the cube into world space.
 */
class Cube(
	override val transformation: Transformation = Transformation(),
	override val material: Material = Material()
) : Shape {
	// 0=x, 1=y, 2=z
	// unitary cube centered in the origin with length 2 [-1,1]
	
	/**
	 * Checks if the [ray] intersect the [Cube], via the slab method.
	 * Returns a [HitRecord] or `null` if no intersection is found.
	 */
	override fun rayIntersection(ray: Ray): HitRecord? {
		val invRay: Ray = ray.transform(transformation.inverse())
		var hitAxis = -1
		var hitSign = 0f
		var tNear = Float.NEGATIVE_INFINITY
		var tFar = Float.POSITIVE_INFINITY
		val origins = floatArrayOf(invRay.origin.x, invRay.origin.y, invRay.origin.z)
		val dirs = floatArrayOf(invRay.dir.x, invRay.dir.y, invRay.dir.z)
		
		if (invRay.dir.norm() < 1e-5f) return null
		
		for (axis in 0..2) {
			val o = origins[axis]
			val d = dirs[axis]
			if (areClose(d, 0f)) { // if the ray is orthogonal to the face on axis
				if (o < -1f || o > 1f) return null // if the origin of ray is out of length range it won't hit the cube
				else continue
			}
			
			// ray is describe by the parametric eq x(t)=o+t*d
			var t1 = (-1 - o) / d //enter
			var t2 = (1 - o) / d //exit
			var sign1 = -1f
			var sign2 = 1f
			
			// condition t2 is in enter and t1 in exit, it depends on sign of d
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
		// choose first valid t, if tNear>0 is t1 else is tFar
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
		
		return HitRecord(
			worldPoint = transformation * hitPoint,
			normal = (transformation * normalVec.toNormal()),
			surfacePoint = cubePointToUV(hitPoint, axis, sign),
			t = tHit,
			ray = ray,
			shape = this
		)
	}
	
	/**
	 * Check if the [ray] intersects the [Cube]
	 * Returns all valid [HitRecord]s along the ray, sorted from the closest to farthest.
	 */
	override fun rayIntersectionShape(ray: Ray): List<HitRecord> {
		val invRay = ray.transform(transformation.inverse())
		
		var nearAxis = -1
		var nearSign = 0f
		var farAxis = -1
		var farSign = 0f
		
		var tNear = Float.NEGATIVE_INFINITY
		var tFar = Float.POSITIVE_INFINITY
		
		val origins = floatArrayOf(invRay.origin.x, invRay.origin.y, invRay.origin.z)
		val dirs = floatArrayOf(invRay.dir.x, invRay.dir.y, invRay.dir.z)
		
		if(invRay.dir.norm() < 1e-5f) return emptyList()
		
		for(axis in 0..2) {
			val o = origins[axis]
			val d = dirs[axis]
			if (areClose(d, 0f)) {
				if (o < -1f || o > 1f) return emptyList()
				else continue
			}
			
			var t1 = (-1 - o) / d
			var t2 = (1 - o) / d
			var sign1 = -1f
			var sign2 = 1f
			
			// condition t2 is in enter and t1 in exit, it depends on sign of d
			if (t1 > t2) {
				val temp = t1
				t1 = t2
				t2 = temp
				val tempSign = sign1
				sign1 = sign2
				sign2 = tempSign
			}
			
			if (t1 > tNear) {
				tNear = t1
				nearAxis = axis
				nearSign = sign1
			}
			
			if (t2 < tFar) {
				tFar = t2
				farAxis = axis
				farSign = sign2
			}
			if (tNear > tFar) return emptyList()
		}
		val hits = mutableListOf<HitRecord>()
		
		for ((tHit, axis, sign) in listOf(Triple(tNear, nearAxis, nearSign), Triple(tFar, farAxis, farSign))) {
			if (tHit > invRay.tMin && tHit < invRay.tMax) {
				val hitPoint = invRay.at(tHit)
				val normalVec = when (axis) {
					0 -> Vec(sign, 0f, 0f)
					1 -> Vec(0f, sign, 0f)
					else -> Vec(0f, 0f, sign)
				}
				hits.add(
					HitRecord(
						worldPoint = transformation * hitPoint,
						normal = transformation * normalVec.toNormal(),
						surfacePoint = cubePointToUV(hitPoint, axis, sign),
						t = tHit,
						ray = ray,
						shape = this
					)
				)
			}
		}
		return hits.sortedBy { hit -> hit.t }
	}
	
	/** Returns `true` if [point] lies inside this [Cube]. */
	override fun contains(point: Point): Boolean {
		val localPoint = transformation.inverse() * point
		return abs(localPoint.x) < 1f && abs(localPoint.y) < 1f && abs(localPoint.z) < 1f
	}
}