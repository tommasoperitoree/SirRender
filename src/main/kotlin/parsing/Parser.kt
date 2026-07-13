package parsing

import core.Camera
import core.OrthogonalCamera
import core.PerspectiveCamera
import core.World
import core.PointLight
import geometry.Cube
import geometry.Plane
import geometry.Sphere
import materials.BRDF
import materials.CheckeredPigment
import materials.Color
import materials.DiffuseBRDF
import materials.HDRImage
import materials.ImagePigment
import materials.Material
import materials.Pigment
import materials.SpecularBRDF
import materials.UniformPigment
import math.Transformation
import math.Point
import math.Vec
import math.rotationX
import math.rotationY
import math.rotationZ
import math.scaling
import math.translation
import java.io.File

/** A scene read from a scene file */
data class Scene(
	val materials: MutableMap<String, Material> = mutableMapOf(),
	val world: World = World(),
	var camera: Camera? = null,
	val floatVariables: MutableMap<String, Float> = mutableMapOf(),
	val overriddenVariables: MutableSet<String> = mutableSetOf()
)

// --- Check expected functions ---

/** Read a [Token] from [s] input file and check that it matches the given [symbol]. */
fun expectSymbol(s: SceneInputStream, symbol: Char) {
	val token = s.readToken()
	if (token is StopToken) throw GrammarError(token.location, "Unexpected EOF reached when expecting $symbol")
	
	if ((token !is SymbolToken) || (token.symbol != symbol)) throw GrammarError(
		token.location,
		"got $token instead of $symbol in ${token.location}"
	)
	
}

/** Read a [Token] from [s] input file  and check that it is one of the keywords in [Keyword]. */
fun expectKeyword(s: SceneInputStream, keywords: List<Keyword>): Keyword {
	val token = s.readToken()
	if (token is StopToken) throw GrammarError(token.location, "Unexpected EOF reached")
	
	
	if (token !is KeywordToken) throw GrammarError(token.location, "got $token instead of $keywords")
	//use !in if keyword now is a list of Keywords
	if (token.keyword !in keywords) throw GrammarError(
		token.location,
		"expected one of keywords in $keywords instead of $token"
	)
	return token.keyword
}

/**
 * Read a toke from [s] input file and check that it is either a literal number or a variable in [scene].
 * @return the number as a [Float].
 */
fun expectNumber(s: SceneInputStream, scene: Scene): Float {
	val token = s.readToken()
	if (token is StopToken) throw GrammarError(token.location, "Unexpected EOF reached")
	
	if (token is NumberToken) return token.value
	if (token is IdentifierToken) {
		val name = token.identifier // Elvis ?: operator already handles null case
		return scene.floatVariables[name] ?: throw GrammarError(token.location, "Unknown variable $token")
	}
	throw GrammarError(token.location, "Got $token instead of number")
}

/**
 * Read a toke from [s] input file and check that it is a literal string.
 * @return the number as a [String].
 */
fun expectString(s: SceneInputStream): String {
	val token = s.readToken()
	if (token is StopToken) throw GrammarError(token.location, "Unexpected EOF reached")
	
	if (token !is StringToken) throw GrammarError(token.location, "Got $token instead of string")
	return token.value
}

/**
 * Read a [Token] from [s] input file and check that it is an identifier.
 * @return the name of the identifier.
 */
fun expectIdentifier(s: SceneInputStream): String {
	val token = s.readToken()
	if (token is StopToken) throw GrammarError(token.location, "Unexpected EOF reached")
	
	if (token !is IdentifierToken) throw GrammarError(token.location, "Got $token instead of identifier")
	return token.identifier
}


// --- Parsing functions ---

/** Parse a [Triple] of types [Float] from input file [s]. */
fun parseTriple(s: SceneInputStream, scene: Scene): Triple<Float, Float, Float> {
	expectSymbol(s, '(')
	val a = expectNumber(s, scene)
	expectSymbol(s, ',')
	val b = expectNumber(s, scene)
	expectSymbol(s, ',')
	val c = expectNumber(s, scene)
	expectSymbol(s, ')')
	return Triple(a, b, c)
}

/** Parse a [Color] from input file [s]. */
fun parseColor(s: SceneInputStream, scene: Scene): Color {
	val (r, g, b) = parseTriple(s, scene)
	
	return Color(r, g, b)
}

/** Parse a [Vec] from input file [s]. */
fun parseVec(s: SceneInputStream, scene: Scene): Vec {
	val (x, y, z) = parseTriple(s, scene)
	
	return Vec(x, y, z)
}

