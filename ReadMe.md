# SirRender

[![Build](https://img.shields.io/github/actions/workflow/status/tommasoperitoree/SirRender/gradle.yml?style=for-the-badge&logo=githubactions&logoColor=white&label=Build)](https://github.com/tommasoperitoree/SirRender/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/github/license/tommasoperitoree/SirRender?style=for-the-badge)](LICENSE)
[![API Docs](https://img.shields.io/badge/Docs-Dokka-0079CC?style=for-the-badge&logo=readthedocs&logoColor=white)](https://tommasoperitoree.github.io/SirRender/)

A physically-based Monte Carlo path tracer written in Kotlin.
SirRender renders 3D scenes defined in a lightweight text format and produces lossless
HDR output (`.pfm`) with optional tone-mapped PNG/JPEG export and animated GIF assembly.

---

## Contents

- [Features](#features)
- [Requirements](#requirements)
- [Building](#building)
- [Quick Start](#quick-start)
- [Commands](#commands)
    - [render](#render--render-a-scene-file)
    - [demo](#demo--render-the-built-in-demo)
    - [animation](#animation--assemble-pfm-frames-into-a-gif)
    - [pfm2png](#pfm2png--convert-pfm-to-ldr)
- [Scene File Format](#scene-file-format)
- [Typical Workflows](#typical-workflows)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)

---

## Features

- **Path tracing** with recursive Monte Carlo integration and Russian roulette termination
- **Three shapes**: sphere, infinite plane, axis-aligned cube — each fully transformable
- **Two BRDFs**: ideal Lambertian diffuse and perfect specular mirror
- **Three pigment types**: uniform color, procedural checkerboard, HDR image texture (bilinear interpolation)
- **Two cameras**: orthogonal and perspective
- **Scene file compiler**: declare geometry, materials, and camera in a readable `.txt` file
- **Antialiasing**: jittered supersampling with `a²` rays per pixel
- **Parallel rendering**: row-stripe partitioning across all CPU cores, fully deterministic
- **HDR pipeline**: PFM storage → Reinhard tone mapping → gamma-corrected PNG/JPEG/WebP
- **GIF animation**: assembles a folder of PFM frames into an animated GIF

---

## Requirements

| Tool   | Version                    |
|--------|----------------------------|
| JDK    | 25 or higher               |
| Kotlin | 2.3.0 (bundled via Gradle) |

No other runtime dependencies. The only compile-time library is
[Clikt](https://github.com/ajalt/clikt) for CLI parsing.

---

## Building

Clone and build:

```bash
git clone https://github.com/tommasoperitoree/SirRender.git
cd SirRender
./gradlew build
```

For repeated use, install a standalone distribution — this avoids Gradle startup
overhead on every invocation:

```bash
./gradlew installDist
# Executable: build/install/SirRender/bin/SirRender
```

The rest of this guide uses `SirRender` as a shorthand.
Either add `build/install/SirRender/bin` to your `PATH`, or substitute the full path.

---

## Quick Start

Render a scene file to PNG in one command:

```bash
SirRender render --input-file scenes/RedSphere-CheckGround.txt --render
# → src/main/resources/frames/RedSphere-CheckGround.pfm
# → src/main/resources/frames/RedSphere-CheckGround.png
```

Render the built-in demo at a specific observer angle:

```bash
SirRender demo --observer-angle 45 --render
```

Convert an existing PFM to PNG with custom tone mapping:

```bash
SirRender pfm2png scene.pfm output.png --factor 0.3 --gamma 2.2
```

---

## Commands

### `render` — Render a Scene File

Loads a `.txt` scene description, traces rays with the path tracer, and writes the
result as a `.pfm` file. Pass `--render` to also produce a tone-mapped PNG.

```bash
SirRender render [options]
```

| Option           | Short  | Default                       | Description                              |
|------------------|--------|-------------------------------|------------------------------------------|
| `--input-file`   | `-inp` | `SceneR/sceneFile.txt`        | Path to the scene file                   |
| `--width`        | `-w`   | `640`                         | Image width in pixels                    |
| `--height`       | `-h`   | `360`                         | Image height in pixels                   |
| `--output-dir`   | `-o`   | `./src/main/resources/frames` | Output directory                         |
| `--render`       | `-r`   | off                           | Also save a tone-mapped PNG              |
| `--antialiasing` | `-a`   | `1`                           | Supersampling factor (`a²` rays/pixel)   |
| `--num-rays`     | `-n`   | `8`                           | Scattered rays per path tracer bounce    |
| `--depth`        | `-d`   | `5`                           | Maximum ray recursion depth              |
| `--roulette`     | `-rou` | `3`                           | Depth at which Russian roulette kicks in |
| `--factor`       | `-f`   | `0.2`                         | Tone-mapping luminosity scale            |
| `--gamma`        | `-g`   | `1.0`                         | Gamma correction for PNG output          |
| `--initState`    |        | `42`                          | PCG seed — state component               |
| `--initSeq`      |        | `54`                          | PCG seed — sequence component            |
| `--threads`      | `-t`   | all CPUs                      | Number of parallel render threads        |

**Examples:**

```bash
# Standard 720p render with antialiasing
SirRender render -inp scenes/WorldSphere-CheckGround.txt -w 1280 -h 720 -a 3 -r

# High-quality render with more rays and deeper paths
SirRender render -inp scenes/CornellBox.txt -n 16 -d 8 -rou 4 -w 960 -h 540 -r

# Reproducible render with fixed seed
SirRender render -inp scenes/RedSphere-CheckGround.txt --initState 123 --initSeq 456 -r
```

---

### `demo` — Render the Built-in Demo

Renders a hardcoded scene: eight small spheres at the vertices of a unit cube,
plus two larger emissive spheres on two faces. Useful for testing the renderer
without a scene file.

```bash
SirRender demo [options]
```

| Option             | Short | Default                       | Description                               |
|--------------------|-------|-------------------------------|-------------------------------------------|
| `--width`          | `-w`  | `1280`                        | Image width in pixels                     |
| `--height`         | `-h`  | `720`                         | Image height in pixels                    |
| `--camera`         | `-c`  | `Perspective`                 | `Orthogonal` or `Perspective`             |
| `--observer-angle` | `-i`  | `0.0`                         | Observer angle around the scene (degrees) |
| `--output-dir`     | `-o`  | `./src/main/resources/frames` | Output directory                          |
| `--render`         | `-r`  | off                           | Also save a tone-mapped PNG               |
| `--factor`         | `-f`  | `0.2`                         | Tone-mapping luminosity scale             |
| `--gamma`          | `-g`  | `1.0`                         | Gamma correction for PNG output           |
| `--initState`      |       | `42`                          | PCG seed — state component                |
| `--initSeq`        |       | `54`                          | PCG seed — sequence component             |

**Examples:**

```bash
# Perspective view at 45°, save PNG
SirRender demo -i 45 --render

# Orthogonal view at full HD
SirRender demo -c Orthogonal -w 1920 -h 1080 --render
```

---

### `animation` — Assemble PFM Frames into a GIF

Reads all `.pfm` files from a directory (sorted alphabetically), applies tone mapping,
and assembles them into an animated GIF. Since frames are kept as raw HDR data, you can
re-assemble with different tone-mapping settings without re-rendering.

```bash
SirRender animation [options]
```

| Option        | Short | Default                              | Description                                |
|---------------|-------|--------------------------------------|--------------------------------------------|
| `--input-dir` | `-i`  | `./src/main/resources/frames`        | Directory of PFM files                     |
| `--output`    | `-o`  | `./src/main/resources/animation.gif` | Output GIF path                            |
| `--delay`     | `-d`  | `4`                                  | Frame delay in centiseconds (`4` ≈ 25 fps) |
| `--factor`    | `-f`  | `0.2`                                | Tone-mapping luminosity scale              |
| `--gamma`     | `-g`  | `1.0`                                | Gamma correction value                     |

**Examples:**

```bash
# Assemble frames at 25 fps
SirRender animation -i ./frames -o output.gif

# Slower animation, custom tone mapping
SirRender animation -i ./frames -o result.gif --delay 10 --factor 0.4 --gamma 2.2
```

---

### `pfm2png` — Convert PFM to LDR

Converts an existing `.pfm` HDR file to a standard image format.
The output format is inferred from the file extension (`.png`, `.jpg`, `.webp`, …).

```bash
SirRender pfm2png INPUT OUTPUT [options]
```

| Option     | Short | Default | Description                   |
|------------|-------|---------|-------------------------------|
| `--factor` | `-f`  | `0.2`   | Tone-mapping luminosity scale |
| `--gamma`  | `-g`  | `1.0`   | Gamma correction value        |

**Examples:**

```bash
SirRender pfm2png scene.pfm output.png
SirRender pfm2png scene.pfm output.jpg --factor 0.5 --gamma 2.2
```

---

## Scene File Format

Scenes are plain `.txt` files parsed by the built-in compiler. All example scenes are
in the [`scenes/`](scenes) directory.

### Complete Example

```
# Float variables — usable inside transform expressions
float clock(0)

# Materials: brdf(pigment), optional_emission
material skyMaterial(
    diffuse(uniform((0, 0, 0))),
    uniform((1.4, 3, 4))
)

material groundMaterial(
    diffuse(checkered((0.9, 0.96, 0.96), (0.12, 0.2, 0.2), 4)),
    uniform((0, 0, 0))
)

material sphereMaterial(
    diffuse(uniform((0.8, 0.2, 0.2))),
    uniform((0, 0, 0))
)

# Shapes: shape(material, transform)
sphere(skyMaterial, scaling((10, 10, 10)))
plane(groundMaterial, identity)
sphere(sphereMaterial, scaling((2, 2, 2)) * translation((0, 0, 1)))

# Camera: camera(type, transform, aspectRatio, distance)
camera(perspective, translation((-5, 0, 2)) * rotationY(10), 1.7777, 2.0)
```

### Shapes

| Keyword  | Description         | Object-space definition        |
|----------|---------------------|--------------------------------|
| `sphere` | Sphere              | Unit sphere centered at origin |
| `plane`  | Infinite flat plane | z = 0, extends in x and y      |
| `cube`   | Box                 | Axis-aligned `[−1, 1]³`        |

### Materials

A material binds a BRDF (reflectance model) to an optional emitted radiance:

```
material name(
    brdf(pigment(...)),
    emission_pigment(...)    ← optional; omit for non-emissive surfaces
)
```

**BRDFs:**

| Keyword    | Class          | Behaviour                                                      |
|------------|----------------|----------------------------------------------------------------|
| `diffuse`  | `DiffuseBRDF`  | Ideal Lambertian — scatters light uniformly in the hemisphere  |
| `specular` | `SpecularBRDF` | Ideal mirror — reflects sharply according to `r = d − 2(n·d)n` |

**Pigments:**

| Keyword     | Class              | Parameters                | Description                             |
|-------------|--------------------|---------------------------|-----------------------------------------|
| `uniform`   | `UniformPigment`   | `(r, g, b)`               | Solid color                             |
| `checkered` | `CheckeredPigment` | `(r,g,b)`, `(r,g,b)`, `n` | Procedural N×N checkerboard             |
| `image`     | `ImagePigment`     | `"path/to/file.pfm"`      | HDR texture with bilinear interpolation |

### Transformations

Transforms compose left-to-right with `*`. The rightmost transform is applied first:

```
scaling((2, 2, 2)) * translation((0, 0, 1))
# → translates to (0,0,1) first, then scales — center ends up at (0,0,2)
```

| Keyword       | Parameters     | Effect                      |
|---------------|----------------|-----------------------------|
| `identity`    | —              | No transform                |
| `translation` | `(tx, ty, tz)` | Translate                   |
| `scaling`     | `(sx, sy, sz)` | Scale (non-uniform allowed) |
| `rotationX`   | `degrees`      | Rotate around X axis        |
| `rotationY`   | `degrees`      | Rotate around Y axis        |
| `rotationZ`   | `degrees`      | Rotate around Z axis        |

### Camera

```
camera(type, transform, aspectRatio, distance)
```

- `type`: `perspective` or `orthogonal`
- `transform`: positions and orients the camera in the scene
- `aspectRatio`: width/height (e.g. `1.7777` for 16:9)
- `distance`: focal distance — only meaningful for `perspective`

### Sky and Emissive Surfaces

SirRender has no concept of a directional light. Lighting comes entirely from emissive
surfaces. The standard approach is a large emissive sky sphere:

```
material skyMaterial(
    diffuse(uniform((0, 0, 0))),   ← must be black (non-reflective)
    uniform((1.4, 3, 4))          ← the actual light color and intensity
)
sphere(skyMaterial, scaling((10, 10, 10)))
```

> ⚠️ The sky sphere BRDF **must** be `diffuse(uniform((0, 0, 0)))`.
> A non-black sky BRDF causes the sky to scatter additional rays on each hit,
> producing a systematic directional bias that looks like a shadow from a point light.

---

## Typical Workflows

### Render a scene to PNG

```bash
./gradlew installDist
SirRender render -inp scenes/RedSphere-CheckGround.txt -r -w 1280 -h 720 -a 3
```

### Full 360° animation

```bash
# Step 1: render 72 frames as PFM (5° steps)
for i in $(seq 0 5 355); do
    SirRender demo --observer-angle $i --output-dir ./frames
done

# Step 2: assemble into GIF at 25 fps
SirRender animation -i ./frames -o rotation.gif
```

### Re-tone-map without re-rendering

```bash
# Frames already rendered; just re-assemble with different settings
SirRender animation -i ./frames -o darker.gif --factor 0.1 --gamma 2.2
```

### Convert a raw PFM

```bash
SirRender pfm2png render.pfm final.png --factor 0.3 --gamma 1.8
```

---

## Architecture

SirRender is divided into five packages with a strict downward dependency order:

```
parsing  ──▶  core  ──▶  geometry  ──▶  math
                │                         ▲
                └──▶  materials  ──────────┘
```

| Package     | Key types                                                       |
|-------------|-----------------------------------------------------------------|
| `math`      | `Vec`, `Point`, `Normal`, `SurfaceVec`, `Transformation`, `PCG` |
| `geometry`  | `Ray`, `Shape`, `Sphere`, `Plane`, `Cube`, `HitRecord`          |
| `materials` | `Color`, `HDRImage`, `Pigment`, `BRDF`, `Material`              |
| `core`      | `Camera`, `World`, `ImageTracer`, `PathTracer`                  |
| `parsing`   | `SceneInputStream`, `parseScene`, `Scene`                       |

### Rendering Equation

`PathTracer` solves the rendering equation via Monte Carlo integration:

```
L(x, ω) = Lₑ(x, ω)  +  ∫_Ω f_r(x, ω', ω) · L(x', ω') · |cos θ| · dω'
```

Each surface bounce fires `numRays` cosine-weighted scattered rays, recurses to depth
`maxRayDepth`, and applies Russian roulette termination beyond `russianRouletteLimit`
to keep the estimator unbiased.

### Parallelism

The `render` command partitions image rows across `N` threads (default: all logical CPUs).
Each thread owns a private `PathTracer` with a deterministically seeded `PCG`,
so the same `--initState`/`--initSeq` always produces the same image regardless of
thread count.

---

## API Documentation

Full API reference generated with Dokka:
👉 [tommasoperitoree.github.io/SirRender/](https://tommasoperitoree.github.io/SirRender/)