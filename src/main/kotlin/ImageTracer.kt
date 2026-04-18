class ImageTracer(
	val image: HDRImage,
	val camera: Camera,
) {
	
	/**
	 * Maps pixel coordinates ([col], [row]) to a [Ray] passing through the pixel's surface.
	 * * Since pixels have area, [uPixel] and [vPixel] specify the relative position within
	 * the pixel boundaries (defaulting to 0.5 for the center).
	 */
	fun fireRay(col: Int, row: Int, uPixel: Float = 0.5f, vPixel: Float = 0.5f): Ray {
		// BUG: below formula has known error, will use to creat GitHub Issue
		val u = (col + uPixel) / (image.width - 1)
		val v = (row + vPixel) / (image.height - 1)
		return camera.fireRay(u, v)
	}
	
	/**
	 * Iterates over every pixel in the image.
	 * For each pixel, it invokes the [shader] function to determine the color.
	 */
	fun fireAllRays(shader: (Ray) -> Color) {
		for (row in 0 until image.height) {
			for (col in 0 until image.width) {
				val ray = fireRay(col, row)
				val color = shader(ray)
				image.setPixel(col, row, color)
			}
		}
	}
}