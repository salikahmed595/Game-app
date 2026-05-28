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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.*

@Composable
fun SettingsHub(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity
) {
    val scrollState = rememberScrollState()
    val isHighContrast = profile.highContrast
    val isNarrator = profile.narratorEnabled
    val activePerf = profile.performanceMode

    // Custom remap variables
    var leftControlXOffset by remember { mutableStateOf(100) }
    var fireButtonSize by remember { mutableStateOf(50) }

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
            // Upper App Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier.testTag("exit_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, 
                        contentDescription = "Return main menu", 
                        tint = if (isHighContrast) Color.White else SleekTextPrimary
                    )
                }
                Text(
                    text = "TACTICAL CALIBRATION SETTINGS",
                    color = if (isHighContrast) Color.White else SleekTextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Section 1: Accessibility
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INCLUSIVE ACCESSIBILITY DESIGN",
                    color = if (isHighContrast) Color.White else SleekOrangeLight,
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

            // High Contrast Mode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else SleekSurface),
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "HIGH CONTRAST INTERFACE", 
                                color = Color.White, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Replaces background sweeps/glowing elements with solid high-contrast black tones and white outlines for clear ocular tracking.", 
                                color = if (isHighContrast) Color.White else SleekTextSecondary, 
                                fontSize = 11.sp, 
                                lineHeight = 15.sp
                            )
                        }
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = { viewModel.toggleHighContrast() },
                            modifier = Modifier.testTag("toggle_high_contrast_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SleekBlueLight,
                                checkedTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }
                }
            }

            // Tactical Narrator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else SleekSurface),
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "TACTICAL NARRATOR VOICE", 
                                color = Color.White, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Broadcasts real-time physical damage logs, blaster specifications, and boss barrier status loudly using text-to-speech engine to aid visually impaired players.", 
                                color = if (isHighContrast) Color.White else SleekTextSecondary, 
                                fontSize = 11.sp, 
                                lineHeight = 15.sp
                            )
                        }
                        Switch(
                            checked = isNarrator,
                            onCheckedChange = { viewModel.toggleNarrator() },
                            modifier = Modifier.testTag("toggle_narrator_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SleekBlueLight,
                                checkedTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }
                }
            }

            // Section 2: Performance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HARDWARE PERFORMANCE CALIBRATION",
                    color = if (isHighContrast) Color.White else SleekOrangeLight,
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else SleekSurface),
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "CENSOR SPARK PARTICLE EMISSIONS", 
                        color = Color.White, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Locks physics processing to 30Hz units, caps maximum combustion impacts to 10 sparks simultaneously, and wipes blurs to spare legacy chipset cells.", 
                        color = if (isHighContrast) Color.White else SleekTextSecondary, 
                        fontSize = 11.sp, 
                        lineHeight = 15.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf("HIGH", "BALANCED", "LOW_END_OPTIMIZED")
                        for (m in modes) {
                            val isSel = activePerf == m
                            Button(
                                onClick = { viewModel.updatePerformanceMode(m) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) {
                                        if (isHighContrast) Color.White else SleekBlueLight
                                    } else {
                                        if (isHighContrast) Color.Black else SleekBg
                                    },
                                    contentColor = if (isSel) {
                                        if (isHighContrast) Color.Black else SleekBlueDark
                                    } else {
                                        Color.White
                                    }
                                ),
                                border = if (!isSel) BorderStroke(1.dp, SleekBorder) else null,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("performance_mode_$m"),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = m.replace("_", " "), 
                                    fontSize = 9.sp, 
                                    fontWeight = FontWeight.Black, 
                                    lineHeight = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Remap
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HUD INTERACTIVE REMAPPING",
                    color = if (isHighContrast) Color.White else SleekOrangeLight,
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else SleekSurface),
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "RE-CALIBRATE ON-SCREEN TOUCH LOCATIONS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )

                    // Joystick slider position
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("JOYSTICK SIDE OFFSET", color = if (isHighContrast) Color.White else SleekTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${leftControlXOffset}px", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = leftControlXOffset.toFloat(),
                            onValueChange = { leftControlXOffset = it.toInt() },
                            valueRange = 50f..250f,
                            modifier = Modifier.testTag("joystick_pad_remapping_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = SleekBlueLight,
                                activeTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }

                    // Fire Button Size slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VIRTUAL FIRE TRIGGER COIL FOOTPRINT", color = if (isHighContrast) Color.White else SleekTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${fireButtonSize}dp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = fireButtonSize.toFloat(),
                            onValueChange = { fireButtonSize = it.toInt() },
                            valueRange = 40f..80f,
                            modifier = Modifier.testTag("fire_button_remapping_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = SleekBlueLight,
                                activeTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }

                    Button(
                        onClick = {
                            leftControlXOffset = 100
                            fireButtonSize = 50
                            viewModel.announce("All inputs reconfigured back to default factory parameters!")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_inputs_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else SleekBg,
                            contentColor = if (isHighContrast) Color.Black else Color.White
                        ),
                        border = if (isHighContrast) BorderStroke(1.dp, Color.White) else BorderStroke(1.dp, SleekBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESET TO STANDARD FACTORY SCHEME", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
