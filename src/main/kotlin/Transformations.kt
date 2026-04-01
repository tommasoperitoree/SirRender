/** Check equality of two matrices with fun [areClose] assuming 4x4 */
fun areMatrClose(m1: HomogMatr4x4, m2: HomogMatr4x4): Boolean {
	for (i in 0..4) {
		for (j in 0..4) {
			if (!areClose(m1[i, j], m2[i, j])) {
				return false
			}
		}
	}
	return true
}

/**
 * An affine transformation.
 *
 * [m] is the Homogeneous Matrix (4x4) of the transformation
 * and [invm] its inverse.
 */
data class Transformations(
	var m: HomogMatr4x4,
	var invm: HomogMatr4x4
) {
	/** Check internal consistency of the transformation */
	fun isConsistent(): Boolean {
		val prod =
	}
	
	operator fun times(other: Transformations) {
	
	}
	
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as Transformations
		
		if (!m.contentEquals(other.m)) return false
		if (!invm.contentEquals(other.invm)) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = m.contentHashCode()
		result = 31 * result + invm.contentHashCode()
		return result
	}
	
	override fun toString(): String {
		return "(M=${m.contentToString()})"
	}
}