/** Parse a [Pigment] of types [UniformPigment], [CheckeredPigment] or [ImagePigment] from input file [s]. */
fun parsePigment(s: SceneInputStream, scene: Scene): Pigment {
	val keyword = expectKeyword(s, listOf(Keyword.UNIFORM, Keyword.CHECKERED, Keyword.IMAGE))
	
	expectSymbol(s, '(')
	
	val pigment = when (keyword) { // this way, last line of each when statement becomes return value
		Keyword.UNIFORM -> {
			val color = parseColor(s, scene)
			UniformPigment(color)
		}
		
		Keyword.CHECKERED -> {
			val color1 = parseColor(s, scene)
			expectSymbol(s, ',')
			val color2 = parseColor(s, scene)
			expectSymbol(s, ',')
			val numStep = (expectNumber(s, scene)).toInt()
			CheckeredPigment(color1, color2, numStep)
		}
		
		// instead of give a materials.HDRImage, from parsing.Scene, the compiler read the name of the PFM file,
		// then parser will open it e and close right after (.use)
		Keyword.IMAGE -> {
			val fileName = expectString(s)
			val image = File(fileName).inputStream().use { HDRImage.fromPFMStream(it) }
			ImagePigment(image)
		}
		
		else -> throw AssertionError("Unreachable: parsing.expectKeyword allowed an invalid keyword through!")
	}
	
	expectSymbol(s, ')')
	return pigment
}

/** Parse a [BRDF] of types [DiffuseBRDF] or [SpecularBRDF] from input file [s]. */
fun parseBRDF(s: SceneInputStream, scene: Scene): BRDF {
	val keyword = expectKeyword(s, listOf(Keyword.DIFFUSE, Keyword.SPECULAR))
	expectSymbol(s, '(')
	val pigment = parsePigment(s, scene)
	expectSymbol(s, ')')
	
	return when (keyword) {
		Keyword.DIFFUSE -> {
			DiffuseBRDF(pigment)
		}
		
		Keyword.SPECULAR -> {
			SpecularBRDF(pigment)
		}
		
		else -> throw AssertionError("Unreachable: parsing.expectKeyword allowed an invalid keyword through!")
	}
}

/** Parse a [Material] from input file [s] and return the name of the variable and the [Material] object . */
fun parseMaterial(s: SceneInputStream, scene: Scene): Pair<String, Material> {
	val name = expectIdentifier(s)
	
	expectSymbol(s, '(')
	val brdf = parseBRDF(s, scene)
	expectSymbol(s, ',')
	val emittedRadiance = parsePigment(s, scene)
	expectSymbol(s, ')')
	
	return Pair(name, Material(brdf, emittedRadiance))
}

/** Parse a [Transformation] from input file [s], checking for chained transformation. */
fun parseTransformation(s: SceneInputStream, scene: Scene): Transformation {
	var result = Transformation()
	
	while (true) {
		val keyword = expectKeyword(
			s,
			listOf(
				Keyword.IDENTITY,
				Keyword.TRANSLATION,
				Keyword.ROTATION_X,
				Keyword.ROTATION_Y,
				Keyword.ROTATION_Z,
				Keyword.SCALING
			)
		)
		
		when (keyword) {
			Keyword.IDENTITY -> {
				// Do nothing (primitive optimization!)
			}
			
			Keyword.TRANSLATION -> {
				expectSymbol(s, '(')
				result *= translation(parseVec(s, scene))
				expectSymbol(s, ')')
			}
			
			Keyword.ROTATION_X -> {
				expectSymbol(s, '(')
				result *= rotationX(expectNumber(s, scene))
				expectSymbol(s, ')')
			}
			
			Keyword.ROTATION_Y -> {
				expectSymbol(s, '(')
				result *= rotationY(expectNumber(s, scene))
				expectSymbol(s, ')')
			}
			
			Keyword.ROTATION_Z -> {
				expectSymbol(s, '(')
				result *= rotationZ(expectNumber(s, scene))
				expectSymbol(s, ')')
			}
			
			Keyword.SCALING -> {
				expectSymbol(s, '(')
				result *= scaling(parseVec(s, scene))
				expectSymbol(s, ')')
			}
			
			else -> throw AssertionError("Unreachable: parsing.expectKeyword allowed an invalid keyword through!")
		}
		
		val nextToken = s.readToken() // check if another transformation is being chained: LL(1) parser.
		
		if (nextToken !is SymbolToken || nextToken.symbol != '*') { // not asterisk then needs to be put back
			s.unreadToken(nextToken)
			break
		}
		// if no break reached here, while loop restarts and chains the new transformation until no more '*' found
	}
	return result
}

/** Parse a [Material] and its associated [Transformation] from input file [s]. */
fun parseMaterialTransformation(s: SceneInputStream, scene: Scene): Pair<Material, Transformation> {
	
	expectSymbol(s, '(')
	
	val materialName = expectIdentifier(s)
	val material = scene.materials[materialName] ?:
	//We raise the exception here because input_file is pointing to the end of the wrong identifier
	throw GrammarError(s.location, "Unknown material $materialName")
	
	expectSymbol(s, ',')
	val transformation = parseTransformation(s, scene)
	expectSymbol(s, ')')
	
	return Pair(material, transformation)
}

