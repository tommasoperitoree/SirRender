/**
 * A 4×4 homogeneous matrix stored as a flat row-major [FloatArray].
 *
 * Using `@JvmInline value class` wrapping a [FloatArray] gives zero
 * heap allocation overhead — the JVM sees only the raw array at runtime.
 *
 * Element access uses row-major indexing: `m[row * 4 + col]`.
 */
@JvmInline
value class HomogMatr4x4(
	val m: FloatArray = FloatArray(16)
) {
	
	/**
	 * Returns the element at ([row], [col]).
	 *
	 * Example:
	 * ```
	 * val identity = HomogMatr4x4.identity()
	 * val diag = identity[0, 0]  // 1.0
	 * ```
	 */
	operator fun get(row: Int, col: Int) = m[row * 4 + col]
	
	/** Sets the element at ([row], [col]) to [value]. */
	operator fun set(row: Int, col: Int, value: Float) {
		m[row * 4 + col] = value
	}
	
	/**
	 * Returns the matrix product of this matrix and [other].
	 *
	 * Example:
	 * ```
	 * val result = matA * matB
	 * ```
	 */
	operator fun times(other: HomogMatr4x4): HomogMatr4x4 {
		val result = HomogMatr4x4()
		for (row in 0..3) {
			for (col in 0..3) {
				var sum = 0f
				for (k in 0..3) sum += m[row * 4 + k] * other.m[k * 4 + col]
				result.m[row * 4 + col] = sum
			}
		}
		return result
	}
	
	/**
	 * Checks whether two [HomogMatr4x4] are equal component-wise
	 * (within floating point tolerance) through [areClose] fun.
	 */
	fun isClose(other: HomogMatr4x4): Boolean {
		for (i in 0..3) {
			for (j in 0..3) {
				if (!areClose(this[i, j], other[i, j])) {
					return false
				}
			}
		}
		return true
	}
	
	/**
	 * Returns `true` if `[m] * [other]` is close to the identity matrix.
	 * Used by [Transformation.isConsistent] to verify correctness of the inverse.
	 */
	fun isInverseOf(other: HomogMatr4x4): Boolean = (this * other).isClose(identity())
	
	companion object {
		/**
		 * Returns the 4×4 identity matrix.
		 *
		 * Example:
		 * ```
		 * val I = HomogMatr4x4.identity()
		 * ```
		 */
		fun identity() = HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, 0f,
				0f, 1f, 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
			)
		)
	}
	
	/**
	 * Returns this matrix formatted as a human-readable 4×4 grid.
	 * Defined as internal to avoid direct use, one should only use
	 * the [Transformation.toString] which overrides the printing of [Transformation] types.
	 */
	internal fun toMatrixString(): String =
		(0..3).joinToString("\n") { row ->
			(0..3).joinToString(" ") { col ->
				"%8.4f".format(this[row, col])
			}
		}
}