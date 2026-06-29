package materials


data class Material(
	val brdf: BRDF = DiffuseBRDF(),
	val emittedRadiance: Pigment = UniformPigment(Color.black)
)