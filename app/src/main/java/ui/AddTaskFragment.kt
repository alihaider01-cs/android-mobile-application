package com.example.taskmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.taskmanager.R
import com.example.taskmanager.data.Task
import com.example.taskmanager.data.TaskViewModel
import com.example.taskmanager.databinding.FragmentAddTaskBinding
import com.google.android.material.snackbar.Snackbar

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel

    private var selectedPriority = 2
    private var selectedCategory = "General"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        setupPriorityChips()
        setupCategoryChips()

        binding.buttonSave.setOnClickListener {
            saveTask()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPriorityChips() {
        binding.chipMedium.isChecked = true

        binding.chipHigh.setOnClickListener {
            selectedPriority = 1
        }
        binding.chipMedium.setOnClickListener {
            selectedPriority = 2
        }
        binding.chipLow.setOnClickListener {
            selectedPriority = 3
        }
    }

    private fun setupCategoryChips() {
        binding.chipGeneral.isChecked = true

        binding.chipShopping.setOnClickListener {
            selectedCategory = "Shopping"
        }
        binding.chipWork.setOnClickListener {
            selectedCategory = "Work"
        }
        binding.chipPersonal.setOnClickListener {
            selectedCategory = "Personal"
        }
        binding.chipHome.setOnClickListener {
            selectedCategory = "Home"
        }
        binding.chipGeneral.setOnClickListener {
            selectedCategory = "General"
        }
    }

    private fun saveTask() {
        val taskName = binding.editTextTaskName.text.toString().trim()

        if (taskName.isEmpty()) {
            Snackbar.make(binding.root, "Please enter a task name", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Get priority directly from ChipGroup
        val priority = when (binding.chipGroupPriority.checkedChipId) {
            R.id.chip_high -> 1
            R.id.chip_medium -> 2
            R.id.chip_low -> 3
            else -> 2
        }

        // Get category directly from ChipGroup
        val category = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chip_shopping -> "Shopping"
            R.id.chip_work -> "Work"
            R.id.chip_personal -> "Personal"
            R.id.chip_home -> "Home"
            R.id.chip_general -> "General"
            else -> "General"
        }

        val task = Task(
            name = taskName,
            priority = priority,
            category = category
        )

        viewModel.addTask(task)

        Snackbar.make(binding.root, "Task added successfully! 🎉", Snackbar.LENGTH_SHORT).show()

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
