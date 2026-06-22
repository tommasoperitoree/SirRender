# SirRender

[![Documentation](https://img.shields.io/badge/docs-dokka-blue)](https://tommasoperitoree.github.io/SirRender/)
[![Build](https://github.com/tommasoperitoree/SirRender/actions/workflows/gradle.yml/badge.svg)](https://github.com/tommasoperitoree/SirRender/actions)

A ray tracer written in Kotlin, capable of rendering HDR scenes and exporting them to standard image formats or animated
GIFs.

---

## 🌐 Live Documentation

Full API reference generated with Dokka
👉 [tommasoperitoree.github.io/SirRender/](https://tommasoperitoree.github.io/SirRender/)

---

## 🔨 Building

```bash
./gradlew build
```

For repeated use, build a standalone distribution — this avoids Gradle startup overhead on every run:

```bash
./gradlew installDist
# executable at: build/install/SirRender/bin/SirRender
```

Note: The executable is generated at `build/install/SirRender/bin/SirRender`.

For the rest of this manual, we will simply use the command `SirRender` for brevity.
To run these commands exactly as written, either add the `bin` directory to your system's PATH, or substitute
`SirRender` with the full path to the executable.

---

## 🖥️ CLI Usage

SirRender exposes three subcommands:

```
SirRender [--help] <command> [<options>]

Commands:
  pfm2png     Convert a PFM HDR image to LDR format (PNG, JPEG, ...)
  demo        Render a demo scene as PFM frames (optionally also PNG)
  animation   Assemble a folder of PFM frames into an animated GIF
```

---

### `pfm2png` — Convert PFM to LDR

Converts an existing `.pfm` file to a standard image format. The output format is inferred from the file extension.

```bash
SirRender pfm2png INPUT OUTPUT [options]
```

| Option     | Short | Default | Description               |
|------------|-------|---------|---------------------------|
| `--factor` | `-f`  | `0.2`   | Luminosity scaling factor |
| `--gamma`  | `-g`  | `1.0`   | Gamma correction value    |

```bash
# Convert to PNG
SirRender pfm2png scene.pfm output.png

# Convert to JPEG with custom tone mapping
SirRender pfm2png scene.pfm output.jpg --factor 0.5 --gamma 2.2
```

---

### `demo` — Render a Demo Scene

Renders a demo scene: spheres at the vertices of a unit cube plus two asymmetric spheres on two faces.
Produces one or more PFM frames by stepping the observer angle around the scene.

```bash
SirRender demo [options]
```

| Option             | Short | Default                       | Description                                       |
|--------------------|-------|-------------------------------|---------------------------------------------------|
| `--width`          | `-W`  | `640`                         | Image width in pixels                             |
| `--height`         | `-H`  | `480`                         | Image height in pixels                            |
| `--camera`         | `-c`  | `Perspective`                 | `Orthogonal` or `Perspective`                     |
| `--observer-angle` | `-i`  | `0.0`                         | Starting observer angle in degrees                |
| `--num-frames`     | `-n`  | `1`                           | Number of frames (angles evenly spaced over 360°) |
| `--output-dir`     | `-o`  | `./src/main/resources/frames` | Output directory for PFM files                    |
| `--render`         | `-r`  | `false`                       | Also save each frame as PNG alongside the PFM     |
| `--factor`         | `-f`  | `0.2`                         | Luminosity scaling factor                         |
| `--gamma`          | `-g`  | `1.0`                         | Gamma correction value                            |

```bash
# Single frame at 45°, save PFM + PNG preview
SirRender demo -i 45 --render

# 360 frames for a full rotation, perspective camera
SirRender demo -n 360 -c Perspective -W 640 -H 480

# 360 frames with PNG previews
SirRender demo -n 360 --render
```

Frames are saved as:

```
<output-dir>/frame_<camera>_<NNN>.pfm
<output-dir>/frame_<camera>_<NNN>.png   ← only with --render
```

---

### `animation` — Assemble PFM Frames into a GIF

Reads all `.pfm` files from a directory (sorted by filename), applies tone mapping, and assembles them into an animated
GIF. No re-rendering needed — you can tweak tone mapping and re-assemble instantly.

```bash
SirRender animation [options]
```

| Option        | Short | Default                              | Description                                |
|---------------|-------|--------------------------------------|--------------------------------------------|
| `--input-dir` | `-i`  | `./src/main/resources/frames`        | Directory containing PFM frame files       |
| `--output`    | `-o`  | `./src/main/resources/animation.gif` | Output GIF path                            |
| `--delay`     | `-d`  | `4`                                  | Frame delay in centiseconds (`4` ≈ 25 fps) |
| `--factor`    | `-f`  | `0.2`                                | Luminosity scaling factor                  |
| `--gamma`     | `-g`  | `1.0`                                | Gamma correction value                     |

```bash
# Assemble frames from the perspective folder
SirRender animation -i ./src/main/resources/frames/perspective

# Custom paths and slower framerate
SirRender animation -i ./frames/orthogonal -o ./output/spin.gif --delay 10

# Re-assemble with different tone mapping (no re-render needed)
SirRender animation -i ./frames/perspective --factor 0.5 --gamma 2.2 -o result_gamma22.gif
```

---

## 🎬 Typical Workflows

### Single image

```bash
./gradlew installDist
build/install/SirRender/bin/SirRender demo -i 45 -c Perspective --render
# → frames/frame_perspective_000.pfm
# → frames/frame_perspective_000.png
```

### Full animation

```bash
./gradlew installDist

# Step 1 — render all 360 frames as PFM
build/install/SirRender/bin/SirRender demo -n 360 -c Perspective -W 640 -H 480

# Step 2 — assemble into GIF
build/install/SirRender/bin/SirRender animation -i ./src/main/resources/frames/perspective -o ./src/main/resources/spheresPerspective.gif
```

Because frames are stored as raw HDR PFM files, you can re-run Step 2 with different tone mapping without re-rendering:

```bash
build/install/SirRender/bin/SirRender animation -i ./src/main/resources/frames/perspective --factor 0.3 --gamma 2.2 -o result_gamma22.gif
```

### Convert an existing PFM

```bash
build/install/SirRender/bin/SirRender pfm2png scene.pfm scene.png --factor 0.5 --gamma 1.8
```

---

## 📚 Documentation Guidelines

We use **KDoc** for inline documentation and **Dokka** to generate the API website.

### Writing KDoc

- **Use `[bracket]` syntax** to link parameters and types directly in the text
- **Avoid `@param` / `@return` for simple functions** — only use them when logic is complex or validation is strict
- **Always use `@throws`** if your function calls `require()`, `check()`, or throws explicitly

```kotlin
// ✅ Good — idiomatic, self-explanatory
/**
 * Returns `true` if ([x], [y]) falls within the image bounds.
 */
fun validCoordinates(x: Int, y: Int): Boolean

// ✅ Good — @throws when there is real validation
/**
 * Parses a PFM size header [line] into a (width, height) [Pair].
 *
 * @throws InvalidPFMImageFormat if the line does not contain exactly two strictly positive integers
 */
internal fun parseImgSize(line: String): Pair<Int, Int>

// ❌ Avoid — redundant tags for obvious functions
/**
 * @param x horizontal coordinate
 * @param y vertical coordinate
 * @return true if valid
 */
fun validCoordinates(x: Int, y: Int): Boolean
```
---

## 🌍 Scene File Format

In addition to the built-in demo, SirRender can render a scene described entirely in a `.txt` file.

### Supported Shapes

- `Plane`
- `Sphere`
- `Cube`

### Syntax Rules

- Triplets (vectors and colors) must be wrapped in **round parentheses** `(...)`, e.g. `(0, 0, 1)`. Do **not** use `<`, `[`, or `{`.
- To render large resolutions (e.g. `1280x720`), the camera's aspect ratio must be explicitly set to match, i.e. `1.777` (`16:9`).

### Defining Materials

A material is declared as:

material <name>(

<brdf>(<pigment>(<color>))

)

- **`brdf`**: `diffuse` or `specular`
- **`pigment`**: `uniform` or `checkered`
- **`color`**: an RGB triplet in parentheses, e.g. `(0, 0, 0)`

An emitted radiance can optionally be added as a second pigment, after a comma:

material sky_material(

diffuse(uniform((0, 0, 0))),

uniform((3.5, 7.5, 10))

)

---
The BRDF (*Bidirectional Reflectance Distribution Function*) determines whether a surface behaves like a matte object, a perfect mirror, or a glossy material when struck by a light ray.
### Available BRDF Types

The BRDF (*Bidirectional Reflectance Distribution Function*) determines whether a surface behaves like a matte object, a perfect mirror, or a glossy material when struck by a light ray.

| BRDF Type | Kotlin Class | Main Parameters | Description and Usage                                                                                                                                                        |
| :--- | :--- | :--- |:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Diffuse** | `DiffuseBRDF` | `pigment: Pigment` | Implements ideally diffuse reflection. Light is reflected uniformly in all directions of the hemisphere.<br/> It is ideal for matte materials like the ground, walls, or the sky. |
| **Specular** | `SpecularBRDF` | `pigment: Pigment` | Implements ideal specular reflection. Rays are reflected sharply following the law of geometric optics.<br/> It is used for perfect mirrors and chromed metallic surfaces.        |
### Available Pigment Types

The `Pigment` defines the surface albedo (the intrinsic color) before any light reflection calculations are performed by the BRDF.

| Pigment Type | Kotlin Class | Main Parameters | Description and Usage                                                                                                                                                  |
| :--- | :--- | :--- |:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Uniform** | `UniformPigment` | `color: Color` | A solid color, perfectly homogeneous and constant across every point of the geometric shape's surface.                                                                 |
| **Checkered** | `CheckeredPigment` | `color1: Color`<br>`color2: Color`<br>`numSteps: Int` | Generates a procedural checkered pattern alternating between the two provided colors. <br/>The `numSteps` parameter controls the frequency and density of the grid squares. |

## The `render` Command

The `render` command loads a 3D scene from a text description file and computes the final image using the Path Tracing algorithm.

### How It Works

1. **Scene Parsing:** Reads the text file via `SceneInputStream` to reconstruct the `World` geometry, materials, and camera.
2. **Camera Init:** Dynamically identifies the camera type (`is OrthogonalCamera` or `is PerspectiveCamera`) and inherits its original physical properties.
3. **Rendering:** The `ImageTracer` fires rays into the scene via `fireAllRays`, while the `PathTracer` computes global illumination, reflections, and shadows using a random `PCG` generator.
4. **Saving:** Exports the high-dynamic-range (HDR) image as a `.pfm` file. If requested, it applies tone-mapping to save a standard `.png` file as well.

### Terminal Usage Example

To launch a high-resolution render with an advanced level of antialiasing, run:

```bash
./gradlew run --args="render -r -inp SceneR/sceneFile.txt -w 1280 -f0.5 -h 720 -a 5 -o output"
```
| Option (Flag) | Type | Description | Default |
| :--- | :--- | :--- | :--- |
| `--input-file` (`-inp`) | `File` | Path to the 3D scene file. | `SceneR/sceneFile.txt` |
| `--width` (`-w`) | `Int` | Image width in pixels. | `640` |
| `--height` (`-h`) | `Int` | Image height in pixels. | `480` |
| `--render` (`-r`) | *Flag* | If present, also exports to `.png`. | `false` |
| `--antialiasing` (`-a`) | `Int` | Samples per pixel ($a^2$ rays)\footnote{The value is squared: e.g., -a 5 traces 25 rays per pixel.}. | `1` |
| `--output-dir` (`-o`) | `String`| Target output directory for files. | `./src/main/resources/frames` |
| `--factor` (`-f`) | `Float` | Luminosity scale (tone-mapping). | `0.2f` |
| `--gamma` (`-g`) | `Float` | Gamma correction for the PNG file. | `1f` |
| `--num-frames` (`-n`) | `Int` | Total number of frames to generate. | `1` |
| `--observer-angle` (`-i`)| `Float` | Starting observer angle in degrees. | `0f` |
| `--initState` | `ULong` | State seed for the PCG generator. | `42` |
| `--initSeq` | `ULong` | Sequence seed for the PCG generator. | `54` |