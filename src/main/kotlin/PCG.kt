data class PCG(
	var state: ULong = 0uL,
	var inc: ULong = 0uL,
	val initState: ULong = 42uL,
	val initSeq: ULong = 54uL
) {
	init {
		state = 0uL
		inc = (initSeq shl 1) or 1uL
		random()
		state += initState
		random()
	}
	
	fun random(): UInt {
		val oldState = state
		state = oldState * 6364136223846793005u + inc
		val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
		val rot = (oldState shr 59).toInt()
		
		return xorShifted.rotateRight(rot)
		
	}
}


