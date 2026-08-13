package com.uvstudio.him.photofilterlibrary

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fluent builder for applying multiple filters, adjustments, geometry transforms,
 * and effects in an optimized pipeline.
 */
class PhotoFilterBuilder(private val source: Bitmap) {

    private var activeFilter: FilterType? = null
    private var filterIntensity: Float = 1.0f
    private var extraParams: MutableMap<String, Any> = mutableMapOf()

    private var brightness: Float = 0.0f
    private var contrast: Float = 1.0f
    private var saturation: Float = 1.0f
    private var warmth: Float = 0.0f
    private var tint: Float = 0.0f

    private var vignetteAmount: Float = 0.0f
    private var grainIntensity: Float = 0.0f
    private var sharpenIntensity: Float = 0.0f
    private var blurRadius: Int = 0

    private var duotoneShadowColor: Int? = null
    private var duotoneHighlightColor: Int? = null
    private var duotoneIntensity: Float = 1.0f

    private var lutBitmap: Bitmap? = null
    private var lutIntensity: Float = 1.0f

    private var rotationDegrees: Float = 0.0f
    private var flipHoriz: Boolean = false
    private var flipVert: Boolean = false
    private var cropParams: CropParams? = null

    private var dateStampText: String? = null
    private var dateStampColor: Int = Color.rgb(255, 140, 20)
    private var dateStampPosition: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT

    private var watermarkBitmap: Bitmap? = null
    private var watermarkPosition: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT
    private var watermarkAlpha: Float = 0.8f

    private data class CropParams(val left: Int, val top: Int, val width: Int, val height: Int)

    /**
     * Applies a preset [FilterType] with specified intensity (0.0f to 1.0f).
     */
    fun filter(filterType: FilterType, intensity: Float = 1.0f): PhotoFilterBuilder = apply {
        this.activeFilter = filterType
        this.filterIntensity = intensity.coerceIn(0f, 1f)
    }

