package geometry

import materials.Material
import math.Transformation


/**
 * A generic 3D shape.
 * Each concrete [Shape] should override the [rayIntersection] method.
 */
interface Shape {
	
	val transformation: Transformation
	val material: Material
	
	/** Compute the intersection between a [ray] and this [Shape] */
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("geometry.Shape.rayIntersection() is abstract")
}