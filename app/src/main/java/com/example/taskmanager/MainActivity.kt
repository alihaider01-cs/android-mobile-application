package com.example.taskmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivityMainBinding
import com.example.taskmanager.ui.TaskListFragment
import com.example.taskmanager.ui.StatisticsFragment
import com.example.taskmanager.ui.SettingsFragment
import com.example.taskmanager.ui.AchievementsFragment
import com.example.taskmanager.utils.ThemeHelper
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.data.TaskViewModel
import com.example.taskmanager.utils.SoundHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var soundHelper: SoundHelper

    private fun observeAchievements() {
        viewModel.newAchievement.observe(this) { achievement ->
            if (achievement != null) {
                soundHelper.playFanfare()
                MaterialAlertDialogBuilder(this)
                    .setTitle("Achievement Unlocked! 🎉")
                    .setMessage("You've earned the ${achievement.title} badge!\n\n${achievement.description}")
                    .setPositiveButton("Awesome") { _, _ -> 
                        viewModel.clearAchievement()
                    }
                    .setOnDismissListener {
                        viewModel.clearAchievement()
                    }
                    .setIcon(android.R.drawable.btn_star_big_on)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundHelper = SoundHelper(this)
        viewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        observeAchievements()

        // Setup listener for the menu item inflated in XML
        binding.toolbar.setOnMenuItemClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
            
            val fragment = when (it.itemId) {
                R.id.action_statistics -> if (currentFragment !is StatisticsFragment) StatisticsFragment() else null
                R.id.action_achievements -> if (currentFragment !is AchievementsFragment) AchievementsFragment() else null
                R.id.action_settings -> if (currentFragment !is SettingsFragment) SettingsFragment() else null
                else -> null
            }

            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TaskListFragment())
                .commit()
        }
    }
}