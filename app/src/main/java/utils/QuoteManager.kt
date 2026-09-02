package com.example.taskmanager.utils

import kotlin.random.Random

object QuoteManager {

    private val quotes = listOf(
        "💭 \"The secret of getting ahead is getting started.\" - Mark Twain",
        "💭 \"Small steps every day lead to big results.\" - Unknown",
        "💭 \"Done is better than perfect.\" - Facebook Motto",
        "💭 \"You don't have to be great to start, but you have to start to be great.\" - Zig Ziglar",
        "💭 \"The future depends on what you do today.\" - Mahatma Gandhi",
        "💭 \"Productivity is never an accident.\" - Paul J. Meyer",
        "💭 \"Focus on being productive instead of busy.\" - Tim Ferriss",
        "💭 \"Eat that frog first thing in the morning.\" - Mark Twain",
        "💭 \"The best time to plant a tree was 20 years ago. The second best time is now.\" - Chinese Proverb",
        "💭 \"Your future self will thank you.\" - Unknown",
        "💭 \"Make each day your masterpiece.\" - John Wooden",
        "💭 \"The only limit is your mind.\" - Unknown"
    )

    fun getRandomQuote(): String {
        return quotes[Random.nextInt(quotes.size)]
    }

    fun getQuoteOfTheDay(): String {
        val dayOfYear = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val index = (dayOfYear % quotes.size).toInt()
        return quotes[index]
    }
}