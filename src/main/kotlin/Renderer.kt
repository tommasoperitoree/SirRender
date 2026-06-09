import kotlin.math.max

interface Renderer {
	val world: World
	val backgroundColor: Color
	
	// Estimate the radiance along a ray
	operator fun invoke(ray: Ray): Color {
		throw NotImplementedError("Renderer($ray) is not implemented")
	}
}


/** [OnOffRenderer] produce images white&black, useful for debugging purposes. */
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
 * [FlatRenderer] estimates the solution of the rendering equation by neglecting any contribution of the light.
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
 * Renderer based on path tracing with Monte Carlo integration.
 * Recursively solves the rendering equation by sampling [numRays] rays per intersection point.
 * Recursion is bounded by [maxRayDepth] and optimized via Russian Roulette beyond [russianRouletteLimit].
 */
class PathTracer(
	override val world: World = World(),
	override val backgroundColor: Color = Color(),
	val pcg: PCG = PCG(),
	val numRays: Int, // number of ray generate for integral calculation
	val maxRayDepth: Int,
	val russianRouletteLimit: Int, // limit of the Russian Roulette
) : Renderer {
	
	override operator fun invoke(ray: Ray): Color { // operator is necessary to use the recursion
		if (ray.depth > maxRayDepth) return Color.black
		//only if isSpecular is true return background, so in case o depth=0 or depth=1 (reflection on mirror)
		val hitRecord = world.rayIntersection(ray) ?: return if (ray.isSpecular) {
			backgroundColor
		} else {
			Color.black
		}
		
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
		if (hitColorLum > 0f) {
			repeat(numRays) {
				val newRay = hitMaterial.brdf.scatterRay(
					pcg,
					hitRecord.ray.dir,
					hitRecord.worldPoint,
					hitRecord.normal,
					ray.depth + 1
				) // depth has to be incremented, otherwise the new ray won't pass the first if
				cumRadiance += hitColor * this(newRay) // recursive call with this(newRay)
			}
		}
		// Rendering equation
		return emittedRadiance + cumRadiance * (1.0f / numRays)
		// return the emitted radiance (ex from a light ball) + mean value of radiance reflected
	}
}