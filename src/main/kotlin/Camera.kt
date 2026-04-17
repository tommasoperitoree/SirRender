interface Camera{}

class Orthogonal(
	val aspectRatio: Float,
	val transformation: Transformation
): Camera{
	fun fireRay(u: Float, v: Float):Ray{
		val origin=Point(-1.0f,(1.0f-2*u)*aspectRatio,1.0f-2*v)
		val dir=Vec(1.0f, 0F, 0F)
		return Ray(origin,dir).transform(transformation)
	}
}

