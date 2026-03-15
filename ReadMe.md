# SirRender Documentation

[![Documentation](https://img.shields.io/badge/docs-dokka-blue)](https://tommasoperitoree.github.io/SirRender/)

## 🌐 Live Documentation

You can find the latest API reference, automatically generated from our codebase, at the following link:
👉 **[tommasoperitoree.github.io/SirRender/](https://tommasoperitoree.github.io/SirRender/)**

---

## 📚 Documentation Guidelines

We use **KDoc** for inline code documentation and **Dokka** to generate our searchable static website. To keep our
codebase clean, modern, and idiomatic, please adhere to the official Kotlin documentation conventions.

### Writing KDoc

Kotlin strongly recommends weaving parameters and return values naturally into descriptive sentences rather than relying
on heavy, structured tags at the bottom of the comment block.

* **Use the `[bracket]` syntax:** link parameters and properties directly in your text.
* **Avoid `@param` and `@return` for simple functions:** only use these explicit tags if a function has highly complex
  logic, numerous arguments, or strict validation rules that require dedicated paragraphs to explain.
* **Document Exceptions:** Always use the `@throws` tag if your function includes a `require()`, `check()`, or
  explicitly throws an exception.

**✅ Good Example (Idiomatic Kotlin):**

```kotlin
/**
 * Checks if the provided horizontal coordinate [x] and vertical coordinate [y] 
 * fall within the valid bounds of the image.
 * * Returns `true` if they are inside the boundaries, or `false` otherwise.
 */
fun validCoordinates(x: Int, y: Int): Boolean