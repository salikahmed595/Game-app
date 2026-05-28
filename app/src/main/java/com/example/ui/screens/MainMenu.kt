package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.*

@Composable
fun MainMenu(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Player Status Header (Extracted style layout)
        SleekPlayerHeader(profile = profile)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. Main High-Tech Showcase Dispatch Card (Aesthetics matching the theme's flagship alert unit)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .then(
                        if (isHighContrast) {
                            Modifier
                                .background(Color.Black)
                                .border(2.dp, Color.White, RoundedCornerShape(24.dp))
                        } else {
                            Modifier
                                .background(SleekSurface)
                                .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
                        }
                    )
                    .padding(20.dp)
            ) {
                // Outer absolute indicator badge (Top Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            if (isHighContrast) Color.White else SleekOrangePrimary,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TACTICAL",
                        color = if (isHighContrast) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CODENAME: PROJECT ASTRA",
                        color = if (isHighContrast) Color.White else SleekOrangeLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "ASTRA STRIKE",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "Take command of magnetic vector force-fields. Demolish tactical blockades, deflect sentry charges, and author custom kinetic physics in the modding workshop.",
                        color = if (isHighContrast) Color.White else SleekTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Secondary tags row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SleekBg, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, SleekBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text("SYSTEM NOISE", color = SleekTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("SYNTHESIZED", color = SleekGreenGlow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(SleekBg, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, SleekBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text("SANDBOX CORE", color = SleekTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("UNLOCKED", color = SleekBlueLight, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // 3. Primary "DEPLOY TO MISSION" Style Button
            Button(
                onClick = { viewModel.navigateTo(ScreenState.CAMPAIGN_BRANCHES) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("play_campaign_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHighContrast) Color.White else SleekBlueLight,
                    contentColor = if (isHighContrast) Color.Black else SleekBlueDark
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DEPLOY TO MISSION HUB",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Separator Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SECTOR LOGISTICS DECK",
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

            // 4. Asymmetric High-Tech Double Column Grid Layout
            // Replace old vertical list with an aligned, high-fidelity responsive layout matching style blocks!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Column 1
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Grid Card 1: Weaponry Upgrades
                    SleekGridCard(
                        title = "Armory Hub",
                        subtitle = "Calibrate kinetic blaster stats",
                        icon = Icons.Default.Build,
                        pillText = "UPGRADE",
                        pillColor = SleekGreenAccent,
                        testTag = "weapon_upgrade_button",
                        onClick = { viewModel.navigateTo(ScreenState.WEAPON_UPGRADES) },
                        isHighContrast = isHighContrast
                    )

                    // Grid Card 3: Regional Leaderboard Scores
                    SleekGridCard(
                        title = "Regional Ranks",
                        subtitle = "Off-grid competitive board",
                        icon = Icons.Default.Menu,
                        pillText = "BOARD",
                        pillColor = SleekBlueSecondary,
                        testTag = "leaderboards_button",
                        onClick = { viewModel.navigateTo(ScreenState.LEADERBOARDS) },
                        isHighContrast = isHighContrast
                    )
                }

                // Column 2
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Grid Card 2: Custom Mod blueprints Lab
                    SleekGridCard(
                        title = "Blueprint Deck",
                        subtitle = "Synthesize custom plasma",
                        icon = Icons.Default.AddCircle,
                        pillText = "WORKSHOP",
                        pillColor = SleekOrangeLight,
                        testTag = "modding_lab_button",
                        onClick = { viewModel.navigateTo(ScreenState.MODDING_LAB) },
                        isHighContrast = isHighContrast
                    )

                    // Grid Card 4: Settings Calibration
                    SleekGridCard(
                        title = "Calibration",
                        subtitle = "Inclusive accessibility configs",
                        icon = Icons.Default.Settings,
                        pillText = "SYSTEMS",
                        pillColor = SleekTextMuted,
                        testTag = "settings_button",
                        onClick = { viewModel.navigateTo(ScreenState.SETTINGS) },
                        isHighContrast = isHighContrast
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Secured Status Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SleekGreenGlow)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SECURED LOCAL DATABASE SAVES ACTIVED",
                color = SleekTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SleekGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    pillText: String,
    pillColor: Color,
    testTag: String,
    onClick: () -> Unit,
    isHighContrast: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighContrast) Color.Black else SleekSurface)
            .border(
                width = 1.dp,
                color = if (isHighContrast) Color.White else SleekBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Icon + Pill Status Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighContrast) Color.White else SleekBlueLight,
                    modifier = Modifier.size(20.dp)
                )

                Box(
                    modifier = Modifier
                        .background(
                            if (isHighContrast) Color.White else SleekBorder,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pillText,
                        color = if (isHighContrast) Color.Black else SleekTextPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Content
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = if (isHighContrast) Color.White else SleekTextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
