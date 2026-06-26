import HDRImage.Companion.parseEndianness
import HDRImage.Companion.readFloat
import HDRImage.Companion.writeFloat
import HDRImage.Companion.readLine
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN
import javax.imageio.ImageIO
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue


class HDRImageTest {
	
	val width: Int = 10
	val height: Int = 10
	var img = HDRImage(width, height)
	
	// generic Height and Width for testing
	val x: Int = 2
	val y: Int = 6
	val eps: Float = 1e-5f
	
	// pfm reference files val declaration
	val referenceBE = byteArrayOfInts(
		0x50, 0x46, 0x0a, 0x33, 0x20, 0x32, 0x0a, 0x31, 0x2e, 0x30, 0x0a, 0x42,
		0xc8, 0x00, 0x00, 0x43, 0x48, 0x00, 0x00, 0x43, 0x96, 0x00, 0x00, 0x43,
		0xc8, 0x00, 0x00, 0x43, 0xfa, 0x00, 0x00, 0x44, 0x16, 0x00, 0x00, 0x44,
		0x2f, 0x00, 0x00, 0x44, 0x48, 0x00, 0x00, 0x44, 0x61, 0x00, 0x00, 0x41,
		0x20, 0x00, 0x00, 0x41, 0xa0, 0x00, 0x00, 0x41, 0xf0, 0x00, 0x00, 0x42,
		0x20, 0x00, 0x00, 0x42, 0x48, 0x00, 0x00, 0x42, 0x70, 0x00, 0x00, 0x42,
		0x8c, 0x00, 0x00, 0x42, 0xa0, 0x00, 0x00, 0x42, 0xb4, 0x00, 0x00
	)
	
	val referenceLE = byteArrayOfInts(
		0x50, 0x46, 0x0a, 0x33, 0x20, 0x32, 0x0a, 0x2d, 0x31, 0x2e, 0x30, 0x0a,
		0x00, 0x00, 0xc8, 0x42, 0x00, 0x00, 0x48, 0x43, 0x00, 0x00, 0x96, 0x43,
		0x00, 0x00, 0xc8, 0x43, 0x00, 0x00, 0xfa, 0x43, 0x00, 0x00, 0x16, 0x44,
		0x00, 0x00, 0x2f, 0x44, 0x00, 0x00, 0x48, 0x44, 0x00, 0x00, 0x61, 0x44,
		0x00, 0x00, 0x20, 0x41, 0x00, 0x00, 0xa0, 0x41, 0x00, 0x00, 0xf0, 0x41,
		0x00, 0x00, 0x20, 0x42, 0x00, 0x00, 0x48, 0x42, 0x00, 0x00, 0x70, 0x42,
		0x00, 0x00, 0x8c, 0x42, 0x00, 0x00, 0xa0, 0x42, 0x00, 0x00, 0xb4, 0x42
	)
	
	@Test
	fun `test dimension`() {
		assertEquals(width, img.width)
		assertEquals(height, img.height)
		assertEquals(height * width, img.pixels.size)
	}
	
	@Test
	fun `test validCoordinates function`() {
		assertTrue(img.validCoordinates(x, y))
		assertTrue(img.validCoordinates(width - 1, height - 1))
		
		assertFalse(img.validCoordinates(- 1, 0))
		assertFalse(img.validCoordinates(width, height)) // out of bounds (exclusive)
		assertFalse(img.validCoordinates(width, 0))
		assertFalse(img.validCoordinates(0, height))
	}
	
	@Test
	fun `test pixelOffset function`() {
		assertEquals(y * width + x, img.pixelOffset(x, y))
		assertEquals(0, img.pixelOffset(0, 0))
		assertEquals(width - 1, img.pixelOffset(width - 1, 0))
		assertEquals(width, img.pixelOffset(0, 1))
	}
	
	//Here we test eather set & get pixel in  one test
	@Test
	fun `test get-set-Pixel`() {
		val img = HDRImage(width, height)
		val refColor = Color(0.5f, 0.1f, 0.2f)
		img.setPixel(4, 5, refColor)
		assertTrue(refColor.isClose(img.getPixel(4, 5)))
	}
	
	@Test
	fun `test writePFImage`() {
		val outputStream = ByteArrayOutputStream()
		
		img.writePFMImage(outputStream, LITTLE_ENDIAN)
		
		ByteArrayInputStream(outputStream.toByteArray()).use { line ->
			assertEquals("PF", HDRImage.readLine(line))
			assertEquals("$width $height", HDRImage.readLine(line))
			assertEquals("-1.0", HDRImage.readLine(line))
		}
	}
	
