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

class checkeredPigment(
	var r: Float = 0f,
	var g: Float = 0f,
	var b: Float = 0f,
	val n: Int
) : Pigment {

}