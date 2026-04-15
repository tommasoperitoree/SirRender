interface Camera{}

fun fireRay(u: Float, v: Float) {}

class OrthogonalCamera(
	val aspectRatio: Float,
	val transformation: Transformation
): Camera{
	fun fireRay(u: Float, v: Float):Ray{
		val origin=Point(-1.0f,(1.0f-2*u)*aspectRatio,1.0f-2*v)
		val dir=Vec(1.0f, 0F, 0F)
		return Ray(origin,dir).transform(transformation)
	}
}


class Perspective(
	var distance: Float = 1.0f,
	var aspectRatio: Float = 1.0f,
	var transformation: Transformation
): Camera {
	
	fun fireRay(u: Float, v: Float): Ray{
		val origin = Point(-distance, 0.0f, 0.0f)
		val direction = Vec(distance, ( 1 - 2 * u ) * aspectRatio, 2 * v - 1 )
		return Ray(origin, direction).transform(transformation)
	}
}