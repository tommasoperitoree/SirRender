package materials

/**
 * Combines a [BRDF] (how the surface reflects light) with an optional emitted radiance
 * [Pigment] (how much light the surface emits). Emissive-only surfaces should use
 * `DiffuseBRDF(UniformPigment(Color.black))` as the BRDF to avoid energy gain.
 */
data class Material(
	val brdf: BRDF = DiffuseBRDF(),
	val emittedRadiance: Pigment = UniformPigment(Color.black)
)