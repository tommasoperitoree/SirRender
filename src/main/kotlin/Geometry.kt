import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.times

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
	fun isVectorClose(other: Vec) = areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Gives the cross product between two [Vec]. */
	fun cross(other: Vec): Vec =
		Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	
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
	
	
	fun isPointClose(other: Point) {
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	}
	
	/** this function return a type [Vec] from a given point
	 * the final vector links the origin of the sdr to the given point
	 */
	fun pointToVec(): Vec {
		return Vec(x, y, z)
	}
	
	operator fun plus(other: Vec): Point {
		return Point(x = x + other.x, y = y + other.y, z = z + other.z)
	}
	
	operator fun minus(other: Point): Vec {
		return Vec(x = x - other.x, y = y - other.y, z = z - other.z)
	}
	
	operator fun minus(other: Vec): Point {
		return Point(x = x - other.x, y = y - other.y, z = z - other.z)
	}
	
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Point($x, $y, $z)"
	
}

data class Normal(
	val x: Float = 0.0f,
	val y: Float = 0.0f,
	val z: Float = 0.0f
) {
	
	
	fun isNormalClose(other: Normal) {
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	}
	
	/** Does dot product between [Normal] and a [scalar] **/
	operator fun times(scalar: Float) {
		Normal(x = x * scalar, y = y * scalar, z = z * scalar)
	}
	
	/** From a given [Normal] n return -n **/
	fun negNormal(): Normal {
		return Normal(x = -x, y = -y, z = -z)
	}
	
	
	/** Does the cross product between [Normal] and [Normal]**/
	fun crossNN(other: Normal): Vec {
		return Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	}
	
	/** Does the cross product between [Normal] and [Vec]**/
	fun crossNV(other: Vec): Vec {
		return Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	}
	
	/** Does dot product between [Normal] and [Vec]**/
	fun dotProductNV(other: Vec) = x * other.x + y * other.y + z * other.z
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Normal($x, $y, $z)"
	
}