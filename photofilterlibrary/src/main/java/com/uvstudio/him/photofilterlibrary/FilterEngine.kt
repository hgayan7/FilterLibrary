package com.uvstudio.him.photofilterlibrary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * High-performance, hardware-accelerated image filtering and color-grading engine.
 *
 * Replaces deprecated RenderScript with native ColorMatrix Canvas drawing,
 * parallel coroutine convolution processing, 3D HALD LUT mapping, and Duotone shading.
 */
object FilterEngine {

    private val IDENTITY_MATRIX = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    /**
     * Retrieves the 4x5 ColorMatrix corresponding to a specific [FilterType].
     * Returns null if the filter is a custom pixel/convolution/LUT/Duotone effect.
     */
    fun getColorMatrixForFilter(filterType: FilterType): FloatArray? {
        return when (filterType) {
            FilterType.GRAYSCALE -> floatArrayOf(
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.NOIR -> floatArrayOf(
                0.45f, 0.88f, 0.17f, 0f, -40f,
                0.45f, 0.88f, 0.17f, 0f, -40f,
                0.45f, 0.88f, 0.17f, 0f, -40f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.SEPIA -> floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.MONO_WARM -> floatArrayOf(
                0.35f, 0.65f, 0.15f, 0f, 15f,
                0.33f, 0.62f, 0.14f, 0f, 10f,
                0.28f, 0.52f, 0.12f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.MONO_COOL -> floatArrayOf(
                0.25f, 0.55f, 0.15f, 0f, -10f,
                0.28f, 0.58f, 0.16f, 0f, 0f,
                0.35f, 0.68f, 0.22f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.SILVER_TONE -> floatArrayOf(
                0.33f, 0.60f, 0.20f, 0f, 25f,
                0.33f, 0.60f, 0.20f, 0f, 25f,
                0.35f, 0.62f, 0.22f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            )

            // VINTAGE & ANALOG
            FilterType.VINTAGE -> floatArrayOf(
                0.90f, 0.05f, 0.05f, 0f, 15f,
                0.05f, 0.85f, 0.05f, 0f, 10f,
                0.02f, 0.05f, 0.70f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.POLAROID_70S -> floatArrayOf(
                1.7281f, -0.4121f, 0.5411f, 0f, 0f,
                0.2894f, 1.1884f, -1.1764f, 0f, 0f,
                -1.0175f, 0.2237f, 1.6352f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.KODAK_CHROME -> floatArrayOf(
                1.25f, -0.05f, -0.05f, 0f, 5f,
                -0.05f, 1.10f, -0.05f, 0f, 0f,
                -0.05f, -0.05f, 0.95f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.KODAK_PORTRA -> floatArrayOf(
                1.15f, 0.05f, -0.05f, 0f, 12f,
                0.02f, 1.08f, 0.02f, 0f, 8f,
                -0.05f, 0.02f, 0.95f, 0f, 2f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.FUJI_VELVIA -> floatArrayOf(
                1.30f, -0.10f, -0.05f, 0f, 0f,
                -0.05f, 1.25f, -0.05f, 0f, 0f,
                -0.05f, -0.10f, 1.35f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.CROSS_PROCESS -> floatArrayOf(
                -0.3597f, 0.3773f, 0.6638f, 0f, 0f,
                1.5668f, 0.4567f, 1.1261f, 0f, 0f,
                -0.1471f, 0.2261f, -0.7300f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.LOMO -> floatArrayOf(
                1.40f, -0.10f, -0.10f, 0f, -20f,
                -0.10f, 1.30f, -0.10f, 0f, -20f,
                -0.10f, -0.10f, 1.20f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.RETRO_WARM -> floatArrayOf(
                1.2f, 0.1f, 0.2f, 0f, 0f,
                0.7f, 1.0f, 0.0f, 0f, -12.75f,
                -0.7f, 0.2f, 0.5f, 0f, 0f,
                0f, -0.1f, 0f, 0.9f, 0f
            )

            // CINEMATIC
            FilterType.TEAL_AND_ORANGE -> floatArrayOf(
                1.25f, -0.05f, -0.10f, 0f, 15f,
                -0.05f, 1.05f, -0.05f, 0f, 0f,
                -0.15f, 0.10f, 1.30f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.CYBERPUNK -> floatArrayOf(
                1.45f, -0.20f, 0.30f, 0f, 20f,
                -0.20f, 1.10f, -0.10f, 0f, -10f,
                -0.10f, 0.40f, 1.60f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.BLEACH_BYPASS -> floatArrayOf(
                0.82f, 0.08f, 0.08f, 0f, 12f,
                0.08f, 0.82f, 0.08f, 0f, 12f,
                0.08f, 0.08f, 0.82f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.WARM_SUNSET -> floatArrayOf(
                1.2749f, -0.2285f, 0.4411f, 0f, 0f,
                0.3237f, 0.9551f, -0.7059f, 0f, 0f,
                -0.6985f, 0.1734f, 1.1648f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.COOL_BLUE -> floatArrayOf(
                1.0f, 0.0f, 0.1f, 0f, -25.5f,
                0.0f, 1.0f, 0.2f, 0f, 0f,
                0.0f, 0.0f, 1.3f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.DRAMATIC -> floatArrayOf(
                -2.0f, -1.0f, 1.0f, 0f, 255f,
                0.0f, -2.0f, 0.0f, 0f, 255f,
                0.0f, 0.0f, -1.0f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.ROSE_GOLD -> floatArrayOf(
                1.25f, 0.10f, 0.15f, 0f, 15f,
                0.05f, 0.95f, 0.05f, 0f, 5f,
                0.10f, 0.05f, 0.90f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.MOODY_DARK -> floatArrayOf(
                0.30f, 0.0f, 0.0f, 0f, 0f,
                0.0f, 0.65f, 0.0f, 0f, 0f,
                0.0f, 0.0f, 0.49f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.HIGH_KEY -> floatArrayOf(
                1.2299f, 0.0210f, 0.3832f, 0f, 11.91f,
                0.4501f, 1.1874f, -0.1069f, 0f, 11.91f,
                -0.3401f, 0.1317f, 1.0637f, 0f, 11.91f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.CINEMATIC_POP -> floatArrayOf(
                2.1028f, -0.2982f, 0.4213f, 0f, 0f,
                0.2229f, 1.6870f, -0.8834f, 0f, 0f,
                -0.7657f, 0.1712f, 2.0221f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )

            // SOCIAL
            FilterType.CLARENDON -> floatArrayOf(
                1.20f, -0.05f, -0.05f, 0f, 5f,
                -0.05f, 1.15f, -0.05f, 0f, 5f,
                -0.05f, -0.05f, 1.25f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.JUNO -> floatArrayOf(
                1.20f, 0.05f, -0.05f, 0f, 10f,
                0.02f, 1.10f, -0.02f, 0f, 5f,
                -0.05f, -0.05f, 0.90f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.VALENCIA -> floatArrayOf(
                1.08f, 0.04f, 0.02f, 0f, 12f,
                0.02f, 1.05f, 0.02f, 0f, 10f,
                0.01f, 0.03f, 0.88f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.ANIME_VIBE -> floatArrayOf(
                1.10f, 0.05f, 0.05f, 0f, 15f,
                0.02f, 1.22f, 0.05f, 0f, 18f,
                0.02f, 0.05f, 1.35f, 0f, 22f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.SLUMBER -> floatArrayOf(
                0.90f, 0.08f, 0.08f, 0f, 15f,
                0.05f, 0.90f, 0.05f, 0f, 12f,
                0.08f, 0.05f, 0.85f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.LARK -> floatArrayOf(
                1.12f, 0.02f, 0.02f, 0f, 8f,
                0.02f, 1.18f, 0.02f, 0f, 12f,
                0.02f, 0.02f, 1.15f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )

            // ARTISTIC COLOR MATRICES
            FilterType.CYANOTYPE -> floatArrayOf(
                0.05f, 0.15f, 0.45f, 0f, -20f,
                0.10f, 0.35f, 0.65f, 0f, 0f,
                0.20f, 0.55f, 0.95f, 0f, 35f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.INVERT -> floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.CYAN_BOOST -> floatArrayOf(
                0.0f, 0.0f, 0.0f, 0f, 0f,
                0.0f, 0.78f, 0.0f, 0f, 0f,
                0.0f, 0.0f, 1.0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.BRIGHTNESS_BOOST -> floatArrayOf(
                1.44f, 0f, 0f, 0f, 0f,
                0f, 1.44f, 0f, 0f, 0f,
                0f, 0f, 1.44f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
            FilterType.SOLARIZE -> floatArrayOf(
                1.5f, 0f, 0f, 0f, -128f,
                0f, 1.5f, 0f, 0f, -128f,
                0f, 0f, 1.5f, 0f, -128f,
                0f, 0f, 0f, 1f, 0f
            )

            else -> null
        }
    }

    /**
     * Creates an interpolated ColorFilter for [FilterType] at the given intensity.
     */
    fun createColorFilter(filterType: FilterType, intensity: Float = 1.0f): ColorFilter? {
        val targetMatrix = getColorMatrixForFilter(filterType) ?: return null
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        if (clampedIntensity == 0f) return null

        val interpolated = FloatArray(20)
        for (i in 0 until 20) {
            interpolated[i] = IDENTITY_MATRIX[i] * (1f - clampedIntensity) + targetMatrix[i] * clampedIntensity
        }
        return ColorMatrixColorFilter(ColorMatrix(interpolated))
    }

    /**
     * Applies a filter synchronously to a bitmap.
     */
    fun applyFilter(
        source: Bitmap,
        filterType: FilterType,
        intensity: Float = 1.0f,
        extraParams: Map<String, Any>? = null
    ): Bitmap {
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        if (clampedIntensity == 0f) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        }

        val colorMatrix = getColorMatrixForFilter(filterType)
        if (colorMatrix != null) {
            return applyColorMatrix(source, colorMatrix, clampedIntensity)
        }

        // Special / Convolution / Pixel filters
        return when (filterType) {
            FilterType.RGB_GLITCH_SHIFT -> applyRgbGlitch(source, clampedIntensity)
            FilterType.THERMAL_VISION -> applyThermalVision(source, clampedIntensity)
            FilterType.POSTERIZE -> applyPosterize(source, clampedIntensity)
            FilterType.COMIC_DOTS -> applyComicDots(source, clampedIntensity)
            FilterType.EMBOSS -> applyEmboss(source, clampedIntensity)
            FilterType.SHARPEN -> applySharpen(source, clampedIntensity)
            FilterType.SOFT_BLUR -> applySoftBlur(source, clampedIntensity)
            FilterType.GAUSSIAN_BLUR -> {
                val radius = (extraParams?.get("radius") as? Int) ?: (max(2, (min(source.width, source.height) * 0.02f * clampedIntensity).toInt()))
                applyFastBlur(source, radius)
            }
            FilterType.EDGE_DETECT -> applyEdgeDetect(source, clampedIntensity)
            FilterType.VIGNETTE -> applyVignette(source, clampedIntensity)
            FilterType.FILM_GRAIN -> applyFilmGrain(source, clampedIntensity)
            FilterType.DUOTONE -> {
                val shadowColor = (extraParams?.get("shadowColor") as? Int) ?: Color.rgb(20, 20, 80)
                val highlightColor = (extraParams?.get("highlightColor") as? Int) ?: Color.rgb(255, 110, 80)
                applyDuotone(source, shadowColor, highlightColor, clampedIntensity)
            }
            FilterType.HALD_LUT -> {
                val lutBitmap = extraParams?.get("lutBitmap") as? Bitmap
                if (lutBitmap != null) {
                    applyHaldLut(source, lutBitmap, clampedIntensity)
                } else {
                    source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
                }
            }
            else -> source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        }
    }

    /**
     * Applies a 4x5 ColorMatrix with intensity blending on a Bitmap.
     */
    fun applyColorMatrix(source: Bitmap, targetMatrix: FloatArray, intensity: Float = 1.0f): Bitmap {
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val interpolated = FloatArray(20)
        for (i in 0 until 20) {
            interpolated[i] = IDENTITY_MATRIX[i] * (1f - clampedIntensity) + targetMatrix[i] * clampedIntensity
        }

        val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(interpolated))
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    /**
     * Applies composite color adjustments (brightness, contrast, saturation, warmth, tint) in a single pass.
     */
    fun applyAdjustments(
        source: Bitmap,
        brightness: Float = 0.0f,
        contrast: Float = 1.0f,
        saturation: Float = 1.0f,
        warmth: Float = 0.0f,
        tint: Float = 0.0f
    ): Bitmap {
        val cm = ColorMatrix()

        if (saturation != 1.0f) {
            val satMatrix = ColorMatrix().apply { setSaturation(saturation.coerceAtLeast(0f)) }
            cm.postConcat(satMatrix)
        }

        if (contrast != 1.0f || brightness != 0.0f) {
            val c = contrast.coerceAtLeast(0f)
            val b = brightness * 255f
            val offset = 128f * (1f - c) + b
            val contrastMatrix = ColorMatrix(floatArrayOf(
                c, 0f, 0f, 0f, offset,
                0f, c, 0f, 0f, offset,
                0f, 0f, c, 0f, offset,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(contrastMatrix)
        }

        if (warmth != 0.0f || tint != 0.0f) {
            val rScale = (1f + warmth * 0.2f).coerceAtLeast(0f)
            val bScale = (1f - warmth * 0.2f).coerceAtLeast(0f)
            val gScale = (1f + tint * 0.2f).coerceAtLeast(0f)
            val tempMatrix = ColorMatrix(floatArrayOf(
                rScale, 0f, 0f, 0f, 0f,
                0f, gScale, 0f, 0f, 0f,
                0f, 0f, bScale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(tempMatrix)
        }

        val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    /**
     * RGB Glitch / Chromatic Aberration filter.
     */
    fun applyRgbGlitch(source: Bitmap, intensity: Float = 1.0f): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val outPixels = IntArray(width * height)

        val shift = max(1, (width * 0.015f * intensity).toInt())

        for (y in 0 until height) {
            val yOffset = y * width
            for (x in 0 until width) {
                val origPixel = pixels[yOffset + x]
                val a = (origPixel ushr 24) and 0xFF

                // Shift red channel left
                val redX = max(0, x - shift)
                val r = (pixels[yOffset + redX] shr 16) and 0xFF

                // Keep green channel center
                val g = (origPixel shr 8) and 0xFF

                // Shift blue channel right
                val blueX = min(width - 1, x + shift)
                val b = pixels[yOffset + blueX] and 0xFF

                outPixels[yOffset + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Thermal Vision / Infrared heat-map mapping.
     */
    fun applyThermalVision(source: Bitmap, intensity: Float = 1.0f): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val clampedIntensity = intensity.coerceIn(0f, 1f)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel ushr 24) and 0xFF
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f

            // Thermal gradient: 0.0 (Navy) -> 0.33 (Purple) -> 0.66 (Orange) -> 1.0 (Yellow/White)
            val (tR, tG, tB) = when {
                lum < 0.33f -> {
                    val t = lum / 0.33f
                    Triple((120f * t).toInt(), 0, (140f + 115f * (1f - t)).toInt())
                }
                lum < 0.66f -> {
                    val t = (lum - 0.33f) / 0.33f
                    Triple((120f + 135f * t).toInt(), (140f * t).toInt(), (140f * (1f - t)).toInt())
                }
                else -> {
                    val t = (lum - 0.66f) / 0.34f
                    Triple(255, (140f + 115f * t).toInt(), (180f * t).toInt())
                }
            }

            val finalR = (r * (1f - clampedIntensity) + tR * clampedIntensity).toInt()
            val finalG = (g * (1f - clampedIntensity) + tG * clampedIntensity).toInt()
            val finalB = (b * (1f - clampedIntensity) + tB * clampedIntensity).toInt()

            pixels[i] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Posterize / Color quantization filter.
     */
    fun applyPosterize(source: Bitmap, intensity: Float = 1.0f, levels: Int = 4): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val clampedLevels = levels.coerceIn(2, 16)
        val step = 255 / (clampedLevels - 1)
        val clampedIntensity = intensity.coerceIn(0f, 1f)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel ushr 24) and 0xFF
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val postR = ((r + step / 2) / step) * step
            val postG = ((g + step / 2) / step) * step
            val postB = ((b + step / 2) / step) * step

            val finalR = (r * (1f - clampedIntensity) + postR * clampedIntensity).toInt().coerceIn(0, 255)
            val finalG = (g * (1f - clampedIntensity) + postG * clampedIntensity).toInt().coerceIn(0, 255)
            val finalB = (b * (1f - clampedIntensity) + postB * clampedIntensity).toInt().coerceIn(0, 255)

            pixels[i] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Halftone / Comic dots print screen effect.
     */
    fun applyComicDots(source: Bitmap, intensity: Float = 1.0f, dotSize: Int = 6): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val dotRadius = max(2, dotSize)

        for (y in 0 until height) {
            val yOffset = y * width
            val dy = (y % dotRadius) - dotRadius / 2f
            for (x in 0 until width) {
                val dx = (x % dotRadius) - dotRadius / 2f
                val dist = sqrt(dx * dx + dy * dy)

                val pixel = pixels[yOffset + x]
                val a = (pixel ushr 24) and 0xFF
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                val threshold = (1f - lum) * (dotRadius / 2f)

                val dotPixelVal = if (dist < threshold) 0 else 255
                val finalR = (r * (1f - clampedIntensity) + dotPixelVal * clampedIntensity).toInt()
                val finalG = (g * (1f - clampedIntensity) + dotPixelVal * clampedIntensity).toInt()
                val finalB = (b * (1f - clampedIntensity) + dotPixelVal * clampedIntensity).toInt()

                pixels[yOffset + x] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
            }
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Applies a 3x3 Convolution Kernel with parallel row processing via Coroutines.
     */
    fun applyConvolution3x3(
        source: Bitmap,
        kernel: FloatArray,
        offset: Float = 0f,
        intensity: Float = 1.0f
    ): Bitmap = runBlocking {
        applyConvolution3x3Async(source, kernel, offset, intensity)
    }

    suspend fun applyConvolution3x3Async(
        source: Bitmap,
        kernel: FloatArray,
        offset: Float = 0f,
        intensity: Float = 1.0f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val srcPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        val outPixels = IntArray(width * height)

        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
        val chunkHeight = (height + numCores - 1) / numCores

        coroutineScope {
            val deferreds = (0 until numCores).map { coreIndex ->
                async {
                    val startY = coreIndex * chunkHeight
                    val endY = min(height, startY + chunkHeight)

                    for (y in startY until endY) {
                        val yOffset = y * width
                        for (x in 0 until width) {
                            if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                                outPixels[yOffset + x] = srcPixels[yOffset + x]
                                continue
                            }

                            var rSum = 0f
                            var gSum = 0f
                            var bSum = 0f

                            var kIdx = 0
                            for (ky in -1..1) {
                                val neighborYOffset = (y + ky) * width
                                for (kx in -1..1) {
                                    val pixel = srcPixels[neighborYOffset + (x + kx)]
                                    val weight = kernel[kIdx++]
                                    rSum += ((pixel shr 16) and 0xFF) * weight
                                    gSum += ((pixel shr 8) and 0xFF) * weight
                                    bSum += (pixel and 0xFF) * weight
                                }
                            }

                            val origPixel = srcPixels[yOffset + x]
                            val origA = (origPixel ushr 24) and 0xFF
                            val origR = (origPixel shr 16) and 0xFF
                            val origG = (origPixel shr 8) and 0xFF
                            val origB = origPixel and 0xFF

                            val convR = (rSum + offset).coerceIn(0f, 255f)
                            val convG = (gSum + offset).coerceIn(0f, 255f)
                            val convB = (bSum + offset).coerceIn(0f, 255f)

                            val finalR = (origR * (1f - clampedIntensity) + convR * clampedIntensity).toInt()
                            val finalG = (origG * (1f - clampedIntensity) + convG * clampedIntensity).toInt()
                            val finalB = (origB * (1f - clampedIntensity) + convB * clampedIntensity).toInt()

                            outPixels[yOffset + x] = (origA shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
                        }
                    }
                }
            }
            deferreds.awaitAll()
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        result
    }

    fun applyEmboss(source: Bitmap, intensity: Float = 1.0f): Bitmap {
        val kernel = floatArrayOf(
            -2f, -1f, 0f,
            -1f,  1f, 1f,
             0f,  1f, 2f
        )
        return applyConvolution3x3(source, kernel, offset = 0f, intensity = intensity)
    }

    fun applySharpen(source: Bitmap, intensity: Float = 1.0f): Bitmap {
        val kernel = floatArrayOf(
             0f, -1f,  0f,
            -1f,  5f, -1f,
             0f, -1f,  0f
        )
        return applyConvolution3x3(source, kernel, offset = 0f, intensity = intensity)
    }

    fun applySoftBlur(source: Bitmap, intensity: Float = 1.0f): Bitmap {
        val kernel = floatArrayOf(
            0.2f, 0.3f, 0.2f,
            0.1f, 0.1f, 0.1f,
            0.2f, 0.3f, 0.2f
        )
        return applyConvolution3x3(source, kernel, offset = 0f, intensity = intensity)
    }

    fun applyEdgeDetect(source: Bitmap, intensity: Float = 1.0f): Bitmap {
        val kernel = floatArrayOf(
            -1f, -1f, -1f,
            -1f,  8f, -1f,
            -1f, -1f, -1f
        )
        return applyConvolution3x3(source, kernel, offset = 0f, intensity = intensity)
    }

    fun applyFastBlur(source: Bitmap, radius: Int): Bitmap {
        if (radius < 1) return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        val width = source.width
        val height = source.height
        val pix = IntArray(width * height)
        source.getPixels(pix, 0, width, 0, 0, width, height)

        val wm = width - 1
        val hm = height - 1
        val wh = width * height
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(max(width, height))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = (idx / divsum)
        }

        var yw = 0
        yi = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (curY in 0 until height) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            for (curI in -radius..radius) {
                p = pix[yi + min(wm, max(curI, 0))]
                sir = stack[curI + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = r1 - Math.abs(curI)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
            }
            stackpointer = radius

            for (curX in 0 until width) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (curY == 0) {
                    vmin[curX] = min(curX + radius + 1, wm)
                }
                p = pix[yw + vmin[curX]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
            }
            yw += width
        }

        for (curX in 0 until width) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            yp = -radius * width
            for (curI in -radius..radius) {
                yi = max(0, yp) + curX
                sir = stack[curI + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(curI)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (curI < hm) {
                    yp += width
                }
            }
            yi = curX
            stackpointer = radius
            for (curY in 0 until height) {
                pix[yi] = (0xff000000.toInt() and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (curX == 0) {
                    vmin[curY] = min(curY + r1, hm) * width
                }
                p = curX + vmin[curY]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += width
            }
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pix, 0, width, 0, 0, width, height)
        return result
    }

    fun applyDuotone(
        source: Bitmap,
        shadowColor: Int,
        highlightColor: Int,
        intensity: Float = 1.0f
    ): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val sR = Color.red(shadowColor)
        val sG = Color.green(shadowColor)
        val sB = Color.blue(shadowColor)

        val hR = Color.red(highlightColor)
        val hG = Color.green(highlightColor)
        val hB = Color.blue(highlightColor)

        val clampedIntensity = intensity.coerceIn(0f, 1f)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel ushr 24) and 0xFF
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f

            val duoR = (sR + (hR - sR) * lum).coerceIn(0f, 255f)
            val duoG = (sG + (hG - sG) * lum).coerceIn(0f, 255f)
            val duoB = (sB + (hB - sB) * lum).coerceIn(0f, 255f)

            val finalR = (r * (1f - clampedIntensity) + duoR * clampedIntensity).toInt()
            val finalG = (g * (1f - clampedIntensity) + duoG * clampedIntensity).toInt()
            val finalB = (b * (1f - clampedIntensity) + duoB * clampedIntensity).toInt()

            pixels[i] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    fun applyHaldLut(source: Bitmap, lutBitmap: Bitmap, intensity: Float = 1.0f): Bitmap {
        val width = source.width
        val height = source.height
        val srcPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val lutWidth = lutBitmap.width
        val lutHeight = lutBitmap.height
        val lutPixels = IntArray(lutWidth * lutHeight)
        lutBitmap.getPixels(lutPixels, 0, lutWidth, 0, 0, lutWidth, lutHeight)

        val clampedIntensity = intensity.coerceIn(0f, 1f)
        val isLut512 = lutWidth >= 512 && lutHeight >= 512
        val lutSize = if (isLut512) 64 else 16
        val lutGrid = if (isLut512) 8 else 4
        val maxColor = lutSize - 1

        for (i in srcPixels.indices) {
            val pixel = srcPixels[i]
            val a = (pixel ushr 24) and 0xFF
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val blueIndex = (b * maxColor) / 255
            val lutTileX = blueIndex % lutGrid
            val lutTileY = blueIndex / lutGrid

            val lutX = (lutTileX * lutSize + (r * maxColor) / 255).coerceIn(0, lutWidth - 1)
            val lutY = (lutTileY * lutSize + (g * maxColor) / 255).coerceIn(0, lutHeight - 1)

            val lutPixel = lutPixels[lutY * lutWidth + lutX]
            val lutR = (lutPixel shr 16) and 0xFF
            val lutG = (lutPixel shr 8) and 0xFF
            val lutB = lutPixel and 0xFF

            val finalR = (r * (1f - clampedIntensity) + lutR * clampedIntensity).toInt()
            val finalG = (g * (1f - clampedIntensity) + lutG * clampedIntensity).toInt()
            val finalB = (b * (1f - clampedIntensity) + lutB * clampedIntensity).toInt()

            srcPixels[i] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(srcPixels, 0, width, 0, 0, width, height)
        return result
    }

    fun applyVignette(source: Bitmap, amount: Float = 0.5f): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val cx = width / 2.0
        val cy = height / 2.0
        val maxDist = sqrt(cx * cx + cy * cy)
        val clampedAmount = amount.coerceIn(0f, 1f)

        for (y in 0 until height) {
            val yOffset = y * width
            val dy = y - cy
            for (x in 0 until width) {
                val dx = x - cx
                val dist = sqrt(dx * dx + dy * dy)
                val normDist = (dist / maxDist).coerceIn(0.0, 1.0)
                val vignetteFactor = (1.0 - clampedAmount * (normDist * normDist)).coerceIn(0.0, 1.0).toFloat()

                val pixel = pixels[yOffset + x]
                val a = (pixel ushr 24) and 0xFF
                val r = (((pixel shr 16) and 0xFF) * vignetteFactor).toInt().coerceIn(0, 255)
                val g = (((pixel shr 8) and 0xFF) * vignetteFactor).toInt().coerceIn(0, 255)
                val b = ((pixel and 0xFF) * vignetteFactor).toInt().coerceIn(0, 255)

                pixels[yOffset + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    fun applyFilmGrain(source: Bitmap, intensity: Float = 0.2f): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val clampedIntensity = intensity.coerceIn(0f, 1f) * 60f
        val random = Random(42)

        for (i in pixels.indices) {
            val noise = (random.nextFloat() * 2f - 1f) * clampedIntensity
            val pixel = pixels[i]
            val a = (pixel ushr 24) and 0xFF
            val r = (((pixel shr 16) and 0xFF) + noise).toInt().coerceIn(0, 255)
            val g = (((pixel shr 8) and 0xFF) + noise).toInt().coerceIn(0, 255)
            val b = ((pixel and 0xFF) + noise).toInt().coerceIn(0, 255)

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    // =========================================================================
    // GEOMETRIC TRANSFORMATIONS
    // =========================================================================

    /**
     * Rotates a bitmap by arbitrary degrees (e.g. 90, 180, 270).
     */
    fun rotate(source: Bitmap, degrees: Float): Bitmap {
        if (degrees % 360f == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Flips a bitmap horizontally and/or vertically.
     */
    fun flip(source: Bitmap, horizontal: Boolean = true, vertical: Boolean = false): Bitmap {
        val sx = if (horizontal) -1f else 1f
        val sy = if (vertical) -1f else 1f
        if (sx == 1f && sy == 1f) return source
        val matrix = Matrix().apply { postScale(sx, sy) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * Crops a rectangular portion of a bitmap.
     */
    fun crop(source: Bitmap, left: Int, top: Int, width: Int, height: Int): Bitmap {
        val safeLeft = left.coerceIn(0, source.width - 1)
        val safeTop = top.coerceIn(0, source.height - 1)
        val safeW = width.coerceIn(1, source.width - safeLeft)
        val safeH = height.coerceIn(1, source.height - safeTop)
        return Bitmap.createBitmap(source, safeLeft, safeTop, safeW, safeH)
    }

    // =========================================================================
    // WATERMARK & RETRO DATE STAMP
    // =========================================================================

    /**
     * Adds an authentic 1990s retro analog camera date stamp (e.g. "'98 08 13").
     */
    fun applyDateStamp(
        source: Bitmap,
        dateText: String,
        color: Int = Color.rgb(255, 140, 20), // Retro LED Amber Orange
        position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
        paddingPercent: Float = 0.04f
    ): Bitmap {
        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val textSize = max(24f, source.height * 0.038f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSize
            this.typeface = Typeface.MONOSPACE
            this.isFakeBoldText = true
            setShadowLayer(6f, 0f, 0f, Color.argb(180, 255, 100, 0)) // Glowing orange halo
        }

        val textBounds = Rect()
        paint.getTextBounds(dateText, 0, dateText.length, textBounds)

        val padX = source.width * paddingPercent
        val padY = source.height * paddingPercent

        val (x, y) = when (position) {
            WatermarkPosition.TOP_LEFT -> padX to padY + textBounds.height()
            WatermarkPosition.TOP_RIGHT -> (source.width - textBounds.width() - padX) to (padY + textBounds.height())
            WatermarkPosition.BOTTOM_LEFT -> padX to (source.height - padY)
            WatermarkPosition.BOTTOM_RIGHT -> (source.width - textBounds.width() - padX) to (source.height - padY)
            WatermarkPosition.CENTER -> ((source.width - textBounds.width()) / 2f) to ((source.height + textBounds.height()) / 2f)
        }

        canvas.drawText(dateText, x, y, paint)
        return result
    }

    /**
     * Overlays a bitmap watermark with custom opacity and position.
     */
    fun applyWatermark(
        source: Bitmap,
        watermark: Bitmap,
        position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
        alpha: Float = 0.8f,
        paddingPercent: Float = 0.03f
    ): Bitmap {
        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            this.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
        }

        val padX = source.width * paddingPercent
        val padY = source.height * paddingPercent

        val (x, y) = when (position) {
            WatermarkPosition.TOP_LEFT -> padX to padY
            WatermarkPosition.TOP_RIGHT -> (source.width - watermark.width - padX) to padY
            WatermarkPosition.BOTTOM_LEFT -> padX to (source.height - watermark.height - padY)
            WatermarkPosition.BOTTOM_RIGHT -> (source.width - watermark.width - padX) to (source.height - watermark.height - padY)
            WatermarkPosition.CENTER -> ((source.width - watermark.width) / 2f) to ((source.height - watermark.height) / 2f)
        }

        canvas.drawBitmap(watermark, x, y, paint)
        return result
    }

    // =========================================================================
    // PALETTE EXTRACTION
    // =========================================================================

    /**
     * Extracts the top dominant and accent colors from the bitmap for Material You dynamic theming.
     */
    fun extractPalette(source: Bitmap, maxColors: Int = 5): List<Int> {
        val sampleSize = 64
        val scaled = Bitmap.createScaledBitmap(source, sampleSize, sampleSize, false)
        val pixels = IntArray(sampleSize * sampleSize)
        scaled.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)

        // Count color frequencies with 4-bit quantization
        val colorCounts = mutableMapOf<Int, Int>()
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xF0)
            val g = (pixel shr 8 and 0xF0)
            val b = (pixel and 0xF0)
            val quantized = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
        }

        return colorCounts.entries
            .sortedByDescending { it.value }
            .take(maxColors.coerceIn(1, 16))
            .map { it.key }
    }

    // =========================================================================
    // BATCH PROCESSING
    // =========================================================================

    /**
     * Applies a filter to a list of bitmaps in parallel using Kotlin Coroutines.
     */
    suspend fun applyBatchAsync(
        bitmaps: List<Bitmap>,
        filterType: FilterType,
        intensity: Float = 1.0f
    ): List<Bitmap> = withContext(Dispatchers.Default) {
        coroutineScope {
            bitmaps.map { bmp ->
                async { applyFilter(bmp, filterType, intensity) }
            }.awaitAll()
        }
    }
}
