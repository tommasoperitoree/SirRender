import kotlin.math.sqrt

/**
 * Represents a 3D vector with components ([x], [y], [z]).
 *
 * Supports standard vector arithmetic: addition, subtraction, scalar multiplication,
 * dot product, cross product, and normalization.
 */
data class Vec(
	val x: Float = 0f,
	val y: Float = 0f,
	val z: Float = 0f
) {
	
	// --- Operator overloading ---
	
	/** Returns the sum of this vector and [other] vector. */
	operator fun plus(other: Vec): Vec =
		Vec(x + other.x, y + other.y, z + other.z)
	
	/** Returns the difference of this vector and [other] vector. */
	operator fun minus(other: Vec): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	/** Returns the negation of this vector. */
	operator fun unaryMinus() =
		Vec(-x, -y, -z)
	
	/** Returns this vector scaled by [scalar]. */
	operator fun times(scalar: Float): Vec =
		Vec(x * scalar, y * scalar, z * scalar)
	
	
	// --- Utility functions ---
	
	/**
	 * Checks whether two [Vec]s are equal component-wise
	 * (within floating point tolerance) through [areClose] fun.
	 */
	fun isClose(other: Vec) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Gives the squared norm of [Vec]. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	/** Gives the norm of [Vec]. */
	fun norm(): Float = sqrt(squaredNorm())
	
	/** Returns the dot product of this vector and [other] vector.
	 * Example:
	 * ```
	 * val a = Vec(1f, 0f, 0f)
	 * val b = Vec(0f, 1f, 0f)
	 * val d = a dot b  // 0.0
	 * ```
	 */
	infix fun dot(other: Vec): Float = x * other.x + y * other.y + z * other.z
	
	/** Returns the cross product of this vector and [other] vector which is a [Normal]
	 * Example:
	 * ```
	 * val a = Vec(1f, 0f, 0f)
	 * val b = Vec(0f, 1f, 0f)
	 * val c = a cross b  // Vec(0.0, 0.0, 1.0)
	 * ```
	 */
	infix fun cross(other: Vec) =
		Normal(
			y * other.z - z * other.y,
			z * other.x - x * other.z,
			x * other.y - y * other.x
		)
	
	/**
	 * Returns a normalized (unit length) copy of this [Normal].
	 * @throws ArithmeticException if the vector has zero length
	 */
	fun normalize(): Vec = times(1 / norm())
	
	/** Returns the [Normal] with components of [Vec] */
	fun toNormal(): Normal =
		Normal(x, y, z)
	
	override fun toString(): String = "Vec($x, $y, $z)"
	
}

// --- Basic vector constructors ---

fun vecX() =
	Vec(1f, 0f, 0f)

fun vecY() =
	Vec(0f, 1f, 0f)

fun vecZ() =
	Vec(0f, 0f, 1f)


/**
 * Represents a 3D point with coordinates ([x], [y], [z]).
 *
 * Unlike [Vec], a point represents a position in space — not a direction.
 * Arithmetic is restricted to geometrically meaningful operations:
 * adding a vector to a point yields a point, subtracting two points yields a vector.
 */
data class Point(
	val x: Float = 0f,
	val y: Float = 0f,
	val z: Float = 0f
) {
	
	// --- Operator overloading ---
	
	/** Returns the point displaced by [other] vector. */
	operator fun plus(other: Vec): Point =
		Point(x + other.x, y + other.y, z + other.z)
	
	/** Returns the vector from [other] point to this point. */
	operator fun minus(other: Point): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	/** Returns this point displaced by the negation of [other] vector. */
	operator fun minus(other: Vec): Point =
		Point(x - other.x, y - other.y, z - other.z)
	
	
	// --- Utility functions ---
	
	/** Converts this point to a [Vec] representing the vector from the origin to this point. */
	fun toVec(): Vec = Vec(x, y, z)
	
	/**
	 * Checks whether two [Point]s are equal component-wise
	 * (within floating point tolerance) through [areClose] fun.
	 */
	fun isClose(other: Point) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	
	override fun toString(): String = "Point($x, $y, $z)"
	
}


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
	fun normalize(): Normal = times(1 / norm())
	
	
	override fun toString(): String = "Normal($x, $y, $z)"
	
}

data class Vec2d(
	var x: Float = 0f,
	var y: Float = 0f,
) {
	fun isClose(other: Vec2d) = areClose(x, other.x) && areClose(y, other.y)
}