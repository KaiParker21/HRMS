package com.skye.hrms.utilities

object PerformanceMetrics {
    // These are the 5 labels for the radar chart
    val labels = listOf(
        "Productivity",
        "Quality of Work",
        "Communication",
        "Teamwork",
        "Problem Solving"
    )

    // Helper function to create a default (or new) review map
    fun getDefaultMap(): Map<String, Float> {
        return labels.associateWith { 50f } // Default all values to 50
    }
}