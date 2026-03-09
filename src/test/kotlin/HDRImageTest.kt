import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HdrimageTest {
	val width: Int =10
	val height: Int =10
	val img=HDRImage(width, height)
	
	@Test
	fun equals() {
		assertTrue { img.width == width }
		assertTrue { img.height == height }
	}
	
	
}
