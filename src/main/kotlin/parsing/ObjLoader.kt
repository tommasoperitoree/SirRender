package parsing

import geometry.Mesh
import math.Point
import java.io.File

/**
 * Loads a Wavefront OBJ file's `v` (vertex) and `f` (face) lines into raw mesh data.
 *
 * Supports all four standard face syntaxes (`f 1 2 3`, `f 1/1 2/2 3/3`, `f 1//1 2//2 3//3`,
 * `f 1/1/1 2/2/2 3/3/3`) — only the vertex index (the first `/`-delimited field) is used;
 * texture and normal indices are parsed and discarded, since [Mesh] does not yet store
 * per-vertex UV or normal data.
 *
 * Faces with more than 3 vertices are triangulated via a simple fan: `(v0,v1,v2)`,
 * `(v0,v2,v3)`, `(v0,v3,v4)`, ...
 *
 * @param path File path to the `.obj` file.
 * @param order Axis remapping from the OBJ file's column order to SirRender's (x, y, z).
 */
fun loadObj(path: String, order: String = "xyz"): Pair<List<Point>, List<Triple<Int, Int, Int>>> {
	val axisIndex = parseAxisOrder(order)
	
	val vertices = mutableListOf<Point>()
	val triangleIndices = mutableListOf<Triple<Int, Int, Int>>()
	
	File(path).forEachLine { rawLine ->
		val tokens = rawLine.trim().split(Regex("\\s+"))
		if (tokens.isEmpty() || tokens[0].isBlank()) return@forEachLine
		
		when (tokens[0]) {
			"v" -> vertices += parseVertexLine(tokens, axisIndex)
			"f" -> triangleIndices += parseFaceLine(tokens, vertices.size)
		}
	}
	return vertices to triangleIndices
}

/** Validates and converts an axis-order string like `"xzy"` into a lookup from axis letter to column index. */
private fun parseAxisOrder(order: String): Map<Char, Int> {
	require(order.length == 3 && order.toSet() == setOf('x', 'y', 'z')) {
		"order must be a permutation of \"xyz\", got \"$order\""
	}
	return order.withIndex().associate { (i, c) -> c to i }
}

private fun parseVertexLine(tokens: List<String>, axisIndex: Map<Char, Int>): Point {
	val coords = tokens.drop(1).take(3).map { it.toFloat() }
	return Point(
		coords[axisIndex.getValue('x')],
		coords[axisIndex.getValue('y')],
		coords[axisIndex.getValue('z')]
	)
}

/** Parses a face line, handling all four OBJ vertex/texture/normal syntaxes, with fan triangulation for n-gons. */
private fun parseFaceLine(tokens: List<String>, vertexCount: Int): List<Triple<Int, Int, Int>> {
	val vertexIndices = tokens.drop(1).map { token ->
		val rawIndex = token.split("/")[0].toInt()
		val resolved = when {
			rawIndex > 0 -> rawIndex - 1                        // standard 1-indexed
			rawIndex < 0 -> vertexCount + rawIndex              // relative: -1 = last vertex so far
			else -> throw IllegalArgumentException("Face index cannot be 0 (OBJ indices are 1-based or negative-relative): $token")
		}
		require(resolved in 0 until vertexCount) {
			"Face index $rawIndex resolves to vertex $resolved, out of range [0, $vertexCount) — check for a typo or truncated file"
		}
		resolved
	}
	require(vertexIndices.size >= 3) { "Face with fewer than 3 vertices: $tokens" }
	return (1 until vertexIndices.size - 1).map { k ->
		Triple(vertexIndices[0], vertexIndices[k], vertexIndices[k + 1])
	}
}