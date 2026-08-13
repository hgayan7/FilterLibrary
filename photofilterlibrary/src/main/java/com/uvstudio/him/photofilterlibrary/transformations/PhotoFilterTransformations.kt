package com.uvstudio.him.photofilterlibrary.transformations

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.uvstudio.him.photofilterlibrary.FilterEngine
import com.uvstudio.him.photofilterlibrary.FilterType
import java.security.MessageDigest

/**
 * Ready-to-use Transformation for Coil image loader.
 *
 * Usage:
 * ```kotlin
 * imageView.load(url) {
 *     transformations(PhotoFilterCoilTransformation(FilterType.VINTAGE, 0.8f))
 * }
 * ```
 */
class PhotoFilterCoilTransformation(
    private val filterType: FilterType,
    private val intensity: Float = 1.0f
) : coil.transform.Transformation {

    override val cacheKey: String = "PhotoFilterCoil_${filterType.name}_$intensity"

    override suspend fun transform(input: Bitmap, size: coil.size.Size): Bitmap {
        return FilterEngine.applyFilter(input, filterType, intensity)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoFilterCoilTransformation) return false
        return filterType == other.filterType && intensity == other.intensity
    }

    override fun hashCode(): Int {
        var result = filterType.hashCode()
        result = 31 * result + intensity.hashCode()
        return result
    }
}

/**
 * Ready-to-use Transformation for Glide image loader.
 *
 * Usage:
 * ```java
 * Glide.with(context)
 *      .load(url)
 *      .transform(new PhotoFilterGlideTransformation(FilterType.CYBERPUNK, 0.85f))
 *      .into(imageView);
 * ```
 */
class PhotoFilterGlideTransformation(
    private val filterType: FilterType,
    private val intensity: Float = 1.0f
) : BitmapTransformation() {

    private val id = "com.uvstudio.him.photofilterlibrary.transformations.PhotoFilterGlide_${filterType.name}_$intensity"

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        return FilterEngine.applyFilter(toTransform, filterType, intensity)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(id.toByteArray(Charsets.UTF_8))
    }

    override fun equals(other: Any?): Boolean {
        if (other is PhotoFilterGlideTransformation) {
            return filterType == other.filterType && intensity == other.intensity
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + filterType.hashCode()
        result = 31 * result + intensity.hashCode()
        return result
    }
}
