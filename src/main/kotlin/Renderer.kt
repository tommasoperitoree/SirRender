interface Renderer {
	val world: World
}

class OnOffRenderer(
	override val world: World = World()
) : Renderer {

}

class FlatRenderer(
	override val world: World = World()
) : Renderer {

}