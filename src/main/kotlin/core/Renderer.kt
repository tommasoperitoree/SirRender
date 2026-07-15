package core

import geometry.Ray
import materials.Color
import math.PCG
import math.Point
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.measureTimedValue
import kotlin.time.Duration

/**
 * Defines a rendering strategy.
 *
 * Implementations may use different techniques, such as [OnOffRenderer],
 * [FlatRenderer] or [PathTracer].
 */
interface Renderer {
	val world: World
	val backgroundColor: Color
	
	/** Estimates the radiance arriving along [ray], from the ray's origin toward its direction. */
	operator fun invoke(ray: Ray): Color {
		throw NotImplementedError("core.Renderer($ray) is not implemented")
	}
}

/**
 * A debugging [Renderer] that colors each pixel white if the ray hits any geometry,
 * black otherwise. Useful for quickly verifying scene geometry without full shading.
 */
class OnOffRenderer(
	override val world: World = World(),
	override val backgroundColor: Color = Color(),
	val color: Color = Color.white
) : Renderer {
	
	override operator fun invoke(ray: Ray): Color {
		return if (world.rayIntersection(ray) != null) color
		else backgroundColor
	}
}

/**
 * A simple [Renderer] that estimates the solution of the rendering equation by neglecting any contribution of the light.
 * It just uses the pigment of each surface to determine how to compute the final radiance.
 */
class FlatRenderer(
	override val world: World = World(),
	override val backgroundColor: Color = Color()
) : Renderer {
	
	override operator fun invoke(ray: Ray): Color {
		val hit = world.rayIntersection(ray) ?: return backgroundColor
		
		val material = hit.shape.material
		
		return (material.brdf.pigment.getColor(hit.surfacePoint) + material.emittedRadiance.getColor(hit.surfacePoint))
	}
}

/**
 * [Renderer] based on path tracing with Monte Carlo integration.
 * Recursively solves the rendering equation by sampling [numRays] rays per intersection point.
 * Recursion is bounded by [maxRayDepth] and optimized via Russian Roulette beyond [russianRouletteLimit].
 */
class PathTracer(
	override val world: World = World(),
	override val backgroundColor: Color = Color(),
	val pcg: PCG = PCG(),
	val numRays: Int, // number of ray generate for integral calculation
	val maxRayDepth: Int,
	val russianRouletteLimit: Int // limit of the Russian Roulette
) : Renderer {
	
	var totalIntersectionTime = Duration.ZERO
	var totalScatterTime = Duration.ZERO
	var calls = 0
	
	companion object {
		const val PROFILING = false  // change to TRUE to profile
	}
	
	override operator fun invoke(ray: Ray): Color { // operator is necessary to use the recursion
		if (ray.depth > maxRayDepth) return Color.black
		
		// profiling
		val (hitRecord, intersectionTime) = measureTimedValue {
			world.rayIntersection(ray)
		}
		
		if (PROFILING) totalIntersectionTime += intersectionTime
		
		hitRecord ?: return backgroundColor
		
		// extract from the point of intersection the color reflected and the emitted radiance
		val hitMaterial = hitRecord.shape.material
		var hitColor = hitMaterial.brdf.pigment.getColor(hitRecord.surfacePoint)
		val emittedRadiance = hitMaterial.emittedRadiance.getColor(hitRecord.surfacePoint)
		
		val hitColorLum = maxOf(
			hitColor.r,
			hitColor.g,
			hitColor.b
		)
		
		// Russian Roulette
		if (ray.depth >= russianRouletteLimit) {
			val q = max(0.05f, 1f - hitColorLum)
			if (pcg.randomFloat() > q) {
				hitColor *= 1f / (1f - q)
			} else return emittedRadiance
		}
		
		// MonteCarlo
		var cumRadiance = Color.black
		
		// if hitColorLum is 0 it means that the surface is completely black, so MonteCarlo is useless
		if (hitColorLum > 0f) { // now timing scale like N exploiting antialiasing
			val newRay = hitMaterial.brdf.scatterRay(
				pcg,
				hitRecord.ray.dir,
				hitRecord.worldPoint,
				hitRecord.normal.normalize(),
				ray.depth + 1
			)
			cumRadiance += hitColor * this(newRay)
		}
		
		// Rendering equation
		return emittedRadiance + cumRadiance
	}
	
	/**
	 * Prints the accumulated path-tracing profiling statistics.
	 * Profiling data is collected only when [PROFILING] is enabled.
	 */
	fun printProfiling() {
		println("=== core.PathTracer Profiling ===")
		println("rayIntersection: $totalIntersectionTime")
		println("scatterRay:      $totalScatterTime")
		println("calls: $calls")
		if (calls > 0) {
			println("media rayIntersection: ${totalIntersectionTime / calls}")
			println("media scatterRay:      ${totalScatterTime / calls}")
		}
	}
}

/**
 * A point-light [Renderer] which estimates illumination from point light source
 * For each ray-surface intersection, it checks which lights are visible using
 * shadow rays and sums their contributions with distance attenuation.
 *
 * If it does not intersect anything, [backgroundColor] color is returned
 * */
class PointLightRenderer(
	override val world: World = World(),
	override val backgroundColor: Color = Color()
) : Renderer {
	
	override operator fun invoke(ray: Ray): Color {
		val hit = world.rayIntersection(ray) ?: return backgroundColor
		
		var result: Color = Color.black
		
		for (light in world.lights) {
			val toLight = light.position - hit.worldPoint
			val dirLight = toLight.normalize()
			val distance = toLight.norm()
			// Cast a shadow ray from the hit point toward the light source.
			val shadowRay = Ray(origin = hit.worldPoint, dir = dirLight, tMin = 1e-3f, tMax = distance)
			
			if (world.rayIntersection(shadowRay) != null) continue
			
			val cosTheta = maxOf(0f, hit.normal.dot(dirLight))
			val brdf = hit.shape.material.brdf.eval(
				normal = hit.normal,
				inDir = toLight.normalize(),
				outDir = -ray.dir,
				uv = hit.surfacePoint
			)
			
			// Apply inverse-square distance attenuation.
			val attenuation = light.linearRadius.pow(2) / distance.pow(2)
			
			result += brdf * light.color * cosTheta * attenuation
		}
		return result
	}

}