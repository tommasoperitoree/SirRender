import java.io.InputStream
import java.util.stream.Stream

/** The High Dynamic Range Image class
 *
 * @param width
 * @param height
 * @param pixels
 */
data class HDRImage(
	val width: Int = 1,
	val height: Int = 1,
	var pixels: Array<Color> = Array(width * height) { Color(0.0f, 0.0f, 0.0f) }

) {
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as HDRImage
		
		if (width != other.width) return false
		if (height != other.height) return false
		if (!pixels.contentEquals(other.pixels)) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = width
		result = 31 * result + height
		result = 31 * result + pixels.contentHashCode()
		return result
	}
	
	fun validCoordinates(x: Int, y: Int): Boolean =
		x in 0 until width && y in 0 until height
	
	fun pixelOffset(x: Int, y: Int): Int =
		y * width + x
	
	fun getPixel(x: Int, y: Int): Color {
		assert(validCoordinates(x, y))
		return pixels[pixelOffset(x, y)]
	}
	
	fun setPixel(x: Int, y: Int, newColor: Color) {
		assert(validCoordinates(x, y))
		pixels[pixelOffset(x, y)] = newColor
	}
}
