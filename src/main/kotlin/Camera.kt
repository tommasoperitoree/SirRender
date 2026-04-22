/**
 * An abstract observer capable of projecting a 3D scene onto a 2D plane.
 * Concrete subclasses [OrthogonalCamera] and [PerspectiveCamera] define
 * specific projection geometries.
 */
interface Camera {
	/**
	 * Fires a [Ray] through the camera screen at normalized coordinates ([u], [v]).
	 * The exact projection logic is implemented by derived classes.
	 */
	fun fireRay(u: Float, v: Float): Ray =
		throw NotImplementedError("Camera.fireRay($u, $v) is not implemented")
}


/**
 * A camera implementing an orthogonal 3D to 2D projection: parallel rays are cast from the screen plane,
 * preserving the relative size of objects regardless of their distance from the observer.
 *
 * @property aspectRatio Defines the width/height ratio (e.g., 1.77 for 16:9).
 * @property transformation Initial orientation and position in the world.
 */
class OrthogonalCamera(
	val aspectRatio: Float = 1f,
	val transformation: Transformation = Transformation()
) : Camera {
	
	override fun fireRay(u: Float, v: Float): Ray {
		val origin = Point(-1f, (1 - 2 * u) * aspectRatio, 2 * v - 1)
		val dir = Vec(1f, 0f, 0f)
		return Ray(origin, dir).transform(transformation)
	}
}


/**
 * A camera implementing a perspective 3D to 2D projection: simulates a pinhole camera
 * where rays converge at a single point, creating a realistic sense of depth and foreshortening.
 *
 * @property distance The distance between the observer's eye and the projection screen.
 * @property aspectRatio Defines the width/height ratio (e.g., 1.77 for 16:9).
 * @property transformation Initial orientation and position in the world.
 */
class PerspectiveCamera(
	var distance: Float = 1f,
	var aspectRatio: Float = 1f,
	var transformation: Transformation = Transformation()
) : Camera {
	
	override fun fireRay(u: Float, v: Float): Ray {
		val origin = Point(-distance, 0f, 0f)
		val direction = Vec(distance, (1 - 2 * u) * aspectRatio, 2 * v - 1)
		return Ray(origin, direction).transform(transformation)
	}
}