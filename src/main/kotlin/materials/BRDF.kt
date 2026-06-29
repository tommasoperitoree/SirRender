package materials

import geometry.Ray
import math.Normal
import math.PCG
import math.Point
import math.SurfaceVec
import math.Vec
import math.createOnbFromZ
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


interface BRDF {
	val pigment: Pigment
	
	/** Evaluates the BRDF for the given geometry: returns reflected radiance fraction. */
	fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: SurfaceVec): Color
	
	/**
	 * Samples a new scattered [Ray] from [intPoint] using Monte Carlo importance sampling.
	 * The returned ray has [depth] already incremented.
	 */
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
	val reflectance: Float = 1f
) : BRDF {
	
	override fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: SurfaceVec): Color =
		pigment.getColor(uv) * (reflectance / PI.toFloat())
	
	override fun scatterRay(
		pcg: PCG,
		incomingDir: Vec,
		intPoint: Point,
		normal: Normal,
		depth: Int
	): Ray {
		// Ensure the normal points into the same hemisphere as the outgoing scattered ray
		// (i.e., away from the surface as seen by the incoming ray).
		val orientedNormal = if (normal dot incomingDir > 0) -normal else normal
		
		val (e1, e2, e3) = createOnbFromZ(orientedNormal)
		val cosThetaSq = pcg.randomFloat()
		val cosTheta = sqrt(cosThetaSq) // cosine-weighted importance sampling: more samples near the normal
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
	
	override fun eval(normal: Normal, inDir: Vec, outDir: Vec, uv: SurfaceVec): Color {
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
		val n = normal.toVec().normalize()
		
		return Ray(
			intPoint,
			rayDir - n * 2f * (n dot rayDir),
			1e-3f,
			Float.POSITIVE_INFINITY,
			depth
		)
	}
}