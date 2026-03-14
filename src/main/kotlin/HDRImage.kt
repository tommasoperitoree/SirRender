import java.io.InputStream
import java.nio.ByteOrder

/**
 * Exception thrown when a file or stream does not perfectly match
 * the expected Portable FloatMap (PFM) specification.
 */
class InvalidPFMImageFormat(
	message: String = "Invalid PFM format"
) : Exception(message)

/**
 * Helper function that creates a [ByteArray] from a variable number of integer arguments.
 * Useful for inline hexadecimal array initialization.
 */
fun byteArrayOfInts(vararg ints: Int) =
	ByteArray(ints.size) { pos ->
		ints[pos].toByte()
	}


/**
 * Represents a High Dynamic Range (HDR) Image.
 *
 * The image is defined by its [width] and [height], while the raw [pixels]
 * are stored efficiently in a flat 1D array using row-major order.
 */
data class HDRImage(
	val width: Int = 1,
	val height: Int = 1,
	var pixels: Array<Color> = Array(width * height) { Color(0.0f, 0.0f, 0.0f) }
) {
	
	// --- Constructors ---
	
	/** Secondary constructor to read an image directly from an [InputStream]. */
	constructor (stream: InputStream) : this()
	
	/** Secondary constructor to read an image from a given [fileName] path. */
	constructor (fileName: String) : this()
	
	
	// --- Helper functions for validity of class ---
	
	/**
	 * Checks if the provided horizontal coordinate [x] and vertical coordinate [y]
	 * fall within the valid bounds of the image. Returns `true` if they are inside.
	 */
	fun validCoordinates(x: Int, y: Int): Boolean =
		x in 0 until width && y in 0 until height
	
	/**
	 * Converts 2D pixel coordinates [x] and [y] into a flat 1D array index.
	 * * This function assumes the image pixel data is stored in memory using
	 * row-major order (reading the image left-to-right, top-to-bottom).
	 */
	fun pixelOffset(x: Int, y: Int): Int =
		y * width + x
	
	/**
	 * Parses the width and height of an image from the PFM header [line].
	 * * Expects a space-separated string containing exactly two positive integers.
	 * * Throws [InvalidPFMImageFormat] if the line does not contain exactly two elements.
	 * * Throws [IllegalArgumentException] if the dimensions are negative or not numbers.
	 */
	fun parseImgSize(line: String): List<Int> {
		val elements = line.split(" ")
		if (elements.size != 2)
			throw InvalidPFMImageFormat("invalid image size specification")
		
		try {
			val (width, height) = elements.map { it.toInt() }
			require(width >= 0 && height >= 0) { "width or height cannot be negative" }
		} catch (e: NumberFormatException) {
			throw IllegalArgumentException("invalid width or height specification")
		}
	}
	
	//ho scritto una funzione che riceve in pasto l'array ottenuto dalla stream
	//prima bisognarebbe trasformare la stream in un array
	// questa funzione è scritta in stile c++ quando prendi la mano sistemala
	fun writeFloat(ByteArrayOfInts, endianess) {
		var : Int j = byteArrayOfInts.size
		if (endianess == ByteOrder.BIG_ENDIAN) {
			for (i in 0 until ByteArrayOfInts.size) {
				var : Float fB += byteArrayOfInts(i) * 2.pow(j)
				j--
			}
			return fB
		}
		if (endianess == ByteOrder.LITTLE_ENDIAN) {
			for (i in 0 until ByteArrayOfInts.size) {
				var : Float fL += byteArrayOfInts(i) * 2.pow(i)
			}
			return fL
		}
	}
	
	// --- Class modifier functions ---
	
	/**
	 * Retrieves the [Color] of the pixel at the specified [x] and [y] coordinates.
	 */
	fun getPixel(x: Int, y: Int): Color {
		assert(validCoordinates(x, y))
		return pixels[pixelOffset(x, y)]
	}
	
	/**
	 * Updates the [newColor] of the pixel at the specified [x] and [y] coordinates.
	 */
	fun setPixel(x: Int, y: Int, newColor: Color) {
		assert(validCoordinates(x, y))
		pixels[pixelOffset(x, y)] = newColor
	}
	
	/**
	 * Parses a complete PFM image from the provided [stream] and returns a new [HDRImage].
	 */
	fun readPFMImage(stream: InputStream): HDRImage {
		// magic = readln(stream)
		
		val result = HDRImage(width, height)
		return result
	}
	
	// fun parseEndianness(line: String) {
	//     val value: Float
	//     try {
	//        value = line.toFloat()
	//     } catch (e: IllegalArgumentException) {
	//        throw IllegalArgumentException("invalid endianness specification")
	//     }
	//
	//     if (value > 0) {
	//        return Endianness.BIG_ENDIAN
	//     }
	// }
	
	
	// --- Default data class function overriding ---
	
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
	
}