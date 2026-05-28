package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PlayerProfileEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup real-time speech accessibility engines with proper data auditing attribution
        val ttsContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            createAttributionContext("AstraStrikeReader")
        } else {
            applicationContext
        }
        tts = TextToSpeech(ttsContext, this)

        setContent {
            MyApplicationTheme {
                val viewModel: GameViewModel = viewModel()
                
                // Collect states from Room Database Flow
                val currentScreen by viewModel.currentScreen.collectAsState()
                val profileRaw by viewModel.playerProfile.collectAsState()
                val profile = profileRaw ?: PlayerProfileEntity() // safe fallback
                val weapons by viewModel.weaponUpgrades.collectAsState()
                val mods by viewModel.moddedWeapons.collectAsState()
                val scores by viewModel.leaderboardScores.collectAsState()
                val narratorText by viewModel.narratorAnnouncement.collectAsState()

                // Physically speak narrator summaries out loud if accessibility is enabled
                LaunchedEffect(narratorText) {
                    if (profile.narratorEnabled && isTtsReady && narratorText.isNotBlank()) {
                        tts?.speak(narratorText, TextToSpeech.QUEUE_FLUSH, null, "AstraStrikeReader")
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentScreen) {
                            ScreenState.MAIN_MENU -> {
                                MainMenu(viewModel = viewModel, profile = profile)
                            }
                            ScreenState.CAMPAIGN_BRANCHES -> {
                                CampaignHub(viewModel = viewModel, profile = profile)
                            }
                            ScreenState.MISSION_PLAY -> {
                                CombatArena(
                                    viewModel = viewModel,
                                    profile = profile,
                                    weapons = weapons,
                                    mods = mods
                                )
                            }
                            ScreenState.WEAPON_UPGRADES -> {
                                UpgradeHub(viewModel = viewModel, profile = profile, weapons = weapons)
                            }
                            ScreenState.MODDING_LAB -> {
                                ModdingDeck(viewModel = viewModel, profile = profile, mods = mods)
                            }
                            ScreenState.LEADERBOARDS -> {
                                LeaderboardDeck(viewModel = viewModel, profile = profile, scores = scores)
                            }
                            ScreenState.SETTINGS -> {
                                SettingsHub(viewModel = viewModel, profile = profile)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
