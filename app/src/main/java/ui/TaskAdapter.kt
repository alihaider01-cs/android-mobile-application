package com.example.taskmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.data.Task
import com.example.taskmanager.databinding.ItemTaskBinding

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxClick: (Task, Boolean) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks = listOf<Task>()

    fun updateTasks(newTasks: List<Task>) {
        val diffCallback = TaskDiffCallback(tasks, newTasks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tasks = newTasks
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.taskName.text = task.name
            binding.taskCategory.text = task.category
            binding.priorityChip.text = task.getPriorityText()
            
            // Show cloud only for incomplete tasks
            binding.animatedCloud.setVisible(!task.isCompleted, true)

            if (task.reminderTime != null) {
                binding.textReminder.visibility = android.view.View.VISIBLE
                val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                binding.textReminder.text = "⏰ ${sdf.format(java.util.Date(task.reminderTime))}"
            } else {
                binding.textReminder.visibility = android.view.View.GONE
            }

            val colorRes = when (task.priority) {
                1 -> com.google.android.material.R.color.design_default_color_error
                2 -> com.google.android.material.R.color.design_default_color_primary
                else -> com.google.android.material.R.color.design_default_color_secondary
            }
            binding.priorityChip.setChipBackgroundColorResource(colorRes)

            binding.taskCheckbox.setOnCheckedChangeListener(null)
            binding.taskCheckbox.isChecked = task.isCompleted

            binding.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClick(task, isChecked)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(task)
            }

            binding.root.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    class TaskDiffCallback(private val oldList: List<Task>, private val newList: List<Task>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
