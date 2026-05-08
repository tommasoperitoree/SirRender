data class PCG(
	var state: ULong,
	var inc: ULong
) {
	companion object {
		fun seeded(initState: ULong = 42uL, initSeq: ULong = 54uL): PCG {
			val pcg = PCG(0uL, 0uL)
			pcg.inc = (initSeq shl 1) or 1uL
			pcg.random()
			pcg.state += initState
			pcg.random()
			return pcg
		}
	}
	
	fun random(): UInt {
		val oldState = state
		state = oldState * 6364136223846793005u + inc
		val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
		val rot = (oldState shr 59).toInt()
		
		return xorShifted.rotateRight(rot)
	}
}

class PCGRegClass(
	initState: ULong = 42uL,
	initSeq: ULong = 54uL
) {
	// These are your actual class properties
	var state: ULong = 0uL
		private set // Good practice: prevent outside code from messing with the PRNG state
	
	var inc: ULong = 0uL
		private set
	
	init {
		// initState and initSeq are perfectly valid to use here
		inc = (initSeq shl 1) or 1uL
		random()
		state += initState
		random()
	}
	
	fun random(): UInt {
		val oldState = state
		state = oldState * 6364136223846793005uL + inc
		val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
		val rot = (oldState shr 59).toInt()
		
		return xorShifted.rotateRight(rot)
	}
}