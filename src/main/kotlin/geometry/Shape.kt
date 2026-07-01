package geometry

import materials.Material
import math.Transformation


/**
 * A generic 3D shape.
 * Interface. Each concrete [Shape] should override the [rayIntersection] method.
 */
interface Shape {
	
	val transformation: Transformation
	val material: Material
	
	/**
	 * Optional axis-aligned bounding box in object space, used as a cheap pre-filter in [rayIntersection].
	 * `null` means the shape has no useful AABB (e.g. for [Sphere] and [Plane]) and every ray must be tested directly.
	 */
	val aabb: AABB? get() = null // overridden by Mesh only
	
	/** Compute the intersection between a [ray] and this [Shape] */
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("geometry.Shape.rayIntersection() is abstract")
}