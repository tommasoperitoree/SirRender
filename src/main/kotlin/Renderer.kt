interface Renderer {
	val world: World
	val backgroundColor: Color
	
	//Estimate the radiance along a ray
	fun call(ray: Ray): Color {
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
	override fun call(ray: Ray): Color {
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
	override fun call(ray: Ray): Color {
		val hit = world.rayIntersection(ray)
		if (hit == null) {
			return backgroundColor
		}
		
		val material = hit.shape.material
		
		return (material.brdf.pigment.getColor(hit.surfacePoint) + material.emittedRadiance.getColor(hit.surfacePoint))
		
		
	}
}

/*
class PathTracer(
	override val world: World = World(),
	override val backgroundColor: Color = Color(),
	val pcg: PCG = PCG(),
	val N: Int, //number of ray generate for integral calculation
	val maxRayDeph: Int,
	val depthLimit: Int
) : Renderer {
	override fun call(ray: Ray): Color {
		if (ray.depth > maxRayDeph) return black()
		
		var hitRecord = world.rayIntersection(ray)
		if (hitRecord == null) return backgroundColor
		
		val hitMaterial = hitRecord.shape.material
		val hitColor = hitMaterial.brdf.pigment.getColor(hitRecord.surfacePoint)
		val Radiance = hitMaterial.emittedRadiance.getColor(hitRecord.surfacePoint)
		
		
	}
}*/