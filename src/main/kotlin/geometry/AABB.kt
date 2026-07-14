package geometry

import math.Point

/**
 * An axis-aligned bounding box (AABB): a rectangular parallelepiped whose faces are
 * perpendicular to the coordinate axes, defined by its minimum [pMin] and maximum [pMax] corners.
 */
data class AABB(
	val pMin: Point,
	val pMax: Point
) {
	
	/**
	 * Tests whether [ray] intersects this box, without computing the exact hit point.
	 *
	 * Uses the slab method: each axis defines a pair of parallel planes (a "slab"), and the
	 * ray's parametric intersection interval `[tMin, tMax]` with each slab is computed
	 * independently. The ray hits the box only if all three per-axis intervals overlap —
	 * equivalently, if the latest entry point (`max` of the three interval minimums) occurs
	 * before the earliest exit point (`min` of the three interval maximums).
	 */
	fun quickRayIntersection(ray: Ray): Boolean {
		
		val o = ray.origin
		val d = ray.dir
		
		val t1x = (pMin.x - o.x) / d.x
		val t2x = (pMax.x - o.x) / d.x
		val t1y = (pMin.y - o.y) / d.y
		val t2y = (pMax.y - o.y) / d.y
		val t1z = (pMin.z - o.z) / d.z
		val t2z = (pMax.z - o.z) / d.z
		
		// latest (with maxOf) of all 3 dir entry points
		val tMin = maxOf(minOf(t1x, t2x), minOf(t1y, t2y), minOf(t1z, t2z))
		// earliest (with minOf) of all 3 dir exit points
		val tMax = minOf(maxOf(t1x, t2x), maxOf(t1y, t2y), maxOf(t1z, t2z))
		
		// No overlap between slab intervals
		if (tMax < tMin) return false
		
		// First hit is tMin if it's ahead of ray origin, otherwise tMax (ray starts inside box)
		val firstHit = if (tMin > ray.tMin) tMin else tMax
		
		// Hit must be within valid ray interval
		if (firstHit < ray.tMin) return false
		
		return firstHit <= ray.tMax
	}
	
	
	companion object {
		/** Computes the tightest AABB containing all [points]. Requires at least one point. */
		fun fromPoints(points: List<Point>): AABB {
			require(points.isNotEmpty()) { "Cannot compute AABB of an empty point list" }
			val minX = points.minOf { it.x }
			val maxX = points.maxOf { it.x }
			val minY = points.minOf { it.y }
			val maxY = points.maxOf { it.y }
			val minZ = points.minOf { it.z }
			val maxZ = points.maxOf { it.z }
			return AABB(Point(minX, minY, minZ), Point(maxX, maxY, maxZ))
		}
	}
}