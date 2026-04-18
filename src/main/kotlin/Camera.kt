interface Camera {
	// needed to call fireRay on a generic Camera type
	// do we need more, e.g. an error if we actually call it on interface Camera
	fun fireRay(u: Float, v: Float): Ray = Ray()
}

class OrthogonalCamera(
	val aspectRatio: Float = 1f,
	val transformation: Transformation = Transformation()
) : Camera {
	
	override fun fireRay(u: Float, v: Float): Ray {
		val origin = Point(-1f, (1 - 2 * u) * aspectRatio, 2 * v - 1)
		val dir = Vec(1f, 0f, 0f)
		return Ray(origin, dir).transform(transformation)
	}
}


class PerspectiveCamera(
	var distance: Float = 1f,
	var aspectRatio: Float = 1f,
	var transformation: Transformation = Transformation()
) : Camera {
	
	override fun fireRay(u: Float, v: Float): Ray {
		val origin = Point(-distance, 0f, 0f)
		val direction = Vec(distance, (1 - 2 * u) * aspectRatio, 2 * v - 1)
		return Ray(origin, direction).transform(transformation)
	}
}