/**
 *
 */
interface Camera {
	
	fun fireRay(u: Float, v: Float) {}

}

class OrthogonalCamera(
	val a: Float,
	val transformation: Transformation
) : Camera {
	
	fun fireRay(){
	
	}
}


class PerspectiveCamera(
	var distance: Float = 1.0f,
	
) : Camera {

}