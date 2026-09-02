package com.example.taskmanager.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.taskmanager.R

class SoundHelper(private val context: Context) {

    /**
     * Note: This implementation assumes sound files exist in res/raw.
     * Since I cannot create binary files (mp3/wav), I have implemented the logic 
     * that will use them if they are added. For now, it will safely skip playing 
     * if the resource is missing but will still perform the haptic feedback.
     */

    fun playPop() {
        vibrate(50)
        playSound(getRawResourceId("pop"))
    }

    fun playBell() {
        vibrate(100)
        playSound(getRawResourceId("bell"))
    }

    fun playWhoosh() {
        vibrate(80)
        playSound(getRawResourceId("whoosh"))
    }

    fun playFanfare() {
        vibrate(200)
        playSound(getRawResourceId("fanfare"))
    }

    private fun playSound(resId: Int) {
        if (resId == 0) return
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRawResourceId(name: String): Int {
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }

    private fun vibrate(duration: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}
