import kotlin.math.max

interface Renderer {
	val world: World
	val backgroundColor: Color
	
	//Estimate the radiance along a ray
	fun invoke(ray: Ray): Color {
		throw NotImplementedError("Renderer.call($ray) is not implemented")
	}
}

/**
 * [OnOffRenderer] produce images white&black, useful for debugging purposes
 */
class OnOffRenderer(
	override val world: World = World(),
	override val backgroundColor: Color = Color(),
	val color: Color = white()
) : Renderer {
	override fun invoke(ray: Ray): Color {
		if (world.rayIntersection(ray) != null) return color
		else return backgroundColor
	}
}

/**
 * [FlatRenderer] estimates the solution of the rendering equation by neglecting any contribution of the light.
 *     It just uses the pigment of each surface to determine how to compute the final radiance.
 */
class FlatRenderer(
	override val world: World = World(),
	override val backgroundColor: Color = Color()
) : Renderer {
	override fun invoke(ray: Ray): Color {
		val hit = world.rayIntersection(ray)
		if (hit == null) {
			return backgroundColor
		}
		
		val material = hit.shape.material
		
		return (material.brdf.pigment.getColor(hit.surfacePoint) + material.emittedRadiance.getColor(hit.surfacePoint))
		
		
	}
}

/**
 * Renderer based on path tracing with Monte Carlo integration.
 * Recursively solves the rendering equation by sampling [N] rays per intersection point.
 * Recursion is bounded by [maxRayDepth] and optimized via Russian Roulette beyond [depthLimit].
 */

class PathTracer(
	override val world: World = World(),
	override val backgroundColor: Color = Color(),
	val pcg: PCG = PCG(),
	val N: Int, //number of ray generate for integral calculation
	val maxRayDeph: Int,
	val depthLimit: Int, //limit of the Russian Roulette
	var q: Float
) : Renderer {
	override operator fun invoke(ray: Ray): Color { // operator is necessary to use the recursion
		if (ray.depth > maxRayDeph) return black()
		
		var hitRecord = world.rayIntersection(ray) ?: return backgroundColor
		
		//extract from the point of intersection the color reflected and the emitted radiance
		val hitMaterial = hitRecord.shape.material
		var hitColor = hitMaterial.brdf.pigment.getColor(hitRecord.surfacePoint)
		val radiance = hitMaterial.emittedRadiance.getColor(hitRecord.surfacePoint)
		
		val cumLum = maxOf(
			hitColor.r,
			hitColor.g,
			hitColor.b
		)
		
		//Russian Roulette
		if (ray.depth >= depthLimit) {
			q = max(0.05f, 1 - cumLum)
			if (pcg.randomFloat() > q) {
				hitColor *= 1 / (1 - q)
			} else return radiance
		}
		
		//MonteCarlo
		var cumRadiance = black()
		//if cumLum is 0 it means that the surface is completely black, so is useless MonteCarlo
		if (cumLum > 0F) {
			for (rayindex in 0 until N) {
				val newRay = hitMaterial.brdf.scatterRay(
					pcg,
					hitRecord.ray.dir,
					hitRecord.worldPoint,
					hitRecord.normal,
					ray.depth + 1
				) //depth has to be incremented, otherwise the new ray won't pass the first if
				cumRadiance += hitColor * this(newRay)
			}
			
		}
		//Rendering equation
		return radiance + cumRadiance * (1.0f / N)
		//return the emitted radiance ( ex from a light ball) + mean value of radiance reflected
	}
}