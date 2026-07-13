package core

import kotlin.math.max

/**
 * A thread-safe terminal progress bar with ETA estimation.
 *
 * [update] is safe to call from multiple threads concurrently (synchronized).
 * The bar redraws in-place using a carriage return (`\r`).
 */
class ProgressBar(
	private val total: Long,
	private val barWidth: Int = 40
) {
	private val startTime = System.nanoTime()
	private var lastPrintTime = 0L
	
	/**
	 * Redraws the progress bar to reflect [done] out of `total` units completed.
	 *
	 * Output is rate-limited to at most once every 100 ms unless [force] is `true`
	 * or the bar has just reached completion.
	 */
	@Synchronized
	fun update(done: Long, force: Boolean = false) {
		val now = System.nanoTime()
		
		// Rate-limit output: redraw at most once every 100 ms to avoid terminal spam.
		if (!force && now - lastPrintTime < 100_000_000L && done < total) {
			return
		}
		
		lastPrintTime = now
		
		val progress = if (total > 0) done.toDouble() / total.toDouble() else 1.0
		val clampedProgress = progress.coerceIn(0.0, 1.0)
		
		val filled = (clampedProgress * barWidth).toInt().coerceIn(0, barWidth)
		val empty = barWidth - filled
		val percent = clampedProgress * 100.0
		
		val elapsedSeconds = (now - startTime) / 1_000_000_000.0
		val speed = if (elapsedSeconds > 0.0) done / elapsedSeconds else 0.0
		val remaining = max(0L, total - done)
		val etaSeconds = if (speed > 0.0) remaining / speed else 0.0
		
		val bar = buildString {
			append("[")
			append("=".repeat(filled))
			append(" ".repeat(empty))
			append("]")
		}
		
		print(
			"\r$bar ${"%.1f".format(percent)}% " +
					"($done/$total pixel) " +
					"ETA: ${formatTime(etaSeconds)}"
		)
		
		if (done >= total) {
			println()
		}
	}
	
	private fun formatTime(seconds: Double): String {
		val totalSeconds = seconds.toLong()
		val hours = totalSeconds / 3600
		val minutes = (totalSeconds % 3600) / 60
		val secs = totalSeconds % 60
		
		return if (hours > 0) {
			"%02d:%02d:%02d".format(hours, minutes, secs)
		} else {
			"%02d:%02d".format(minutes, secs)
		}
	}
}