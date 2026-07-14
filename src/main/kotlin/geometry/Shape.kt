package geometry

import materials.Material
import math.Point
import math.Transformation


/**
 * A generic 3D shape.
 * Each concrete [Shape] should override the [rayIntersection] method.
 */
interface Shape {
	
	val transformation: Transformation
	val material: Material
	
	/** Compute the intersection between a [ray] and this [Shape]. */
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("geometry.Shape.rayIntersection() is abstract")
	
	/** Computes all intersections between a [ray] and this [Shape]. */
	fun rayIntersectionShape(ray: Ray): List<HitRecord> {
		val hit = rayIntersection(ray)
		return if (hit != null) listOf(hit) else emptyList()
	}
	
	/** Returns true if [point] is inside the [Shape]. */
	fun contains(point: Point): Boolean =
		throw NotImplementedError("geometry.Shape.contains() is abstract")
	
}