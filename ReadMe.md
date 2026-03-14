# SirRender Documentation

## 📚 Documentation Guidelines

We use **KDoc** for inline code documentation and **Dokka** to generate our searchable static website. To keep our
codebase clean, modern, and idiomatic, please adhere to the official Kotlin documentation conventions.

### Writing KDoc (The Kotlin Way)

Unlike Java, Kotlin strongly prefers weaving parameters and return values naturally into descriptive sentences rather
than relying on heavy, structured tags at the bottom of the comment block.

* **Use the `[bracket]` syntax:** Link parameters and properties directly in your text.
* **Avoid `@param` and `@return` for simple functions:** Only use these explicit tags if a function has highly complex
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
```

### Dokka (Documentation Generator)

Dokka is the native API documentation engine for Kotlin. It automatically generates a searchable website by parsing the
codebase and extracting all `KDoc` comments.

**To generate the `.html` documentation locally:**

1. Open your terminal in the project root and run:
   ```bash
   ./gradlew :dokkaGenerateHtml
2. Once the build finishes, navigate to the generated output at:
   `build/dokka/html/index.html`
3. Right-click the file and open it in your web browser to view the live site.

