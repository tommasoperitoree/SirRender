Lesson 8a is foundational sphere/plane review (already implemented). Let me get the mesh-specific lessons.This is still review material — already implemented. Let me get the mesh-specific lessons (09a, 10a, 10b).Good — got the mesh storage model, ray-AABB algorithm, and rationale. Now the lab lessons with implementation specifics.This lesson 10a is path tracing theory (already implemented in `PathTracer`), not mesh-specific. Let me check 10b for the actual mesh implementation lab.Confirmed — lessons 10a and 10b cover PCG, path tracing, and BRDFs/Pigments, all already implemented in SirRender. The mesh-relevant material is entirely in **08a** (sphere/plane review, already done) and **09a** (mesh storage, AABB, ray-AABB intersection — new). The lab counterpart for triangle/mesh implementation specifics isn't in these five pages directly, but 09a gives everything needed structurally. Here's the TODO list and class proposal based on what's confirmed.

---

## TODO List & Class Structure for Mesh Support

### New files in `geometry/`

**`AABB.kt`**
```kotlin
data class AABB(val pMin: Point, val pMax: Point) {
    fun quickRayIntersection(ray: Ray): Boolean   // slab method, no HitRecord needed
    companion object {
        fun fromPoints(points: List<Point>): AABB  // for mesh bounding
        fun union(a: AABB, b: AABB): AABB           // for future BVH
    }
}
```
Implements the slab test from 09a: per-axis `t_min/t_max` intervals via `(f_i - O_i)/d_i`, intersect all three axis intervals, empty intersection ⇒ miss.

**`Triangle.kt`**
```kotlin
class Triangle(
    val a: Point, val b: Point, val c: Point,
    override val transformation: Transformation = Transformation(),
    override val material: Material
) : Shape {
    override fun rayIntersection(ray: Ray): HitRecord?
    // Möller–Trumbore or barycentric-coordinate solve;
    // UV from barycentric (u,v,w) weights, normal from (B-A)×(C-A)
}
```
Single triangle as a standalone `Shape`, reusing the existing `Shape` interface — no special-casing needed elsewhere.

**`Mesh.kt`**
```kotlin
class Mesh(
    val vertices: List<Point>,
    val triangleIndices: List<Triple<Int, Int, Int>>,  // i1, i2, i3 per triangle
    val uvCoords: List<SurfaceVec>? = null,             // optional per-vertex UV
    override val transformation: Transformation = Transformation(),
    override val material: Material
) : Shape {
    private val aabb: AABB by lazy { AABB.fromPoints(vertices) }
    override fun rayIntersection(ray: Ray): HitRecord?
    // 1. transform ray to object space
    // 2. aabb.quickRayIntersection(invRay) early-out
    // 3. linear scan all triangles, keep closest hit
}
```
Implements the indexed vertex storage from 09a (3×int per triangle vs 3×3×float), with the AABB early-out from the `MyComplexShape` pattern shown in the lesson.

---

### Modified files

**`geometry/Shape.kt`** — add optional `aabb` member per the lesson's suggested pattern (nullable, defaults to no early-out for simple shapes like Sphere/Plane where it isn't worth it):
```kotlin
interface Shape {
    val transformation: Transformation
    val material: Material
    val aabb: AABB? get() = null   // override in Mesh
    fun rayIntersection(ray: Ray): HitRecord?
}
```

**`parsing/Lexer.kt`** — add `MESH` keyword and `"file"` parameter keyword for OBJ loading.

**`parsing/Parser.kt`** — add `parseMesh(s, scene): Mesh`, supporting either inline vertex/triangle lists or a `file("model.obj")` reference.

**New file `parsing/ObjLoader.kt`** — minimal Wavefront OBJ parser (`v`, `vt`, `f` lines only) since meshes will realistically be loaded from external files, not hand-written in scene `.txt` syntax.

---

### Tests

- `AABBTest.kt` — slab test hit/miss/edge cases (ray parallel to a face, ray inside box, ray starting on a face)
- `TriangleTest.kt` — standard hit, edge-on grazing, behind-origin (negative t), degenerate triangle
- `MeshTest.kt` — AABB early-out correctness, multi-triangle closest-hit selection, indexed-vertex sharing correctness
- `ObjLoaderTest.kt` — round-trip a small fixture `.obj` file

---

### Documentation

- `ReadMe.md` — add `mesh` keyword to Shapes table, document OBJ file loading syntax
- `README-dokka.md` — add `Mesh`, `Triangle`, `AABB` to the geometry package table and architecture overview

---

### Order of implementation (dependency order)

1. `AABB.kt` + tests (no dependencies)
2. `Triangle.kt` + tests (depends on existing `Shape`, `HitRecord`)
3. `Mesh.kt` + tests (depends on `Triangle`, `AABB`)
4. `ObjLoader.kt` + tests (depends on `Mesh`)
5. Parser/Lexer integration for scene file `mesh(...)` keyword
6. Add a sample mesh scene file to `scenes/`
7. Documentation updates