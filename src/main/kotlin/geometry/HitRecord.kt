package geometry

import materials.areClose
import math.Normal
import math.Point
import math.Vec2d

data class HitRecord(
	val worldPoint: Point = Point(),
	val normal: Normal = Normal(),
	val surfacePoint: Vec2d = Vec2d(),
	val t: Float = 1e-5f,
	val ray: Ray = Ray(),
	val shape: Shape
) {
	fun isClose(other: HitRecord) =
		worldPoint.isClose(other.worldPoint) &&
				normal.isClose(other.normal) &&
				surfacePoint.isClose(other.surfacePoint) &&
				areClose(t, other.t) &&
				ray.isClose(other.ray)
}