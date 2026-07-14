package math

import materials.areClose

/**
 * Represents a 3D point with coordinates ([x], [y], [z]).
 *
 * Unlike [Vec], a point represents a position in space — not a direction.
 * Arithmetic is restricted to geometrically meaningful operations:
 * adding a vector to a point yields a point, subtracting two points yields a vector.
 */
data class Point(
	val x: Float = 0f,
	val y: Float = 0f,
	val z: Float = 0f
) {
	
	// --- Operator overloading ---
	
	/** Returns the point displaced by [other] vector. */
	operator fun plus(other: Vec): Point =
		Point(x + other.x, y + other.y, z + other.z)
	
	/** Returns the vector from [other] point to this point. */
	operator fun minus(other: Point): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	/** Returns this point displaced by the negation of [other] vector. */
	operator fun minus(other: Vec): Point =
		Point(x - other.x, y - other.y, z - other.z)
	
	
	// --- Utility functions ---
	
	/** Converts this point to a [Vec] representing the vector from the origin to this point. */
	fun toVec(): Vec = Vec(x, y, z)
	
	/**
	 * Checks whether two [Point]s are equal component-wise
	 * (within floating point tolerance) through [areClose] fun.
	 */
	fun isClose(other: Point) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Allows to convert [math.Point] to a [String] */
	override fun toString(): String = "Point($x, $y, $z)"
	
}