package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM weapon_upgrades")
    fun getWeaponUpgrades(): Flow<List<WeaponUpgradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeaponUpgrades(upgrades: List<WeaponUpgradeEntity>)

    @Update
    suspend fun updateWeaponUpgrade(upgrade: WeaponUpgradeEntity)

    @Query("SELECT * FROM modded_weapons ORDER BY id DESC")
    fun getModdedWeapons(): Flow<List<ModdedWeaponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModdedWeapon(weapon: ModdedWeaponEntity)

    @Query("DELETE FROM modded_weapons WHERE id = :id")
    suspend fun deleteModdedWeapon(id: Int)

    @Query("SELECT * FROM leaderboard_scores ORDER BY score DESC LIMIT 100")
    fun getLeaderboardScores(): Flow<List<LocalScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: LocalScoreEntity)
}
