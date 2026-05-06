data class PCG(
	var state: ULong = 0uL,
	val inc: ULong = 0uL
) {
	
	fun random(): UInt {
		val oldState = state
		state = oldState * 6364136223846793005u + inc
		val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
		val rot = oldState shr 59
		
		return xorShifted.rotateRight(rot.toInt())
		
	}
}


