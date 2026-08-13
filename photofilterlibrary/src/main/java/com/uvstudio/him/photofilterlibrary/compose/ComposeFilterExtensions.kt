package com.uvstudio.him.photofilterlibrary.compose

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.uvstudio.him.photofilterlibrary.FilterEngine
import com.uvstudio.him.photofilterlibrary.FilterType

/**
 * Converts a [FilterType] preset into a Jetpack Compose [ColorFilter].
 *
 * Can be passed directly to `Image(painter = ..., contentDescription = ..., colorFilter = filter.toComposeColorFilter(intensity))`.
 */
fun FilterType.toComposeColorFilter(intensity: Float = 1.0f): ColorFilter? {
    val matrix = FilterEngine.getColorMatrixForFilter(this) ?: return null
    val clampedIntensity = intensity.coerceIn(0f, 1f)
    if (clampedIntensity == 0f) return null

    val identity = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    val interpolated = FloatArray(20)
    for (i in 0 until 20) {
        interpolated[i] = identity[i] * (1f - clampedIntensity) + matrix[i] * clampedIntensity
    }

    return ColorFilter.colorMatrix(ColorMatrix(interpolated))
}
