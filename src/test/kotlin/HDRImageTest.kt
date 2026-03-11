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
	fun equals() {
		assertTrue { img.width == width }
		assertTrue { img.height == height }
	}
	
	@Test
	fun validCoordinates() {
		assertTrue { x >= 0 && y >= 0 }
		assertTrue { x <= img.width && y <= img.height }
	}
	
	@Test
	fun pixelOffset() {
		assertTrue { img.pixelOffset(x, y) == y * width + x }
	}
	
}
