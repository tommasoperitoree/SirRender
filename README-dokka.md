# Module SirRender

A Monte Carlo path tracer written in Kotlin. Renders physically-based images from
a declarative scene description language and exports HDR output in PFM format with
optional tone-mapped PNG conversion.

**Version:** 0.3.0 · [GitHub](https://github.com/tommasoperitoree/SirRender) ·
[CHANGELOG](https://github.com/tommasoperitoree/SirRender/blob/main/CHANGELOG.md)

---

## Architecture

SirRender is organized into five layers, each a Kotlin package:

| Package     | Responsibility                                                         |
|-------------|------------------------------------------------------------------------|
| `math`      | Linear algebra: [Vec], [Point], [Normal], [Transformation], [PCG]      |
| `geometry`  | Ray–shape intersection: [Ray], [Sphere], [Plane], [Cube], [HitRecord]  |
| `materials` | Surface appearance: [Color], [HDRImage], [Pigment], [BRDF], [Material] |
| `core`      | Rendering pipeline: [Camera], [World], [ImageTracer], [PathTracer]     |
| `parsing`   | Scene file compiler: [SceneInputStream], [parseScene]                  |

Dependencies flow strictly downward: `parsing` → `core` → `geometry` + `materials` → `math`.

---

## Rendering Pipeline

A complete render proceeds in five steps:

1. **Parse** — `parseScene()` reads a `.txt` scene file and builds a [World] with shapes and a [Camera].
2. **Trace** — [ImageTracer] maps each pixel `(col, row)` to a [Ray] via the camera's `fireRay(u, v)`.
3. **Integrate** — [PathTracer] evaluates the rendering equation by recursive Monte Carlo sampling,
   using a [PCG] generator for scatter direction sampling.
4. **Write HDR** — The result is stored in an [HDRImage] and written to a `.pfm` file (lossless, full float precision).
5. **Tone-map** — `normalizeImage` + `clampImage` + `writeLDRImage` produce a viewable PNG.

---

## Quick Start

```kotlin
// Build a scene programmatically
val world = World()
world.addShape(Sphere(scaling(Vec(2f, 2f, 2f)), Material(DiffuseBRDF(UniformPigment(Color.white)))))

// Choose a camera
val cam = PerspectiveCamera(distance = 2f, aspectRatio = 16f/9f,
              transformation = translation(Vec(-5f, 0f, 0f)))

// Render
val img = HDRImage(640, 360)
val tracer = ImageTracer(img, cam, antialiasing = 2, pcg = PCG())
val renderer = PathTracer(world, numRays = 4, maxRayDepth = 6, russianRouletteLimit = 3)
tracer.fireAllRays { ray -> renderer(ray) }

// Save
img.normalizeImage(0.2f)
img.clampImage()
FileOutputStream("output.png").use { img.writeLDRImage(it, "png", gamma = 2.2f) }
```

---

## Scene File Format

Scenes can also be described in a plain-text file (`.txt`) and loaded with the `render` CLI command.
See the [ReadMe.md](https://github.com/tommasoperitoree/SirRender/blob/main/ReadMe.md#-scene-file-format)
for the full grammar.

```
# scenes/example.txt
float clock(0)

material groundMaterial(
    diffuse(checkered((0.8, 0.8, 0.8), (0.3, 0.3, 0.3), 4)),
    uniform((0, 0, 0))
)

plane(groundMaterial, identity)

camera(perspective, translation((-5, 0, 1)), 1.7777, 2.0)
```