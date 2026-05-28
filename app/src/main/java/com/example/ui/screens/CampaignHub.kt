package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
fun CampaignHub(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity
) {
    val scrollState = rememberScrollState()
    val isHighContrast = profile.highContrast

    // Extract node detail
    val activeNodeId = profile.campaignProgress
    val node = viewModel.campaignNodes[activeNodeId] ?: viewModel.campaignNodes["PROLOGUE"]!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isHighContrast) Color.Black else SleekBg)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // App header (Player Info can sit nicely right here under the maps)
        SleekPlayerHeader(profile = profile)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upper App Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier.testTag("exit_campaign_hub_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Return home Menu",
                        tint = if (isHighContrast) Color.White else SleekTextPrimary
                    )
                }
                Text(
                    text = "CAMPAIGN DISPATCH HUB",
                    color = if (isHighContrast) Color.White else SleekTextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Large Narrative Box (Styled to resemble Act III: Iron Siege flagship card)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500))
            ) {
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
                    // BOSS / ADVISORY tag on top right
                    val isBossNode = node.missionType == "BOSS" || node.title.lowercase().contains("boss") || node.title.lowercase().contains("titan")
                    if (isBossNode) {
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
                                text = "BOSS ALERT",
                                color = if (isHighContrast) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Region Locator subtitle
                        Text(
                            text = "STORY COMPONENT: ${node.title.uppercase()}",
                            color = if (isHighContrast) Color.White else SleekOrangeLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Main Giant Node Heading
                        Text(
                            text = node.title.replace(":", "\n").uppercase(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 32.sp
                        )

                        // Mock Visual-novel Simulator Graphics Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isHighContrast) Color.Black else SleekBg)
                                .border(1.dp, if (isHighContrast) Color.White else SleekBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    tint = if (isHighContrast) Color.White else SleekOrangeLight,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "[RE-RENDERED HOVER MOCKUP: ${node.imagePrompt}]",
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    color = SleekTextSecondary,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Rich Descriptive text
                        Text(
                            text = node.description,
                            color = if (isHighContrast) Color.White else SleekTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // Branch Choices Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DECISION CODES AVAILABLE",
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

            // Decisions Columns
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Choice A
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
                        .clickable {
                            viewModel.selectCampaignChoice(node.choiceANode, "A")
                        }
                        .testTag("campaign_choice_a_button")
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isHighContrast) Color.White else SleekBorder,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ALPHA",
                                color = if (isHighContrast) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = node.choiceALabel,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Choice B
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
                        .clickable {
                            viewModel.selectCampaignChoice(node.choiceBNode, "B")
                        }
                        .testTag("campaign_choice_b_button")
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isHighContrast) Color.White else SleekBorder,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BETA",
                                color = if (isHighContrast) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = node.choiceBLabel,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Firing Mission launching anchor! Only display if current location is a Battle node
                if (node.missionType != "ENDING" && node.missionType != "MAIN_MENU") {
                    Button(
                        onClick = {
                            viewModel.playMission(node.missionType)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("launch_mission_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else SleekBlueLight,
                            contentColor = if (isHighContrast) Color.Black else SleekBlueDark
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DEPLOY TO MISSION: ${node.missionType}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
