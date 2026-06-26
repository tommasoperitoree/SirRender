package cli

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) =
	SirRender() // tie together all subcommands and prepare CLI
		.subcommands(
			Pfm2Png(),
			Demo(),
			Animation(),
			Render()
		).main(args)

// ./gradlew run --args="demo -w 640 -h 480 -c "Orthogonal" -o demo.png"
// ./gradlew run --args="animation --width=480 --height=480 --output demo.png --num-frames=72"
//./gradlew run --args="render -r -inp SceneR/cube.txt -w 1280 -f0.5 -h 720 -a 3 -o output"