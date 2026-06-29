package materials

import kotlin.math.abs

/** Checks if the two [Float]s, [x] and [y] are within [epsilon] of each other. */
fun areClose(x: Float, y: Float, epsilon: Float = 1e-5f) =
	abs(x - y) < epsilon

/** An RGB color
 *
 * @param r The level of red
 * @param g The level of green
 * @param b The level of blue
 */
data class Color(
	var r: Float = 0f,
	var g: Float = 0f,
	var b: Float = 0f
) {
	
	// --- Operator overloading ---
	/** Adds the components of this [Color] to those of [other]. */
	operator fun plus(other: Color): Color =
		Color(r + other.r, g + other.g, b + other.b)
	
	/** Multiplies each component of this [Color] by [scalar]. */
	operator fun times(scalar: Float): Color =
		Color(r * scalar, g * scalar, b * scalar)
	
	/** Multiplies this [Color] by [other] component by component. */
	operator fun times(other: Color): Color =
		Color(r * other.r, g * other.g, b * other.b)
	
	
	// --- Utility functions ---
	
	/** Checks whether two [Color] are equal through [areClose] fun. */
	fun isClose(other: Color, epsilon: Float = 1e-5f) =
		areClose(this.r, other.r, epsilon) && areClose(this.g, other.g, epsilon) && areClose(this.b, other.b, epsilon)
	
	/**
	 * Returns the lightness component as `(max(r,g,b) + min(r,g,b)) / 2`.
	 *
	 * This is the HSL lightness, not a perceptual luminance formula.
	 * Used for Russian roulette termination probability in [core.PathTracer].
	 */
	fun luminosity(): Float {
		val max = maxOf(r, g, b)
		val min = minOf(r, g, b)
		return (max + min) / 2
	}
	
	
	// --- Default data class function overriding ---
	
	/** Checks whether [other] is a [Color] with exactly the same components as this one. */
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as Color
		
		if (r != other.r) return false
		if (g != other.g) return false
		if (b != other.b) return false
		
		return true
	}
	
	/** Computes the hash code from the components of this [Color]. */
	override fun hashCode(): Int {
		var result = r.hashCode()
		result = 31 * result + g.hashCode()
		result = 31 * result + b.hashCode()
		return result
	}
	
	companion object {
		/**
		 * Shared white color constant `(1, 1, 1)`.
		 * Do not mutate fields directly; use `Color(1f, 1f, 1f)` for a fresh instance.
		 */
		val white = Color(1f, 1f, 1f)
		
		/** Shared black color constant `(0, 0, 0)`. See [white] for mutability note. */
		val black = Color(0f, 0f, 0f)
	}
	
}