import kotlin.math.sqrt

data class Vec(
	var x: Float = 0.0f,
	var y: Float = 0.0f,
	var z: Float = 0.0f
) {
	
	// --- Operator overloading ---
	
	operator fun plus(other: Vec): Vec =
		Vec(x + other.x, y + other.y, z + other.z)
	
	operator fun minus(other: Vec): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	operator fun unaryMinus() = Vec(-x, -y, -z)
	
	operator fun times(scalar: Float): Vec =
		Vec(x * scalar, y * scalar, z * scalar)
	
	operator fun times(other: Vec): Vec =
		Vec(x * other.x, y * other.y, z * other.z)
	
	
	// --- Utility functions ---
	
	/** Checks whether two [Vec] are equal through [areClose] fun. */
	fun isCloseVector(other: Vec) = areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Does the cross product between two [Vec]. */
	fun cross(other: Vec): Vec =
		Vec(y * other.z - x * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	
	/** Does the norm of [Vec]. */
	fun norm(): Float = sqrt((x * x + y * y + z * z))
	
	/** Does the squared norm of [Vec]. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	fun normalize(): Vec = times(1 / norm())
	
	//fun vectoNormal()
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Vec = ($x, $y, $z)"
}
