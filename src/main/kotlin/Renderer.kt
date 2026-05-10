interface Renderer {
	val world: World
}

class OnOffRenderer(
	override val world: World = World()
) : Renderer {

}

class flatRenderer(
	override val world: World = World()
) : Renderer {

}