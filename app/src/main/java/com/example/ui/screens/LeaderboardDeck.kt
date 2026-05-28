package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.data.LocalScoreEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.*

@Composable
fun LeaderboardDeck(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity,
    scores: List<LocalScoreEntity>
) {
    val scrollState = rememberScrollState()
    val isHighContrast = profile.highContrast

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isHighContrast) Color.Black else SleekBg)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Player Status Header
        SleekPlayerHeader(profile = profile)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upper navigation app bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier.testTag("exit_leaderboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, 
                        contentDescription = "Back To Home Menu", 
                        tint = if (isHighContrast) Color.White else SleekTextPrimary
                    )
                }
                Text(
                    text = "ASTRA REGIONAL LEADERBOARDS",
                    color = if (isHighContrast) Color.White else SleekTextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Secured offline registries Card Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHighContrast) Color.Black else SleekSurface
                ),
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star, 
                        contentDescription = null, 
                        tint = if (isHighContrast) Color.White else SleekOrangeLight, 
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "SECURED OFFLINE REGISTRY",
                            color = if (isHighContrast) Color.White else SleekOrangeLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your high scores are catalogued in secure local SQLite storage. Once you connect to the regional comms grids, results merge automatically with seasonal rankings.",
                            color = if (isHighContrast) Color.White else SleekTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Section boundary title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP COMMENDED RANKS OFFICE",
                    color = SleekTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(SleekBorder)
                )
            }

            // Ranks Listing Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (scores.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No records archived yet. Complete campaign missions to post high scores!", 
                            color = SleekTextMuted, 
                            fontSize = 12.sp
                        )
                    }
                } else {
                    scores.sortedByDescending { it.score }.forEachIndexed { index, item ->
                        val isPlayer = item.playerName == "Commander"
                        val medalColor = when (index) {
                            0 -> Color(0xFFFFD700) // Beautiful Gold matching the theme coin
                            1 -> Color(0xFFB0BEC5) // Silver
                            2 -> Color(0xFFFFAB91) // Coral Bronze
                            else -> if (isHighContrast) Color.White else SleekBorder
                        }

                        val cellBackground = if (isHighContrast) {
                            Color.Black
                        } else {
                            if (isPlayer) SleekSurface else SleekSurface.copy(alpha = 0.6f)
                        }

                        val cellBorder = if (isHighContrast) {
                            Color.White
                        } else {
                            if (isPlayer) SleekBlueLight else SleekBorder
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cellBackground, shape = RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isPlayer) 2.dp else 1.dp,
                                    color = cellBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Rank Number Box
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(medalColor, shape = RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isPlayer) "Viper_Strike" else item.playerName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        if (isPlayer) {
                                            Box(
                                                modifier = Modifier
                                                    .background(SleekBlueActiveContainer, shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "YOU", 
                                                    fontSize = 8.sp, 
                                                    fontWeight = FontWeight.Black, 
                                                    color = SleekBlueLight
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Stage: ${item.stageCleared.uppercase()} • Accuracy: ${item.accuracy.toInt()}%",
                                        color = if (isHighContrast) Color.White else SleekTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "${item.score} PTS",
                                color = if (isHighContrast) Color.White else if (isPlayer) SleekBlueLight else SleekTextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                modifier = Modifier.testTag("leaderboard_score_text_${index}")
                            )
                        }
                    }
                }
            }
        }
    }
}
