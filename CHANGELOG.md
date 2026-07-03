# Unreleased

- Implement cube shape
- Implement antialiasing algorithm
- Add scene file parser
- Implement Cube shape with per-face UV mapping
- Add antialiasing via jittered supersampling
- Fix PCG `randomFloat()` divisor (was 16× too large)
- Fix sky sphere material energy conservation
- Fix plane UV coordinates (missing floor normalization)
- Fix scattered ray `tMin` (1e-3f) to eliminate self-intersection acne

# Version 0.3.0

- *Breaking change*: Implement path tracer method in demo
- Production of first photorealistic image through `render` command

# Version 0.2.0

- *Breaking change*: Implement the `demo` and the `animation` command with CLI Interface through `Clickt` Kotlin
  Library [#4](https://github.com/tommasoperitoree/SirRender/pull/4)
- Fix an issue with the vertical order of the images [#5](https://github.com/tommasoperitoree/SirRender/pull/5)

# Version 0.1.0

- First release of the code