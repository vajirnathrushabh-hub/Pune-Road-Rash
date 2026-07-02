package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM bikes")
    fun getAllBikes(): Flow<List<BikeEntity>>

    @Query("SELECT * FROM bikes WHERE id = :id LIMIT 1")
    suspend fun getBikeById(id: String): BikeEntity?

    @Query("SELECT * FROM bikes WHERE current = 1 LIMIT 1")
    fun getActiveBike(): Flow<BikeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBikes(bikes: List<BikeEntity>)

    @Update
    suspend fun updateBike(bike: BikeEntity)

    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerProfile(profile: PlayerProfileEntity)

    @Update
    suspend fun updatePlayerProfile(profile: PlayerProfileEntity)
}
