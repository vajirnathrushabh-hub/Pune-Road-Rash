package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bikes")
data class BikeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val englishName: String,
    val description: String,
    val owned: Boolean,
    val engineStage: Int = 0,      // 0: Stock, 1: High-Perf Piston, 2: Racing Cylinder, 3: Pro Turbo Setup
    val ecuStage: Int = 0,         // 0: Stock, 1: Stage 1 Remap, 2: Stage 2 Racing, 3: Custom ECU Tuner
    val exhaustStage: Int = 0,     // 0: Silent, 1: Semi Thump, 2: Loud Scream, 3: Pure Custom Thump/Decibel
    val airFilterStage: Int = 0,   // 0: Stock, 1: Cotton Grid, 2: High Flow, 3: Air Intake Charger
    val paintColor: Int = 0,       // Color index or Hex
    val neonColor: Int = 0,        // Color index (0 means none, others represent Red, Purple, Green, etc.)
    val stickerText: String = "",  // Marathi sticker text (e.g., "एकच राजा", "पुणेकर")
    val current: Boolean = false,
    val baseSpeed: Float,
    val baseAcceleration: Float,
    val baseHandling: Float,
    val price: Int
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val cash: Int = 5000,
    val unlockedTracks: String = "swargate" // Comma-separated track IDs
)
