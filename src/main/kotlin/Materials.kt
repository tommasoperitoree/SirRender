import com.github.ajalt.mordant.rendering.TextColors.Companion.color
import kotlin.math.PI
import kotlin.math.floor

/**
 * The [Pigment] type is abstract and represents the color associated with a particular point on a surface ([u],[v])
 */
interface Pigment {
	fun getColor(uv: Vec2d): Color {
		throw NotImplementedError("Pigment.getColor($uv.u,$uv.v) is not implemented")
	}
}

class uniformPigment(
	val color: Color = Color(),
) : Pigment {
	override fun getColor(uv: Vec2d): Color = color
}


class checkeredPigment(
	val color1: Color,
	val color2: Color,
	val numSteps: Int // number of boxes for side
) : Pigment {
	
	/**
	 * [floor] return the greatest integer less than or equal to the number.
	 * the boxes that have even summed coordinates (x+y) are painted with color1 (es (0,0), (3,1)...)
	 */
	override fun getColor(uv: Vec2d): Color {
		return if ((floor(uv.u * numSteps) + floor(uv.v * numSteps)).toInt() % 2 == 0) color1
		else color2
	}
}


interface BRDF {
	val pigment: Pigment
	
	fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: Vec2d): Color
	
}

class DiffuseBRDF(
	override val pigment: Pigment = uniformPigment(white()),
	val reflectance: Float = 0.5f
) : BRDF {
	
	override fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: Vec2d): Color =
		pigment.getColor(uv) * (reflectance / PI.toFloat())
}


data class Material(
	val brdf: BRDF = DiffuseBRDF(),
	val emittedRadiance: Pigment = uniformPigment(black())
)