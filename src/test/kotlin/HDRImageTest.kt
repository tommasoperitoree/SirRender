import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class HDRImageTest {
	
	val width: Int = 10
	val height: Int = 10
	val img = HDRImage(width, height)
	
	// generic Height and Width for testing
	var x: Int = 2
	var y: Int = 6
	
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
		assertTrue { img.pixelOffset(x, y) == y * width + x }
	}
	
	@Test
	fun `test PFM parseImgSize function`() {
		// check correct conversion of width, height from string
		assertEquals(listOf(3, 2), img.parseImgSize("3 2"))
		// check correct Exception for different types of wrong arguments
		assertThrows(InvalidPFMImageFormat::class.java) {
			img.parseImgSize("1 2 3")
		}
		assertThrows(IllegalArgumentException::class.java) {
			img.parseImgSize("-1 2")
		}
		assertThrows(IllegalArgumentException::class.java) {
			img.parseImgSize("width height")
		}
	}
	
}
