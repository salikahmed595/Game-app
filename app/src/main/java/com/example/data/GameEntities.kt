package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val credits: Int = 200,
    val campaignProgress: String = "PROLOGUE",
    val endingsReached: String = "",
    val highContrast: Boolean = false,
    val performanceMode: String = "HIGH", // HIGH, COMPACT, BATTERY_SAVER
    val narratorEnabled: Boolean = false,
    val equippedWeaponId: String = "plasma_plasma",
    val controlMappingJson: String = "" // Remapped inputs in JSON or key-value format
)

@Entity(tableName = "weapon_upgrades")
data class WeaponUpgradeEntity(
    @PrimaryKey val weaponId: String,
    val name: String,
    val damageTier: Int = 1,
    val fireRateTier: Int = 1,
    val speedTier: Int = 1,
    val bounceTier: Int = 1,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "modded_weapons")
data class ModdedWeaponEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val projectileType: String, // LASER, FIREBALL, PULSE
    val speed: Float, // speed velocity
    val size: Float, // projectile radius/width
    val colorHex: String,
    val damage: Int,
    val bounceCount: Int
)

@Entity(tableName = "leaderboard_scores")
data class LocalScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val accuracy: Float,
    val stageCleared: String,
    val timestamp: Long = System.currentTimeMillis()
)
