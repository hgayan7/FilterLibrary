package com.uvstudio.him.photofilterlibrary

import android.graphics.Bitmap
import android.graphics.Color
import com.uvstudio.him.photofilterlibrary.compose.toComposeColorFilter
import com.uvstudio.him.photofilterlibrary.transformations.PhotoFilterCoilTransformation
import com.uvstudio.him.photofilterlibrary.transformations.PhotoFilterGlideTransformation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class PhotoFilterUnitTest {

    private lateinit var testBitmap: Bitmap
    private lateinit var rectangularBitmap: Bitmap

    @Before
    fun setUp() {
        // Create a 100x100 RGB bitmap with a gradient for testing
        testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                testBitmap.setPixel(x, y, Color.rgb(x * 2, y * 2, 128))
            }
        }

        // Rectangular 200x100 bitmap
        rectangularBitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        for (y in 0 until 100) {
            for (x in 0 until 200) {
                rectangularBitmap.setPixel(x, y, Color.rgb(x, y * 2, 200))
            }
        }
    }

    @Test
    fun testAllPresetFiltersProduceValidOutput() {
        for (filter in FilterType.entries) {
            val result = PhotoFilter.apply(testBitmap, filter, 1.0f)
            assertNotNull("Filter ${filter.name} returned null bitmap", result)
            assertEquals("Width should be preserved for ${filter.name}", 100, result.width)
            assertEquals("Height should be preserved for ${filter.name}", 100, result.height)
        }
    }

    @Test
    fun testRectangularBitmapAspectPreserved() {
        for (filter in FilterType.entries) {
            val result = PhotoFilter.apply(rectangularBitmap, filter, 0.8f)
            assertNotNull(result)
            assertEquals(200, result.width)
            assertEquals(100, result.height)
        }
    }

    @Test
    fun testIntensityClamping() {
        // Zero intensity -> Returns copy with identical dimensions
        val zeroResult = PhotoFilter.apply(testBitmap, FilterType.SEPIA, 0.0f)
        assertNotNull(zeroResult)
        assertEquals(testBitmap.width, zeroResult.width)

        // Negative intensity is clamped to 0
        val negResult = PhotoFilter.apply(testBitmap, FilterType.SEPIA, -0.5f)
        assertNotNull(negResult)

        // Intensity > 1 is clamped to 1
        val overResult = PhotoFilter.apply(testBitmap, FilterType.SEPIA, 2.5f)
        assertNotNull(overResult)
    }

    @Test
    fun testFilterCategories() {
        val classicFilters = FilterType.getByCategory(FilterCategory.CLASSIC)
        assertTrue(classicFilters.contains(FilterType.GRAYSCALE))
        assertTrue(classicFilters.contains(FilterType.SEPIA))
        assertTrue(classicFilters.contains(FilterType.NOIR))

        val cinematicFilters = FilterType.getByCategory(FilterCategory.CINEMATIC)
        assertTrue(cinematicFilters.contains(FilterType.TEAL_AND_ORANGE))
        assertTrue(cinematicFilters.contains(FilterType.CYBERPUNK))
        assertTrue(cinematicFilters.contains(FilterType.BLEACH_BYPASS))

        val vintageFilters = FilterType.getByCategory(FilterCategory.VINTAGE)
        assertTrue(vintageFilters.contains(FilterType.KODAK_PORTRA))

        val socialFilters = FilterType.getByCategory(FilterCategory.SOCIAL)
        assertTrue(socialFilters.contains(FilterType.ANIME_VIBE))

        val artisticFilters = FilterType.getByCategory(FilterCategory.ARTISTIC)
        assertTrue(artisticFilters.contains(FilterType.RGB_GLITCH_SHIFT))
        assertTrue(artisticFilters.contains(FilterType.THERMAL_VISION))
        assertTrue(artisticFilters.contains(FilterType.POSTERIZE))
        assertTrue(artisticFilters.contains(FilterType.COMIC_DOTS))
        assertTrue(artisticFilters.contains(FilterType.CYANOTYPE))

        val allFilters = FilterType.getByCategory(FilterCategory.ALL)
        assertEquals(FilterType.entries.size, allFilters.size)
    }

    @Test
    fun testPhotoFilterBuilderPipeline() {
        val filtered = PhotoFilter.builder(testBitmap)
            .rotate(90f)
            .filter(FilterType.VINTAGE, 0.8f)
            .brightness(0.1f)
            .contrast(1.2f)
            .saturation(1.1f)
            .warmth(0.2f)
            .tint(0.1f)
            .vignette(0.3f)
            .grain(0.1f)
            .sharpen(0.5f)
            .blur(2)
            .dateStamp("'98 08 13")
            .build()

        assertNotNull(filtered)
        assertEquals(100, filtered.width)
        assertEquals(100, filtered.height)
    }

    @Test
    fun testKotlinDslBuilder() {
        val filtered = photoFilter(testBitmap) {
            filter(FilterType.TEAL_AND_ORANGE, 0.9f)
            brightness(0.05f)
            contrast(1.1f)
        }

        assertNotNull(filtered)
        assertEquals(100, filtered.width)
        assertEquals(100, filtered.height)
    }

    @Test
    fun testGeometricTransforms() {
        // 200x100 rotated by 90 degrees becomes 100x200
        val rotated = rectangularBitmap.rotate(90f)
        assertNotNull(rotated)
        assertEquals(100, rotated.width)
        assertEquals(200, rotated.height)

        val flippedH = testBitmap.flipHorizontal()
        assertNotNull(flippedH)
        assertEquals(100, flippedH.width)

        val flippedV = testBitmap.flipVertical()
        assertNotNull(flippedV)
        assertEquals(100, flippedV.height)

        val cropped = testBitmap.crop(10, 10, 50, 40)
        assertNotNull(cropped)
        assertEquals(50, cropped.width)
        assertEquals(40, cropped.height)
    }

    @Test
    fun testDateStampAndWatermark() {
        val stamped = testBitmap.applyDateStamp("'24 08 13", Color.rgb(255, 140, 20), WatermarkPosition.BOTTOM_RIGHT)
        assertNotNull(stamped)
        assertEquals(100, stamped.width)

        val dummyWatermark = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        val watermarked = testBitmap.applyWatermark(dummyWatermark, WatermarkPosition.TOP_LEFT, 0.8f)
        assertNotNull(watermarked)
        assertEquals(100, watermarked.width)
    }

    @Test
    fun testPaletteExtraction() {
        val palette = testBitmap.extractPalette(count = 4)
        assertNotNull(palette)
        assertTrue(palette.isNotEmpty())
        assertTrue(palette.size <= 4)
    }

    @Test
    fun testBatchProcessing() = runBlocking {
        val batch = listOf(testBitmap, rectangularBitmap)
        val filteredBatch = batch.applyFilterBatchAsync(FilterType.ANIME_VIBE, 0.8f)

        assertNotNull(filteredBatch)
        assertEquals(2, filteredBatch.size)
        assertEquals(100, filteredBatch[0].width)
        assertEquals(200, filteredBatch[1].width)
    }

    @Test
    fun testDuotoneMapping() {
        val navy = Color.rgb(10, 15, 60)
        val gold = Color.rgb(255, 200, 50)
        val result = FilterEngine.applyDuotone(testBitmap, navy, gold, 1.0f)

        assertNotNull(result)
        assertEquals(100, result.width)
        assertEquals(100, result.height)
    }

    @Test
    fun testHaldLutMapping() {
        // Create a 64x64 dummy HALD-4 LUT bitmap
        val dummyLut = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val result = FilterEngine.applyHaldLut(testBitmap, dummyLut, 0.8f)

        assertNotNull(result)
        assertEquals(100, result.width)
        assertEquals(100, result.height)
    }

    @Test
    fun testConvolutionFilters() {
        val emboss = FilterEngine.applyEmboss(testBitmap)
        assertNotNull(emboss)
        assertEquals(100, emboss.width)

        val sharpen = FilterEngine.applySharpen(testBitmap)
        assertNotNull(sharpen)
        assertEquals(100, sharpen.width)

        val blur = FilterEngine.applySoftBlur(testBitmap)
        assertNotNull(blur)
        assertEquals(100, blur.width)

        val edgeDetect = FilterEngine.applyEdgeDetect(testBitmap)
        assertNotNull(edgeDetect)
        assertEquals(100, edgeDetect.width)
    }

    @Test
    fun testLegacyBackwardCompatibilityMethods() {
        val legacy = PhotoFilter()
        assertNotNull(legacy.one(null, testBitmap))
        assertNotNull(legacy.two(null, testBitmap))
        assertNotNull(legacy.three(null, testBitmap))
        assertNotNull(legacy.four(null, testBitmap))
        assertNotNull(legacy.five(null, testBitmap))
        assertNotNull(legacy.six(null, testBitmap))
        assertNotNull(legacy.seven(null, testBitmap))
        assertNotNull(legacy.eight(null, testBitmap))
        assertNotNull(legacy.nine(null, testBitmap))
        assertNotNull(legacy.ten(null, testBitmap))
        assertNotNull(legacy.eleven(null, testBitmap))
        assertNotNull(legacy.twelve(null, testBitmap))
        assertNotNull(legacy.thirteen(null, testBitmap))
        assertNotNull(legacy.fourteen(null, testBitmap))
        assertNotNull(legacy.fifteen(null, testBitmap))
        assertNotNull(legacy.sixteen(null, testBitmap))
    }

    @Test
    fun testCoroutineAsyncFiltering() = runBlocking {
        val result = testBitmap.applyFilterAsync(FilterType.CYBERPUNK, 0.75f)
        assertNotNull(result)
        assertEquals(100, result.width)
        assertEquals(100, result.height)
    }

    @Test
    fun testZeroAllocationColorFilterCreation() {
        val cfVintage = FilterEngine.createColorFilter(FilterType.VINTAGE, 0.8f)
        assertNotNull(cfVintage)

        val cfZero = FilterEngine.createColorFilter(FilterType.VINTAGE, 0.0f)
        assertNull(cfZero)
    }

    @Test
    fun testComposeColorFilterExtension() {
        val composeFilter = FilterType.TEAL_AND_ORANGE.toComposeColorFilter(0.85f)
        assertNotNull(composeFilter)

        val composeFilterZero = FilterType.TEAL_AND_ORANGE.toComposeColorFilter(0.0f)
        assertNull(composeFilterZero)
    }

    @Test
    fun testCoilAndGlideTransformations() {
        val coilTrans1 = PhotoFilterCoilTransformation(FilterType.VINTAGE, 0.8f)
        val coilTrans2 = PhotoFilterCoilTransformation(FilterType.VINTAGE, 0.8f)
        assertEquals(coilTrans1, coilTrans2)
        assertEquals(coilTrans1.hashCode(), coilTrans2.hashCode())
        assertTrue(coilTrans1.cacheKey.contains("VINTAGE"))

        val glideTrans1 = PhotoFilterGlideTransformation(FilterType.CYBERPUNK, 0.9f)
        val glideTrans2 = PhotoFilterGlideTransformation(FilterType.CYBERPUNK, 0.9f)
        assertEquals(glideTrans1, glideTrans2)
        assertEquals(glideTrans1.hashCode(), glideTrans2.hashCode())
    }
}
