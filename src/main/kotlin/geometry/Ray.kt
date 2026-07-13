package geometry

import math.Point
import math.Transformation
import math.Vec

/**
 * Represents a ray defined by an [origin] and a [dir].
 *
 * @property origin The starting point of the ray
 * @property dir    The direction of the ray
 * @property tMin   Minimum valid ray parameter. It avoids self-intersection.
 * @property tMax   Maximum valid ray parameter
 * @property depth  Current recursion depth of the ray
 */
class Ray(
	var origin: Point = Point(),
	var dir: Vec = Vec(),
	var tMin: Float = 1e-3f,
	var tMax: Float = Float.POSITIVE_INFINITY,
	var depth: Int = 0,
) {
	
	/**
	 * Checks whether two [Ray]s are equal, comparing [origin] and [dir]
	 * (within floating point tolerance) through [isClose] fun.
	 */
	fun isClose(other: Ray) =
		origin.isClose(other.origin) && dir.isClose(other.dir)
	
	/** Calculates the [Point] along the ray's path at some distance from the origin given by [t]. */
	fun at(t: Float): Point =
		origin + dir * t
	
	/** Returns a new ray whose [origin] and [dir] are the transformation of the original [Ray]. */
	fun transform(transformation: Transformation) =
		Ray(
			transformation * origin,
			transformation * dir,
			tMin,
			tMax,
			depth,
		)
}