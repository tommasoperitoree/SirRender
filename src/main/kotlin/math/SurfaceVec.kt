package math

import materials.areClose

/**
 * A 2D UV surface coordinate, used to map a 3D hit point to a texture or pigment.
 *
 * Both [u] and [v] are in [0, 1). [u] increases left to right; [v] increases bottom to top.
 */
data class SurfaceVec(
	var u: Float = 0f,
	var v: Float = 0f,
) {
	fun isClose(other: SurfaceVec) = areClose(u, other.u) && areClose(v, other.v)
}