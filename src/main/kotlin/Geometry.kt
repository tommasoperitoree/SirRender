import kotlin.math.sqrt

interface Geometry {
	val x: Float
	val y: Float
	val z: Float
}

/** Checks whether two [Geometry] elements are equal through [areClose] fun. */
fun Geometry.isCloseTo(other: Geometry): Boolean =
	areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)

/** Gives the dot product between two [Geometry] elements. */
infix fun Geometry.dot(other: Geometry): Float = x * other.x + y * other.y + z * other.z

/** Gives the cross product between two [Geometry] elements. */
infix fun Geometry.cross(other: Geometry) =
	Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)

/** Gives the squared norm of [Geometry] elements. */
fun Geometry.squaredNorm(): Float = x * x + y * y + z * z

/** Gives the norm of [Geometry] elements. */
fun Geometry.norm(): Float = sqrt(squaredNorm())


data class Vec(
	override val x: Float = 0.0f,
	override val y: Float = 0.0f,
	override val z: Float = 0.0f
) : Geometry {
	
	// --- Operator overloading ---
	
	operator fun plus(other: Vec): Vec =
		Vec(x + other.x, y + other.y, z + other.z)
	
	operator fun minus(other: Vec): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	operator fun unaryMinus() =
		Vec(-x, -y, -z)
	
	operator fun times(scalar: Float): Vec =
		Vec(x * scalar, y * scalar, z * scalar)
	
	fun normalizeVec(): Vec {
		val n = norm()
		return if (n == 0f) this else times(1f / n)
	}
	
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Vec($x, $y, $z)"
}

data class Point(
	override val x: Float = 0.0f,
	override val y: Float = 0.0f,
	override val z: Float = 0.0f
) : Geometry {
	
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
	override val x: Float = 0.0f,
	override val y: Float = 0.0f,
	override val z: Float = 0.0f
) : Geometry {
	
	/** Does dot product between [Normal] and a [scalar] **/
	operator fun times(scalar: Float): Normal =
		Normal(x * scalar, y * scalar, z * scalar)
	
	/** From a given [Normal] n return -n **/
	operator fun unaryMinus(): Normal =
		Normal(-x, -y, -z)
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Normal($x, $y, $z)"
	
}