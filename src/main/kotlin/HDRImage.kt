import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.Math.pow
import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN
import kotlin.div
import kotlin.math.log10
import kotlin.math.pow

/**
 * Exception thrown when a file or stream does not perfectly match
 * the expected Portable FloatMap (PFM) specification.
 */
class InvalidPFMImageFormat(
	message: String = "Invalid PFM format"
) : Exception(message)

/**
 * Creates a [ByteArray] from a variable number of integer arguments.
 * Useful for inline hexadecimal array initialization.
 */
fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos ->
	ints[pos].toByte()
}

/**
 * Represents a High Dynamic Range (HDR) image stored in row-major order.
 *
 * @property width the number of horizontal pixels
 * @property height the number of vertical pixels
 * @property pixels flat 1D array of [Color] values in row-major order
 */
data class HDRImage(
	val width: Int = 1,
	val height: Int = 1,
	var pixels: Array<Color> = Array(width * height) { Color() }
) {
	
	// --- Helper functions ---
	
	/** Returns `true` if ([x], [y]) falls within the image bounds. */
	fun validCoordinates(x: Int, y: Int): Boolean =
		x in 0 until width && y in 0 until height
	
	/** Converts 2D coordinates ([x], [y]) into a flat 1D array index. */
	fun pixelOffset(x: Int, y: Int): Int =
		y * width + x // It gives me the position in the array
	
	/** Returns the [Color] of the pixel at ([x], [y]). */
	fun getPixel(x: Int, y: Int): Color {
		assert(validCoordinates(x, y))
		return pixels[pixelOffset(x, y)] // e.g. pixels[3]
	}
	
	/* example usage
		val img = HDRImage()
		val color = img.getPixel(1, 2)
		val r: Float = color.r
	*/
	
	/** Sets the pixel at ([x], [y]) to [newColor]. */
	fun setPixel(x: Int, y: Int, newColor: Color) {
		assert(validCoordinates(x, y))
		pixels[pixelOffset(x, y)] = newColor
	}
	
	/** Writes this image to [stream] in PFM format with [order] endianness. */
	fun writePFMImage(stream: OutputStream, order: ByteOrder) {
		stream.write("PF\n".toByteArray())
		stream.write("$width $height\n".toByteArray())
		val endiannessMarker = if (order == LITTLE_ENDIAN) "-1.0" else "1.0"
		stream.write("$endiannessMarker\n".toByteArray())
		
		val writePixel: (Int, Int) -> Unit = { x, y ->
			val c = getPixel(x, y)
			writeFloat(stream, c.r, order)
			writeFloat(stream, c.g, order)
			writeFloat(stream, c.b, order)
		}
		
		(height - 1 downTo 0).forEach { y ->
			(0 until width).forEach { x ->
				writePixel(x, y)
			}
		}
	}
	
	/* old version
	fun writePFMImage(stream: OutputStream, order: ByteOrder = LITTLE_ENDIAN) {
		stream.write("PF\n".toByteArray())
		stream.write("$width $height\n".toByteArray())
		val endiannessMarker = if (order == LITTLE_ENDIAN) "-1.0" else "1.0"
		stream.write("$endiannessMarker\n".toByteArray())
		
		for (y in (height - 1) downTo 0) {
			for (x in 0 until width) {
				val c = getPixel(x, y)
				writeFloat(stream, c.r, order)
				writeFloat(stream, c.g, order)
				writeFloat(stream, c.b, order)
			}
		}
	} // listOf(c.r, c.g, c.b).forEach { writeFloat(stream, it, order) }
	*/
	
	/** Uses the [writePFMImage] fun to save this image to [fileName] in PFM format. */
	fun writePFMFile(fileName: String, order: ByteOrder = LITTLE_ENDIAN) {
		File(fileName).outputStream().use { writePFMImage(it, order) }
	}
	
	/**
	 * Uses the [Color.luminosity] Color fun to calculate the logarithmic mean of the image luminosity.
	 * [delta] is a default ...
	 */
	fun averageLuminosity(delta: Float = 1e-10f): Float {
		var sum = 0.0
		for (pix in pixels) {
			sum += log10(delta + pix.luminosity())
		}
		return 10.0.pow(sum / pixels.size).toFloat()
	}
	
	/**
	 * Renormalizes the luminosity of the image pixels.
	 *
	 * Each pixel value is multiplied by a scaling factor calculated as [factor] / [luminosity].
	 * If no specific luminosity is provided, the function automatically calculates
	 * the average luminosity of the current image calling [averageLuminosity].
	 */
	fun normalizeImage(factor: Float, luminosity: Float? = null) {
		val lum = luminosity ?: averageLuminosity()
		val scale = factor / lum
		for (i in 0 until pixels.size) {
			pixels[i] = pixels[i] * scale
		}
	}
	
	fun clamp(x: Float): Float = x / (1 + x)
	
	fun clampImage() {
		for (i in 0 until pixels.size) {
			pixels[i].r = clamp(pixels[i].r)
			pixels[i].g = clamp(pixels[i].g)
			pixels[i].b = clamp(pixels[i].b)
		}
	}
	
	companion object {
		
		// --- parsing utilities ---
		
		/**
		 * Reads a single line from the [stream] by processing bytes individually.
		 * Stops at the newline character (0x0a) without over-reading into binary data.
		 * @throws InvalidPFMImageFormat if the end of file (EOF) is reached unexpectedly.
		 */
		internal fun readLine(stream: InputStream): String =
			buildString {
				while (true) {
					val byte = stream.read() // reads a single Byte (e.g. 0x50)
					if (byte == 0x0a || byte == -1) {
						if (isEmpty()) throw InvalidPFMImageFormat("Unexpected End of File")
						break
					}
					append(byte.toChar()) // append the byte as a character (e.g. 0x50->'P')
				}
			}.trim() // trim to handle potential \r (0x0d) characters in Windows-encoded files
		
		/**
		 * Reads 4 bytes from the [stream] and decodes them as a [Float] with the given [endianness].
		 * * Uses [ByteBuffer] to wrap the [ByteArray] and decode the bits.
		 * @throws InvalidPFMImageFormat if fewer than 4 bytes are available
		 */
		internal fun readFloat(stream: InputStream, endianness: ByteOrder): Float {
			// Read exactly 4 bytes
			val bytes = stream.readNBytes(4)
			// Check if we actually got 4 bytes
			if (bytes.size < 4) throw InvalidPFMImageFormat("Insufficient data: expected 4 bytes for float, but found ${bytes.size}")
			
			// Wrap, set order, and decode
			return ByteBuffer.wrap(bytes).order(endianness).float
		}
		
		/** Encodes [value] as a 4-byte float with the given [order] and writes it to [stream]. */
		internal fun writeFloat(stream: OutputStream, value: Float, order: ByteOrder) {
			val bytes = ByteBuffer.allocate(4).clear() // always reuses the same buffer by calling .clear()
			bytes.order(order).putFloat(value)
			stream.write(bytes.array())
		}
		
		/**
		 * Parses a PFM size header [line] into a ([width], [height]) [Pair].
		 *
		 * @throws InvalidPFMImageFormat if the line does not contain exactly two strictly positive integers
		 */
		internal fun parseImgSize(line: String): Pair<Int, Int> {
			val parts = line.trim().split(" ").filter { it.isNotBlank() }
			if (parts.size != 2) throw InvalidPFMImageFormat("Invalid size line: '$line'")
			
			val w = parts[0].toIntOrNull() ?: throw InvalidPFMImageFormat("Invalid width")
			val h = parts[1].toIntOrNull() ?: throw InvalidPFMImageFormat("Invalid height")
			
			if (w <= 0 || h <= 0) throw InvalidPFMImageFormat("Width and height must be positive, got: $w x $h")
			return Pair(w, h)
		}
		
		/**
		 * Parses the PFM endianness scale factor from [line].
		 * Negative values map to [LITTLE_ENDIAN], positive to [BIG_ENDIAN].
		 *
		 * @throws InvalidPFMImageFormat if the value is zero or not a valid number
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
		
		/**
		 * Constructs an [HDRImage] by reading a PFM-formatted [stream].
		 *
		 * @param stream the input stream containing PFM data
		 * @return the decoded [HDRImage]
		 * @throws InvalidPFMImageFormat if the stream does not conform to the PFM specification
		 */
		fun fromPFMStream(stream: InputStream): HDRImage {
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
		
		/**
		 * Constructs an [HDRImage] by reading a PFM-formatted file at [fileName].
		 *
		 * @throws InvalidPFMImageFormat if the file does not conform to the PFM specification
		 */
		fun fromPFMFile(fileName: String): HDRImage =
			File(fileName).inputStream().use { fromPFMStream(it) }
		// usage: val img = HDRImage.fromPFMFile("img.pfm")
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
