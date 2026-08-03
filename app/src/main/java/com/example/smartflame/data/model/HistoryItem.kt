package com.example.smartflame.data.model

data class HistoryItem(
    val alertId: String,
    val date: String,
    val time: String,
    val address: String,
    val status: String,
    val isResolved: Boolean
)
