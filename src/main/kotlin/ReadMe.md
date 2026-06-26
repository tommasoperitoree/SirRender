# cli.SirRender — Class & Format Reference

Internal technical reference for contributors. For usage and CLI documentation see the cli.main [ReadMe](../SirRender/ReadMe.md).

---

## materials.HDRImage

Represents a High Dynamic Range image as a flat array of `materials.Color` values in row-major order.

### PFM Format

PFM (Portable FloatMap) stores HDR pixel data as raw IEEE 754 floats. cli.SirRender uses PFM as its internal format — all rendering writes PFM, and tone mapping converts to LDR formats.

**Header structure:**

```
PF\n
<width> <height>\n
<scale factor>\n
<binary pixel data...>
```

| Field | Value | Meaning |
|---|---|---|
| Magic | `PF` | Identifies the file as a colour PFM |
| Size | e.g. `3 2` | Width × Height |
| Scale | `-1.0` | Little-endian floats |
| Scale | `1.0` | Big-endian floats |
| Pixels | raw bytes | 3 × 32-bit floats per pixel (R, G, B), stored **bottom-to-top** |

**Example — little-endian 3×2 image (`reference_le.pfm`):**

```
0x50, 0x46, 0x0a,               → "PF\n"
0x33, 0x20, 0x32, 0x0a,         → "3 2\n"
0x2d, 0x31, 0x2e, 0x30, 0x0a,  → "-1.0\n"  (little-endian flag)
0x00, 0x00, 0xc8, 0x42, ...     → 100.0 (R of bottom-left pixel)
```

> ⚠️ PFM stores rows **bottom-to-top**, so the first byte of pixel data is the bottom-left pixel of the image.

### Tone Mapping Pipeline

Before converting to LDR (PNG, JPEG, etc.), HDR images go through a two-step pipeline:

```kotlin
img.normalizeImage(factor)   // scale luminosity
img.clampImage()             // bring values into [0.0, 1.0]  via x → x/(1+x)
img.writeLDRImage(...)       // apply gamma + quantize to 8-bit
```

- `normalizeImage(factor)` — scales each pixel by `factor / averageLuminosity()`
- `clampImage()` — applies the smooth compression `x / (1 + x)`, bringing HDR values into `[0, 1]`
- `writeLDRImage(stream, format, gamma)` — applies gamma correction `P_out = P_in^(1/γ)` and writes to stream

---

## Geometry

### Sealed Interface: `GeoElement`

Common parent for `math.Vec`, `math.Point`, and `math.Normal`. Provides shared `squaredNorm()` and `norm()` implementations.
`sealed` means all subtypes are known at compile time — `when` expressions on `GeoElement` are exhaustiveness-checked.

### `math.Vec` — 3D Vector

Represents a direction or displacement in 3D space.

| Operation | Syntax | Returns |
|---|---|---|
| Addition | `a + b` | `math.Vec` |
| Subtraction | `a - b` | `math.Vec` |
| Negation | `-a` | `math.Vec` |
| Scalar multiply | `a * s` | `math.Vec` |
| Dot product | `a dot b` | `Float` |
| Cross product | `a cross b` | `math.Normal` |
| Normalize | `a.normalize()` | `math.Vec` |

Axis constructors: `math.vecX()`, `math.vecY()`, `math.vecZ()`

### `math.Point` — 3D Position

Represents a position in space. Arithmetic is restricted to geometrically meaningful operations:

| Operation | Syntax | Returns |
|---|---|---|
| Displace by vector | `p + v` | `math.Point` |
| Vector between points | `p - q` | `math.Vec` |
| Displace negatively | `p - v` | `math.Point` |
| Convert to vector | `p.toVec()` | `math.Vec` |

### `math.Normal` — Surface math.Normal

Normals transform differently from vectors under non-uniform math.scaling — they use the **inverse transpose** of the transformation matrix. See `math.Transformation.times(math.Normal)`.

| Operation | Syntax | Returns |
|---|---|---|
| Negation | `-n` | `math.Normal` |
| Scalar multiply | `n * s` | `math.Normal` |
| Dot with vector | `n dot v` | `Float` |
| Cross with normal | `n cross m` | `math.Vec` |
| Cross with vector | `n cross v` | `math.Vec` |

