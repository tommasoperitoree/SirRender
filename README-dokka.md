# Module SirRender

A physically-based Monte Carlo path tracer written in Kotlin.
SirRender renders scenes defined in a lightweight text format and exports lossless HDR output
(`.pfm`) with optional tone-mapped PNG/JPEG conversion and animated GIF assembly.

**Version:** 1.1.0 &nbsp;·&nbsp;
[GitHub](https://github.com/tommasoperitoree/SirRender) &nbsp;·&nbsp;
[CHANGELOG](https://github.com/tommasoperitoree/SirRender/blob/main/CHANGELOG.md) &nbsp;·&nbsp;
[CLI Reference](https://github.com/tommasoperitoree/SirRender/blob/main/README.md)

## Architecture Overview

SirRender is organized into five packages with a strict downward dependency order:

```
parsing  ──▶  core  ──▶  geometry  ──▶  math
                 │                       ▲
                 └──▶  materials  ───────┘
```

| Package     | What lives here                                                                                                                                                        |
|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `math`      | Linear algebra primitives: [math.Vec], [math.Point], [math.Normal], [math.SurfaceVec], [math.Transformation], [math.PCG]                                               |
| `geometry`  | Ray–shape intersection: [geometry.Ray], [geometry.Shape], [geometry.Sphere], [geometry.Plane], [geometry.Cube], [geometry.Mesh], [geometry.AABB], [geometry.HitRecord] |
| `materials` | Surface appearance: [materials.Color], [materials.HDRImage], [materials.Pigment], [materials.BRDF], [materials.Material]                                               |
| `core`      | Rendering pipeline: [core.Camera], [core.World], [core.ImageTracer], [core.PathTracer]                                                                                 |
| `parsing`   | Scene file compiler: [parsing.SceneInputStream], [parsing.parseScene], [parsing.loadObj]                                                                               |

The `cli` package (the command-line interface) is omitted from the API reference — it is an
end-user tool, not part of the library surface.

## Architecture: Five-Layer Pipeline

A complete render follows these five steps in order:

### 1 — Math (`math`)

All geometry computation starts here. Three distinct types encode positions and directions
so the type system prevents mixing them accidentally:

| Type              | Role                              | Key operations                                 |
|-------------------|-----------------------------------|------------------------------------------------|
| [math.Vec]        | Direction / displacement          | `+`, `-`, `dot`, `cross`, `normalize()`        |
| [math.Point]      | Position in space                 | `+ Vec`, `- Point` → Vec                       |
| [math.Normal]     | Surface normal                    | transforms via inverse-transpose of the matrix |
| [math.SurfaceVec] | 2D UV coordinate `(u, v) ∈ [0,1)` | used to index pigments/textures                |

[math.Transformation] wraps a 4×4 homogeneous matrix and its precomputed inverse together,
so composition and inversion are always consistent.
Factory functions — [math.translation], [math.scaling], [math.rotationX], [math.rotationY], [math.rotationZ] —
are the only safe way to construct transformations.

[math.PCG] is a fast, high-quality pseudo-random generator used for Monte Carlo scatter
direction sampling. Each rendering thread must own its own instance.

### 2 — Geometry (`geometry`)

[geometry.Ray] carries an origin, direction, and a valid `[tMin, tMax]` interval.
`tMin = 1e-3f` by default, preventing self-intersection after a surface bounce.

Each [geometry.Shape] implementation transforms the ray into object space (where the shape is
canonical), solves the intersection analytically, and transforms the result back to
world space:

| Shape             | Object-space definition                      | UV mapping                                     |
|-------------------|----------------------------------------------|------------------------------------------------|
| [geometry.Sphere] | Unit sphere centered at origin               | Spherical: `atan2(y,x)` for u, `acos(z)` for v |
| [geometry.Plane]  | z = 0, infinite extent                       | Tiling: fractional part of world x, y          |
| [geometry.Cube]   | `[−1, 1]³` axis-aligned                      | Per-face projection                            |
| [geometry.Mesh]   | Indexed vertex list + triangle index triples | Barycentric `(u, v)` per triangle              |

A successful intersection returns a [geometry.HitRecord] containing the world-space hit point,
surface normal, UV coordinates, ray parameter `t`, the originating ray, and the shape.

**All shapes in SirRender are two-sided**: the surface normal always flips to face the
incoming ray, regardless of geometric winding order or which side is approached. This
applies uniformly to [geometry.Sphere], [geometry.Plane], and [geometry.Mesh] alike, so
any code reading `HitRecord.normal` can rely on it being outward-facing relative to the
ray without a per-shape special case.

#### Meshes

[geometry.Mesh] stores a shared `vertices: List<Point>` and `triangleIndices:
List<Triple<Int, Int, Int>>` — index triples into that list — rather than one
independent shape per triangle. This avoids duplicating shared-vertex data between
adjacent triangles. An [geometry.AABB] bounding box is computed once (lazily, on first
use) from the raw vertex positions and used as a cheap early-out: rays that miss the
whole mesh are rejected before any per-triangle intersection math runs. Triangle
intersection itself (`triangleHitRecord`, internal) is a stateless free function using
the Möller–Trumbore algorithm — not a `Shape` in its own right — called in a loop from
`Mesh.rayIntersection`.

[geometry.AABB] implements the slab method: each axis defines a pair of parallel planes,
and the ray's intersection interval with each slab is intersected across all three axes.

### 3 — Materials (`materials`)

[materials.Material] pairs a [materials.BRDF] with an optional emitted radiance [materials.Pigment].

**Pigments** map a [math.SurfaceVec] to a [materials.Color]:

| Pigment                      | Description                                    |
|------------------------------|------------------------------------------------|
| [materials.UniformPigment]   | Solid color — same value everywhere            |
| [materials.CheckeredPigment] | Procedural N×N checkerboard of two colors      |
| [materials.ImagePigment]     | HDR texture lookup with bilinear interpolation |

**BRDFs** define how light scatters at a surface:

| BRDF                     | Model            | Scatter direction                     |
|--------------------------|------------------|---------------------------------------|
| [materials.DiffuseBRDF]  | Ideal Lambertian | Cosine-weighted importance sampling   |
| [materials.SpecularBRDF] | Ideal mirror     | Perfect reflection: `r = d − 2(n·d)n` |

[materials.HDRImage] stores the rendered pixel buffer as a flat `Array<Color>` in row-major order
and handles PFM I/O and tone mapping (`normalizeImage` → `clampImage` → `writeLDRImage`).

### 4 — Rendering (`core`)

[core.World] is the scene container: a flat list of [geometry.Shape]s searched linearly for the
closest intersection on each ray call.

[core.Camera] defines the projection. Both subclasses produce rays through a normalized
`(u, v) ∈ [0,1)²` screen:

| Camera                   | Projection            | Effect                                        |
|--------------------------|-----------------------|-----------------------------------------------|
| [core.OrthogonalCamera]  | Parallel rays         | No foreshortening; useful for technical views |
| [core.PerspectiveCamera] | Rays from focal point | Realistic depth and perspective               |

[core.ImageTracer] iterates over every pixel, computes jittered sub-pixel samples when
antialiasing is enabled (using a private [math.PCG] for reproducible jitter), and writes
the averaged color into the [materials.HDRImage].

[core.PathTracer] implements the rendering equation via recursive Monte Carlo integration:

```
L(x, ω) = Lₑ(x, ω) + ∫ f_r(x, ω', ω) · L(x', ω') · cos θ · dω'
```

Each call fires `numRays` scattered rays, recurses up to `maxRayDepth`, and applies
Russian roulette termination beyond `russianRouletteLimit` to keep paths unbiased.
The renderer is **not thread-safe** — the parallel `render` command solves this by
giving each thread its own [core.PathTracer] instance with a deterministically seeded [math.PCG].

### 5 — Parsing (`parsing`)

[parsing.parseScene] reads a `.txt` scene file token by token via [parsing.SceneInputStream] and
builds a fully initialized [core.World] plus a [core.Camera], ready to hand to [core.ImageTracer].

[parsing.loadObj] loads a Wavefront `.obj` file's `v` (vertex) and `f` (face) lines into
raw `(vertices, triangleIndices)` data, ready to construct a [geometry.Mesh]. It supports
all four standard OBJ face syntaxes (bare, vertex/texture, vertex//normal,
vertex/texture/normal — only the vertex index is used), fan-triangulates faces with more
than 3 vertices, and accepts an optional `order` parameter to remap the file's axis
columns to SirRender's `(x, y, z)` convention for files exported with a different
up-axis.

## Quick Start (Kotlin API)

```kotlin
import core.*
import geometry.*
import materials.*
import math.*

// 1. Build a scene
val world = World()
world.addShape(
	Sphere(
		transformation = scaling(Vec(2f, 2f, 2f)) * translation(Vec(0f, 0f, 1f)),
		material = Material(
			brdf = DiffuseBRDF(UniformPigment(Color(0.8f, 0.2f, 0.2f))),
			emittedRadiance = UniformPigment(Color.black)
		)
	)
)
// Emissive sky sphere
world.addShape(
	Sphere(
		transformation = scaling(Vec(10f, 10f, 10f)),
		material = Material(
			brdf = DiffuseBRDF(UniformPigment(Color.black)),
			emittedRadiance = UniformPigment(Color(1.4f, 3f, 4f))
		)
	)
)

// 2. Set up camera
val cam = PerspectiveCamera(
	distance = 2f,
	aspectRatio = 16f / 9f,
	transformation = translation(Vec(-5f, 0f, 1f)) * rotationY(10f)
)

// 3. Render
val img = HDRImage(640, 360)
val tracer = ImageTracer(img, cam, antialiasing = 2, pcg = PCG())
val renderer = PathTracer(
	world = world,
	pcg = PCG(initState = 42uL, initSeq = 54uL),
	numRays = 4,
	maxRayDepth = 6,
	russianRouletteLimit = 3
)
tracer.fireAllRays { ray -> renderer(ray) }

// 4. Tone-map and save
img.normalizeImage(factor = 0.2f)
img.clampImage()
FileOutputStream("output.png").use { img.writeLDRImage(it, "png", gamma = 2.2f) }
```

## Scene File Format (mini-reference)

Scenes can be described in a plain `.txt` file and rendered with the `render` CLI command.

```
float clock(0)                         // float variable (usable in transforms)

material groundMaterial(
    diffuse(checkered((0.9, 0.96, 0.96), (0.12, 0.2, 0.2), 4)),
    uniform((0, 0, 0))                 // emitted radiance: black = no emission
)

material skyMaterial(
    diffuse(uniform((0, 0, 0))),       // non-reflective sky
    uniform((1.4, 3, 4))              // emitted: blue-white light
)

sphere(skyMaterial,  scaling((10, 10, 10)))
plane(groundMaterial, identity)

camera(perspective, translation((-5, 0, 2)) * rotationY(10), 1.7777, 2.0)
//     type          transform                               aspect  distance
```

**Grammar highlights:**

- Float variables: `float name(value)` — usable inside transform expressions.
- Colors and vectors: always `(r, g, b)` in round parentheses.
- Transforms compose left-to-right with `*`: `scaling * translation` translates first, then scales.
- `identity` is a valid no-op transform.
- Camera: `camera(type, transform, aspectRatio, distance)` — `distance` only applies to `perspective`.
- Comments: `//` to end of line — matches the lexer's `skipWhitespacesAndComments`.

**Meshes**, loaded from an external Wavefront `.obj` file:

```
mesh(pawnMaterial, file("scenes/pawn.obj, "xyz"), identity)
//   material      obj file path   opt. axis-order   transform
```

Meshes are two-sided (see [geometry.Mesh]). Vertex/face parsing accepts any of the four
standard OBJ face syntaxes; texture and normal indices in the file are currently ignored.

**All supported keywords:**

| Category  | Keywords                                                                    |
|-----------|-----------------------------------------------------------------------------|
| Shapes    | `sphere`, `plane`, `cube`, `mesh`                                           |
| BRDF      | `diffuse`, `specular`                                                       |
| Pigment   | `uniform`, `checkered`, `image`                                             |
| Transform | `identity`, `translation`, `scaling`, `rotationX`, `rotationY`, `rotationZ` |
| Camera    | `camera`, `perspective`, `orthogonal`                                       |
| Variable  | `float`                                                                     |
| Mesh      | `file` — sub-keyword inside `mesh(...)`, wraps the `.obj` file path string  |

## Tone-Mapping Pipeline

All renders are stored as floating-point HDR data (`.pfm`) before tone mapping.
The three-step pipeline converts HDR to a viewable LDR image:

```
HDRImage  ──normalizeImage(factor)──▶  scaled HDR
          ──clampImage()───────────▶  x / (1+x) ∈ [0,1]
          ──writeLDRImage(γ)────────▶  PNG/JPEG (8-bit, gamma-corrected)
```

1. **`normalizeImage(factor)`** — scales every pixel by `factor / averageLuminosity()`.
   Higher `factor` → brighter overall image.
2. **`clampImage()`** — applies the smooth Reinhard operator `x → x / (1 + x)`,
   compressing any HDR value into `[0, 1]` without hard clipping.
3. **`writeLDRImage(stream, format, gamma)`** — applies `P_out = P_in^(1/γ)` per channel
   and writes to any format supported by `javax.imageio` (PNG, JPEG, WebP, …).