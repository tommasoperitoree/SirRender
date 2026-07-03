package math

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class TransformationTest {
	val t = Vec(10f, 1f, 2f)
	val p = Point(1f, 2f, 3f)
	val v = Vec(1f, 2f, 3f)
	val s = Vec(10f, 20f, 30f)
	val a = 90f
	val b = 45f
	val c = 180f
	
	@Test
	fun `translation test`() {
		val p1 = Point(11f, 3f, 5f)
		val transl = translation(t)
		Assertions.assertTrue((transl * p).isClose(p1))
	}
	
	@Test
	fun `test translation isConsistent`() {
		val transl = translation(t)
		Assertions.assertTrue(transl.isConsistent())
	}
	
	@Test
	fun `scaling test`() {
		val v1 = Vec(10f, 40f, 90f)
		val scale = scaling(s)
		Assertions.assertTrue((scale * v).isClose(v1))
	}
	
	@Test
	fun `test scaling isConsistent`() {
		val scale = scaling(s)
		Assertions.assertTrue(scale.isConsistent())
	}
	
	/**
	 * The tests on rotation are verified with 3 different angles: 90, 45, 180.
	 */
	@Test
	fun `rotationX test`() {
		val vx9 = Vec(1f, -3f, 2f)
		val vx4 = Vec(1f, -sqrt(2f) / 2, (5f * sqrt(2f)) / 2f)
		val vx18 = Vec(1f, -2f, -3f)
		
		val rotA = rotationX(a)
		val rotB = rotationX(b)
		val rotC = rotationX(c)
		
		Assertions.assertTrue((rotA * v).isClose(vx9))
		Assertions.assertTrue((rotB * v).isClose(vx4))
		Assertions.assertTrue((rotC * v).isClose(vx18))
	}
	
	@Test
	fun `rotationY test`() {
		val vy9 = Vec(3f, 2f, -1f)
		val vy4 = Vec(2f * sqrt(2f), 2f, sqrt(2f))
		val vy18 = Vec(-1f, 2f, -3f)
		
		val rotA = rotationY(a)
		val rotB = rotationY(b)
		val rotC = rotationY(c)
		
		Assertions.assertTrue((rotA * v).isClose(vy9))
		Assertions.assertTrue((rotB * v).isClose(vy4))
		Assertions.assertTrue((rotC * v).isClose(vy18))
	}
	
	@Test
	fun `rotationZ test`() {
		val vz9 = Vec(-2f, 1f, 3f)
		val vz4 = Vec((-sqrt(2f)) / 2f, (3f * sqrt(2f)) / 2f, 3f)
		val vz18 = Vec(-1f, -2f, 3f)
		
		val rotA = rotationZ(a)
		val rotB = rotationZ(b)
		val rotC = rotationZ(c)
		
		Assertions.assertTrue((rotA * v).isClose(vz9))
		Assertions.assertTrue((rotB * v).isClose(vz4))
		Assertions.assertTrue((rotC * v).isClose(vz18))
	}
	
	@Test
	fun `test rotation isConsistent`() {
		Assertions.assertTrue(rotationX(a).isConsistent())
		Assertions.assertTrue(rotationY(a).isConsistent())
		Assertions.assertTrue(rotationZ(a).isConsistent())
	}
}