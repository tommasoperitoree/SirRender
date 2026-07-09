package geometry

import materials.Material
import math.Point
import math.Transformation

class CSG(
	val firstShape: Shape,
	val secondShape: Shape,
	val operation: Operation,
	override val transformation: Transformation = Transformation(),
	override val material: Material = firstShape.material
) : Shape {
	
	/**
	 * Available CSG operations for shapes.
	 * - UNION: inside [firstShape] or inside [secondShape] shape
	 * - DIFFERENCE: inside [firstShape] and outside [secondShape] shape
	 * - INTERSECTION: inside both shapes.
	 */
	enum class Operation {
		UNION,
		DIFFERENCE,
		INTERSECTION
	}
	
	/**
	 * Associates a [HitRecord] with the shape that generated it.
	 *
	 * [fromFirst] is `true` if the hit comes from [firstShape], else it's `false`.
	 */
	private data class TaggedHit(
		val hit: HitRecord,
		val fromFirst: Boolean
	)
	
	/** Returns the closest valid intersection between [ray] and this CSG shape. */
	override fun rayIntersection(ray: Ray): HitRecord? {
		return rayIntersectionShape(ray).firstOrNull()
	}
	
	/**
	 * Computes all valid intersections between [ray] and the CSG shape.
	 *
	 * All intersections with [firstShape] and [secondShape] are collected and sorted
	 * along the ray.
	 *
	 * Then, depending on [operation], only the intersections that
	 * represent a real boundary of the final CSG solid are returned.
	 */
	override fun rayIntersectionShape(ray: Ray): List<HitRecord> {
		val allHits = firstShape.rayIntersectionShape(ray).map { hit -> TaggedHit(hit, true) } +
				secondShape.rayIntersectionShape(ray).map { hit -> TaggedHit(hit, false) }
		
		val sortedHit = allHits.sortedBy { taggedHit -> taggedHit.hit.t }
		
		return when (operation) {
			Operation.UNION -> {
				sortedHit.map { taggedHit -> buildHitRecord(taggedHit) }
			}
			
			Operation.DIFFERENCE,
			Operation.INTERSECTION -> {
				filterHits(ray, sortedHit)
			}
		}
	}
	
	/** Returns `true` if [point] lies inside the final CSG solid. */
	override fun contains(point: Point): Boolean {
		return when (operation) {
			Operation.UNION -> firstShape.contains(point) || secondShape.contains(point)
			Operation.DIFFERENCE -> firstShape.contains(point) && !secondShape.contains(point)
			Operation.INTERSECTION -> firstShape.contains(point) && secondShape.contains(point)
		}
	}
	
	/**
	 * Selects the intersections that are actual boundaries of the final CSG solid.
	 *
	 * For each candidate hit, the function checks a point just before and just after
	 * the intersection. If the ray changes state from outside to inside, or from
	 * inside to outside, the hit is kept.
	 */
	private fun filterHits(ray: Ray, hits: List<TaggedHit>): List<HitRecord> {
		val result = mutableListOf<HitRecord>()
		val epsilon = 1e-4f
		
		for (taggedHit in hits) {
			val t = taggedHit.hit.t
			
			val pointBefore = ray.at(t - epsilon)
			val pointAfter = ray.at(t + epsilon)
			
			val wasInside = contains(pointBefore)
			val isInside = contains(pointAfter)
			
			if (wasInside != isInside) {
				result.add(buildHitRecord(taggedHit))
			}
		}
		return result
	}
	
	/**
	 * Builds the final [HitRecord] returned by the CSG shape.
	 *
	 * The original hit information is preserved, but the shape is replaced with
	 * this CSG object. In a difference operation, normals coming from [secondShape]
	 * are flipped because [secondShape] represents the removed volume.
	 */
	private fun buildHitRecord(taggedHit: TaggedHit): HitRecord {
		val hit = taggedHit.hit
		
		val finalNormal =
			if (operation == Operation.DIFFERENCE && !taggedHit.fromFirst) {
				-hit.normal
			} else hit.normal
		
		return HitRecord(
			worldPoint = hit.worldPoint,
			normal = finalNormal,
			surfacePoint = hit.surfacePoint,
			t = hit.t,
			ray = hit.ray,
			shape = hit.shape //to preserve the material of the original shape
		)
	}
	
}