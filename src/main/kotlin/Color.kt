package org.example

import kotlin.math.abs

/** A RGB color
 *
 * @param r The level of red
 * @param g The level of green
 * @param b The level of blue
 */

fun are_close (x: Float, y: Float, epsilon: Float = 1e-5f)
= abs( x - y ) < epsilon

public data class Color(val r: Float = 0.0f, val g: Float = 0.0f, val b: Float = 0.0f) {

    operator fun plus(other: Color) : Color
    = Color(r + other.r, g + other.g, b + other.b)

    operator fun times(scalar: Float) : Color
    = Color(r * scalar, g * scalar, b * scalar)

    operator fun times(other: Color) : Color
    = Color(r * other.r, g * other.g, b * other.b)

    operator fun plus(other: Color):Color{
        return Color(r+other.r,g+other.g,b+other.b)
    }
    /* questa funzione è inutile voglio fare un conflitto*/

    fun are_colors_close(other: Color)
    = are_close (r,other.r) && are_close (g,other.g) && are_close (b,other.b)

}

