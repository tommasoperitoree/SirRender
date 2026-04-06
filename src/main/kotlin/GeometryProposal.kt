import kotlin.math.sqrt

// =============================================================================
// PROPOSAL: Geometry.kt refactor
// Changes from current version:
//   - Vec, Point, Normal → Vec2, Point2, Normal2 (rename to avoid conflicts)
//   - Added sealed interface GeoElement as common parent
//   - var → val in Vec (immutability)
//   - Removed Vec.times(other: Vec) component-wise multiply (not standard)
//   - Added infix dot and cross functions (more idiomatic than crossNN/crossNV)
//   - Fixed isClose functions (were discarding return value)
//   - Fixed Normal.times (was discarding return value)
//   - negNormal() replaced by unaryMinus() operator
//   - Removed import kotlin.times (unused)
//   - squaredNorm() and norm() moved to sealed interface (shared logic)
// =============================================================================

/**
 * Common sealed interface for all 3D geometric primitives.
 *
 * Provides shared [squaredNorm] and [norm] implementations, and enforces
 * that [x], [y], [z] components are present on all subtypes.
 * Being sealed, the compiler knows all subtypes: [Vec2], [Point2], [Normal2].
 */
sealed interface GeoElement {
	val x: Float
	val y: Float
	val z: Float
	
	/** Returns the squared Euclidean norm `x² + y² + z²`. Cheaper than [norm] — prefer this when comparing magnitudes. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	/** Returns the Euclidean norm `√(x² + y² + z²)`. */
	fun norm(): Float = sqrt(squaredNorm())
}

// =============================================================================

/**
 * Represents a 3D vector with components ([x], [y], [z]).
 *
 * Supports standard vector arithmetic: addition, subtraction, scalar multiplication,
 * dot product, cross product, and normalization.
 */
data class Vec2(
	override val x: Float = 0f,
	override val y: Float = 0f,
	override val z: Float = 0f
) : GeoElement {
	
	// --- Operator overloading ---
	
	/** Returns the sum of this vector and [other] vector. */
	operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y, z + other.z)
	
	/** Returns the difference of this vector and [other] vector. */
	operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y, z - other.z)
	
	/** Returns the negation of this vector. */
	operator fun unaryMinus() = Vec2(-x, -y, -z)
	
	/** Returns this vector scaled by [scalar]. */
	operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar, z * scalar)
	
	// --- Utility functions ---
	
	/**
	 * Returns `true` if this vector is component-wise close to [other] vector
	 * within floating point tolerance using [areClose].
	 */
	fun isClose(other: Vec2) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/**
	 * Returns a normalized (unit length) copy of this vector.
	 * @throws ArithmeticException if the vector has zero length
	 */
	fun normalize(): Vec2 = times(1f / norm())
	
	/** Returns the dot product of this vector and [other] vector.
	 * Example:
	 * ```
	 * val a = Vec(1f, 0f, 0f)
	 * val b = Vec(0f, 1f, 0f)
	 * val d = a dot b  // 0.0
	 * ```
	 */
	infix fun dot(other: Vec2): Float = x * other.x + y * other.y + z * other.z
	
	/** Returns the cross product of this vector and [other] vector.
	 * Example:
	 * ```
	 * val a = Vec(1f, 0f, 0f)
	 * val b = Vec(0f, 1f, 0f)
	 * val c = a cross b  // Vec(0.0, 0.0, 1.0)
	 * ```
	 */
	infix fun cross(other: Vec2) = Vec2(
		y * other.z - z * other.y,
		z * other.x - x * other.z,
		x * other.y - y * other.x
	)
	
	override fun toString() = "Vec($x, $y, $z)"
}

// =============================================================================

/**
 * Represents a 3D point with coordinates ([x], [y], [z]).
 *
 * Unlike [Vec2], a point represents a position in space — not a direction.
 * Arithmetic is restricted to geometrically meaningful operations:
 * adding a vector to a point yields a point, subtracting two points yields a vector.
 */
data class Point2(
	override val x: Float = 0f,
	override val y: Float = 0f,
	override val z: Float = 0f
) : GeoElement {
	
	// --- Operator overloading ---
	
	/** Returns the point displaced by [other] vector. */
	operator fun plus(other: Vec2) = Point2(x + other.x, y + other.y, z + other.z)
	
	/** Returns the vector from [other] point to this point. */
	operator fun minus(other: Point2) = Vec2(x - other.x, y - other.y, z - other.z)
	
	/** Returns this point displaced by the negation of [other] vector. */
	operator fun minus(other: Vec2) = Point2(x - other.x, y - other.y, z - other.z)
	
	// --- Utility functions ---
	
	/**
	 * Returns `true` if this point is component-wise close to [other] point
	 * within floating point tolerance using [areClose].
	 */
	fun isClose(other: Point2) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Converts this point to a [Vec2] representing the vector from the origin to this point. */
	fun toVec(): Vec2 = Vec2(x, y, z)
	
	override fun toString() = "Point($x, $y, $z)"
}

// =============================================================================

/**
 * Represents a 3D surface normal with components ([x], [y], [z]).
 *
 * Normals behave differently from vectors under non-uniform transformations —
 * they must be transformed by the inverse transpose of the transformation matrix.
 * See [Transformation.times] for details.
 */
data class Normal2(
	override val x: Float = 0f,
	override val y: Float = 0f,
	override val z: Float = 0f
) : GeoElement {
	
	// --- Operator overloading ---
	
	/** Returns the negation of this normal. */
	operator fun unaryMinus() = Normal2(-x, -y, -z)
	
	/** Returns this normal scaled by [scalar]. */
	operator fun times(scalar: Float) = Normal2(x * scalar, y * scalar, z * scalar)
	
	// --- Utility functions ---
	
	/**
	 * Returns `true` if this normal is component-wise close to [other] normal
	 * within floating point tolerance using [areClose].
	 */
	fun isClose(other: Normal2) =
		areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Returns the dot product of this normal and [other] vector. */
	infix fun dot(other: Vec2): Float = x * other.x + y * other.y + z * other.z
	
	/** Returns the cross product of this normal and [other] normal. */
	infix fun cross(other: Normal2) = Vec2(
		y * other.z - z * other.y,
		z * other.x - x * other.z,
		x * other.y - y * other.x
	)
	
	/** Returns the cross product of this normal and [other] vector. */
	infix fun cross(other: Vec2) = Vec2(
		y * other.z - z * other.y,
		z * other.x - x * other.z,
		x * other.y - y * other.x
	)
	
	override fun toString() = "Normal($x, $y, $z)"
}