/** Parse a [Sphere] with its material and transformation from input stream [s]. */
fun parseSphere(s: SceneInputStream, scene: Scene): Sphere {
	
	val (material, transformation) = parseMaterialTransformation(s, scene)
	return Sphere(transformation = transformation, material = material)
}

/** Parse a [Cube] with its material and transformation from input stream [s]. */
fun parseCube(s: SceneInputStream, scene: Scene): Cube {
	
	val (material, transformation) = parseMaterialTransformation(s, scene)
	return Cube(transformation = transformation, material = material)
	
}

/** Parse a [Plane] with its material and transformation from input stream [s]. */
fun parsePlane(s: SceneInputStream, scene: Scene): Plane {
	
	val (material, transformation) = parseMaterialTransformation(s, scene)
	return Plane(transformation = transformation, material = material)
	
}

/** Parse a [Point] from input file [s]. */
fun parsePoint(s: SceneInputStream, scene: Scene): Point {
	val (x, y, z) = parseTriple(s, scene)
	
	return Point(x, y, z)
}

/** Parse a [core.PointLight] from input stream. */
fun parsePointLight(s: SceneInputStream, scene: Scene): PointLight {
	expectSymbol(s, '(')
	val position = parsePoint(s, scene)
	
	expectSymbol(s, ',')
	val color = parseColor(s, scene)
	
	expectSymbol(s, ',')
	val linearRadius = expectNumber(s, scene)
	expectSymbol(s, ')')
	
	return PointLight(
		position = position,
		color = color,
		linearRadius = linearRadius
	)
}

/**
 * Parse either a [PerspectiveCamera] or an [OrthogonalCamera] from [s].
 *
 * The screen distance is used only when constructing a perspective camera.
 */
fun parseCamera(s: SceneInputStream, scene: Scene): Camera {
	
	expectSymbol(s, '(')
	val typeKW = expectKeyword(s, listOf(Keyword.PERSPECTIVE, Keyword.ORTHOGONAL))
	expectSymbol(s, ',')
	val transformation = parseTransformation(s, scene)
	expectSymbol(s, ',')
	val aspectRatio = expectNumber(s, scene)
	expectSymbol(s, ',')
	val distance = expectNumber(s, scene)
	expectSymbol(s, ')')
	
	return when (typeKW) {
		Keyword.PERSPECTIVE -> PerspectiveCamera(distance, aspectRatio = aspectRatio, transformation = transformation)
		Keyword.ORTHOGONAL -> OrthogonalCamera(aspectRatio = aspectRatio, transformation = transformation)
		else -> throw AssertionError("Unreachable: parsing.expectKeyword allowed an invalid keyword through!")
	}
}

/** Read a scene description from input file [s] and return a class [Scene] object. */
fun parseScene(s: SceneInputStream, variables: Map<String, Float> = emptyMap()): Scene {
	val scene = Scene(
		floatVariables = variables.toMutableMap(),
		overriddenVariables = variables.keys.toMutableSet()
	)
	while (true) { // keep reading until EOF
		val what = s.readToken()
		
		if (what is StopToken) break // check EOF
		
		if (what !is KeywordToken) throw GrammarError(what.location, "Expected a keyword instead of $what")
		
		when (what.keyword) {
			Keyword.FLOAT -> {
				val variableName = expectIdentifier(s)
				val variableLoc = s.location.copy() // saved for the error message
				
				expectSymbol(s, '(')
				val variableValue = expectNumber(s, scene)
				expectSymbol(s, ')')
				
				if (variableName in scene.floatVariables && variableName !in scene.overriddenVariables)
					throw GrammarError(variableLoc, "Variable '$variableName' cannot be redefined")
				if (variableName !in scene.overriddenVariables)
				// only define the variable if it was not defined by the user *outside* the scene file (e.g., from the command line)
					scene.floatVariables[variableName] = variableValue
			}
			
			Keyword.SPHERE -> scene.world.addShape(parseSphere(s, scene))
			
			Keyword.CUBE -> scene.world.addShape(parseCube(s, scene))
			
			Keyword.PLANE -> scene.world.addShape(parsePlane(s, scene))
			
			Keyword.POINT_LIGHT -> scene.world.addLight((parsePointLight(s, scene)))
			
			Keyword.CAMERA -> {
				if (scene.camera != null) throw GrammarError(
					what.location,
					"You cannot define more than one camera"
				)
				scene.camera = parseCamera(s, scene)
			}
			
			Keyword.MATERIAL -> {
				val (name, material) = parseMaterial(s, scene)
				scene.materials[name] = material
			}
			
			else -> throw GrammarError(what.location, "Unexpected token $what at ${what.location}")
		}
	}
	return scene
}