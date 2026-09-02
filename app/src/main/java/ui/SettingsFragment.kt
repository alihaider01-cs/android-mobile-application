package com.example.taskmanager.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.data.TaskViewModel
import com.example.taskmanager.databinding.FragmentSettingsBinding
import com.example.taskmanager.utils.ThemeHelper
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        setupThemePicker()
        setupDeleteAllButton()
    }

    private fun setupThemePicker() {
        // Pre-select the current theme
        val currentTheme = ThemeHelper.getThemeIndex(requireContext())
        when (currentTheme) {
            ThemeHelper.THEME_OCEAN -> binding.radioOcean.isChecked = true
            ThemeHelper.THEME_SUNSET -> binding.radioSunset.isChecked = true
            ThemeHelper.THEME_FOREST -> binding.radioForest.isChecked = true
            ThemeHelper.THEME_PURPLE -> binding.radioPurple.isChecked = true
            ThemeHelper.THEME_DARK -> binding.radioDark.isChecked = true
            else -> binding.radioDefault.isChecked = true
        }

        binding.buttonApplyTheme.setOnClickListener {
            val selectedTheme = when (binding.radioGroupThemes.checkedRadioButtonId) {
                binding.radioOcean.id -> ThemeHelper.THEME_OCEAN
                binding.radioSunset.id -> ThemeHelper.THEME_SUNSET
                binding.radioForest.id -> ThemeHelper.THEME_FOREST
                binding.radioPurple.id -> ThemeHelper.THEME_PURPLE
                binding.radioDark.id -> ThemeHelper.THEME_DARK
                else -> ThemeHelper.THEME_DEFAULT
            }

            ThemeHelper.saveTheme(requireContext(), selectedTheme)
            
            // Re-create activity to apply theme
            requireActivity().recreate()
        }
    }

    private fun setupDeleteAllButton() {
        binding.buttonDeleteAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete All Tasks")
                .setMessage("Are you sure you want to delete ALL tasks? This cannot be undone.")
                .setPositiveButton("Delete All") { _, _ ->
                    viewModel.tasks.value?.let { tasks ->
                        tasks.forEach { task ->
                            viewModel.deleteTask(task)
                        }
                    }
                    Snackbar.make(binding.root, "All tasks deleted", Snackbar.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}