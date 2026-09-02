package com.example.taskmanager.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class AnimatedCloudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var floatOffset = 0f
    
    private val cloudColor = Color.parseColor("#E0F7FA") // Light fluffy blue-white
    
    private val floatAnimator = ValueAnimator.ofFloat(-10f, 10f).apply {
        duration = 2000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            floatOffset = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        floatAnimator.start()
    }

    fun setVisible(isVisible: Boolean, animate: Boolean = true) {
        if (isVisible) {
            if (animate) {
                this.visibility = View.VISIBLE
                this.alpha = 0f
                this.translationX = -50f
                animate().alpha(1f).translationX(0f).setDuration(500).start()
            } else {
                this.visibility = View.VISIBLE
                this.alpha = 1f
                this.translationX = 0f
            }
        } else {
            if (animate) {
                animate().alpha(0f).translationX(50f).setDuration(500).withEndAction {
                    this.visibility = View.GONE
                }.start()
            } else {
                this.visibility = View.GONE
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f + floatOffset
        
        paint.color = cloudColor
        paint.style = Paint.Style.FILL
        
        // Draw a fluffy cloud using 3 circles
        canvas.drawCircle(centerX - 25, centerY + 5, 20f, paint)
        canvas.drawCircle(centerX, centerY - 5, 25f, paint)
        canvas.drawCircle(centerX + 25, centerY + 5, 20f, paint)
    }

    override fun onDetachedFromWindow() {
        floatAnimator.cancel()
        super.onDetachedFromWindow()
    }
}
