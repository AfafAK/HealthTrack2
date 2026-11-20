package com.example.healthtrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trajets")
data class Trajet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nom: String,
    val distance: Float,
    val duree: Long,
    val pas: Int,
    val calories: Float,
    val date: Long = System.currentTimeMillis(),
    val routeJson: String = ""
)