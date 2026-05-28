package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenState {
    MAIN_MENU,
    CAMPAIGN_BRANCHES,
    MISSION_PLAY,
    WEAPON_UPGRADES,
    MODDING_LAB,
    LEADERBOARDS,
    SETTINGS
}

data class CampaignNode(
    val id: String,
    val title: String,
    val description: String,
    val imagePrompt: String,
    val choiceALabel: String,
    val choiceANode: String,
    val choiceBLabel: String,
    val choiceBNode: String,
    val missionType: String // REACTOR, SENTRY, CORE, FORTRESS or ENDING
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GameDatabase.getInstance(application)
    private val repository = GameRepository(database.gameDao)

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(ScreenState.MAIN_MENU)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    // Active Mission settings
    val activeMissionType = MutableStateFlow("SENTRY")
    val isSandboxMode = MutableStateFlow(false)

    // Reactive State Flows from Database
    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weaponUpgrades: StateFlow<List<WeaponUpgradeEntity>> = repository.weaponUpgrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moddedWeapons: StateFlow<List<ModdedWeaponEntity>> = repository.moddedWeapons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboardScores: StateFlow<List<LocalScoreEntity>> = repository.leaderboardScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Speech Output Logs for Visually Impaired (narrator Assist)
    private val _narratorAnnouncement = MutableStateFlow("Welcome to Astra Strike. Offline Combat Tactical Simulator initiated.")
    val narratorAnnouncement: StateFlow<String> = _narratorAnnouncement.asStateFlow()

    // Temporary Selected Mod Target
    val activeModWeapon = MutableStateFlow<ModdedWeaponEntity?>(null)

    // Story Nodes Map
    val campaignNodes = mapOf(
        "PROLOGUE" to CampaignNode(
            id = "PROLOGUE",
            title = "Sector 4 Sentry Grid",
            description = "Captain, our heavy cruiser is tracking massive energy emissions from Orbit Alpha. The Sentry fleet is blocking our passage. How do we approach?",
            imagePrompt = "sci-fi spaceships hovering near futuristic defensive space grid, high-contrast laser lines",
            choiceALabel = "Infiltrate Reactor Core (Stealth Hack)",
            choiceANode = "HACK_REACTOR",
            choiceBLabel = "Direct Frontend Fleet Assault (Breach)",
            choiceBNode = "FLEET_ASSAULT",
            missionType = "REACTOR"
        ),
        "HACK_REACTOR" to CampaignNode(
            id = "HACK_REACTOR",
            title = "Thermal Reactor Chamber",
            description = "You successfully bypassed outer security, but entering the glowing fuel core triggers automated thermal defensive drone swarms! Destroy the Core Guard Boss.",
            imagePrompt = "futuristic giant mechanical core glowing with orange sparks, plasma tubes, tactical radar",
            choiceALabel = "System Overcharge (Chaos Action)",
            choiceANode = "ENDING_CHAOS",
            choiceBLabel = "System Stabilization (Order Action)",
            choiceBNode = "ENDING_PARAGON",
            missionType = "REACTOR"
        ),
        "FLEET_ASSAULT" to CampaignNode(
            id = "FLEET_ASSAULT",
            title = "Outer Iron Sentinel Gates",
            description = "Your shields are burning, but firepower remains heavy. The Goliath Sentries have materialised. Break their destructible heavy walls and neutralise the Prime Iron Boss!",
            imagePrompt = "massive cyberpunk blast fortress wall with neon laser turrets firing at heavy armored tanks",
            choiceALabel = "Deploy Colony Core Defense Shields",
            choiceANode = "ENDING_PARAGON",
            choiceBLabel = "Integrate Outer Sentinel Armory for Empire",
            choiceBNode = "ENDING_CONQUEROR",
            missionType = "SENTRY"
        ),
        "ENDING_PARAGON" to CampaignNode(
            id = "ENDING_PARAGON",
            title = "Ending: Earth\'s Savior (Paragon)",
            description = "Congratulations, Captain! Your choices successfully reactivated the local planetary shield array, preserving lives across Orbit Alpha. Complete peace has been restored.",
            imagePrompt = "glowing futuristic peaceful city with vibrant blue force fields protecting civilian towers",
            choiceALabel = "Restart Campaign",
            choiceANode = "PROLOGUE",
            choiceBLabel = "Return Main Menu",
            choiceBNode = "MAIN_MENU",
            missionType = "ENDING"
        ),
        "ENDING_CHAOS" to CampaignNode(
            id = "ENDING_CHAOS",
            title = "Ending: Cosmic Void Wanderer",
            description = "By overcharging the massive reactor core, both the swarm fleet and your cruiser were scattered into a bright starlight void. You survived as a legendary wanderer of the deep nebula.",
            imagePrompt = "beautiful cosmic nebula dust gas swirling deep space with scattered starship mechanical wreckage",
            choiceALabel = "Restart Campaign",
            choiceANode = "PROLOGUE",
            choiceBLabel = "Return Main Menu",
            choiceBNode = "MAIN_MENU",
            missionType = "ENDING"
        ),
        "ENDING_CONQUEROR" to CampaignNode(
            id = "ENDING_CONQUEROR",
            title = "Ending: Iron Galaxy Tyrant",
            description = "You seized the alien sentinel armory codes. Equipped with unmatched technological supremacy, you now conquer and rule the Outer Rim as the Iron Galaxy Emperor.",
            imagePrompt = "armored futuristic warlord sitting on heavy carbon throne holding laser weapon, cyber guard array",
            choiceALabel = "Restart Campaign",
            choiceANode = "PROLOGUE",
            choiceBLabel = "Return Main Menu",
            choiceBNode = "MAIN_MENU",
            missionType = "ENDING"
        )
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultWeapons()
            repository.getOrCreatePlayerProfile()
            generateMockLeaderboard()
        }
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
        SoundSynthesisManager.playTick()
        announce("Navigated to ${screen.name.replace("_", " ").lowercase()}")
    }

    fun playMission(missionType: String, isSandbox: Boolean = false) {
        activeMissionType.value = missionType
        isSandboxMode.value = isSandbox
        navigateTo(ScreenState.MISSION_PLAY)
        announce("Battle began. Objective: Destroy the boss and clear destructible boundaries. Tap to fire!")
    }

    fun selectCampaignChoice(nodeId: String, currentChoiceSelected: String) {
        SoundSynthesisManager.playChime()
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            
            // Build visual ending tracker
            var reached = profile.endingsReached
            if (nodeId.startsWith("ENDING_")) {
                val endingName = nodeId.removePrefix("ENDING_")
                if (!reached.contains(endingName)) {
                    reached = if (reached.isEmpty()) endingName else "$reached,$endingName"
                }
            }

            // Save state progress locally in database
            val updated = profile.copy(
                campaignProgress = if (nodeId == "MAIN_MENU") "PROLOGUE" else nodeId,
                endingsReached = reached
            )
            repository.savePlayerProfile(updated)
            
            if (nodeId == "MAIN_MENU") {
                _currentScreen.value = ScreenState.MAIN_MENU
            } else {
                _currentScreen.value = ScreenState.CAMPAIGN_BRANCHES
            }

            campaignNodes[nodeId]?.let { node ->
                announce("Story Update: ${node.title}. ${node.description}")
            }
        }
    }

    fun addCredits(amount: Int) {
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            val updated = profile.copy(credits = profile.credits + amount)
            repository.savePlayerProfile(updated)
            announce("Acquired $amount credits!")
        }
    }

    fun setEquippedWeapon(weaponId: String) {
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            val updated = profile.copy(equippedWeaponId = weaponId)
            repository.savePlayerProfile(updated)
            val weaponName = weaponUpgrades.value.find { it.weaponId == weaponId }?.name ?: "Mod Weapon"
            announce("Equipped $weaponName")
            SoundSynthesisManager.playChime()
        }
    }

    fun upgradeWeaponStat(weaponId: String, stat: String, cost: Int) {
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            if (profile.credits >= cost) {
                // Find and update stat in database
                val upgrades = weaponUpgrades.value
                val item = upgrades.find { it.weaponId == weaponId } ?: return@launch
                
                val updatedItem = when(stat) {
                    "damage" -> item.copy(damageTier = item.damageTier + 1)
                    "fireRate" -> item.copy(fireRateTier = item.fireRateTier + 1)
                    "speed" -> item.copy(speedTier = item.speedTier + 1)
                    "bounce" -> item.copy(bounceTier = item.bounceTier + 1)
                    "unlock" -> item.copy(isUnlocked = true)
                    else -> item
                }

                repository.upgradeWeapon(updatedItem)
                repository.savePlayerProfile(profile.copy(credits = profile.credits - cost))
                
                SoundSynthesisManager.playChime()
                announce("Successfully upgraded $stat level. Remaining credits: ${profile.credits - cost}")
            } else {
                announce("Insufficient Credits! Please complete campaign battles to earn credits.")
            }
        }
    }

    fun createAndSaveMod(name: String, projType: String, speed: Float, size: Float, color: String, damage: Int, bounce: Int) {
        viewModelScope.launch {
            val newMod = ModdedWeaponEntity(
                name = name,
                projectileType = projType,
                speed = speed,
                size = size,
                colorHex = color,
                damage = damage,
                bounceCount = bounce
            )
            repository.saveModdedWeapon(newMod)
            SoundSynthesisManager.playChime()
            announce("Weapon Blueprint $name successfully compiled and saved to local firmware Database!")
        }
    }

    fun deleteMod(id: Int) {
        viewModelScope.launch {
            repository.deleteModdedWeapon(id)
            announce("Mod blueprint removed.")
        }
    }

    fun submitLocalScore(score: Int, accuracy: Float, stageCleared: String) {
        viewModelScope.launch {
            val scoreObj = LocalScoreEntity(
                playerName = "Commander",
                score = score,
                accuracy = accuracy,
                stageCleared = stageCleared
            )
            repository.saveScore(scoreObj)
            announce("Offline battle score of $score with $accuracy% accuracy filed to global records!")
        }
    }

    fun toggleHighContrast() {
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            val updated = profile.copy(highContrast = !profile.highContrast)
            repository.savePlayerProfile(updated)
            announce("High Contrast visibility mode set to ${if (updated.highContrast) "enabled" else "disabled"}")
        }
    }

    fun toggleNarrator() {
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            val updated = profile.copy(narratorEnabled = !profile.narratorEnabled)
            repository.savePlayerProfile(updated)
            announce("System Voice Narrator set to ${if (updated.narratorEnabled) "active" else "inactive"}")
        }
    }

    fun updatePerformanceMode(mode: String) {
        viewModelScope.launch {
            val profile = repository.getOrCreatePlayerProfile()
            val updated = profile.copy(performanceMode = mode)
            repository.savePlayerProfile(updated)
            announce("Performance rendering level locked to $mode")
        }
    }

    fun announce(text: String) {
        _narratorAnnouncement.value = text
    }

    private suspend fun generateMockLeaderboard() {
        val scores = leaderboardScores.value
        if (scores.isEmpty()) {
            val initial = listOf(
                LocalScoreEntity(playerName = "Aegis_V", score = 9450, accuracy = 94.5f, stageCleared = "SENTRY"),
                LocalScoreEntity(playerName = "Star_Lancer", score = 8720, accuracy = 88.0f, stageCleared = "REACTOR"),
                LocalScoreEntity(playerName = "Ranger_3", score = 7900, accuracy = 82.3f, stageCleared = "REACTOR"),
                LocalScoreEntity(playerName = "Sector_Patrol", score = 6500, accuracy = 75.0f, stageCleared = "SENTRY")
            )
            for (score in initial) {
                repository.saveScore(score)
            }
        }
    }
}
