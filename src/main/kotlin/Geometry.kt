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
	
	operator fun unaryMinus() =
		Vec(-x, -y, -z)
	
	operator fun times(scalar: Float): Vec =
		Vec(x * scalar, y * scalar, z * scalar)
	
	operator fun times(other: Vec): Vec =
		Vec(x * other.x, y * other.y, z * other.z)
	
	
	// --- Utility functions ---
	
	/** Checks whether two [Vec] are equal through [areClose] fun. */
	fun isClose(other: Vec) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Gives the cross product between two [Vec]. */
	
	infix fun cross(other: Vec) = Vec(
		y * other.z - z * other.y,
		z * other.x - x * other.z,
		x * other.y - y * other.x
	)
	/** Gives the squared norm of [Vec]. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	/** Gives the norm of [Vec]. */
	fun norm(): Float = sqrt(squaredNorm())
	
	fun normalize(): Vec = times(1 / norm())
	
	//fun vectoNormal()
	
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Vec($x, $y, $z)"
}


data class Point(
	val x: Float = 0.0f,
	val y: Float = 0.0f,
	val z: Float = 0.0f
) {
	
	fun isClose(other: Point) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** this function return a type [Vec] from a given point
	 * the final vector links the origin of the sdr to the given point
	 */
	fun pointToVec(): Vec = Vec(x, y, z)
	
	operator fun plus(other: Vec): Point = Point(x = x + other.x, y = y + other.y, z = z + other.z)
	
	operator fun minus(other: Point): Vec = Vec(x = x - other.x, y = y - other.y, z = z - other.z)
	
	operator fun minus(other: Vec): Point = Point(x = x - other.x, y = y - other.y, z = z - other.z)
	
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Point($x, $y, $z)"
	
}

data class Normal(
	val x: Float = 0.0f,
	val y: Float = 0.0f,
	val z: Float = 0.0f
) {
	
	fun isClose(other: Normal) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Does dot product between [Normal] and a [scalar] **/
	operator fun times(scalar: Float): Normal =
		Normal(x * scalar, y * scalar, z * scalar)
	
	/** From a given [Normal] n return -n **/
	operator fun unaryMinus(): Normal =
		Normal(-x, -y, -z)
	
	/** Returns the dot product of this normal and [other] vector. */
	infix fun dot(other: Normal): Float = x * other.x + y * other.y + z * other.z
	
	/** Returns the cross product of this normal and [other] normal. */
	infix fun cross(other: Normal) = Vec(
		y * other.z - z * other.y,
		z * other.x - x * other.z,
		x * other.y - y * other.x
	)
	
	/** Returns the cross product of this normal and [other] vector. */
	infix fun cross(other: Vec) = Vec(
		y * other.z - z * other.y,
		z * other.x - x * other.z,
		x * other.y - y * other.x
	)
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Normal($x, $y, $z)"
	
}