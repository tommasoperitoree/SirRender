# Module SirRender

A physically-based ray tracer written in Kotlin, capable of rendering HDR scenes
and exporting them to PNG, JPEG, or animated GIF.

## Overview

SirRender renders scenes using path tracing. The pipeline is:

1. Define shapes and materials in a `World`
2. Choose a `Camera` (orthogonal or perspective)
3. Fire rays with `ImageTracer`
4. Apply tone mapping and export

## Architecture

| Layer | Classes |
|---|---|
| Core | `HDRImage`, `Color`, `Ray` |
| Geometry | `Vec`, `Point`, `Normal`, `Transformation` |
| Scene | `World`, `Sphere`, `HitRecord` |
| Rendering | `Camera`, `ImageTracer`, `Renderer` |
| Materials | `BRDF`, `Pigment`, `Material` |

## Quick Start

```kotlin
val world = World()
world.addShape(Sphere(Transformation.translation(Vec(0f, 0f, 1f))))

val camera = PerspectiveCamera(
    transformation = Transformation.translation(Vec(-2f, 0f, 0f))
)
val img = HDRImage(640, 480)
ImageTracer(img, camera).fireAllRays { ray ->
    world.rayIntersection(ray)?.let { white() } ?: black()
}
img.normalizeImage(0.2f)
img.clampImage()
img.writePFMFile("output.pfm")
```

## CLI Usage

See the [README](https://github.com/tommasoperitoree/SirRender) for full CLI documentation.