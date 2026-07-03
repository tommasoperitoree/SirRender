package math

/**
 * A PCG (Permuted Congruential Generator) pseudo-random number generator.
 *
 * Produces a statistically high-quality, fast sequence of pseudo-random 32-bit integers
 * and uniform floats in [0, 1). Each instance maintains independent state — use separate
 * instances per thread. Not thread-safe.
 *
 * @param initState Seeds the internal state; different values yield different sequences.
 * @param initSeq   Selects the stream; two generators sharing [initState] but with
 *                  different [initSeq] produce uncorrelated sequences.
 */
class PCG(
	initState: ULong = 42uL,
	initSeq: ULong = 54uL
) {
	
	var state: ULong = 0uL
		private set
	var inc: ULong = 0uL
		private set
	
	init {
		inc = (initSeq shl 1) or 1uL
		random()
		state += initState
		random()
	}
	
	/** Generate random 32-bit integer. */
	fun random(): UInt {
		val oldState = state
		state = oldState * 6364136223846793005uL + inc
		val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
		val rot = (oldState shr 59).toInt()
		
		return xorShifted.rotateRight(rot)
	}
	
	/** Generate random float uniformly distributed over [0,1). */
	fun randomFloat(): Float = random().toFloat() / 0xffffffff
}