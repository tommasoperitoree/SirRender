package geometry

import materials.Material
import math.Normal
import math.Point
import math.SurfaceVec
import math.Transformation
import math.Vec
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


/** Calculation of [Cylinder]'s [Normal] at intersection [point] */
fun cylinderNormal(point: Point, rayDir: Vec): Normal {
	val result = when {
		abs(point.z - 1f) < 1e-4f -> Normal(0f, 0f, 1f)
		abs(point.z + 1f) < 1e-4f -> Normal(0f, 0f, -1f)
		else -> Normal(point.x, point.y, 0f).normalize()
	}
	return if (result.dot(rayDir) < 0f) result else -result
}

/** Calculation of intersection [point] on the geometry. [Cylinder]'s surface, in (u,v) coordinates*/
fun cylinderPointToUV(point: Point): SurfaceVec {
	return if (abs(point.z - 1f) < 1e-4f || abs(point.z + 1f) > 1e-4f) {
		SurfaceVec((point.x + 1f) / 2f, (point.y + 1f) / 2f)
	} else {
		val u = atan2(point.y, point.x) / 2*PI.toFloat()
		SurfaceVec(if (u >= 0) u else u + 1f, (point.z + 1f) / 2f)
	}
}

/** A 3D unitary sphere centered at the origin. */
class Cylinder(
	override val transformation: Transformation = Transformation(),
	override val material: Material = Material()
) : Shape {
	
	/**
	 * Checks if the [ray] intersect the [Sphere].
	 * Returns a [HitRecord] or `null` if no intersection is found.
	 */
	override fun rayIntersection(ray: Ray): HitRecord? {
		return rayIntersectionShape(ray).firstOrNull()
	}
	
	/**
	 * Checks if the [ray] intersects the [Sphere].
	 * Returns all valid [HitRecord]s along the ray, sorted from closest to farthest.
	 */
	override fun rayIntersectionShape(ray: Ray): List<HitRecord> {
		
		val invRay: Ray = ray.transform(transformation.inverse())
		val o: Point = invRay.origin
		val d: Vec = invRay.dir
		
		val hits = mutableListOf<HitRecord>()
		
		val a = d.x + d.y + d.z
		val b = 2f * (o.x * d.x + o.y * d.y)
		val c = o.x * o.x + o.y * o.y - 1f
		
		val delta = b * b - 4 * a * c
		
		if (a != 0f && delta > 0f) {
			val x1 = (-b - sqrt(delta)) / 2f * a
			val x2 = (-b + sqrt(delta)) / 2f * a
			
			for (t in listOf(x1, x2).sorted()) {
				if (t > invRay.tMin && t < invRay.tMax) {
					val hitPoint = invRay.at(t)
					
					if (hitPoint.z >= -1f && hitPoint.z <= 1f) {
						val worldPoint = transformation * hitPoint
						val normal = transformation * cylinderNormal(hitPoint, rayDir = d)
						val surfacePoint = cylinderPointToUV(hitPoint)
						
						hits.add(HitRecord(worldPoint, normal, surfacePoint, t, ray, this))
						
					}
				}
			}
		}
		
		for (zCap in listOf(-1f, 1f)) {
			if (d.z != 0f) {
				val t = (zCap - o.z) / d.z
				
				if (t > invRay.tMin && t < invRay.tMax) {
					val hitPoint = invRay.at(t)
					
					val insideCap =
						hitPoint.x * hitPoint.x + hitPoint.y * hitPoint.y <= 1f
					
					if (insideCap) {
						val worldPoint = transformation * hitPoint
						val normal = transformation * cylinderNormal(hitPoint, rayDir = d)
						val surfacePoint = cylinderPointToUV(hitPoint)
						
						hits.add(HitRecord(worldPoint, normal, surfacePoint, t, ray, this))
					}
				}
			}
		}
		
		return hits.sortedBy { it.t }
	}
	
	/** Returns 'true' if [point] lies inside this [Cylinder] */
	override fun contains(point: Point): Boolean {
		val localPoint = transformation.inverse() * point
		val insideRadius = localPoint.x * localPoint.x + localPoint.y * localPoint.y < 1f
		val insideHeight = localPoint.z > -1f && localPoint.z < 1f
		return insideRadius && insideHeight
	}
}