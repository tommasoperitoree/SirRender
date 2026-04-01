import kotlin.math.abs
import kotlin.math.sqrt

data class Vec(
	var x: Float = 0.0f,
	var y: Float = 0.0f,
	var z: Float = 0.0f
)



data class Point(val x: Float = 0.0f, val y: Float = 0.0f, val z: Float = 0.0f){
	
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

data class Normal(val x:Float = 0.0f, val y:Float = 0.0f, val z:Float = 0.0f){
	override fun toString(): String {
		return "Normal = ($x, $y, $z)"
	}
	
	fun iscloseNormal(other: Normal) {
		areClose(x,other.x) && areClose(y,other.y) && areClose(z,other.z)
	}
	
	/**
	 * from a given normal n return -n
	 */
	fun negNormal(): Normal {
		return Normal(x=-x,y=-y,z=-z)
	}
}


