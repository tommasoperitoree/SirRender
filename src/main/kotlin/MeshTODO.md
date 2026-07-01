# Mesh Support — Implementation Plan

Based on lesson 09a (mesh storage, AABB, ray-AABB intersection). Lessons 08a, 10a, 10b
are foundational review already implemented in SirRender (spheres/planes, PCG, path
tracing, BRDFs/pigments) — no new work needed from those.

## Design decision: no standalone `Triangle` class

The lesson explicitly contrasts meshes with spheres/planes: *"storing a 4×4
transformation and its inverse requires 32 floating-point numbers (128 bytes)... but we
can do better!"* — meshes exist precisely to avoid per-element transformation overhead.
Triangles are therefore stored as **raw index triples into a shared vertex list**, never
as independent `Shape`s with their own `Transformation`.

The lesson's own `MyComplexShape` example has exactly **one** `transformation` and
**one** `aabb` — confirming `Mesh` is the sole `Shape`. AABBs *"obviously do not apply to
the individual elements, but to the mesh as a whole."*

**Conclusion:** `Mesh` is the only new `Shape`. Triangle intersection is a stateless free
function operating on raw `Point`s, called in a loop from `Mesh.rayIntersection` — no
per-triangle object allocation, no per-triangle `Transformation`.

---

## New files in `geometry/`

### `AABB.kt`

```kotlin
data class AABB(val pMin: Point, val pMax: Point) {
	fun quickRayIntersection(ray: Ray): Boolean   // slab method
	
	companion object {
		fun fromPoints(points: List<Point>): AABB
		fun union(a: AABB, b: AABB): AABB          // for future BVH, not required now
	}
}
```

Slab test: per-axis `tMin/tMax` via `(f_i - O_i)/d_i`; ray hits iff `max(all tMins) ≤ min(all tMaxs)`.

### `triangleHitRecord` standalone function

```kotlin
/**
 * Möller–Trumbore intersection of [invRay] (already in object space) against
 * triangle (a, b, c). Returns a world-space HitRecord, or null if the ray misses.
 */
internal fun triangleHitRecord(
	a: Point, b: Point, c: Point,
	invRay: Ray, originalRay: Ray, shape: Shape
): HitRecord?
```

No `Shape` implementation — pure function, used by `Mesh` only.

### `Mesh.kt`

```kotlin
class Mesh(
	val vertices: List<Point>,
	val triangleIndices: List<Triple<Int, Int, Int>>,
	override val transformation: Transformation = Transformation(),
	override val material: Material
) : Shape {
	override val aabb: AABB by lazy { AABB.fromPoints(vertices) }
	
	override fun rayIntersection(ray: Ray): HitRecord? {
		val invRay = ray.transform(transformation.inverse())
		if (!aabb.quickRayIntersection(invRay)) return null
		
		var closest: HitRecord? = null
		for ((i1, i2, i3) in triangleIndices) {
			val hit = triangleHitRecord(vertices[i1], vertices[i2], vertices[i3], invRay, ray, this) ?: continue
			if (closest == null || hit.t < closest.t) closest = hit
		}
		return closest
	}
}
```

---

## Modified files

### `geometry/Shape.kt`

Add optional `aabb` member, `null` by default:

```kotlin
interface Shape {
	val transformation: Transformation
	val material: Material
	val aabb: AABB? get() = null   // overridden by Mesh only
	fun rayIntersection(ray: Ray): HitRecord?
}
```

### `parsing/Lexer.kt`

Add `MESH` keyword and `"file"` parameter keyword for OBJ loading.

### `parsing/Parser.kt`

Add `parseMesh(s, scene): Mesh`, supporting `file("model.obj")` reference.

### `parsing/ObjLoader.kt` (new)

Minimal Wavefront OBJ parser (`v`, `f` lines only — no normals/UV from file initially).

---

## Tests

- `AABBTest.kt` — hit/miss/edge cases (ray parallel to a face, ray inside box, ray on a face)
- `TriangleIntersectionTest.kt` — standard hit, edge-on grazing, behind-origin (negative t), degenerate triangle — tests
  `triangleHitRecord` directly
- `MeshTest.kt` — AABB early-out correctness, multi-triangle closest-hit selection, shared-vertex correctness
- `ObjLoaderTest.kt` — round-trip a small fixture `.obj` file

---

## Documentation

- `ReadMe.md` — add `mesh` keyword to Shapes table, document OBJ file loading syntax
- `README-dokka.md` — add `Mesh`, `AABB` to the geometry package table and architecture overview

---

## Checklist

> Split by dependency order — items with no unchecked dependencies above them can be
> worked on in parallel.

- [ ] `AABB.kt` — `quickRayIntersection`, `fromPoints`, `union`
- [ ] `AABBTest.kt`
- [ ] `Shape.kt` — add nullable `aabb` member
- [ ] `TriangleIntersection.kt` — `triangleHitRecord` (Möller–Trumbore)
- [ ] `TriangleIntersectionTest.kt`
- [ ] `Mesh.kt` — `Shape` implementation using `AABB` + `triangleHitRecord`
- [ ] `MeshTest.kt`
- [ ] `ObjLoader.kt` — minimal OBJ parser (`v`, `f` lines)
- [ ] `ObjLoaderTest.kt`
- [ ] `Lexer.kt` — `MESH` keyword, `file` parameter keyword
- [ ] `Parser.kt` — `parseMesh`
- [ ] Sample mesh scene file in `scenes/`
- [ ] `ReadMe.md` — mesh documentation
- [ ] `README-dokka.md` — mesh documentation
- [ ] `CHANGELOG.md` — entry for mesh support