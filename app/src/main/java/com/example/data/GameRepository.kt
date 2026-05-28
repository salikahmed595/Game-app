package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val playerProfile: Flow<PlayerProfileEntity?> = gameDao.getPlayerProfile()
    val weaponUpgrades: Flow<List<WeaponUpgradeEntity>> = gameDao.getWeaponUpgrades()
    val moddedWeapons: Flow<List<ModdedWeaponEntity>> = gameDao.getModdedWeapons()
    val leaderboardScores: Flow<List<LocalScoreEntity>> = gameDao.getLeaderboardScores()

    suspend fun getOrCreatePlayerProfile(): PlayerProfileEntity {
        val existing = gameDao.getPlayerProfileSync()
        if (existing != null) return existing
        val defaultProfile = PlayerProfileEntity()
        gameDao.insertPlayerProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun savePlayerProfile(profile: PlayerProfileEntity) {
        gameDao.insertPlayerProfile(profile)
    }

    suspend fun initializeDefaultWeapons() {
        val list = gameDao.getWeaponUpgrades().firstOrNull() ?: emptyList()
        if (list.isEmpty()) {
            val defaults = listOf(
                WeaponUpgradeEntity("plasma_plasma", "Plasma Burst", 1, 1, 1, 1, isUnlocked = true),
                WeaponUpgradeEntity("laser_rail", "Rail Vanguard", 1, 1, 1, 1, isUnlocked = false),
                WeaponUpgradeEntity("scatter_shred", "Scatter Shredder", 1, 1, 1, 1, isUnlocked = false),
                WeaponUpgradeEntity("vortex_grav", "Vortex Singularity", 1, 1, 1, 1, isUnlocked = false)
            )
            gameDao.insertWeaponUpgrades(defaults)
        }
    }

    suspend fun upgradeWeapon(upgrade: WeaponUpgradeEntity) {
        gameDao.updateWeaponUpgrade(upgrade)
    }

    suspend fun saveModdedWeapon(weapon: ModdedWeaponEntity) {
        gameDao.insertModdedWeapon(weapon)
    }

    suspend fun deleteModdedWeapon(id: Int) {
        gameDao.deleteModdedWeapon(id)
    }

    suspend fun saveScore(score: LocalScoreEntity) {
        gameDao.insertScore(score)
    }
}
