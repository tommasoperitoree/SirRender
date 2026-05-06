/**
 * The [Pigment] type is abstract and represents the color associated with a particular point on a surface ([u],[v])
 */
interface Pigment {
	val u: Float
	val v: Float
	
	fun getColor(u: Float, v: Float): Color {
		throw NotImplementedError("Pigment.getColor($u,$v) is not implemented")
	}
}

class uniformPigment() : Pigment {

}

class checkeredPigment() : Pigment {

}