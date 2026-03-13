import java.io.InputStream
import java.nio.ByteOrder

// Specific exception for HDRImage class
class InvalidPFMImageFormat(
	message: String = "Invalid PFM format"
) : Exception(message)


// Helper function for hexadecimal array initialization
fun byteArrayOfInts(vararg ints: Int) =
	ByteArray(ints.size) { pos ->
		ints[pos].toByte()
	}


/** The High Dynamic Range Image HDRImage class
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
	
	constructor (stream: InputStream) : this()
	
	constructor (fileName: String) : this()
	
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
	

		//ho scritto una funzione che riceve in pasto l'array ottenuto dalla stream
		//prima bisognarebbe trasformare la stream in un array
		// questa funzione è scritta in stile c++ quando prendi la mano sistemala
	fun writeFloat(ByteArrayOfInts, endianess){
		var: Int j=byteArrayOfInts.size
		if (endianess == ByteOrder.BIG_ENDIAN){
			for (i in 0 until ByteArrayOfInts.size) {
				 var: Float fB += byteArrayOfInts(i) * 2.pow(j)
						j --
			}
			return fB}
		if (endianess == ByteOrder.LITTLE_ENDIAN){
			for (i in 0 until ByteArrayOfInts.size) {
				var: Float fL += byteArrayOfInts(i) * 2.pow(i)
			}
			return fL
		}
	}
	
	
	fun readPFMFile(stream: InputStream): InputStream {
		return stream

	fun readPFMImage(stream: InputStream): HDRImage {
		// magic = readln(stream)
		
		val result = HDRImage(width, height)
		return result

	}
	
	fun parseImgSize(line: String): List<Int> {
		val elements = line.split(" ")
		if (elements.size != 2)
			throw InvalidPFMImageFormat("invalid image size specification")
		
		try {
			//val (width, height) = Pair(elements[0], elements[1]).toInt()
			val width = elements[0].toInt()
			val height = elements[1].toInt()
			if (width < 0 || height < 0) // throw Value Error
				throw IllegalArgumentException()
			return listOf(width, height)
		} catch (e: Exception) {
			throw IllegalArgumentException("invalid width or height specification")
		}
	}
	
	
	// fun parseEndianness(line: String) {
	// 	val value: Float
	// 	try {
	// 		value = line.toFloat()
	// 	} catch (e: IllegalArgumentException) {
	// 		throw IllegalArgumentException("invalid endianness specification")
	// 	}
	//
	// 	if (value > 0) {
	// 		return Endianness.BIG_ENDIAN
	// 	}
	// }
	
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
