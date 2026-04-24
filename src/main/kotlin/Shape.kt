interface Shape {
	// if we need to have shape already have method Transformation, then interface does not work
	fun rayIntersection(ray: Ray): HitRecord? =
		throw NotImplementedError("Shape.rayIntersection() is abstract")
}

class Sphere() {

}