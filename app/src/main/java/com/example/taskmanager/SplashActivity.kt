package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmanager.databinding.ActivitySplashBinding
import com.example.taskmanager.utils.ThemeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
    }

    private fun startAnimations() {
        val fullText = "Task Manager"
        
        lifecycleScope.launch {
            // 1. Typing Animation
            binding.appName.text = ""
            for (i in 0..fullText.length) {
                binding.appName.text = fullText.substring(0, i)
                delay(120) // Speed of typing
            }

            // 2. Checkmark Shoot Out
            delay(200)
            binding.checkmark.visibility = View.VISIBLE
            binding.checkmark.alpha = 0f
            binding.checkmark.scaleX = 0f
            binding.checkmark.scaleY = 0f
            binding.checkmark.translationX = -50f

            binding.checkmark.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .translationX(20f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator())
                .withEndAction {
                    // 3. Confetti Burst
                    explodeConfetti()
                    
                    // 4. Transition to MainActivity
                    lifecycleScope.launch {
                        delay(1500)
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
                .start()
        }
    }

    private fun explodeConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.5)
        )
        binding.konfettiSplash.start(party)
    }
}
