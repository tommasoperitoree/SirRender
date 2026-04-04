@JvmInline
value class HomogMatr4x4(
	val m: FloatArray = FloatArray(16)
) {
	
	operator fun get(row: Int, col: Int) = m[row * 4 + col]
	
	operator fun set(row: Int, col: Int, value: Float) {
		m[row * 4 + col] = value
	}
	
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
	
	/** Check equality of the matrix with the [other] matrix through fun [areClose] assuming 4x4 */
	fun isMatrClose(other: HomogMatr4x4): Boolean {
		for (i in 0..3) {
			for (j in 0..3) {
				if (!areClose(this[i, j], other[i, j])) {
					return false
				}
			}
		}
		return true
	}
	
	/** Returns true if [m] * [other] is close to the identity matrix. */
	fun isInverseOf(other: HomogMatr4x4): Boolean = (this * other).isMatrClose(identity())
	
	companion object {
		fun identity() = HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, 0f,
				0f, 1f, 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
			)
		)
	}
	
	/** Prints [HomogMatr4x4] as a formatted 4x4 matrix. */
	fun toMatrixString(): String =
		(0..3).joinToString("\n") { row ->
			(0..3).joinToString(" ") { col ->
				"%8.4f".format(this[row, col])
			}
		}
}