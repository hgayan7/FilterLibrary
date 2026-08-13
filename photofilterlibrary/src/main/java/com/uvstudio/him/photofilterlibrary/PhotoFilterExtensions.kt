package com.uvstudio.him.photofilterlibrary

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Idiomatic Kotlin extension functions for Bitmaps, ImageViews, and Bitmap collections.
 */

/**
 * Applies a preset filter with optional intensity (0.0f .. 1.0f).
 */
fun Bitmap.applyFilter(
    filterType: FilterType,
    intensity: Float = 1.0f,
    extraParams: Map<String, Any>? = null
): Bitmap {
    return FilterEngine.applyFilter(this, filterType, intensity, extraParams)
}

/**
 * Applies a filter asynchronously on Dispatchers.Default.
 */
suspend fun Bitmap.applyFilterAsync(
    filterType: FilterType,
    intensity: Float = 1.0f,
    extraParams: Map<String, Any>? = null
): Bitmap = withContext(Dispatchers.Default) {
    FilterEngine.applyFilter(this@applyFilterAsync, filterType, intensity, extraParams)
}

/**
 * Adjusts composite color properties in a single pass.
 */
fun Bitmap.adjust(
    brightness: Float = 0.0f,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f,
    warmth: Float = 0.0f,
    tint: Float = 0.0f
): Bitmap {
    return FilterEngine.applyAdjustments(this, brightness, contrast, saturation, warmth, tint)
}

/**
 * Applies Duotone two-color mapping.
 */
fun Bitmap.applyDuotone(
    shadowColor: Int,
    highlightColor: Int,
    intensity: Float = 1.0f
): Bitmap {
    return FilterEngine.applyDuotone(this, shadowColor, highlightColor, intensity)
}

/**
 * Applies a 3D HALD LUT table.
 */
fun Bitmap.applyHaldLut(
    lutBitmap: Bitmap,
    intensity: Float = 1.0f
): Bitmap {
    return FilterEngine.applyHaldLut(this, lutBitmap, intensity)
}

/**
 * Geometric Transformations.
 */
fun Bitmap.rotate(degrees: Float): Bitmap = FilterEngine.rotate(this, degrees)
fun Bitmap.flipHorizontal(): Bitmap = FilterEngine.flip(this, horizontal = true, vertical = false)
fun Bitmap.flipVertical(): Bitmap = FilterEngine.flip(this, horizontal = false, vertical = true)
fun Bitmap.crop(left: Int, top: Int, width: Int, height: Int): Bitmap = FilterEngine.crop(this, left, top, width, height)

/**
 * Watermarking & Date Stamp.
 */
fun Bitmap.applyDateStamp(
    text: String,
    color: Int = Color.rgb(255, 140, 20),
    position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT
): Bitmap = FilterEngine.applyDateStamp(this, text, color, position)

fun Bitmap.applyWatermark(
    watermark: Bitmap,
    position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    alpha: Float = 0.8f
): Bitmap = FilterEngine.applyWatermark(this, watermark, position, alpha)

/**
 * Palette Extraction (Material You color theming).
 */
fun Bitmap.extractPalette(count: Int = 5): List<Int> = FilterEngine.extractPalette(this, count)

/**
 * Batch processing on a collection of Bitmaps.
 */
fun List<Bitmap>.applyFilterBatch(filterType: FilterType, intensity: Float = 1.0f): List<Bitmap> = runBlocking {
    FilterEngine.applyBatchAsync(this@applyFilterBatch, filterType, intensity)
}

suspend fun List<Bitmap>.applyFilterBatchAsync(filterType: FilterType, intensity: Float = 1.0f): List<Bitmap> {
    return FilterEngine.applyBatchAsync(this, filterType, intensity)
}

/**
 * Convenience methods for common filters.
 */
fun Bitmap.toGrayscale(intensity: Float = 1.0f): Bitmap = applyFilter(FilterType.GRAYSCALE, intensity)
fun Bitmap.toSepia(intensity: Float = 1.0f): Bitmap = applyFilter(FilterType.SEPIA, intensity)
fun Bitmap.toVintage(intensity: Float = 1.0f): Bitmap = applyFilter(FilterType.VINTAGE, intensity)
fun Bitmap.toNoir(intensity: Float = 1.0f): Bitmap = applyFilter(FilterType.NOIR, intensity)
fun Bitmap.toTealAndOrange(intensity: Float = 1.0f): Bitmap = applyFilter(FilterType.TEAL_AND_ORANGE, intensity)
fun Bitmap.toCyberpunk(intensity: Float = 1.0f): Bitmap = applyFilter(FilterType.CYBERPUNK, intensity)
fun Bitmap.applyVignette(amount: Float = 0.5f): Bitmap = FilterEngine.applyVignette(this, amount)
fun Bitmap.applyFilmGrain(intensity: Float = 0.2f): Bitmap = FilterEngine.applyFilmGrain(this, intensity)
fun Bitmap.applySharpen(intensity: Float = 1.0f): Bitmap = FilterEngine.applySharpen(this, intensity)
fun Bitmap.applyBlur(radius: Int): Bitmap = FilterEngine.applyFastBlur(this, radius)

/**
 * Zero-allocation visual filter on ImageView using ColorFilter.
 * Does NOT mutate or duplicate the Bitmap in memory.
 */
fun ImageView.applyFilter(filterType: FilterType, intensity: Float = 1.0f) {
    this.colorFilter = FilterEngine.createColorFilter(filterType, intensity)
}

/**
 * Clears any applied ColorFilter from the ImageView.
 */
fun ImageView.clearFilter() {
    this.colorFilter = null
}
