import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN


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
fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos ->
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
	
	// --- Helper functions ---
	
	/**
	 * Checks if the provided horizontal coordinate [x] and vertical coordinate [y]
	 * fall within the valid bounds of the image. Returns `true` if they are inside.
	 */
	fun validCoordinates(x: Int, y: Int): Boolean =
		x in 0 until width && y in 0 until height
	
	/**
	 * Converts 2D pixel coordinates [x] and [y] into a flat 1D array index.
	 * * This function assumes the image pixel data is stored in memory using
	 * * row-major order (reading the image left-to-right, top-to-bottom).
	 */
	fun pixelOffset(x: Int, y: Int): Int =
		y * width + x
	
	/** Retrieves the [Color] of the pixel at the specified [x] and [y] coordinates. */
	fun getPixel(x: Int, y: Int): Color {
		assert(validCoordinates(x, y))
		return pixels[pixelOffset(x, y)]
	}
	
	/** Updates the [newColor] of the pixel at the specified [x] and [y] coordinates. */
	fun setPixel(x: Int, y: Int, newColor: Color) {
		assert(validCoordinates(x, y))
		pixels[pixelOffset(x, y)] = newColor
	}
	
	/** Saves the current [HDRImage] to a file. */
	
	fun writePFMImage(fileName: String, order: ByteOrder = LITTLE_ENDIAN) {
		File(fileName).outputStream().use {
			val header= "PF\n "width" "height"\n "endianness"\n
			stream.write(header.toByteArray())
			val FTS= writeFloatToStream(OutputStream, Float, LITTLE_ENDIAN)
			
			outStream-> outStream.write(header,FTS)
		}
	}
	
	companion object {
		
		// --- parsing utilities ---
		
		/**
		 * Reads a single line from the [stream] by processing bytes individually.
		 * Stops at the newline character (0x0a) without over-reading into binary data.
		 * @throws InvalidPFMImageFormat if the End of File (EOF) is reached unexpectedly.
		 */
		internal fun readLine(stream: InputStream): String {
			val sb = StringBuilder()
			while (true) {
				val byte = stream.read() // stream.read() reads a single Byte
				if (byte == 0x0a || byte == -1) {
					if (byte == -1 && sb.isEmpty()) throw InvalidPFMImageFormat("Unexpected EOF")
					break
				}
				sb.append(byte.toChar()) // Append the byte as a character
			}
			// Trim to handle potential \r (0x0d) characters in Windows-encoded files
			return sb.toString().trim()
		}
		
		/**
		 * Reads 4 bytes from the [stream] and converts them into a [Float] based on the [endianness].
		 * * Uses [ByteBuffer] to wrap the [ByteArray] and decode the bits.
		 */
		internal fun readFloat(stream: InputStream, endianness: ByteOrder): Float {
			// Read exactly 4 bytes
			val bytes = stream.readNBytes(4)
			// Check if we actually got 4 bytes
			if (bytes.size < 4) throw InvalidPFMImageFormat("Insufficient data: expected 4 bytes for float, but found ${bytes.size}")
			
			// Wrap, set order, and decode
			return ByteBuffer.wrap(bytes).order(endianness).float
		}
		
		/**
		 * Writes to byte [stream] the [value] of a float, depending on [order]
		 * It is effectively the inverse of [readFloat].
		 */
		internal fun writeFloat(stream: OutputStream, value: Float, order: ByteOrder) {
			val bytes = ByteBuffer.allocate(4).order(order).putFloat(value).array()
			stream.write(bytes)
		}
		
		/**
		 * Parses the [width] and [height] of an image from the PFM header [line].
		 * * Expects a space-separated string containing exactly two positive integers.
		 * * Returns a [List] containing [width, height].
		 * * Throws [InvalidPFMImageFormat] if the line does not contain exactly two elements.
		 * * Throws [IllegalArgumentException] if the dimensions are negative or not numbers.
		 */
		internal fun parseImgSize(line: String): Pair<Int, Int> {
			val parts = line.trim().split(" ").filter { it.isNotBlank() }
			if (parts.size != 2) throw InvalidPFMImageFormat("Invalid size line: '$line'")
			
			val w = parts[0].toIntOrNull() ?: throw InvalidPFMImageFormat("Invalid width")
			val h = parts[1].toIntOrNull() ?: throw InvalidPFMImageFormat("Invalid height")
			
			require(w > 0 && h > 0) { "Width and height must be positive" }
			return Pair(w, h)
		}
		
		/**
		 * Determines the [ByteOrder] from the PFM scale factor# SirRender Documentation
		 * * Returns [ByteOrder.BIG_ENDIAN] for positive values.
		 * * Returns [ByteOrder.LITTLE_ENDIAN] for negative values.
		 * * Throws [InvalidPFMImageFormat] if the value is zero or not a valid number.
		 */
		internal fun parseEndianness(line: String): ByteOrder {
			// Try to parse, if it fails to be a float, throw the exception immediately
			val value = line.trim().toFloatOrNull()
				?: throw InvalidPFMImageFormat("invalid endianness specification: '$line'")
			
			return when {
				value > 0 -> BIG_ENDIAN
				value < 0 -> LITTLE_ENDIAN
				else -> throw InvalidPFMImageFormat("invalid endianness specification: cannot be zero")
			}
		}
		
		// --- Public factory functions ---
		
		/** Creates [HDRImage] reading from a PFM formatted [stream] */
		fun readPFM(stream: InputStream): HDRImage {
			if (readLine(stream) != "PF") throw InvalidPFMImageFormat("Invalid magic number")
			
			val (w, h) = parseImgSize(readLine(stream))
			val order = parseEndianness(readLine(stream))
			
			val img = HDRImage(w, h)
			
			for (y in (h - 1) downTo 0) {
				for (x in 0 until w) {
					val (r, g, b) = List(3) { readFloat(stream, order) }
					img.setPixel(x, y, Color(r, g, b))
				}
			}
			return img
		}
		
		/** Creates [HDRImage] reading directly from a PFM formatted file [fileName] */
		fun fromFile(fileName: String): HDRImage =
			File(fileName).inputStream().use { readPFM(it) }
	}
	
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