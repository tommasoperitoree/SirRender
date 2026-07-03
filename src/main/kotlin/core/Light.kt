package core

import materials.Color
import math.Point

data class PointLight(
	val position: Point,
	val color: Color,
	val linearRadius: Float = 1f
)