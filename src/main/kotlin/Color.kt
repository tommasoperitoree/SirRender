package org.example

import kotlin.math.abs

// Checks if two Floats are within epsilon
fun areClose (x: Float, y: Float, epsilon: Float = 1e-5f) =
    abs( x - y ) < epsilon

/** An RGB color
 *
 * @param r The level of red
 * @param g The level of green
 * @param b The level of blue
 */
data class Color(val r: Float = 0.0f, val g: Float = 0.0f, val b: Float = 0.0f) {

    operator fun plus(other: Color) : Color =
        Color(r + other.r, g + other.g, b + other.b)

    operator fun times(scalar: Float) : Color =
        Color(r * scalar, g * scalar, b * scalar)

    operator fun times(other: Color) : Color =
        Color(r * other.r, g * other.g, b * other.b)

    fun areColorsClose(other: Color) =
        areClose (r,other.r) && areClose (g,other.g) && areClose (b,other.b)


}

