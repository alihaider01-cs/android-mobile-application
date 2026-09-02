package com.example.taskmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.data.Task
import com.example.taskmanager.data.TaskViewModel
import com.example.taskmanager.databinding.FragmentTaskListBinding
import com.example.taskmanager.utils.BounceItemAnimator
import com.example.taskmanager.utils.QuoteManager
import com.example.taskmanager.utils.SoundHelper
import com.google.android.material.snackbar.Snackbar
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var soundHelper: SoundHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundHelper = SoundHelper(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        binding.quoteText.text = QuoteManager.getQuoteOfTheDay()

        setupRecyclerView()

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            tasks?.let {
                adapter.updateTasks(it)
                updateUI(it)
            }
        }

        binding.fabAdd.setOnClickListener {
            val dialog = AddTaskDialog()
            dialog.show(childFragmentManager, "AddTaskDialog")
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onTaskClick = { task ->
                try {
                    val intent = android.content.Intent(requireContext(), com.example.taskmanager.FocusActivity::class.java)
                    intent.putExtra("taskName", task.name)
                    startActivity(intent)
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Could not open Focus Mode", Snackbar.LENGTH_SHORT).show()
                }
            },
            onCheckboxClick = { task, isChecked ->
                val updatedTask = task.copy(isCompleted = isChecked)
                viewModel.updateTask(updatedTask)
                
                if (isChecked) {
                    soundHelper.playBell()
                    checkMilestone()
                }

                val message = if (isChecked) "✓ Task completed!" else "○ Task marked as pending"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            },
            onDeleteClick = { task ->
                soundHelper.playWhoosh()
                viewModel.deleteTask(task)
                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.addTask(task)
                    }.show()
            }
        )

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTasks.adapter = adapter
        binding.recyclerViewTasks.itemAnimator = BounceItemAnimator()
    }

    private fun checkMilestone() {
        val completedCount = viewModel.tasks.value?.count { it.isCompleted } ?: 0
        val totalCount = viewModel.tasks.value?.size ?: 0
        
        if (completedCount == totalCount && totalCount > 0 || (completedCount > 0 && completedCount % 5 == 0)) {
            showConfetti()
        }
    }

    private fun showConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        binding.konfettiView.start(party)
    }

    private fun updateUI(tasks: List<Task>) {
        val isEmpty = tasks.isEmpty()

        binding.recyclerViewTasks.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE

        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val progress = if (total > 0) (completed.toFloat() / total.toFloat()) else 0f

        binding.plantView.setProgress(progress)
        val progressPercent = (progress * 100).toInt()
        binding.progressText.text = "$progressPercent% Plant Health ($completed/$total tasks)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}