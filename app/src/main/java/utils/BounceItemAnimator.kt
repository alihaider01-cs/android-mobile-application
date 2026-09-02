package com.example.taskmanager.utils

import android.view.View
import android.view.animation.BounceInterpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class BounceItemAnimator : DefaultItemAnimator() {

    private val pendingAdditions = mutableListOf<RecyclerView.ViewHolder>()
    private val runningAdditions = mutableListOf<RecyclerView.ViewHolder>()

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        // Reset state and stop current animations
        endAnimation(holder)
        
        holder.itemView.alpha = 0f
        holder.itemView.translationY = -200f
        
        pendingAdditions.add(holder)
        return true
    }

    override fun runPendingAnimations() {
        val additions = pendingAdditions.toList()
        pendingAdditions.clear()

        for (holder in additions) {
            animateAddImpl(holder)
        }
        
        super.runPendingAnimations()
    }

    private fun animateAddImpl(holder: RecyclerView.ViewHolder) {
        runningAdditions.add(holder)
        val view = holder.itemView
        
        val animation = ViewCompat.animate(view)
        animation.alpha(1f)
            .translationY(0f)
            .setDuration(addDuration)
            .setInterpolator(BounceInterpolator())
            .setListener(object : androidx.core.view.ViewPropertyAnimatorListener {
                override fun onAnimationStart(view: View) {
                    dispatchAddStarting(holder)
                }

                override fun onAnimationEnd(view: View) {
                    animation.setListener(null)
                    view.alpha = 1f
                    view.translationY = 0f
                    dispatchAddFinished(holder)
                    runningAdditions.remove(holder)
                    if (!isRunning) dispatchAnimationsFinished()
                }

                override fun onAnimationCancel(view: View) {
                    view.alpha = 1f
                    view.translationY = 0f
                }
            }).start()
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        item.itemView.animate().cancel()
        if (pendingAdditions.remove(item)) {
            item.itemView.alpha = 1f
            item.itemView.translationY = 0f
            dispatchAddFinished(item)
        }
        if (runningAdditions.remove(item)) {
            item.itemView.alpha = 1f
            item.itemView.translationY = 0f
            dispatchAddFinished(item)
        }
        super.endAnimation(item)
    }

    override fun endAnimations() {
        for (i in pendingAdditions.indices.reversed()) {
            val holder = pendingAdditions[i]
            holder.itemView.alpha = 1f
            holder.itemView.translationY = 0f
            dispatchAddFinished(holder)
            pendingAdditions.removeAt(i)
        }
        for (i in runningAdditions.indices.reversed()) {
            val holder = runningAdditions[i]
            holder.itemView.animate().cancel()
            runningAdditions.removeAt(i)
        }
        super.endAnimations()
    }

    override fun isRunning(): Boolean {
        return pendingAdditions.isNotEmpty() || runningAdditions.isNotEmpty() || super.isRunning()
    }

    init {
        addDuration = 700
    }
}
