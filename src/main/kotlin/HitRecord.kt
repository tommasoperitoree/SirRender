data class HitRecord(
	val worldPoint: Point,
	val normal: Normal,
	val surfacePoint: Vec2d,
	val t: Float,
	val ray: Ray,
) {
	fun isClose(other: HitRecord) = worldPoint.isClose(other.worldPoint) &&
			normal.isClose(other.normal) &&
			surfacePoint.isClose(other.surfacePoint) &&
			areClose(t, other.t) && ray.isClose(other.ray)
}