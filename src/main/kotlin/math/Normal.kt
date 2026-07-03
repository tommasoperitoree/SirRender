package math

import materials.areClose
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.withSign


/**
 * Represents a 3D surface normal with components ([x], [y], [z]).
 *
 * Normals behave differently from vectors under non-uniform transformations —
 * they must be transformed by the inverse transpose of the transformation matrix.
 * See [Transformation.times] for details.
 */
data class Normal(
	val x: Float = 0f,
	val y: Float = 0f,
	val z: Float = 0f
) {
	
	// --- Operator overloading ---
	
	/** Returns the negation of this normal. */
	operator fun unaryMinus(): Normal =
		Normal(-x, -y, -z)
	
	/** Returns this normal scaled by [scalar]. */
	operator fun times(scalar: Float): Normal =
		Normal(x * scalar, y * scalar, z * scalar)
	
	
	// --- Utility functions ---
	
	/**
	 * Checks whether two [Normal]s are equal component-wise
	 * (within floating point tolerance) through [areClose] fun.
	 */
	fun isClose(other: Normal) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Returns the dot product of this normal and [other] vector. */
	infix fun dot(other: Vec): Float = x * other.x + y * other.y + z * other.z
	
	/** Gives the squared norm of [Normal]. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	/** Gives the norm of [Normal]. */
	fun norm(): Float = sqrt(squaredNorm())
	
	/**
	 * Returns a normalized (unit length) copy of this [Normal].
	 * @throws ArithmeticException if the vector has zero length
	 */
	fun normalize(): Normal {
		val currentNorm = norm()
		if (currentNorm == 0f) throw ArithmeticException("Cannot normalize a normal with zero length")
		return times(1f / currentNorm)
	}
	
	/** Returns the [Vec] with components of [Normal] */
	fun toVec(): Vec =
		Vec(x, y, z)
	
	override fun toString(): String = "Normal($x, $y, $z)"
	
}


/**
 * Builds an orthonormal basis (ONB) with [normal] as the Z axis (e3).
 *
 * Uses the Frisvad/Duff method for numerically stable construction at any orientation,
 * including at the poles (normal ≈ ±Z). Returns (e1, e2, e3) where e3 = [normal].
 *
 * @throws IllegalArgumentException if [normal] is not unit length (tolerance 1e-3).
 */
fun createOnbFromZ(normal: Normal): Triple<Vec, Vec, Vec> {
	
	require(abs(normal.norm() - 1f) < 1e-3f) { "Normals need to be normalized when creating onb" }
	
	val sign: Float = 1f.withSign(normal.z)
	
	val a = -1f / (sign + normal.z)
	val b = normal.x * normal.y * a
	
	val e1 = Vec(1.0f + sign * normal.x * normal.x * a, sign * b, -sign * normal.x)
	val e2 = Vec(b, sign + normal.y * normal.y * a, -normal.y)
	
	return Triple(e1, e2, normal.toVec())
}