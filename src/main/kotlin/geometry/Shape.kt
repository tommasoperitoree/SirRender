package geometry

import materials.Material
import materials.areClose
import math.Normal
import math.Point
import math.Transformation
import math.Vec
import math.SurfaceVec
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