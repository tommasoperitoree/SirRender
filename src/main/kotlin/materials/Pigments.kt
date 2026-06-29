package materials

import math.SurfaceVec
import kotlin.math.floor


/**
 * The [Pigment] type is abstract and represents the color associated with a particular
 * point on a surface represented by a [SurfaceVec].
 */
interface Pigment {
	fun getColor(uv: SurfaceVec): Color {
		throw NotImplementedError("materials.Pigment.getColor($uv.u,$uv.v) is not implemented")
	}
}

/** A [Pigment] that returns the same [color] for every surface point. */
class UniformPigment(
	val color: Color = Color(),
) : Pigment {
	override fun getColor(uv: SurfaceVec): Color = color
}


/**
 * A textured pigment, given through a `PFM Image`.
 */
class ImagePigment(
	val image: HDRImage = HDRImage()
) : Pigment {
	
	/**
	 * Utilize Bilinear Interpolation as presented in https://en.wikipedia.org/wiki/Bilinear_interpolation
	 * under the method "on the unit square" to interpolate the 2d coordinate [uv]
	 * with the known colors of pixel definition of [image].
	 */
	override fun getColor(uv: SurfaceVec): Color {
		val scaledX = (uv.u * image.width).coerceIn(0f, (image.width - 1).toFloat())
		val scaledY = (uv.v * image.height).coerceIn(0f, (image.height - 1).toFloat())
		
		val x = scaledX.toInt().coerceIn(0, image.width - 2)
		val y = scaledY.toInt().coerceIn(0, image.height - 2)
		
		val tx = scaledX - x
		val ty = scaledY - y
		
		// four surrounding pixels
		val c00 = image.getPixel(x, y)
		val c10 = image.getPixel(x + 1, y)
		val c01 = image.getPixel(x, y + 1)
		val c11 = image.getPixel(x + 1, y + 1)
		
		// bilinear interpolation: first along x, then along y
		val top = c00 * (1 - tx) + c10 * tx
		val bottom = c01 * (1 - tx) + c11 * tx
		return top * (1 - ty) + bottom * ty
	}
}

/**
 * A procedurally generated checkerboard [Pigment].
 *
 * The surface is divided into a [numSteps] × [numSteps] grid. Tiles whose summed
 * grid indices are even use [color1]; odd tiles use [color2].
 */
class CheckeredPigment(
	val color1: Color,
	val color2: Color,
	val numSteps: Int // number of boxes for side
) : Pigment {
	
	/**
	 * Returns the color of the passed [uv] surface point.
	 * With [floor] return the greatest integer less than or equal to the number.
	 */
	override fun getColor(uv: SurfaceVec): Color {
		return if ((floor(uv.u * numSteps) + floor(uv.v * numSteps)).toInt() % 2 == 0) color1
		else color2
	}
}