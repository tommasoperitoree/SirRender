package geometry

import materials.Material
import math.Point
import math.SurfaceVec
import math.Transformation
import kotlin.math.abs


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
		val invRay = ray.transform(transformation.inverse())
		if (!aabb.quickRayIntersection(invRay)) return null
		
		var closest: HitRecord? = null
		for ((i1, i2, i3) in triangleIndices) {
			val hit = triangleHitRecord(
				vertices[i1],
				vertices[i2],
				vertices[i3],
				invRay = invRay,
				originalRay = ray,
				shape = this,
				transformation = transformation,
			) ?: continue
			if (closest == null || hit.t < closest.t) closest = hit
		}
		return closest
	}
	
}

/**
 *
 */
internal fun triangleHitRecord(
	v0: Point, v1: Point, v2: Point,
	invRay: Ray, originalRay: Ray, shape: Shape, transformation: Transformation
): HitRecord? {
	
	val parallelEps = 1e-5f
	val edge1 = v1 - v0
	val edge2 = v2 - v0
	val h = (invRay.dir cross edge2).toVec()
	val det = edge1 dot h
	
	if (abs(det) < parallelEps) return null // ray parallel to triangle
	
	// barycentric coordinates
	
	val a = 1 / det
	val s = invRay.origin - v0
	
	val u = a * (s dot h)
	
	if (u !in 0f..1f) return null
	
	val q = (s cross edge1).toVec()
	
	val v = a * (invRay.dir dot q)
	
	if (v < 0f || v + u > 1f) return null
	
	
	// At this stage we can compute t to find out where the intersection point is on the line.
	val t = a * (edge2 dot q)
	if (t < invRay.tMin || t > invRay.tMax) return null
	
	val normal = (edge1 cross edge2).normalize()
	val localPoint = invRay.origin + invRay.dir.times(t)
	val worldPoint = transformation * localPoint
	val worldNormal = transformation * normal
	val surfacePoint = SurfaceVec(u, v)
	
	return HitRecord(worldPoint, worldNormal, surfacePoint, t, originalRay, shape)
	
}