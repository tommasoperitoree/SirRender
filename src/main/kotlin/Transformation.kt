import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// --- Angle utilities ---

/** Converts this angle from degrees to radians. */
fun Float.toRadians(): Float = (this * PI / 180.0).toFloat()

/** Converts this angle from radians to degrees. */
fun Float.toDegrees(): Float = (this * 180.0 / PI).toFloat()

/** Simple function to return a [Pair] of `(cos(angleDeg), sin(angleDeg))` of the given [angleDeg] in degrees.
 * Converts to radians internally.
 * Used by rotation factory functions to avoid computing [toRadians] twice.
 */
fun angleCosSin(angleDeg: Float): Pair<Float, Float> {
	val rad = angleDeg.toRadians().toDouble()
	return Pair(cos(rad).toFloat(), sin(rad).toFloat())
}


// --- Transformation ---

/**
 * Represents an affine transformation as a pair of 4×4 homogeneous matrices.
 *
 * @property m the transformation matrix
 * @property invm the inverse of [m]
 *
 * Always construct via the [companion object][Transformation.Companion] factory functions:
 * [translation], [scaling], [rotationX], [rotationY], [rotationZ].
 */
data class Transformation(
	val m: HomogMatr4x4 = HomogMatr4x4.identity(),
	val invm: HomogMatr4x4 = HomogMatr4x4.identity()
) {
	
	/**
	 * Returns `true` if `m * invm` is close to the identity matrix.
	 * Use this in tests to verify that a transformation was constructed correctly.
	 */
	fun isConsistent(): Boolean = invm.isInverseOf(m)
	
	
	// --- Operations ---
	
	/**
	 * Returns the composition of this transformation with [other].
	 * Multiplies both matrices and maintains the correct inverse: `(A*B)⁻¹ = B⁻¹ * A⁻¹`.
	 */
	operator fun times(other: Transformation) =
		Transformation(
			m * other.m,
			other.invm * invm  // inverse order for the inverse
		)
	
	/** Applies this transformation to [p], renormalizing when w ≠ 1. */
	operator fun times(p: Point): Point {
		val x = m[0, 0] * p.x + m[0, 1] * p.y + m[0, 2] * p.z + m[0, 3]
		val y = m[1, 0] * p.x + m[1, 1] * p.y + m[1, 2] * p.z + m[1, 3]
		val z = m[2, 0] * p.x + m[2, 1] * p.y + m[2, 2] * p.z + m[2, 3]
		val w = m[3, 0] * p.x + m[3, 1] * p.y + m[3, 2] * p.z + m[3, 3]
		
		return if (areClose(w, 1f)) Point(x, y, z)
		else Point(x / w, y / w, z / w)
	}
	
	/** Applies this transformation to [vec], ignoring translation. */
	operator fun times(vec: Vec): Vec =
		Vec(
			m[0, 0] * vec.x + m[0, 1] * vec.y + m[0, 2] * vec.z,
			m[1, 0] * vec.x + m[1, 1] * vec.y + m[1, 2] * vec.z,
			m[2, 0] * vec.x + m[2, 1] * vec.y + m[2, 2] * vec.z
		)
	
	/** Applies this transformation to [normal] using the inverse transpose of [m]. */
	operator fun times(normal: Normal): Normal =
		Normal(
			invm[0, 0] * normal.x + invm[1, 0] * normal.y + invm[2, 0] * normal.z,
			invm[0, 1] * normal.x + invm[1, 1] * normal.y + invm[2, 1] * normal.z,
			invm[0, 2] * normal.x + invm[1, 2] * normal.y + invm[2, 2] * normal.z
		)
	
	/** Returns the inverse of this transformation, swapping [m] and [invm]. */
	fun inverse(): Transformation =
		Transformation(
			invm,
			m
		)
	
	
	// --- Default data class function overrides ---
	
	override fun toString(): String = "Transformation:\n${m.toMatrixString()}"
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as Transformation
		if (!m.m.contentEquals(other.m.m)) return false
		if (!invm.m.contentEquals(other.invm.m)) return false
		return true
	}
	
	override fun hashCode(): Int {
		var result = m.m.contentHashCode()
		result = 31 * result + invm.m.contentHashCode()
		return result
	}
}


// --- Generators of Transformations ---

/** Returns a [Transformation] encoding translation by [vec]. */
fun translation(vec: Vec): Transformation =
	Transformation(
		HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, vec.x,
				0f, 1f, 0f, vec.y,
				0f, 0f, 1f, vec.z,
				0f, 0f, 0f, 1f
			)
		),
		HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, -vec.x,
				0f, 1f, 0f, -vec.y,
				0f, 0f, 1f, -vec.z,
				0f, 0f, 0f, 1f
			)
		)
	)

/** Returns a [Transformation] encoding scaling of the amount per axis given by [vec]. */
fun scaling(vec: Vec): Transformation =
	Transformation(
		HomogMatr4x4(
			floatArrayOf(
				vec.x, 0f, 0f, 0f,
				0f, vec.y, 0f, 0f,
				0f, 0f, vec.z, 0f,
				0f, 0f, 0f, 1f
			)
		),
		HomogMatr4x4(
			floatArrayOf(
				1f / vec.x, 0f, 0f, 0f,
				0f, 1f / vec.y, 0f, 0f,
				0f, 0f, 1f / vec.z, 0f,
				0f, 0f, 0f, 1f
			)
		)
	)

/**
 * Returns a [Transformation] encoding rotation around the X axis by [angleDeg] degrees.
 * The inverse is the transpose of the rotation matrix (rotation by -[angleDeg]).
 */
fun rotationX(angleDeg: Float): Transformation {
	val (c, s) = angleCosSin(angleDeg)
	
	return Transformation(
		HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, 0f,
				0f, c, -s, 0f,
				0f, s, c, 0f,
				0f, 0f, 0f, 1f
			)
		),
		HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, 0f,
				0f, c, s, 0f,
				0f, -s, c, 0f,
				0f, 0f, 0f, 1f
			)
		)
	)
}

/**
 * Returns a [Transformation] encoding rotation around the Y axis by [angleDeg] degrees.
 * The inverse is the transpose of the rotation matrix (rotation by -[angleDeg]).
 */
fun rotationY(angleDeg: Float): Transformation {
	val (c, s) = angleCosSin(angleDeg)
	
	return Transformation(
		HomogMatr4x4(
			floatArrayOf(
				c, 0f, s, 0f,
				0f, 1f, 0f, 0f,
				-s, 0f, c, 0f,
				0f, 0f, 0f, 1f
			)
		),
		HomogMatr4x4(
			floatArrayOf(
				c, 0f, -s, 0f,
				0f, 1f, 0f, 0f,
				s, 0f, c, 0f,
				0f, 0f, 0f, 1f
			)
		)
	)
}

/**
 * Returns a [Transformation] encoding rotation around the Z axis by [angleDeg] degrees.
 * The inverse is the transpose of the rotation matrix (rotation by -[angleDeg]).
 */
fun rotationZ(angleDeg: Float): Transformation {
	val (c, s) = angleCosSin(angleDeg)
	
	return Transformation(
		HomogMatr4x4(
			floatArrayOf(
				c, -s, 0f, 0f,
				s, c, 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
			)
		),
		HomogMatr4x4(
			floatArrayOf(
				c, s, 0f, 0f,
				-s, c, 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
			)
		)
	)
	
}