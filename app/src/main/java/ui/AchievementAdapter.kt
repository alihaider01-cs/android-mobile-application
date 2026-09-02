package com.example.taskmanager.ui

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.data.Achievement
import com.example.taskmanager.databinding.ItemAchievementBinding

class AchievementAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(val binding: ItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.binding.apply {
            textAchievementTitle.text = achievement.title
            textAchievementDescription.text = achievement.description
            textAchievementIcon.text = achievement.icon

            if (achievement.isEarned) {
                layoutAchievement.alpha = 1.0f
                imageEarnedStatus.setImageResource(android.R.drawable.checkbox_on_background)
                imageEarnedStatus.setColorFilter(Color.parseColor("#4CAF50"))
                textAchievementIcon.background = null
            } else {
                layoutAchievement.alpha = 0.5f
                imageEarnedStatus.setImageResource(android.R.drawable.checkbox_off_background)
                imageEarnedStatus.setColorFilter(Color.LTGRAY)
                
                // Grayscale effect for icon text if needed, but emojis don't respond well to that easily
                // Instead just lower alpha
            }
        }
    }

    override fun getItemCount() = achievements.size
}
