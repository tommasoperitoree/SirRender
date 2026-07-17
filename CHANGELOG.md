# Unreleased (v1.1.0)

- Add triangle mesh support: `Mesh` shape, Möller–Trumbore intersection, AABB early-out,
  Wavefront OBJ file loading (`.obj`, `v`/`f` lines, all four face syntaxes, n-gons
  triangulation, configurable axis remapping)
- Add `mesh(...)` scene file keyword with `file("path.obj")` sub-syntax
- Mesh triangles are two-sided: surface normal always faces the incoming ray, matching
  the existing `Sphere`/`Plane` convention

# Version 1.0.0

- Update and reorganize documentation [#18](https://github.com/tommasoperitoree/SirRender/pull/18)
- Add scene file parser, allowing scenes to be defined through simple text
  files [#14](https://github.com/tommasoperitoree/SirRender/pull/14)
- Add antialiasing via jittered supersampling [#16](https://github.com/tommasoperitoree/SirRender/pull/16)
- Implement Cube shape with per-face UV mapping  [#15](https://github.com/tommasoperitoree/SirRender/pull/15)
- Fix PCG `randomFloat()` divisor (was 16× too large)
- Fix sky sphere material energy conservation
- Fix plane UV coordinates (missing floor normalization)
- Fix scattered ray `tMin` (1e-3f) to eliminate self-intersection acne

# Version 0.3.0

- Implement path tracing [#7](https://github.com/tommasoperitoree/SirRender/pull/7)
- Add PCG random generator [#6](https://github.com/tommasoperitoree/SirRender/pull/6)
- Production of first photorealistic image through `render` command

# Version 0.2.0

- Fix an issue with the vertical order of the images [#5](https://github.com/tommasoperitoree/SirRender/pull/5)
- Add the `demo` command and CLI interface using the Clikt Kotlin
  library [#4](https://github.com/tommasoperitoree/SirRender/pull/4)
- Add camera support [#2](https://github.com/tommasoperitoree/SirRender/pull/2)

# Version 0.1.0

- First release of the code
- Add initial geometry objects and transformations [#1](https://github.com/tommasoperitoree/SirRender/pull/1)