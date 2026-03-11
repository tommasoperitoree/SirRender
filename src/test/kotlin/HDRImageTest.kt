import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class HDRImageTest {
	
	val width: Int = 10
	val height: Int = 10
	val img = HDRImage(width, height)
	
	// generic Height and Width for testing
	val x: Int = 2
	val y: Int = 6
	
	@Test
	fun `test overwritten equals function`() {
		assertEquals(width, img.width)
		assertEquals(height, img.height)
	}
	
	@Test
	fun `test validCoordinates function`() {
		assertTrue { x >= 0 && y >= 0 }
		assertTrue { x <= img.width && y <= img.height }
	}
	
	@Test
	fun `test pixelOffset function`() {
		assertEquals(y * width + x, img.pixelOffset(x, y))
	}
	
	@Test
	fun `test PFM parseImgSize function`() {
		// check correct conversion of width, height from string
		assertEquals(listOf(3, 2), img.parseImgSize("3 2"))
		// check correct Exception for different types of wrong arguments
		assertThrows(InvalidPFMImageFormat::class.java) {
			img.parseImgSize("1 2 3") // too many arguments
		}
		assertThrows(IllegalArgumentException::class.java) {
			img.parseImgSize("-1 2") // negative dimension
		}
		assertThrows(IllegalArgumentException::class.java) {
			img.parseImgSize("width height") // not Int
		}
	}
	
}
