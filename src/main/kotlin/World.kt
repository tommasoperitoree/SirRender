class World {
	
	val shapes = mutableListOf<Shape>()
	
	fun addShape(shape: Shape) =
		shapes.add(shape)
	
	fun rayIntersection(ray: Ray): HitRecord? {
		var closest: HitRecord? = null
		
		for (shape in shapes) {
			val intersection: HitRecord = shape.rayIntersection(ray) ?: continue //if(intersection==null) continue
			
			if (closest == null || intersection.t < closest.t) {
				closest = intersection
			}
		}
		return closest
	}
}