    /**
     * Adjusts brightness (-1.0f to 1.0f, where 0.0f is neutral).
     */
    fun brightness(value: Float): PhotoFilterBuilder = apply {
        this.brightness = value.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Adjusts contrast (0.0f to 3.0f, where 1.0f is neutral).
     */
    fun contrast(value: Float): PhotoFilterBuilder = apply {
        this.contrast = value.coerceAtLeast(0f)
    }

    /**
     * Adjusts color saturation (0.0f for B&W to 3.0f, where 1.0f is neutral).
     */
    fun saturation(value: Float): PhotoFilterBuilder = apply {
        this.saturation = value.coerceAtLeast(0f)
    }

    /**
     * Adjusts color temperature / warmth (-1.0f cool blue to 1.0f warm amber).
     */
    fun warmth(value: Float): PhotoFilterBuilder = apply {
        this.warmth = value.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Adjusts tint (-1.0f magenta to 1.0f green).
     */
    fun tint(value: Float): PhotoFilterBuilder = apply {
        this.tint = value.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Rotates image by degrees (e.g. 90, 180, 270).
     */
    fun rotate(degrees: Float): PhotoFilterBuilder = apply {
        this.rotationDegrees += degrees
    }

    /**
     * Flips image horizontally (mirror).
     */
    fun flipHorizontal(): PhotoFilterBuilder = apply {
        this.flipHoriz = !this.flipHoriz
    }

    /**
     * Flips image vertically.
     */
    fun flipVertical(): PhotoFilterBuilder = apply {
        this.flipVert = !this.flipVert
    }

    /**
     * Crops a rectangular portion of the image.
     */
    fun crop(left: Int, top: Int, width: Int, height: Int): PhotoFilterBuilder = apply {
        this.cropParams = CropParams(left, top, width, height)
    }

    /**
     * Adds a vintage 1990s analog camera date stamp (e.g. "'98 08 13").
     */
    fun dateStamp(
        text: String,
        color: Int = Color.rgb(255, 140, 20),
        position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT
    ): PhotoFilterBuilder = apply {
        this.dateStampText = text
        this.dateStampColor = color
        this.dateStampPosition = position
    }

    /**
     * Overlays a watermark logo bitmap.
     */
    fun watermark(
        watermark: Bitmap,
        position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
        alpha: Float = 0.8f
    ): PhotoFilterBuilder = apply {
        this.watermarkBitmap = watermark
        this.watermarkPosition = position
        this.watermarkAlpha = alpha.coerceIn(0f, 1f)
    }

    /**
     * Applies optical vignette darkening at corners (0.0f to 1.0f).
     */
    fun vignette(amount: Float): PhotoFilterBuilder = apply {
        this.vignetteAmount = amount.coerceIn(0f, 1f)
    }

    /**
     * Applies organic analog film grain (0.0f to 1.0f).
     */
    fun grain(intensity: Float): PhotoFilterBuilder = apply {
        this.grainIntensity = intensity.coerceIn(0f, 1f)
    }

    /**
     * Applies micro-edge sharpening (0.0f to 1.0f).
     */
    fun sharpen(intensity: Float): PhotoFilterBuilder = apply {
        this.sharpenIntensity = intensity.coerceIn(0f, 1f)
    }

    /**
     * Applies smooth blur with specified pixel radius.
     */
    fun blur(radius: Int): PhotoFilterBuilder = apply {
        this.blurRadius = radius.coerceAtLeast(0)
    }

    /**
     * Applies two-color Duotone mapping (Spotify-style).
     */
    fun duotone(shadowColor: Int, highlightColor: Int, intensity: Float = 1.0f): PhotoFilterBuilder = apply {
        this.duotoneShadowColor = shadowColor
        this.duotoneHighlightColor = highlightColor
        this.duotoneIntensity = intensity.coerceIn(0f, 1f)
    }

    /**
     * Applies a 3D HALD LUT bitmap (from Lightroom / Photoshop).
     */
    fun lut(lutBitmap: Bitmap, intensity: Float = 1.0f): PhotoFilterBuilder = apply {
        this.lutBitmap = lutBitmap
        this.lutIntensity = intensity.coerceIn(0f, 1f)
    }

    /**
     * Executes the entire processing pipeline synchronously.
     */
    fun build(): Bitmap {
        var current = source

        // 1. Geometry transforms (Crop -> Rotate -> Flip)
        cropParams?.let { c ->
            current = FilterEngine.crop(current, c.left, c.top, c.width, c.height)
        }
        if (rotationDegrees != 0f) {
            current = FilterEngine.rotate(current, rotationDegrees)
        }
        if (flipHoriz || flipVert) {
            current = FilterEngine.flip(current, flipHoriz, flipVert)
        }

        // 2. Preset filter
        activeFilter?.let { filter ->
            current = FilterEngine.applyFilter(current, filter, filterIntensity, extraParams)
        }

        // 3. Adjustments (single pass)
        val hasAdjustments = brightness != 0.0f || contrast != 1.0f || saturation != 1.0f || warmth != 0.0f || tint != 0.0f
        if (hasAdjustments) {
            current = FilterEngine.applyAdjustments(current, brightness, contrast, saturation, warmth, tint)
        }

        // 4. LUT if specified
        lutBitmap?.let { lut ->
            current = FilterEngine.applyHaldLut(current, lut, lutIntensity)
        }

        // 5. Duotone if specified
        if (duotoneShadowColor != null && duotoneHighlightColor != null) {
            current = FilterEngine.applyDuotone(current, duotoneShadowColor!!, duotoneHighlightColor!!, duotoneIntensity)
        }

        // 6. Sharpen / Blur
        if (sharpenIntensity > 0f) {
            current = FilterEngine.applySharpen(current, sharpenIntensity)
        }
        if (blurRadius > 0) {
            current = FilterEngine.applyFastBlur(current, blurRadius)
        }

        // 7. Vignette
        if (vignetteAmount > 0f) {
            current = FilterEngine.applyVignette(current, vignetteAmount)
        }

        // 8. Film Grain
        if (grainIntensity > 0f) {
            current = FilterEngine.applyFilmGrain(current, grainIntensity)
        }

        // 9. Watermark overlay
        watermarkBitmap?.let { wm ->
            current = FilterEngine.applyWatermark(current, wm, watermarkPosition, watermarkAlpha)
        }

        // 10. Vintage Date stamp
        dateStampText?.let { date ->
            current = FilterEngine.applyDateStamp(current, date, dateStampColor, dateStampPosition)
        }

        return current
    }

    /**
     * Executes the processing pipeline asynchronously on Dispatchers.Default.
     */
    suspend fun buildAsync(): Bitmap = withContext(Dispatchers.Default) {
        build()
    }
}

/**
 * Kotlin DSL builder for fluid filter pipelines.
 *
 * Example:
 * ```kotlin
 * val result = photoFilter(myBitmap) {
 *     rotate(90f)
 *     filter(FilterType.TEAL_AND_ORANGE, intensity = 0.8f)
 *     brightness(0.1f)
 *     vignette(0.3f)
 *     dateStamp("'98 08 13")
 * }
 * ```
 */
inline fun photoFilter(source: Bitmap, block: PhotoFilterBuilder.() -> Unit): Bitmap {
    return PhotoFilterBuilder(source).apply(block).build()
}

/**
 * Suspend Kotlin DSL builder for non-blocking filter pipelines.
 */
suspend inline fun photoFilterAsync(source: Bitmap, crossinline block: PhotoFilterBuilder.() -> Unit): Bitmap {
    return withContext(Dispatchers.Default) {
        PhotoFilterBuilder(source).apply(block).build()
    }
}
