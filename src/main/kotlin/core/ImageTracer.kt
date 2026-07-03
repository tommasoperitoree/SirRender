package core

import geometry.Ray
import materials.Color
import materials.HDRImage
import math.PCG


/** Orchestrates the rendering process by casting rays through an [image] using a [camera]. */
class ImageTracer(
	val image: HDRImage,
	val camera: Camera,
	val antialiasing: Int,
	val pcg: PCG
) {
	
	/**
	 * Maps pixel coordinates ([col], [row]) to a [Ray] passing through the pixel's surface.
	 * * Since pixels have area, [uPixel] and [vPixel] specify the relative position within
	 * the pixel boundaries (defaulting to 0.5 for the center).
	 *
	 */
	fun fireRay(col: Int, row: Int, uPixel: Float = 0.5f, vPixel: Float = 0.5f): Ray {
		val u = (col + uPixel) / image.width
		val v = 1f - (row + vPixel) / image.height
		return camera.fireRay(u, v)
	}
	
	
	/**
	 * Iterates over every pixel in the image.
	 * For each pixel, it invokes the [shader] function to determine the color.
	 */
	fun fireAllRays(shader: (Ray) -> Color) {
		for (row in 0 until image.height) {
			for (col in 0 until image.width) {
				var colorSum = Color.black
				
				if (antialiasing <= 1) {
					val ray = fireRay(col, row)
					colorSum = shader(ray)
					
				} else {
					for (i in 0 until antialiasing) {
						for (j in 0 until antialiasing) {
							val uPixel = (i + pcg.randomFloat()) / antialiasing
							val vPixel = (j + pcg.randomFloat()) / antialiasing
							val ray = fireRay(col, row, uPixel, vPixel)
							colorSum += shader(ray)
						}
					}
					colorSum *= (1f / (antialiasing * antialiasing))
				}
				
				image.setPixel(col, row, colorSum)
			}
		}
	}
}