	@Test
	fun `test writePFMFile`() {
		
		val img = HDRImage(2, 2)
		img.setPixel(0, 0, Color(1f, 0f, 0f))
		img.setPixel(1, 0, Color(0f, 1f, 0f))
		img.setPixel(0, 1, Color(0f, 0f, 1f))
		img.setPixel(1, 1, Color(1f, 1f, 1f))
		
		// Write the image to a temporary file, then read it back to verify that each pixel's color matches
		val tempFile = File.createTempFile("temp", ".pfm")
		//use try & finally to open and close the temporary file at the end of test even if there are some exception
		try {
			img.writePFMFile(tempFile.path) //path extract from file the path of File that is a String
			val imgRead = tempFile.inputStream().use { HDRImage.fromPFMStream(it) }
			assertEquals(img.width, imgRead.width)
			assertEquals(img.height, imgRead.height)
			for (y in 0 until 2) {
				for (x in 0 until 2) {
					assertTrue { imgRead.getPixel(x, y).isClose(img.getPixel(x, y)) }
				}
			}
		} finally {
			tempFile.delete()
		}
	}
	
	@Test
	fun `test averageLuminosityDelta`() {
		img = HDRImage(2, 1)
		img.setPixel(0, 0, Color(0f, 0f, 0f))
		img.setPixel(1, 0, Color(100f, 100f, 100f))
		
		assertTrue { areClose(sqrt(101f), img.averageLuminosity(1f), 1e-5f) }
	}
	
	@Test
	fun `test normalizeImage`() {
		
		img = HDRImage(width = 2, height = 1)
		img.setPixel(0, 0, Color(5.0f, 10.0f, 15.0f))
		img.setPixel(1, 0, Color(500.0f, 1000.0f, 1500.0f))
		
		img.normalizeImage(
			factor = 100f,
			luminosity = 1000f
		)
		
		assertTrue(
			img.getPixel(0, 0)
				.isClose(Color(0.5f, 1f, 1.5f))
		)
		
		assertTrue(img.getPixel(1, 0).isClose(Color(50f, 100f, 150f)))
	}
	
	@Test
	fun `test clampImage`(){
		val img = HDRImage(1, 1)
		img.setPixel(0, 0, Color(1.0f, 3.0f, 9.0f))
		img.clampImage()
		
		val result = img.getPixel(0, 0)
		assertEquals(0.5f, result.r, 1e-6f)
		assertEquals(0.75f, result.g, 1e-6f)
		assertEquals(0.9f, result.b, 1e-6f)
	}
	
	@Test
	fun `test writeLDRImage`() {
		val img = HDRImage(2, 2)
		img.setPixel(0, 0, Color(1f, 0f, 0f))
		img.setPixel(1, 0, Color(0f, 1f, 0f))
		img.setPixel(0, 1, Color(0f, 0f, 1f))
		img.setPixel(1, 1, Color(1f, 1f, 1f))
		
		//write on an output stream
		val byteOut = ByteArrayOutputStream()
		
		img.writeLDRImage(byteOut, "png", 1f)
		
		//Read the image from the stream e check  the dimension of the written image and the colors
		val imgRead = ImageIO.read(ByteArrayInputStream(byteOut.toByteArray()))
		
		assertNotNull(imgRead)
		assertEquals(img.width, imgRead.width)
		assertEquals(img.height, imgRead.height)
		//and is operation bit-bit (1 if they are the same else 0) is useful for getRGB format
		assertEquals(0xFF0000, imgRead.getRGB(0, 0) and 0xFFFFFF)
		assertEquals(0x00FF00, imgRead.getRGB(1, 0) and 0xFFFFFF)
		assertEquals(0x0000FF, imgRead.getRGB(0, 1) and 0xFFFFFF)
		assertEquals(0xFFFFFF, imgRead.getRGB(1, 1) and 0xFFFFFF)
	}
	
	//--- test on parsing utilities ---
	
	@Test
	fun `test readLine`() {
		val sb = "Hello\nWorld"
		val line: InputStream = sb.byteInputStream()
		assertEquals("Hello", readLine(line))
		assertEquals("World", readLine(line))
		val exception = assertThrows<InvalidPFMImageFormat> { readLine(line) }
		assertEquals("Unexpected End of File", exception.message)
	}
	
	@Test
	fun `test readFloat`() {
		val stream = ByteArrayInputStream(byteArrayOfInts(0x00, 0x00, 0x00))
		
		assertThrows<InvalidPFMImageFormat> {
			readFloat(stream, LITTLE_ENDIAN)
		}
		// 1.0f in little endian = 0x00 0x00 0x80 0x3F
		//Kotlin use signed byte so 0x80=128, and it is out of range (-128/127), 0x80.toByte()=-128 that is permitted,
		//ByteArray doesn't look at the sign so -128 & 128 are equals
		val bytesLE = byteArrayOf(0x00, 0x00, 0x80.toByte(), 0x3F)
		val streamLE = ByteArrayInputStream(bytesLE)
		assertEquals(1f, readFloat(streamLE, LITTLE_ENDIAN), eps)
		
		// 1.0f in big endian = 0x3F 0x80 0x00 0x00
		val bytesBE = byteArrayOf(0x3F, 0x80.toByte(), 0x00, 0x00)
		val streamBE = ByteArrayInputStream(bytesBE)
		assertEquals(1f, readFloat(streamBE, BIG_ENDIAN), eps)
	}
	
