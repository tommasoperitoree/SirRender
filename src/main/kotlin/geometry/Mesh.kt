package geometry

import materials.Material
import math.Point
import math.Transformation


/**
 * A triangle mesh: a set of triangles sharing an indexed vertex list.
 *
 * Vertices are stored once in [vertices]; each entry in [triangleIndices] references
 * three of them by index to form one triangle, avoiding duplicate storage for shared
 * vertices. [aabb] is used as a cheap early-out before testing individual triangles.
 */
class Mesh(
	val vertices: List<Point>,
	val triangleIndices: List<Triple<Int, Int, Int>>,
	override val transformation: Transformation = Transformation(),
	override val material: Material
) : Shape {
	
	// override val aabb = AABB.fromPoints(vertices)
	/**
	 * Axis-aligned bounding box of this mesh, in object space (i.e. computed from the raw
	 * [vertices] before [transformation] is applied).
	 * Computed lazily on first access and cached thereafter: cost of O(N) only paid once.
	 */
	override val aabb: AABB by lazy { AABB.fromPoints(vertices) }
	
	/** Returns the closest ray-triangle hit, or `null` if the ray misses the mesh. */
	override fun rayIntersection(ray: Ray): HitRecord? {
	
	}
}