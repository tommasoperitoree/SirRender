class Ray(
	var origin: Point,
	var dir: Vec,
	var tMin: Float = 1e-5f,
	var tMax: Float = Float.POSITIVE_INFINITY,
	var depth: Int = 0,
) {
	
	/** Calculates the [Point] along the ray's path at some distance from the origin given by [t]. */
	fun at(t: Float) =
		origin + dir * t
	
	/** Returns a new ray whose [origin] and [dir] are the transformation of the original [Ray]. */
	fun transform(transformation: Transformation) =
		Ray(
			transformation * origin,
			transformation * dir,
			tMin,
			tMax,
			depth,
		)
	
	
}