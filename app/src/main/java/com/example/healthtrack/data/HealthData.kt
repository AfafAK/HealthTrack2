package com.example.healthtrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val poids: Float,
    val taille: Float,
    val tension: String,
    val date: Long = System.currentTimeMillis()
) {
    val imc: Float
        get() = poids / ((taille / 100f) * (taille / 100f))
}