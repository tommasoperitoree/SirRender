import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HDRImageTest {
	
	val width: Int = 10
	val height: Int = 10
	val img = HDRImage(width, height)
	
	/*scrivo delle coordinate generiche di altezza e larghezza per utilizzarle nel test */
	var w1: Int = 2
	var h1: Int = 6
	
	@Test
	fun equals() {
		assertTrue { img.width == width }
		assertTrue { img.height == height }
	}
	
	
	@Test
	fun validCoordinates(): Unit {
		assertTrue {w1>=0 && h1>=0}
		assertTrue{w1<= img.width && h1<= img.height}
	}
	
	@Test
	fun pixelOffset(): Unit{
		assertTrue { img.pixelOffset(w1, h1) == h1 * width + w1 }
		println(img.pixelOffset(w1, h1))
	}
	
	
}
