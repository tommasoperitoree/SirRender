import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.times

data class Vec(
	var x: Float = 0.0f,
	var y: Float = 0.0f,
	var z: Float = 0.0f
)
 {
	
	// --- Operator overloading ---
	
	operator fun plus(other: Vec): Vec =
		Vec(x + other.x, y + other.y, z + other.z)
	
	operator fun minus(other: Vec): Vec =
		Vec(x - other.x, y - other.y, z - other.z)
	
	operator fun unaryMinus() = Vec(-x, -y, -z)
	
	operator fun times(scalar: Float): Vec =
		Vec(x * scalar, y * scalar, z * scalar)
	
	operator fun times(other: Vec): Vec =
		Vec(x * other.x, y * other.y, z * other.z)
	
	
	// --- Utility functions ---
	
	/** Checks whether two [Vec] are equal through [areClose] fun. */
	fun isCloseVector(other: Vec) = areClose(x, other.x) && areClose(y, other.y) && areClose(z, other.z)
	
	/** Does the cross product between two [Vec]. */
	fun cross(other: Vec): Vec =
		Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	
	/** Does the norm of [Vec]. */
	fun norm(): Float = sqrt((x * x + y * y + z * z))
	
	/** Does the squared norm of [Vec]. */
	fun squaredNorm(): Float = x * x + y * y + z * z
	
	fun normalize(): Vec = times(1 / norm())
	
	//fun vectoNormal()
	
	// --- Default data class function overriding ---
	
	override fun toString(): String = "Vec = ($x, $y, $z)"
}


data class Point(val x: Float = 0.0f, val y: Float = 0.0f, val z: Float = 0.0f)
{
	
	override fun toString(): String {
		return "Point = ($x, $y, $z)"
	}
	
	fun isclosePoint(other: Point) {
		areClose(x,other.x) && areClose(y,other.y) && areClose(z,other.z)
	}
	
	/**override fun toString(): String {
	return "Point = ($x, $y, $z)"
	}
	 * this function return a typer vec from a given point
	 * the final vector linked the origin of the sdr to the given point
	 */
	fun PointToVec(): Vec{
		return Vec(x,y,z)
	}
	
	operator fun plus( other:Vec ): Point {
		return Point(x=x+other.x, y=y+other.y, z=z+other.z)
	}
	operator fun minus( other:Point ): Vec {
		return  Vec(x=x-other.x, y=y-other.y, z=z-other.z)
	}
	operator fun minus(other:Vec): Point {
		return Point(x=x-other.x, y=y-other.y, z=z-other.z)
	}
	
}

data class Normal(val x:Float = 0.0f, val y:Float = 0.0f, val z:Float = 0.0f)
{
	override fun toString(): String {
		return "Normal = ($x, $y, $z)"
	}
	
	fun iscloseNormal(other: Normal) {
		areClose(x,other.x) && areClose(y,other.y) && areClose(z,other.z)
	}
	
	/**Does dot product between [Normal] and a [scalar] **/
	operator fun times(scalar: Float){
		Normal(x=x*scalar,y=y*scalar,z=z*scalar)
	}
	
	/**from a given [Normal] n return -n **/
	fun negNormal(): Normal {
		return Normal(x=-x,y=-y,z=-z)
	}
	
	
	/** Does the cross product between [Normal] and [Normal]**/
	fun crossNN(other:Normal): Vec{
		return Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	}
	
	/** Does the cross product between [Normal] and [Vec]**/
	
	fun crossNV(other:Vec): Vec{
		return Vec(y * other.z - z* other.y, z * other.x - x * other.z, x * other.y - y * other.x)
	}
	
	/** Does dot product between [Normal] and [Vec]**/
	fun dotProductNV(other: Vec) = x * other.x + y * other.y + z * other.z
	
}