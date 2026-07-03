package math

import materials.areClose
import kotlin.math.sqrt

/**
 * Represents a 3D vector with components ([x], [y], [z]).
 *
 * Supports standard vector arithmetic: addition, subtraction, scalar multiplication,
 * dot product, cross product, and normalization.
 */
data class Vec(
	val x: Float = 0f,
	val y: Float = 0f,
	val z: Float = 0f
) {
	
	// --- Operator overloading ---
	
	/** Returns the sum of this vector and [other] vector. */
	operator fun plus(other: Vec): Vec =
		Vec(x + other.x, y + other.y, z + other.z)
	
	/** Returns the difference of this vector and [other] vector. */
	operator fun minus(other: Vec): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	/** Returns the negation of this vector. */
	operator fun unaryMinus() =
		Vec(-x, -y, -z)
	
	/** Returns this vector scaled by [scalar]. */
	operator fun times(scalar: Float): Vec =
		Vec(x * scalar, y * scalar, z * scalar)
	
	
	// --- Utility functions ---
	
	/**
	 * Checks whether two [Vec]s are equal component-wise
	 * (within floating point tolerance) through [areClose] fun.
	 */
	fun isClose(other: Vec) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Gives the squared norm of [Vec]. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	/** Gives the norm of [Vec]. */
	fun norm(): Float = sqrt(squaredNorm())
	
	/** Returns the dot product of this vector and [other] vector.
	 * Example:
	 * ```
	 * val a = math.Vec(1f, 0f, 0f)
	 * val b = math.Vec(0f, 1f, 0f)
	 * val d = a dot b  // 0.0
	 * ```
	 */
	infix fun dot(other: Vec): Float = x * other.x + y * other.y + z * other.z
	
	/** Returns the cross product of this vector and [other] vector which is a [Normal]
	 * Example:
	 * ```
	 * val a = math.Vec(1f, 0f, 0f)
	 * val b = math.Vec(0f, 1f, 0f)
	 * val c = a cross b  // math.Vec(0.0, 0.0, 1.0)
	 * ```
	 */
	infix fun cross(other: Vec) =
		Normal(
			y * other.z - z * other.y,
			z * other.x - x * other.z,
			x * other.y - y * other.x
		)
	
	/**
	 * Returns a normalized (unit length) copy of this [Normal].
	 * @throws ArithmeticException if the vector has zero length.
	 */
	fun normalize(): Vec {
		val currentNorm = norm()
		if (currentNorm == 0f) throw ArithmeticException("Cannot normalize a vector with zero length")
		return times(1f / currentNorm)
	}
	
	/** Returns the [Normal] with components of [Vec] */
	fun toNormal(): Normal =
		Normal(x, y, z)
	
	override fun toString(): String = "Vec($x, $y, $z)"
	
}

// --- Basic vector constructors ---

/** Returns the unit vector along the X axis: (1, 0, 0). */
fun vecX() =
	Vec(1f, 0f, 0f)

/** Returns the unit vector along the Y axis: (0, 1, 0). */
fun vecY() =
	Vec(0f, 1f, 0f)

/** Returns the unit vector along the Z axis: (0, 0, 1). */
fun vecZ() =
	Vec(0f, 0f, 1f)