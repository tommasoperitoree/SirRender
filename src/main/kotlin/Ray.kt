class Ray(
	var origin: Point,
	var dir: Vec,
	var tMin: Float = 1e-5f,
	var tMax: Float = Float.POSITIVE_INFINITY,
	var depth: Int = 0,
) {
	fun isClose(other: Ray) =
		origin.isClose(other.origin) && dir.isClose(other.dir)
	
}