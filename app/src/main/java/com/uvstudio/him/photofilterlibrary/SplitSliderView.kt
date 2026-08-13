package com.uvstudio.him.photofilterlibrary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * Interactive before-and-after split comparison view.
 *
 * Allows dragging a vertical slider handle across the bitmap to compare
 * original vs filtered images in real-time.
 */
class SplitSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var originalBitmap: Bitmap? = null
    private var filteredBitmap: Bitmap? = null

    // Split position from 0.0f (all original) to 1.0f (all filtered)
    private var splitPosition: Float = 0.5f

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        setShadowLayer(8f, 0f, 0f, Color.argb(120, 0, 0, 0))
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(10f, 0f, 2f, Color.argb(140, 0, 0, 0))
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        isFakeBoldText = true
    }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val clipPath = Path()
    private val viewRect = RectF()

    fun setBitmaps(original: Bitmap?, filtered: Bitmap?) {
        this.originalBitmap = original
        this.filteredBitmap = filtered
        invalidate()
    }

    fun setFilteredBitmap(filtered: Bitmap?) {
        this.filteredBitmap = filtered
        invalidate()
    }

    fun getFilteredBitmap(): Bitmap? = filteredBitmap
    fun getOriginalBitmap(): Bitmap? = originalBitmap

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val orig = originalBitmap ?: return
        val filt = filteredBitmap ?: orig

        val viewW = width.toFloat()
        val viewH = height.toFloat()
        if (viewW <= 0f || viewH <= 0f) return

        // Compute aspect-fit dimensions
        val bmpW = orig.width.toFloat()
        val bmpH = orig.height.toFloat()
        val scale = min(viewW / bmpW, viewH / bmpH)

        val drawW = bmpW * scale
        val drawH = bmpH * scale
        val left = (viewW - drawW) / 2f
        val top = (viewH - drawH) / 2f
        viewRect.set(left, top, left + drawW, top + drawH)

        val splitX = left + drawW * splitPosition

        // 1. Draw Original Image (Left side)
        canvas.save()
        clipPath.reset()
        clipPath.addRect(left, top, splitX, top + drawH, Path.Direction.CW)
        canvas.clipPath(clipPath)
        canvas.drawBitmap(orig, null, viewRect, bitmapPaint)
        canvas.restore()

        // 2. Draw Filtered Image (Right side)
        canvas.save()
        clipPath.reset()
        clipPath.addRect(splitX, top, left + drawW, top + drawH, Path.Direction.CW)
        canvas.clipPath(clipPath)
        canvas.drawBitmap(filt, null, viewRect, bitmapPaint)
        canvas.restore()

        // 3. Draw Split Divider Line
        canvas.drawLine(splitX, top, splitX, top + drawH, linePaint)

        // 4. Draw Center Slider Handle
        val handleY = top + drawH / 2f
        val handleRadius = 36f
        canvas.drawCircle(splitX, handleY, handleRadius, handlePaint)

        // Draw left/right mini arrows inside handle
        val arrowPath = Path().apply {
            // Left arrow
            moveTo(splitX - 16f, handleY)
            lineTo(splitX - 6f, handleY - 10f)
            lineTo(splitX - 6f, handleY + 10f)
            close()
            // Right arrow
            moveTo(splitX + 16f, handleY)
            lineTo(splitX + 6f, handleY - 10f)
            lineTo(splitX + 6f, handleY + 10f)
            close()
        }
        canvas.drawPath(arrowPath, arrowPaint)

        // 5. Draw "Before" & "After" Badges
        val badgeH = 44f
        val badgeW = 120f
        val badgeRadius = 12f
        val margin = 20f

        // "Original" Badge (Top Left)
        val origBadgeRect = RectF(left + margin, top + margin, left + margin + badgeW, top + margin + badgeH)
        canvas.drawRoundRect(origBadgeRect, badgeRadius, badgeRadius, badgePaint)
        canvas.drawText("Original", origBadgeRect.left + 16f, origBadgeRect.bottom - 12f, textPaint)

        // "Filtered" Badge (Top Right)
        val filtBadgeRect = RectF(left + drawW - margin - badgeW, top + margin, left + drawW - margin, top + margin + badgeH)
        canvas.drawRoundRect(filtBadgeRect, badgeRadius, badgeRadius, badgePaint)
        canvas.drawText("Filtered", filtBadgeRect.left + 18f, filtBadgeRect.bottom - 12f, textPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val orig = originalBitmap ?: return super.onTouchEvent(event)
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val bmpW = orig.width.toFloat()
        val bmpH = orig.height.toFloat()
        val scale = min(viewW / bmpW, viewH / bmpH)
        val drawW = bmpW * scale
        val left = (viewW - drawW) / 2f

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val touchX = event.x
                val newPos = ((touchX - left) / drawW).coerceIn(0.0f, 1.0f)
                splitPosition = newPos
                invalidate()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