### `math.Vec2d` — 2D Screen Coordinate

Used for UV screen coordinates to avoid confusion with 3D spatial coordinates.

---

## math.HomogMatr4x4

A 4×4 homogeneous matrix stored as a flat row-major `FloatArray(16)`.

Declared as `@JvmInline value class` — zero heap allocation overhead, the JVM sees only the raw array at runtime.

Element access: `m[row, col]` maps to `m.m[row * 4 + col]`.

| Operation | Description |
|---|---|
| `m[row, col]` | Get element |
| `m[row, col] = v` | Set element |
| `m * other` | Matrix multiplication |
| `m.isClose(other)` | Component-wise float comparison |
| `m.isInverseOf(other)` | Returns true if `m * other ≈ I` |
| `m.toMatrixString()` | Formatted 4×4 grid for printing |
| `math.HomogMatr4x4.identity()` | Returns the 4×4 identity matrix |

---

## math.Transformation

An affine transformation stored as a pair of matrices: `m` (the transformation) and `invm` (its inverse).
Always constructed via factory functions — never build `m` and `invm` by hand without verifying consistency.

```kotlin
val t = math.Transformation.math.translation(math.Vec(1f, 2f, 3f))
assert(t.isConsistent())  // verifies m * invm ≈ I
```

### Factory Functions

| Function | Description |
|---|---|
| `math.Transformation.math.translation(vec)` | Translation by `vec` |
| `math.Transformation.math.scaling(vec)` | Non-uniform math.scaling per axis |
| `math.Transformation.math.rotationX(deg)` | Rotation around X axis |
| `math.Transformation.math.rotationY(deg)` | Rotation around Y axis |
| `math.Transformation.math.rotationZ(deg)` | Rotation around Z axis |

### Operations

| Operation | Syntax | Returns |
|---|---|---|
| Compose | `t1 * t2` | `math.Transformation` |
| Apply to point | `t * p` | `math.Point` |
| Apply to vector | `t * v` | `math.Vec` |
| Apply to normal | `t * n` | `math.Normal` (uses inverse transpose) |
| Invert | `t.inverse()` | `math.Transformation` (swaps `m` and `invm`) |
| Check consistency | `t.isConsistent()` | `Boolean` |

> ⚠️ Composition order matters: `t1 * t2` applies `t2` first, then `t1`. This matches standard mathematical convention.

---

## Cameras

To avoid confusion between 3D spatial coordinates (x, y, z) and 2D screen coordinates, screen points use **(u, v)**,
where `u ∈ [0, 1]` is horizontal and `v ∈ [0, 1]` is vertical.

Both cameras accept a `math.Transformation` that controls position and orientation in the scene.

### `core.OrthogonalCamera`

Parallel projection — no perspective distortion. All rays are parallel and perpendicular to the image plane.
Good for technical/architectural views or when you want to avoid foreshortening.

### `core.PerspectiveCamera`

Realistic projection with depth foreshortening. Rays converge at a focal point behind the image plane.
Controlled by a distance parameter that sets the field of view.

---

## Shapes & core.World

### `geometry.Sphere`

A unit sphere centered at the origin, transformed by an optional `math.Transformation`.
geometry.Ray intersection uses the analytic solution to the sphere equation in object space.

### `core.World`

A container for all shapes in the scene. Provides `rayIntersection(ray)` which returns the closest `geometry.HitRecord`
or `null` if no shape is hit.

```kotlin
val world = core.World()
world.addShape(geometry.Sphere(math.Transformation.math.translation(math.Vec(0f, 0f, 1f))))
val hit = world.rayIntersection(ray)  // geometry.HitRecord? — null if no intersection
```

---

## core.ImageTracer

Fires rays through every pixel of an `materials.HDRImage` using a given `core.Camera`.

```kotlin
val tracer = core.ImageTracer(img, camera)
tracer.fireAllRays { ray ->
    world.rayIntersection(ray)?.let { white() } ?: black()
}
```

The lambda receives each ray and returns a `materials.Color`. The tracer maps each pixel's UV coordinates to a ray direction
via the camera, then writes the returned color into the image.