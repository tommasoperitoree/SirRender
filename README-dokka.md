# Module SirRender

A physically-based Monte Carlo path tracer written in Kotlin.
SirRender renders scenes defined in a lightweight text format and exports lossless HDR output
(`.pfm`) with optional tone-mapped PNG/JPEG conversion and animated GIF assembly.

**Version:** 0.3.0 &nbsp;·&nbsp;
[GitHub](https://github.com/tommasoperitoree/SirRender) &nbsp;·&nbsp;
[CHANGELOG](https://github.com/tommasoperitoree/SirRender/blob/main/CHANGELOG.md) &nbsp;·&nbsp;
[CLI Reference](https://github.com/tommasoperitoree/SirRender/blob/main/ReadMe.md)

## Architecture Overview

SirRender is organized into five packages with a strict downward dependency order:

```
parsing  ──▶  core  ──▶  geometry  ──▶  math
                 │                         ▲
                 └──▶  materials  ──────────┘
```

| Package     | What lives here                                                                            |
|-------------|--------------------------------------------------------------------------------------------|
| `math`      | Linear algebra primitives: [Vec], [Point], [Normal], [SurfaceVec], [Transformation], [PCG] |
| `geometry`  | Ray–shape intersection: [Ray], [Shape], [Sphere], [Plane], [Cube], [HitRecord]             |
| `materials` | Surface appearance: [Color], [HDRImage], [Pigment], [BRDF], [Material]                     |
| `core`      | Rendering pipeline: [Camera], [World], [ImageTracer], [PathTracer]                         |
| `parsing`   | Scene file compiler: [SceneInputStream], [parseScene]                                      |

The `cli` package (the command-line interface) is omitted from the API reference — it is an
end-user tool, not part of the library surface.

## Architecture: Five-Layer Pipeline

A complete render follows these five steps in order:

### 1 — Math (`math`)

All geometry computation starts here. Three distinct types encode positions and directions
so the type system prevents mixing them accidentally:

| Type         | Role                              | Key operations                                 |
|--------------|-----------------------------------|------------------------------------------------|
| [Vec]        | Direction / displacement          | `+`, `-`, `dot`, `cross`, `normalize()`        |
| [Point]      | Position in space                 | `+ Vec`, `- Point` → Vec                       |
| [Normal]     | Surface normal                    | transforms via inverse-transpose of the matrix |
| [SurfaceVec] | 2D UV coordinate `(u, v) ∈ [0,1)` | used to index pigments/textures                |

[Transformation] wraps a 4×4 homogeneous matrix and its precomputed inverse together,
so composition and inversion are always consistent.
Factory functions — [translation], [scaling], [rotationX], [rotationY], [rotationZ] —
are the only safe way to construct transformations.

[PCG] is a fast, high-quality pseudo-random generator used for Monte Carlo scatter
direction sampling. Each rendering thread must own its own instance.

### 2 — Geometry (`geometry`)

[Ray] carries an origin, direction, and a valid `[tMin, tMax]` interval.
`tMin = 1e-3f` by default, preventing self-intersection after a surface bounce.

Each [Shape] implementation transforms the ray into object space (where the shape is
canonical), solves the intersection analytically, and transforms the result back to
world space:

| Shape    | Object-space definition        | UV mapping                                     |
|----------|--------------------------------|------------------------------------------------|
| [Sphere] | Unit sphere centered at origin | Spherical: `atan2(y,x)` for u, `acos(z)` for v |
| [Plane]  | z = 0, infinite extent         | Tiling: fractional part of world x, y          |
| [Cube]   | `[−1, 1]³` axis-aligned        | Per-face projection                            |

A successful intersection returns a [HitRecord] containing the world-space hit point,
surface normal, UV coordinates, ray parameter `t`, the originating ray, and the shape.

### 3 — Materials (`materials`)

[Material] pairs a [BRDF] with an optional emitted radiance [Pigment].

**Pigments** map a [SurfaceVec] to a [Color]:

| Pigment            | Description                                    |
|--------------------|------------------------------------------------|
| [UniformPigment]   | Solid color — same value everywhere            |
| [CheckeredPigment] | Procedural N×N checkerboard of two colors      |
| [ImagePigment]     | HDR texture lookup with bilinear interpolation |

**BRDFs** define how light scatters at a surface:

| BRDF           | Model            | Scatter direction                     |
|----------------|------------------|---------------------------------------|
| [DiffuseBRDF]  | Ideal Lambertian | Cosine-weighted importance sampling   |
| [SpecularBRDF] | Ideal mirror     | Perfect reflection: `r = d − 2(n·d)n` |

[HDRImage] stores the rendered pixel buffer as a flat `Array<Color>` in row-major order
and handles PFM I/O and tone mapping (`normalizeImage` → `clampImage` → `writeLDRImage`).

### 4 — Rendering (`core`)

[World] is the scene container: a flat list of [Shape]s searched linearly for the
closest intersection on each ray call.

[Camera] defines the projection. Both subclasses produce rays through a normalized
`(u, v) ∈ [0,1)²` screen:

| Camera              | Projection            | Effect                                        |
|---------------------|-----------------------|-----------------------------------------------|
| [OrthogonalCamera]  | Parallel rays         | No foreshortening; useful for technical views |
| [PerspectiveCamera] | Rays from focal point | Realistic depth and perspective               |

[ImageTracer] iterates over every pixel, computes jittered sub-pixel samples when
antialiasing is enabled (using a private [PCG] for reproducible jitter), and writes
the averaged color into the [HDRImage].

[PathTracer] implements the rendering equation via recursive Monte Carlo integration:

```
L(x, ω) = Lₑ(x, ω) + ∫ f_r(x, ω', ω) · L(x', ω') · cos θ · dω'
```

Each call fires `numRays` scattered rays, recurses up to `maxRayDepth`, and applies
Russian roulette termination beyond `russianRouletteLimit` to keep paths unbiased.
The renderer is **not thread-safe** — the parallel `render` command solves this by
giving each thread its own [PathTracer] instance with a deterministically seeded [PCG].

### 5 — Parsing (`parsing`)

[parseScene] reads a `.txt` scene file token by token via [SceneInputStream] and
builds a fully initialized [World] plus a [Camera], ready to hand to [ImageTracer].

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
float clock(0)                         # float variable (usable in transforms)

material groundMaterial(
    diffuse(checkered((0.9, 0.96, 0.96), (0.12, 0.2, 0.2), 4)),
    uniform((0, 0, 0))                 # emitted radiance: black = no emission
)

material skyMaterial(
    diffuse(uniform((0, 0, 0))),       # non-reflective sky
    uniform((1.4, 3, 4))              # emitted: blue-white light
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

**All supported keywords:**

| Category  | Keywords                                                                    |
|-----------|-----------------------------------------------------------------------------|
| Shapes    | `sphere`, `plane`, `cube`                                                   |
| BRDF      | `diffuse`, `specular`                                                       |
| Pigment   | `uniform`, `checkered`, `image`                                             |
| Transform | `identity`, `translation`, `scaling`, `rotationX`, `rotationY`, `rotationZ` |
| Camera    | `camera`, `perspective`, `orthogonal`                                       |
| Variable  | `float`                                                                     |

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