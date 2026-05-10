data class PCG(
	var state: ULong,
	var inc: ULong
) {
	// secondary constructor with parameters in data class is a companion object
	companion object {
		// callable with `var pcg = PCG.seeded()`
		// or with e.g. `var pcg = PCG.seeded(initState = 0ul, initSeq = 2uL)`
		fun seeded(initState: ULong = 42uL, initSeq: ULong = 54uL): PCG {
			val pcg = PCG(0uL, 0uL)
			pcg.inc = (initSeq shl 1) or 1uL
			pcg.random()
			pcg.state += initState
			pcg.random()
			return pcg
		}
	}
	
	/** Generate random 32-bit integer. */
	fun random(): UInt {
		val oldState = state
		state = oldState * 6364136223846793005u + inc
		val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
		val rot = (oldState shr 59).toInt()
		
		return xorShifted.rotateRight(rot)
	}
	
	/** Returns a normalized 32-bit integer, i.e. a [Float] ∈ [0, 1), by calling [random]. */
	fun randomFloat(): Float = (random() / 0x100000000uL).toFloat()  // Result ∈ [0, 1)
	
}

class PCGRegClass(
	initState: ULong = 42uL,
	initSeq: ULong = 54uL
) {
	// with `var` one tags the actual class properties
	var state: ULong = 0uL
		private set // prevent outside code from messing with vars
	var inc: ULong = 0uL
		private set
	
	// default call whenever class gets initialized
	// var pcg = PCGRegClass() default initializer calls also this init block
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
}