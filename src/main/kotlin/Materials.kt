import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The [Pigment] type is abstract and represents the color associated with a particular
 * point on a surface represented by a [Vec2d].
 */
interface Pigment {
	fun getColor(uv: Vec2d): Color {
		throw NotImplementedError("Pigment.getColor($uv.u,$uv.v) is not implemented")
	}
}


class UniformPigment(
	val color: Color = Color(),
) : Pigment {
	override fun getColor(uv: Vec2d): Color = color
}

/**
 * A textured pigment, given through a `PFM Image`.
 */
class ImagePigment(
	val image: HDRImage = HDRImage()
) : Pigment {
	
	// simple getColor through floor reduction of pixel coordinates from uv vec
	// override fun getColor(uv: Vec2d): Color {
	// 	val col = (uv.u * image.width).toInt().coerceIn(0, image.width - 1)
	// 	val row = (uv.v * image.height).toInt().coerceIn(0, image.height - 1)
	// 	return image.getPixel(col, row)
	// }
	
	/**
	 * Utilize Bilinear Interpolation as presented in https://en.wikipedia.org/wiki/Bilinear_interpolation
	 * under the method "on the unit square" to interpolate the 2d coordinate [uv]
	 * with the known colors of pixel definition of [image].
	 */
	override fun getColor(uv: Vec2d): Color {
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


class CheckeredPigment(
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
	
	fun scatterRay(
		pcg: PCG,
		incomingDir: Vec,
		intPoint: Point,
		normal: Normal,
		depth: Int
	): Ray
}

class DiffuseBRDF(
	override val pigment: Pigment = UniformPigment(Color.white),
	val reflectance: Float = 0.5f
) : BRDF {
	
	override fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: Vec2d): Color =
		pigment.getColor(uv) * (reflectance / PI.toFloat())
	
	override fun scatterRay(
		pcg: PCG,
		incomingDir: Vec,
		intPoint: Point,
		normal: Normal,
		depth: Int
	): Ray {
		val (e1, e2, e3) = createOnbFromZ(normal)
		val cosThetaSq = pcg.randomFloat()
		val cosTheta = sqrt(cosThetaSq)
		val sinTheta = sqrt(1f - cosThetaSq)
		val phi = 2f * PI.toFloat() * pcg.randomFloat()
		
		return Ray(
			intPoint,
			e1 * cos(phi) * sinTheta + e2 * sin(phi) * sinTheta + e3 * cosTheta,
			1e-3f,
			Float.POSITIVE_INFINITY,
			depth
		)
	}
}

class SpecularBRDF(
	override val pigment: Pigment = UniformPigment(Color.white),
	val thresholdAngle: Float = PI.toFloat() / 1800f,
) : BRDF {
	
	override fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: Vec2d): Color {
		val thetaIn = acos(normal.toVec().dot(inDir))
		val thetaOut = acos(normal.toVec().dot(outDir))
		
		return if (abs(thetaIn - thetaOut) < thresholdAngle) pigment.getColor(uv) else Color.black
	}
	
	override fun scatterRay(
		pcg: PCG,
		incomingDir: Vec,
		intPoint: Point,
		normal: Normal,
		depth: Int
	): Ray {
		val rayDir = Vec(incomingDir.x, incomingDir.y, incomingDir.z).normalize()
		val normal = normal.toVec().normalize()
		
		return Ray(
			intPoint,
			rayDir - normal * 2f * (normal dot rayDir),
			1e-3f,
			Float.POSITIVE_INFINITY,
			depth
		)
	}
}


data class Material(
	val brdf: BRDF = DiffuseBRDF(),
	val emittedRadiance: Pigment = UniformPigment(Color.black)
)