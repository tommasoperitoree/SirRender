import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN
import javax.imageio.ImageIO
import kotlin.test.assertEquals

class HDRImageTest {
	
	val width: Int = 10
	val height: Int = 10
	var img = HDRImage(width, height)
	
	// generic Height and Width for testing
	val x: Int = 2
	val y: Int = 6
	
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
	fun `test overwritten equals operator`() {
		assertEquals(width, img.width)
		assertEquals(height, img.height)
	}
	
	@Test
	fun `test validCoordinates function`() {
		assertTrue(img.validCoordinates(x, y))
		assertFalse(img.validCoordinates(-1, 0))
		assertFalse(img.validCoordinates(width, height)) // out of bounds (exclusive)
	}
	
	@Test
	fun `test pixelOffset function`() {
		assertEquals(y * width + x, img.pixelOffset(x, y))
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
		val filename = "PFMImage.pfm"
		FileOutputStream(filename).use { line -> img.writePFMImage(line, LITTLE_ENDIAN) }
		
		FileInputStream(filename).use { line ->
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
	fun `test averageLuminosity`() {
		img = HDRImage(2, 1)
		img.setPixel(0, 0, Color(5.0f, 10.0f, 15.0f))
		img.setPixel(1, 0, Color(500.0f, 1000.0f, 1500.0f))
		
		//We pass delta=0.0 to avoid roundings
		print(img.averageLuminosity(delta = 0f))
		assertTrue { areClose(100.0f, img.averageLuminosity(delta = 0f)) }
	}
	
	@Test
	fun `test averageLuminosityDelta`() {
		img = HDRImage(2, 1)
		img.setPixel(0, 0, Color(5.0f, 10.0f, 15.0f))
		img.setPixel(1, 0, Color(500.0f, 1000.0f, 1500.0f))
		print(img.averageLuminosity())
		assertTrue { areClose(100.0f, img.averageLuminosity()) }
	}
	
	@Test
	fun `test normalizeImage`() {
		img = HDRImage(width = 2, height = 1)
		img.setPixel(0, 0, Color(5.0f, 10.0f, 15.0f))
		img.setPixel(1, 0, Color(500.0f, 1000.0f, 1500.0f))
		
		img.normalizeImage(100.0f, 1000.0f)
		
		assertTrue { img.getPixel(0, 0).isClose(Color(5.0e-1f, 1.0f, 1.5f)) }
		assertTrue { img.getPixel(1, 0).isClose(Color(50.0f, 1.0e2f, 1.5e2f)) }
	}
	
	@Test
	fun `test clampImage`() {
		img = HDRImage(2, 1)
		img.setPixel(0, 0, Color(0.5e1f, 1.0e1f, 1.5e1f))
		img.setPixel(1, 0, Color(0.5e3f, 1.0e3f, 1.5e3f))
		
		img.clampImage()
		
		for (clampPixel in img.pixels) {
			assertTrue { clampPixel.r in 0.0f..1.0f }
			assertTrue { clampPixel.g in 0.0f..1.0f }
			assertTrue { clampPixel.b in 0.0f..1.0f }
		}
	}
	
	@Test
	fun `test WriteLDRImage`() {
		val img = HDRImage(2, 2)
		img.setPixel(0, 0, Color(1f, 0f, 0f))
		img.setPixel(1, 0, Color(0f, 1f, 0f))
		img.setPixel(0, 1, Color(0f, 0f, 1f))
		img.setPixel(1, 1, Color(1f, 1f, 1f))
		
		//write on an output stream
		val byteOut = ByteArrayOutputStream()
		val imgOut = img.writeLDRImage(byteOut, "png", 1f)
		
		//Read the image from the stream e check  the dimension of the written image and the colors
		val imgRead = ImageIO.read(ByteArrayInputStream(byteOut.toByteArray()))
		
		assertEquals(img.width, imgRead.width)
		assertEquals(img.height, imgRead.height)
		//and is operation bit-bit (1 if are the same else 0) is useful for getRGB format
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
		assertEquals("Hello", HDRImage.readLine(line))
		assertEquals("World", HDRImage.readLine(line))
		// assertEquals("", HDRImage.readLine(line))  // gives error, should we allow reading EOF
	}
	
	/*@Test
	fun `test readFloat`(){
	TODO()
	 }
	*/
	
	/*@Test
	fun `test WriteFloat`(){
	TODO()
	 }
	*/
	
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
	}
	
	
	// --- test on public factory function ---
	
	@Test
	fun `test parseEndianness`() {
		assertEquals(BIG_ENDIAN, HDRImage.parseEndianness("1.0"))
		assertEquals(LITTLE_ENDIAN, HDRImage.parseEndianness("-3.0"))
		assertThrows(InvalidPFMImageFormat::class.java) { HDRImage.parseEndianness("0.0") }
		assertThrows(InvalidPFMImageFormat::class.java) { HDRImage.parseEndianness("ABC") }
	}
	
	@Test
	fun `test constructor fromPFMStream`() {
		for (referenceBytes in arrayOf(referenceBE, referenceLE)) {
			img = HDRImage.fromPFMStream(ByteArrayInputStream(referenceBytes))
			
			assertEquals(img.width, 3)
			assertEquals(img.height, 2)
			
			assertTrue(img.getPixel(0, 0).isClose(Color(1.0e1f, 2.0e1f, 3.0e1f)))
			assertTrue(img.getPixel(1, 0).isClose(Color(4.0e1f, 5.0e1f, 6.0e1f)))
			assertTrue(img.getPixel(2, 0).isClose(Color(7.0e1f, 8.0e1f, 9.0e1f)))
			assertTrue(img.getPixel(0, 1).isClose(Color(1.0e2f, 2.0e2f, 3.0e2f)))
			assertTrue(img.getPixel(0, 0).isClose(Color(1.0e1f, 2.0e1f, 3.0e1f)))
			assertTrue(img.getPixel(1, 1).isClose(Color(4.0e2f, 5.0e2f, 6.0e2f)))
			assertTrue(img.getPixel(2, 1).isClose(Color(7.0e2f, 8.0e2f, 9.0e2f)))
		}
		val p = "PA"
		assertThrows(InvalidPFMImageFormat::class.java) { HDRImage.fromPFMStream(p.byteInputStream()) }
	}
	
}