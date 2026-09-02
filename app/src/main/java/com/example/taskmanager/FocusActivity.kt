package com.example.taskmanager

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taskmanager.databinding.ActivityFocusBinding
import com.example.taskmanager.utils.FocusService
import java.util.Locale

class FocusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusBinding
    private var taskName: String = ""
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startFocusService()
        } else {
            Toast.makeText(this, "Notification permission required for timer", Toast.LENGTH_SHORT).show()
        }
    }

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "FOCUS_TIMER_UPDATE" -> {
                    val timeLeft = intent.getLongExtra("timeLeft", 0L)
                    updateUI(timeLeft)
                }
                "FOCUS_TIMER_FINISHED" -> {
                    resetUI()
                    Toast.makeText(this@FocusActivity, "Focus session finished! 🎉", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityFocusBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        taskName = intent.getStringExtra("taskName") ?: "Focus Session"
        binding.textTaskName.text = taskName

        binding.buttonBack.setOnClickListener { finish() }

        binding.buttonStart.setOnClickListener {
            checkPermissionAndStart()
        }

        binding.buttonStop.setOnClickListener {
            stopFocusService()
            resetUI()
        }

        if (FocusService.isServiceRunning) {
            binding.buttonStart.visibility = View.GONE
            binding.buttonStop.visibility = View.VISIBLE
            updateUI(FocusService.currentTimeLeft)
        }
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startFocusService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startFocusService()
        }
    }

    private fun startFocusService() {
        val intent = Intent(this, FocusService::class.java).apply {
            action = "START"
            putExtra("duration", 1500000L) // 25 min in millis
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            binding.buttonStart.visibility = View.GONE
            binding.buttonStop.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start timer service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopFocusService() {
        val intent = Intent(this, FocusService::class.java).apply {
            action = "STOP"
        }
        startService(intent)
    }

    private fun updateUI(timeLeft: Long) {
        val minutes = (timeLeft / 1000) / 60
        val seconds = (timeLeft / 1000) % 60
        binding.textCountdown.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        
        val progress = (timeLeft / 1000).toInt()
        binding.progressTimer.progress = progress
    }

    private fun resetUI() {
        binding.textCountdown.text = "25:00"
        binding.progressTimer.progress = 1500
        binding.buttonStart.visibility = View.VISIBLE
        binding.buttonStop.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction("FOCUS_TIMER_UPDATE")
            addAction("FOCUS_TIMER_FINISHED")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(timerReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(timerReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
}