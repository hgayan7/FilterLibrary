package com.uvstudio.him.photofilterlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main entry point for PhotoFilter library v2.0.0.
 *
 * Provides high-performance, hardware-accelerated image filtering with:
 * - 35+ named presets across classic, vintage, cinematic, social, and artistic categories.
 * - Dynamic intensity blending (0.0f .. 1.0f).
 * - Fluent builder pipelines.
 * - Non-blocking coroutines and Java callbacks.
 * - Zero-allocation [ColorFilter] generation.
 * - Full backward compatibility for legacy v1.0 methods.
 */
class PhotoFilter {

    companion object {

        /**
         * Applies a filter preset with optional intensity synchronously.
         *
         * @param bitmap The source Bitmap.
         * @param filterType The preset [FilterType] to apply.
         * @param intensity The blend strength from 0.0f (no effect) to 1.0f (full effect). Default is 1.0f.
         * @return The filtered [Bitmap].
         */
        @JvmStatic
        @JvmOverloads
        fun apply(
            bitmap: Bitmap,
            filterType: FilterType,
            intensity: Float = 1.0f
        ): Bitmap {
            return FilterEngine.applyFilter(bitmap, filterType, intensity)
        }

        /**
         * Applies a filter preset asynchronously with a Java-friendly [FilterCallback].
         */
        @JvmStatic
        @JvmOverloads
        fun applyAsync(
            bitmap: Bitmap,
            filterType: FilterType,
            intensity: Float = 1.0f,
            callback: FilterCallback
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val result = FilterEngine.applyFilter(bitmap, filterType, intensity)
                    withContext(Dispatchers.Main) {
                        callback.onSuccess(result)
                    }
                } catch (t: Throwable) {
                    withContext(Dispatchers.Main) {
                        callback.onError(t)
                    }
                }
            }
        }

        /**
         * Creates a zero-allocation [ColorFilter] for View or Compose drawing without bitmap allocation.
         */
        @JvmStatic
        @JvmOverloads
        fun getColorFilter(filterType: FilterType, intensity: Float = 1.0f): ColorFilter? {
            return FilterEngine.createColorFilter(filterType, intensity)
        }

        /**
         * Starts a fluent pipeline builder for combining filters, adjustments, and effects.
         */
        @JvmStatic
        fun builder(bitmap: Bitmap): PhotoFilterBuilder {
            return PhotoFilterBuilder(bitmap)
        }
    }

    // =========================================================================
    // LEGACY 1.0 BACKWARD COMPATIBILITY METHODS
    // (Preserved without requiring RenderScript; mapped to named presets)
    // =========================================================================

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.INVERT) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.INVERT)")
    )
    fun one(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.INVERT)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.GRAYSCALE) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.GRAYSCALE)")
    )
    fun two(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.GRAYSCALE)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.CYAN_BOOST) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.CYAN_BOOST)")
    )
    fun three(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.CYAN_BOOST)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.MOODY_DARK) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.MOODY_DARK)")
    )
    fun four(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.MOODY_DARK)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.CROSS_PROCESS) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.CROSS_PROCESS)")
    )
    fun five(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.CROSS_PROCESS)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.RETRO_WARM) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.RETRO_WARM)")
    )
    fun six(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.RETRO_WARM)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.HIGH_KEY) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.HIGH_KEY)")
    )
    fun seven(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.HIGH_KEY)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.BRIGHTNESS_BOOST) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.BRIGHTNESS_BOOST)")
    )
    fun eight(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.BRIGHTNESS_BOOST)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.DRAMATIC) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.DRAMATIC)")
    )
    fun nine(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.DRAMATIC)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.COOL_BLUE) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.COOL_BLUE)")
    )
    fun ten(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.COOL_BLUE)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.POLAROID_70S) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.POLAROID_70S)")
    )
    fun eleven(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.POLAROID_70S)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.SEPIA) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.SEPIA)")
    )
    fun twelve(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.SEPIA)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.EMBOSS) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.EMBOSS)")
    )
    fun thirteen(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.EMBOSS)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.SOFT_BLUR) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.SOFT_BLUR)")
    )
    fun fourteen(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.SOFT_BLUR)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.CINEMATIC_POP) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.CINEMATIC_POP)")
    )
    fun fifteen(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.CINEMATIC_POP)

    @Deprecated(
        message = "Use PhotoFilter.apply(bitmap, FilterType.WARM_SUNSET) instead.",
        replaceWith = ReplaceWith("PhotoFilter.apply(bitmap, FilterType.WARM_SUNSET)")
    )
    fun sixteen(context: Context?, bitmap: Bitmap): Bitmap = apply(bitmap, FilterType.WARM_SUNSET)
}
