import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import kotlin.math.pow
import kotlin.math.sqrt

class `Transormation Test` {
	val t = Vec(10f, 1f, 2f)
	val p = Point(1f, 2f, 3f)
	val v = Vec(1f, 2f, 3f)
	val s = Vec(10f, 20f, 30f)
	val a = 90f
	val b = 45f
	val c = 180f
	
	@Test
	fun `traslation test`() {
		val p1 = Point(11f, 3f, 5f)
		val trasl = translation(t)
		assertTrue((trasl * p).isClose(p1))
	}
	
	@Test
	fun `scaling test`() {
		val v1 = Vec(10f, 40f, 90f)
		val scale = scaling(s)
		assertTrue((scale * v).isClose(v1))
	}
	
	/**
	 * The test about [rotation] are verified with 3 different angles 90,45,180
	 */
	@Test
	fun `rotationX test`() {
		val vx9 = Vec(1f, -3f, 2f)
		val vx4 = Vec(1f, -sqrt(2f) / 2, (5f * sqrt(2f)) / 2f)
		val vx18 = Vec(1f, -2f, -3f)
		
		val rota = rotationX(a)
		val rotb = rotationX(b)
		val rotc = rotationX(c)
		
		assertTrue((rota * v).isClose(vx9))
		assertTrue((rotb * v).isClose(vx4))
		assertTrue((rotc * v).isClose(vx18))
	}
	
	
	@Test
	fun `rotationY test`() {
		val vy9 = Vec(3f, 2f, -1f)
		val vy4 = Vec(2f * sqrt(2f), 2f, sqrt(2f))
		val vy18 = Vec(-1f, 2f, -3f)
		
		val rota = rotationY(a)
		val rotb = rotationY(b)
		val rotc = rotationY(c)
		
		assertTrue((rota * v).isClose(vy9))
		assertTrue((rotb * v).isClose(vy4))
		assertTrue((rotc * v).isClose(vy18))
	}
	
	@Test
	fun `rotationZ test`() {
		val vz9 = Vec(-2f, 1f, 3f)
		val vz4 = Vec((-sqrt(2f)) / 2f, (3f * sqrt(2f)) / 2f, 3f)
		val vz18 = Vec(-1f, -2f, 3f)
		
		val rota = rotationZ(a)
		val rotb = rotationZ(b)
		val rotc = rotationZ(c)
		
		assertTrue((rota * v).isClose(vz9))
		assertTrue((rotb * v).isClose(vz4))
		assertTrue((rotc * v).isClose(vz18))
	}
}
