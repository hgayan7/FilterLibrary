package com.uvstudio.him.photofilterlibrary

import android.graphics.Bitmap

/**
 * Model class for filter preset carousel items in the sample app.
 */
data class FilterThumbnailItem(
    val filterType: FilterType,
    var thumbnailBitmap: Bitmap? = null,
    var isSelected: Boolean = false
)
