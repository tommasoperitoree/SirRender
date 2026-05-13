class PCG(
	initState: ULong = 42uL,
	initSeq: ULong = 54uL
) {
	// with `var` one tags the actual class properties
	var state: ULong = 0uL
		private set // prevent outside code from messing with vars
	var inc: ULong = 0uL
		private set
	
	// default call whenever class gets initialized
	// var pcg = PCG() default initializer calls also this init block
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
	
	/** Generate random float uniformly distributed over [0,1]. */
	fun randomFloat(): Float = random().toFloat()
}