package core

import geometry.HitRecord
import geometry.Shape
import geometry.Ray

/**
 * The scene graph: a flat list of [Shape]s and [PointLight]s.
 *
 * [rayIntersection] performs a linear search and returns the closest hit.
 * For large scenes, a BVH acceleration structure would replace this.
 */
class World(
	var shapes: Array<Shape> = arrayOf<Shape>(),
	var lights: Array<PointLight> = arrayOf()
) {
	/** Adds [shape] to the scene. */
	fun addShape(shape: Shape) {
		shapes += shape
	}
	
	/** Adds [lights] to the scene*/
	fun addLight(light: PointLight) {
		lights += light
	}
	
	/**
	 * Returns the closest [HitRecord] along [ray], or `null` if no shape is hit.
	 * Only intersections within `[ray.tMin, ray.tMax]` are considered.
	 */
	fun rayIntersection(ray: Ray): HitRecord? {
		var closest: HitRecord? = null
		
		for (shape in shapes) {
			val intersection: HitRecord = shape.rayIntersection(ray) ?: continue
			
			if (closest == null || intersection.t < closest.t) {
				closest = intersection
			}
		}
		return closest
	}
}