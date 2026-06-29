package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

/**
 * Root CLI command. Registers all subcommands and routes invocations.
 *
 * Subcommands: `demo`, `render`, `pfm-to-gif`, `pfm2png`.
 */
class SirRender : CliktCommand() {
	override fun help(context: Context) = "SirRender: a ray tracer CLI"
	override fun run() = Unit
}


/**
 * Formats a [Long] in two-decimal scientific notation (e.g. `9.22e+05`).
 * Used in the render startup summary to display pixel and ray counts concisely.
 */
internal fun Long.toSci(): String = "%.2e".format(this.toDouble())