import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.nio.ByteOrder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.ByteOrder.BIG_ENDIAN


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
	 * Reads from a [stream] a single line
	 */
	private fun readLine(stream: InputStream): String {
		val stringBuilder = StringBuilder()
		
		while (true) val byteRead=stream.read() { //stream.read() legge singolarmente ogni byte
			//controllo 1: quando arrivo a 0x0a finisce la stringa
			if (byteRead == 0x0a)
				break
			//controllo 2: il file contiene un errore, qui bisognerebbe mettere un exeption
			if(byteRead==-1){
				if(stringBuilder.isEmpty()){throw EOFException("File is finished before 0x0a")
				}
				break
			}
		}
		return stringBuilder.toString()
		
	
	/**
	 * Reads from [stream] a 4-Byte using ByteBuffer to turn into Float depending on [endianness].
	 * [ByteBuffer] create an array of 4 elements(bytes), the method [wrap] reorganize the array in order to convert it
	 * in to a float (.float) dipending by the endianess
	 */
	fun readFloat(stream: InputStream, endianness: ByteOrder): Float {
		val byteChuck= ByteArray(4) //ho bisogno di un blocco di 4 byte
		val readBytes = stream.read(byteChuck)
		
			if (readBytes == -1) throw EOFException("End of file unexpected")
			if (readBytes<4) throw EOFException("Byte buffer is not made by 4 bytes, file incomplete")
		
		return ByteBuffer.wrap(byteChuck).order(endianness).float
	}
	
	/**
	 * Determines the [ByteOrder] from the PFM scale factor# SirRender Documentation

## 📚 Documentation Guidelines

We use **KDoc** for inline code documentation and **Dokka** to generate our searchable static website. To keep our
codebase clean, modern, and idiomatic, please adhere to the official Kotlin documentation conventions.

### Writing KDoc (The Kotlin Way)

Unlike Java, Kotlin strongly prefers weaving parameters and return values naturally into descriptive sentences rather
than relying on heavy, structured tags at the bottom of the comment block.

* **Use the `[bracket]` syntax:** Link parameters and properties directly in your text.
* **Avoid `@param` and `@return` for simple functions:** Only use these explicit tags if a function has highly complex
  logic, numerous arguments, or strict validation rules that require dedicated paragraphs to explain.
* **Document Exceptions:** Always use the `@throws` tag if your function includes a `require()`, `check()`, or
  explicitly throws an exception.

**✅ Good Example (Idiomatic Kotlin):**

```kotlin
/**
 * Checks if the provided horizontal coordinate [x] and vertical coordinate [y]
 * fall within the valid bounds of the image.
 * * Returns `true` if they are inside the boundaries, or `false` otherwise.
 */
fun validCoordinates(x: Int, y: Int): Boolean
```

### Dokka (Documentation Generator)

Dokka is the native API documentation engine for Kotlin. It automatically generates a searchable website by parsing the
codebase and extracting all `KDoc` comments.

**To generate the `.html` documentation locally:**

1. Open your terminal in the project root and run:
   ```bash
   ./gradlew :dokkaGenerateHtml
2. Once the build finishes, navigate to the generated output at:
   `build/dokka/html/index.html`
3. Right-click the file and open it in your web browser to view the live site.

 [line].
	 * * Returns [ByteOrder.BIG_ENDIAN] for positive values.
	 * * Returns [ByteOrder.LITTLE_ENDIAN] for negative values.
	 * * Throws [InvalidPFMImageFormat] if the value is zero or not a valid number.
	 */
	fun parseEndianness(line: String): ByteOrder {
		// Try to parse, if it fails to be a float, throw the exception immediately
		val value =
			line.trim().toFloatOrNull() ?: throw InvalidPFMImageFormat("invalid endianness specification: not a number")
		
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