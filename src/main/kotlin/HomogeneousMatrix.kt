@JvmInline
value class HomogMatr4x4(val m: FloatArray = FloatArray(16)) {
	
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
	
	companion object {
		fun identityMatr4x4() = HomogMatr4x4(
			floatArrayOf(
				1f, 0f, 0f, 0f,
				0f, 1f, 0f, 0f,
				0f, 0f, 1f, 0f,
				0f, 0f, 0f, 1f
			)
		)
	}
}