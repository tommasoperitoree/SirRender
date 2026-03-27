import kotlin.math.abs

// Checks if two Floats are within epsilon
fun areClose(x: Float, y: Float, epsilon: Float = 1e-5f) =
	abs(x - y) < epsilon

/** An RGB color
 *
 * @param r The level of red
 * @param g The level of green
 * @param b The level of blue
 */
data class Color(
	var r: Float = 0.0f,
	var g: Float = 0.0f,
	var b: Float = 0.0f
) {
	
	// --- Operator overloading ---
	
	operator fun plus(other: Color): Color =
		Color(r + other.r, g + other.g, b + other.b)
	
	operator fun times(scalar: Float): Color =
		Color(r * scalar, g * scalar, b * scalar)
	
	operator fun times(other: Color): Color =
		Color(r * other.r, g * other.g, b * other.b)
	
	
	// --- Utility functions ---
	
	fun isCloseColor(other: Color) =
		areClose(r, other.r) && areClose(g, other.g) && areClose(b, other.b)
	
	/** Calculates luminosity of a single pixel. */
	fun luminosity(): Float {
		val a = maxOf(r, g)
		// val b = listOf(r, g, b).maxOrNull() ?: 0
		val c = minOf(r, g)
		
		return (maxOf(a, b) + minOf(c, b)) / 2
	}
	
	// --- Default data class function overriding ---
	
	override fun equals(other: Any?): Boolean {
		if (this == other) return true
		if (javaClass != other?.javaClass) return false
		
		other as Color
		
		if (r != other.r) return false
		if (g != other.g) return false
		if (b != other.b) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = r.hashCode()
		result = 31 * result + g.hashCode()
		result = 31 * result + b.hashCode()
		return result
	}
	
}