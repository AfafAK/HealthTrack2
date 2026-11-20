package com.example.healthtrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealth(data: HealthData)

    @Query("SELECT * FROM health_data ORDER BY date DESC LIMIT 10")
    fun getHealthHistory(): Flow<List<HealthData>>

    @Query("SELECT * FROM health_data ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): HealthData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrajet(trajet: Trajet)

    @Query("SELECT * FROM trajets ORDER BY date DESC")
    fun getAllTrajets(): Flow<List<Trajet>>
}