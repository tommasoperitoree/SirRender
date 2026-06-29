package geometry

import materials.areClose
import math.Normal
import math.Point
import math.SurfaceVec

/**
 * Records the result of a ray–shape intersection.
 *
 * @property worldPoint   The hit point in world space.
 * @property normal       The surface normal at the hit point, in world space (not necessarily unit length before normalization).
 * @property surfacePoint The UV coordinates of the hit point on the surface, in [0, 1).
 * @property t            The ray parameter at the intersection: `worldPoint = ray.origin + t * ray.dir`.
 * @property ray          The ray that produced this intersection.
 * @property shape        The shape that was hit.
 */
data class HitRecord(
	val worldPoint: Point = Point(),
	val normal: Normal = Normal(),
	val surfacePoint: SurfaceVec = SurfaceVec(),
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