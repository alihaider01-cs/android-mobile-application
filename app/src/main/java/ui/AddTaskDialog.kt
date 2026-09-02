package com.example.taskmanager.ui

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.R
import com.example.taskmanager.data.Task
import com.example.taskmanager.data.TaskViewModel
import com.example.taskmanager.utils.ReminderReceiver
import com.example.taskmanager.utils.SoundHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskDialog : DialogFragment() {

    private lateinit var viewModel: TaskViewModel
    private var reminderTimestamp: Long? = null

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val soundHelper = SoundHelper(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_task, null)

        val taskNameEditText = view.findViewById<EditText>(R.id.editText_task_name)
        val priorityChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_priority)
        val categoryChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_category)
        val saveButton = view.findViewById<Button>(R.id.button_save)
        val cancelButton = view.findViewById<Button>(R.id.button_cancel)
        val reminderButton = view.findViewById<Button>(R.id.button_set_reminder)
        val selectedReminderText = view.findViewById<TextView>(R.id.text_selected_reminder)

        view.findViewById<Chip>(R.id.chip_medium)?.isChecked = true
        view.findViewById<Chip>(R.id.chip_general)?.isChecked = true

        reminderButton.setOnClickListener {
            showDateTimePicker { timestamp ->
                reminderTimestamp = timestamp
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                selectedReminderText.text = "Reminder: ${sdf.format(timestamp)}"
            }
        }

        saveButton.setOnClickListener {
            val taskName = taskNameEditText.text.toString().trim()
            if (taskName.isEmpty()) {
                Snackbar.make(view, "Please enter a task name", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val priority = when (priorityChipGroup.checkedChipId) {
                R.id.chip_high -> 1
                R.id.chip_medium -> 2
                R.id.chip_low -> 3
                else -> 2
            }

            val category = when (categoryChipGroup.checkedChipId) {
                R.id.chip_shopping -> "Shopping"
                R.id.chip_work -> "Work"
                R.id.chip_personal -> "Personal"
                R.id.chip_home -> "Home"
                R.id.chip_general -> "General"
                else -> "General"
            }

            val newTask = Task(
                name = taskName,
                priority = priority,
                category = category,
                reminderTime = reminderTimestamp
            )

            viewModel.addTask(newTask)
            soundHelper.playPop()

            reminderTimestamp?.let { scheduleReminder(it, taskName) }
            
            val activityView = requireActivity().findViewById<View>(android.R.id.content)
            Snackbar.make(activityView, "Task added: $taskName 🎉", Snackbar.LENGTH_SHORT).show()
            dismiss()
        }

        cancelButton.setOnClickListener { dismiss() }

        builder.setView(view)
        return builder.create()
    }

    private fun showDateTimePicker(onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                onDateTimeSelected(calendar.timeInMillis)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun scheduleReminder(timestamp: Long, taskName: String) {
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("taskName", taskName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            timestamp.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent)
        }
    }
}