	@Test
	fun `test writeFloat`() {
		val byteOutLE = ByteArrayOutputStream()
		writeFloat(byteOutLE, 1f, LITTLE_ENDIAN)
		assertContentEquals(byteArrayOf(0x00, 0x00, 0x80.toByte(), 0x3F), byteOutLE.toByteArray())
		
		val byteOutBE = ByteArrayOutputStream()
		writeFloat(byteOutBE, 1f, BIG_ENDIAN)
		assertContentEquals(byteArrayOf(0x3F, 0x80.toByte(), 0x00, 0x00), byteOutBE.toByteArray())
	}
	
	// --- test on public factory function ---
	
	@Test
	fun `test parseEndianness`() {
		assertEquals(BIG_ENDIAN, HDRImage.parseEndianness("1.0"))
		assertEquals(LITTLE_ENDIAN, HDRImage.parseEndianness("-3.0"))
		assertThrows(InvalidPFMImageFormat::class.java) { HDRImage.parseEndianness("0.0") }
		assertThrows(InvalidPFMImageFormat::class.java) { HDRImage.parseEndianness("ABC") }
		assertThrows(InvalidPFMImageFormat::class.java) { parseEndianness("NaN") }
		assertThrows(InvalidPFMImageFormat::class.java) { parseEndianness("Infinity") }
		
	}
	
	@Test
	fun `test parseImgSize`() {
		assertEquals(Pair(3, 2), HDRImage.parseImgSize("3 2"))
		assertThrows(InvalidPFMImageFormat::class.java) {
			HDRImage.parseImgSize("1 2 3")         // too many args
		}
		assertThrows(InvalidPFMImageFormat::class.java) {
			HDRImage.parseImgSize("-1 2")           // negative dimension
		}
		assertThrows(InvalidPFMImageFormat::class.java) {
			HDRImage.parseImgSize("width height")   // not integers
		}
		assertThrows(InvalidPFMImageFormat::class.java) { // not zero dimensions
			HDRImage.parseImgSize("0 2")
		}
		assertThrows(InvalidPFMImageFormat::class.java) {
			HDRImage.parseImgSize("1 0")
		}
		assertThrows(InvalidPFMImageFormat::class.java) {
			HDRImage.parseImgSize("9999999999999999 1") // not values outside integer range
		}
	}
	
	@Test
	fun `test constructor fromPFMStream`() {
		for (referenceBytes in arrayOf(referenceBE, referenceLE)) {
			img = HDRImage.fromPFMStream(ByteArrayInputStream(referenceBytes))
			
			assertEquals(3, img.width)
			assertEquals(2, img.height)
			
			assertTrue(img.getPixel(0, 0).isClose(Color(1.0e1f, 2.0e1f, 3.0e1f)))
			assertTrue(img.getPixel(1, 0).isClose(Color(4.0e1f, 5.0e1f, 6.0e1f)))
			assertTrue(img.getPixel(2, 0).isClose(Color(7.0e1f, 8.0e1f, 9.0e1f)))
			
			assertTrue(img.getPixel(0, 1).isClose(Color(1.0e2f, 2.0e2f, 3.0e2f)))
			assertTrue(img.getPixel(1, 1).isClose(Color(4.0e2f, 5.0e2f, 6.0e2f)))
			assertTrue(img.getPixel(2, 1).isClose(Color(7.0e2f, 8.0e2f, 9.0e2f)))
		}
		val p = "PA"
		assertThrows(InvalidPFMImageFormat::class.java) { HDRImage.fromPFMStream(p.byteInputStream()) }
	}
	
	@Test
	fun `test equals`() {
		val img1 = HDRImage(2, 2)
		val img2 = HDRImage(2, 2)
		
		img1.setPixel(0, 0, Color(1f, 0f, 0f))
		img1.setPixel(1, 0, Color(0f, 1f, 0f))
		img1.setPixel(0, 1, Color(0f, 0f, 1f))
		img1.setPixel(1, 1, Color(1f, 1f, 1f))
		
		img2.setPixel(0, 0, Color(1f, 0f, 0f))
		img2.setPixel(1, 0, Color(0f, 1f, 0f))
		img2.setPixel(0, 1, Color(0f, 0f, 1f))
		img2.setPixel(1, 1, Color(1f, 1f, 1f))
		
		// same image
		assertEquals(img1, img2)
		assertEquals(img1.hashCode(), img2.hashCode())
		
		//same hashcode
		assertEquals(img1.hashCode(), img2.hashCode())
		
		// different dimension
		assertNotEquals(img1, HDRImage(3, 2))
		
		// different pixels
		val img3 = img2.copy(pixels = img2.pixels.copyOf())
		img3.setPixel(0, 0, Color(0.5f, 0f, 0f))
		
		assertNotEquals(img1, img3)
	}
}