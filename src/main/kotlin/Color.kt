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
	
	/** Checks whether two [Color] are equal through [areClose] fun. */
	fun isCloseColor(other: Color): Boolean {
		return areClose(this.r, other.r) &&
				areClose(this.g, other.g) &&
				areClose(this.b, other.b)
	}
	
	
	/* questa funzione non viene usata mai,
	quella sopra è identica ma restituisce un booleano che va bene per AssertTrue
	
	fun isColorClose(other: Color) =
		areClose(r, other.r) && areClose(g, other.g) && areClose(b, other.b)
	*/
	/** Computes the luminosity of a color as
	 *
	 * `L = ( max(r,g,b) + min(r,g,b) ) / 2`
	 */
	fun luminosity(): Float {
		val max = listOf(r, g, b).max()
		val min = listOf(r, g, b).min()
		
		return (max + min) / 2
	}
	
	// --- Default data class function overriding ---
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
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