import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class `Transormation Test` {
val t=Vec(10f,1f,2f)
val p=Point(1f,2f,3f)

@Test
fun `traslation test`(){
	val p1=Point(11f,3f,5f)
	val trasl=translation(t)
	assertTrue((trasl*p).isClose(p1))
}

}
