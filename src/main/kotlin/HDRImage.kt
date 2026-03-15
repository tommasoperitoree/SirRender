import java.io.EOFException
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
	
	// --- Constructors ---
	
	/** Secondary constructor to read an image directly from an [InputStream]. */
	constructor (stream: InputStream) : this()
	
	/** Secondary constructor to read an image from a given [fileName] path. */
	constructor(fileName: String) : this(File(fileName).inputStream())
	
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
	
	/**
	 * Parses the [width] and [height] of an image from the PFM header [line].
	 * * Expects a space-separated string containing exactly two positive integers.
	 * * Returns a [List] containing [width, height].
	 * * Throws [InvalidPFMImageFormat] if the line does not contain exactly two elements.
	 * * Throws [IllegalArgumentException] if the dimensions are negative or not numbers.
	 */
	fun parseImgSize(line: String): List<Int> {
		val elements = line.split(" ")
		if (elements.size != 2) throw InvalidPFMImageFormat("invalid image size specification")
		
		return try {
			val (w, h) = elements.map { it.toInt() }
			require(w >= 0 && h >= 0) { "width or height cannot be negative" }
			listOf(w, h) // This is what the 'try' block evaluates to
		} catch (e: NumberFormatException) {
			throw IllegalArgumentException("invalid width or height specification")
		}
	}
	
	/**
	 * Reads a single line from the [stream] by processing bytes individually.
	 * Stops at the newline character (0x0a) without over-reading into binary data.
	 *
	 * @throws InvalidPFMImageFormat if the End of File (EOF) is reached unexpectedly.
	 */
	fun readLine(stream: InputStream): String {
		val stringBuilder = StringBuilder()
		
		while (true) {
			val byteRead = stream.read() // stream.read() reads a single Byte
			
			// Control 1: Check for the PFM newline (0x0a)
			if (byteRead == 0x0a) break
			// Control 2: Handle End of File
			if (byteRead == -1) {
				// If the stream ends before we hit a newline, it's a malformed PFM
				if (stringBuilder.isEmpty()) {
					throw InvalidPFMImageFormat("Unexpected end of stream")
				}
				break
			}
			// Append the byte as a character
			stringBuilder.append(byteRead.toChar())
		}
		// Trim to handle potential \r (0x0d) characters in Windows-encoded files
		return stringBuilder.toString().trim()
	}
	
	/**
	 * Reads 4 bytes from the [stream] and converts them into a [Float] based on the [endianness].
	 * * Uses [ByteBuffer] to wrap the [ByteArray] and decode the bits according to the
	 * IEEE 754 floating-point standard.
	 */
	fun readFloat(stream: InputStream, endianness: ByteOrder): Float {
		// Read exactly 4 bytes
		val byteChunk = stream.readNBytes(4)
		
		// Check if we actually got 4 bytes
		if (byteChunk.size < 4) {
			throw InvalidPFMImageFormat("Insufficient data: expected 4 bytes for float, but found ${byteChunk.size}")
		}
		// Wrap, set order, and decode
		return ByteBuffer.wrap(byteChunk)
			.order(endianness)
			.getFloat()
	}
	
	/**
	 * Determines the [ByteOrder] from the PFM scale factor# SirRender Documentation
	 * * Returns [ByteOrder.BIG_ENDIAN] for positive values.
	 * * Returns [ByteOrder.LITTLE_ENDIAN] for negative values.
	 * * Throws [InvalidPFMImageFormat] if the value is zero or not a valid number.
	 */
	fun parseEndianness(line: String): ByteOrder {
		// Try to parse, if it fails to be a float, throw the exception immediately
		val value =
			line.trim().toFloatOrNull()
				?: throw InvalidPFMImageFormat("invalid endianness specification: not a number")
		
		return when {
			value > 0 -> BIG_ENDIAN
			value < 0 -> LITTLE_ENDIAN
			else -> throw InvalidPFMImageFormat("invalid endianness specification: cannot be zero")
		}
	}
	
	/**
	 * Writes to byte [stream] the [value] of a float, depending on [order]
	 * It is effectively the inverse of [readFloat].
	 */
	fun writeFloatToStream(stream: OutputStream, value: Float, order: ByteOrder) {
		TODO("complete from suggestions")
		
		// val buffer = ByteBuffer.allocate(4).order(order).putFloat(value)
		// stream.write(buffer.array())
		
		// part suggested from notes
		// val bytes = ByteBuffer.allocate(4).putFloat(value).array() // Big endian
		//
		// if (order == ByteOrder.LITTLE_ENDIAN) {
		// 	bytes.reverse()
		// }
		//
		// stream.write(bytes)
		
		
		//ho scritto una funzione che riceve in pasto l'array ottenuto dalla stream
		//prima bisognarebbe trasformare la stream in un array
		// questa funzione è scritta in stile c++ quando prendi la mano sistemala
		
		// var : Int j = byteArrayOfInts.size
		// if (endianess == ByteOrder.BIG_ENDIAN) {
		// 	for (i in 0 until ByteArrayOfInts.size) {
		// 		var : Float fB += byteArrayOfInts(i) * 2.pow(j)
		// 		j--
		// 	}
		// 	return fB
		// }
		// if (endianess == ByteOrder.LITTLE_ENDIAN) {
		// 	for (i in 0 until ByteArrayOfInts.size) {
		// 		var : Float fL += byteArrayOfInts(i) * 2.pow(i)
		// 	}
		// 	return fL
		// }
	}
	
	/**
	 * Saves the current [HDRImage] to a file.
	 * TODO: Use FileOutputStream and call the stream-based writer.
	 */
	fun writePFMImage(fileName: String, order: ByteOrder = LITTLE_ENDIAN) {
		File(fileName).outputStream().use { out ->
			// TODO: Write header, then loop pixels bottom-up
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
	 * Parses a complete PFM image from the provided [stream].
	 * TODO: Implement the orchestrator logic including the Bottom-Up pixel loop.
	 */
	fun readPFMImage(stream: InputStream): HDRImage {
		// This allows the code to compile, but warns the user it's not ready
		TODO("Follow the integration steps in the ReadMe to complete this function")
	}
	
	companion object {
		fun fromStream(stream: InputStream): HDRImage {
			// call the 'readPFMImage' logic and return a fully formed HDRImage
			return HDRImage().readPFMImage(stream)
		}
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