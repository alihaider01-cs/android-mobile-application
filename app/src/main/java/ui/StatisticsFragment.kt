package com.example.taskmanager.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.taskmanager.data.Task
import com.example.taskmanager.data.TaskViewModel
import com.example.taskmanager.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            processDataAndUpdateUI(tasks)
        }
    }

    private fun processDataAndUpdateUI(tasks: List<Task>) {
        lifecycleScope.launch {
            // Data processing in background
            val stats = withContext(Dispatchers.Default) {
                calculateStats(tasks)
            }
            
            // UI Update in Main thread
            if (_binding != null) {
                updateUI(stats, tasks)
            }
        }
    }

    private data class StatsResult(
        val total: Int,
        val completed: Int,
        val pending: Int,
        val productiveDay: String,
        val priorityCounts: List<Float>,
        val categoryData: Pair<List<BarEntry>, List<String>>,
        val weeklyData: Pair<List<Entry>, List<String>>
    )

    private fun calculateStats(tasks: List<Task>): StatsResult {
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed

        // Productive day
        val completedTasks = tasks.filter { it.isCompleted }
        val mostProductive = if (completedTasks.isNotEmpty()) {
            val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
            val dayCounts = completedTasks.groupingBy { sdf.format(Date(it.createdAt)) }.eachCount()
            dayCounts.maxByOrNull { it.value }?.key ?: "None"
        } else "None"

        // Priorities
        val high = tasks.count { it.priority == 1 }.toFloat()
        val medium = tasks.count { it.priority == 2 }.toFloat()
        val low = tasks.count { it.priority == 3 }.toFloat()

        // Categories
        val categoryCounts = tasks.groupingBy { it.category }.eachCount()
        val catEntries = categoryCounts.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }
        val catLabels = categoryCounts.keys.toList()

        // Weekly
        val weeklySdf = SimpleDateFormat("EEE", Locale.getDefault())
        val days = mutableListOf<String>()
        val counts = mutableListOf<Entry>()
        for (i in 0..6) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            days.add(0, weeklySdf.format(cal.time))
            val start = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
            val end = start + 86400000L
            val count = tasks.count { it.createdAt in start until end }
            counts.add(0, Entry((6 - i).toFloat(), count.toFloat()))
        }

        return StatsResult(
            total, completed, pending, mostProductive,
            listOf(high, medium, low),
            catEntries to catLabels,
            counts to days
        )
    }

    private fun updateUI(stats: StatsResult, tasks: List<Task>) {
        binding.textTotalCount.text = stats.total.toString()
        binding.textCompletedCount.text = stats.completed.toString()
        binding.textPendingCount.text = stats.pending.toString()
        binding.textProductiveDay.text = stats.productiveDay

        setupCompletionChart(stats.completed.toFloat(), stats.pending.toFloat())
        setupPriorityChart(stats.priorityCounts)
        setupCategoryChart(stats.categoryData.first, stats.categoryData.second)
        setupWeeklyChart(stats.weeklyData.first, stats.weeklyData.second)
    }

    private fun setupCompletionChart(completed: Float, pending: Float) {
        val entries = listOf(PieEntry(completed, "Completed"), PieEntry(pending, "Pending"))
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#48BB78"), Color.parseColor("#F56565"))
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        binding.pieChartCompletion.apply {
            data = PieData(dataSet).apply { setValueFormatter(PercentFormatter(binding.pieChartCompletion)) }
            setUsePercentValues(true)
            description.isEnabled = false
            setEntryLabelColor(Color.TRANSPARENT)
            animateY(800)
            invalidate()
        }
    }

    private fun setupPriorityChart(counts: List<Float>) {
        val entries = mutableListOf<PieEntry>()
        if (counts[0] > 0) entries.add(PieEntry(counts[0], "High"))
        if (counts[1] > 0) entries.add(PieEntry(counts[1], "Medium"))
        if (counts[2] > 0) entries.add(PieEntry(counts[2], "Low"))

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#F56565"), Color.parseColor("#ECC94B"), Color.parseColor("#4299E1"))
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        binding.pieChartPriority.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setEntryLabelColor(Color.TRANSPARENT)
            animateY(800)
            invalidate()
        }
    }

    private fun setupCategoryChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Tasks").apply {
            color = Color.parseColor("#667eea")
            valueTextSize = 10f
        }
        binding.barChartCategory.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            animateX(800)
            invalidate()
        }
    }

    private fun setupWeeklyChart(entries: List<Entry>, labels: List<String>) {
        val dataSet = LineDataSet(entries, "New Tasks").apply {
            color = Color.parseColor("#764ba2")
            setCircleColor(Color.parseColor("#764ba2"))
            lineWidth = 2f
            setDrawFilled(true)
            fillColor = Color.parseColor("#667eea")
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.lineChartWeekly.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
            animateY(800)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
