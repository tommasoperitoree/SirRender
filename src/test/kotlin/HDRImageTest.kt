import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HDRImageTest {
	val width: Int = 10
	val height: Int = 10
	val img = HDRImage(width, height)
	
	@Test
	fun equals() {
		assertTrue { img.width == width }
		assertTrue { img.height == height }
	}
	
	
}
