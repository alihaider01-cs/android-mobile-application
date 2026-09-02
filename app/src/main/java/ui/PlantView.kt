package com.example.taskmanager.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.sin

class PlantView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    // Colors
    private val stemColor = Color.parseColor("#4CAF50")
    private val leafColor = Color.parseColor("#81C784")
    private val flowerColor = Color.parseColor("#E91E63")
    private val potColor = Color.parseColor("#8D6E63")
    private val droopColor = Color.parseColor("#9E9E9E")

    private var swingOffset = 0f
    private val swingAnimator = ValueAnimator.ofFloat(-5f, 5f).apply {
        duration = 2000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            swingOffset = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        swingAnimator.start()
    }

    fun setProgress(newProgress: Float) {
        val animator = ValueAnimator.ofFloat(progress, newProgress)
        animator.duration = 1000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2
        val bottomY = h - 40f

        // Draw Pot
        paint.color = potColor
        path.reset()
        path.moveTo(centerX - 40, bottomY)
        path.lineTo(centerX + 40, bottomY)
        path.lineTo(centerX + 30, bottomY + 30)
        path.lineTo(centerX - 30, bottomY + 30)
        path.close()
        canvas.drawPath(path, paint)

        // Stem
        paint.color = if (progress > 0.1f) stemColor else droopColor
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        
        path.reset()
        path.moveTo(centerX, bottomY)
        
        val stemHeight = h * 0.6f * progress.coerceAtLeast(0.1f)
        val curveX = centerX + swingOffset
        path.quadTo(centerX + swingOffset * 2, bottomY - stemHeight / 2, curveX, bottomY - stemHeight)
        canvas.drawPath(path, paint)

        // Leaves
        paint.style = Paint.Style.FILL
        if (progress > 0.3f) drawLeaf(canvas, curveX, bottomY - stemHeight * 0.4f, true)
        if (progress > 0.6f) drawLeaf(canvas, curveX, bottomY - stemHeight * 0.7f, false)

        // Flower
        if (progress >= 0.95f) {
            paint.color = flowerColor
            canvas.drawCircle(curveX, bottomY - stemHeight, 20f, paint)
            for (i in 0..5) {
                val angle = Math.toRadians(i * 60.0)
                val px = curveX + Math.cos(angle).toFloat() * 30f
                val py = (bottomY - stemHeight) + Math.sin(angle).toFloat() * 30f
                canvas.drawCircle(px, py, 15f, paint)
            }
            paint.color = Color.YELLOW
            canvas.drawCircle(curveX, bottomY - stemHeight, 10f, paint)
        }
    }

    private fun drawLeaf(canvas: Canvas, x: Float, y: Float, isLeft: Boolean) {
        paint.color = if (progress > 0.2f) leafColor else droopColor
        val scale = if (isLeft) -1f else 1f
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(scale, 1f)
        canvas.rotate(swingOffset * 2)
        
        path.reset()
        path.moveTo(0f, 0f)
        path.quadTo(30f, -20f, 60f, 0f)
        path.quadTo(30f, 20f, 0f, 0f)
        canvas.drawPath(path, paint)
        
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        swingAnimator.cancel()
        super.onDetachedFromWindow()
    }
}
