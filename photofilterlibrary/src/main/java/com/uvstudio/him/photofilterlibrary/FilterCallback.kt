package com.uvstudio.him.photofilterlibrary

import android.graphics.Bitmap

/**
 * Asynchronous callback interface for photo filter processing.
 */
interface FilterCallback {
    /**
     * Called when the filter processing has successfully completed.
     */
    fun onSuccess(bitmap: Bitmap)

    /**
     * Called when an error occurs during filter processing.
     */
    fun onError(throwable: Throwable)
}
