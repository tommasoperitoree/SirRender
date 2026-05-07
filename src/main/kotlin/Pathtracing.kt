import kotlin.math.floor

/**
 * The [Pigment] type is abstract and represents the color associated with a particular point on a surface ([u],[v])
 */
interface Pigment {
	fun getColor(u: Float, v: Float): Color {
		throw NotImplementedError("Pigment.getColor($u,$v) is not implemented")
	}
}

class uniformPigment(
	var r: Float = 0f,
	var g: Float = 0f,
	var b: Float = 0f
) : Pigment {
	override fun getColor(u: Float, v: Float): Color = Color(r, g, b)
}


/**
 * [floor] return the greatest integer less than or equal to the number.
 * the boxes that have even coordinates (x+y) are painted with color 1 (es (0,0), (3,1)..)
 */
class checkeredPigment(
	val color1: Color,
	val color2: Color,
	val n: Int //number of boxes for side
) : Pigment {
	override fun getColor(u: Float, v: Float): Color {
		if ((floor(u * n) + floor(v * n)).toInt() % 2 == 0) return color1
		else return color2
	}
}