package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.data.ModdedWeaponEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModdingDeck(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity,
    mods: List<ModdedWeaponEntity>
) {
    val scrollState = rememberScrollState()
    val isHighContrast = profile.highContrast

    // Form inputs for Custom Weapon creation
    var modName by remember { mutableStateOf("Quasar Star-Crusher") }
    var projectileType by remember { mutableStateOf("PLASMA") } // PLASMA, LASER, VORTEX
    var velocityMult by remember { mutableStateOf(16f) } // Range 8f to 30f
    var radiusSize by remember { mutableStateOf(10f) } // Range 5f to 25f
    var bounceLimit by remember { mutableStateOf(2) } // Range 0 to 5
    var damageValue by remember { mutableStateOf(40) } // Range 20 to 100

    // Color Pick sliders Red, Green, Blue
    var rSlider by remember { mutableStateOf(0f) }
    var gSlider by remember { mutableStateOf(220f) }
    var bSlider by remember { mutableStateOf(255f) }

    val computedHexColor by remember {
        derivedStateOf {
            val r = rSlider.toInt().coerceIn(0..255)
            val g = gSlider.toInt().coerceIn(0..255)
            val b = bSlider.toInt().coerceIn(0..255)
            String.format("#FF%02X%02X%02X", r, g, b)
        }
    }

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
            // Upper App Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier.testTag("exit_workshop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, 
                        contentDescription = "Return Main Menu", 
                        tint = if (isHighContrast) Color.White else SleekTextPrimary
                    )
                }
                Text(
                    text = "WEAPON PROTOTYPING WORKSHOP",
                    color = if (isHighContrast) Color.White else SleekTextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Text(
                text = "Welcome to the custom mechanical modding bay. Author custom weapon specifications, load blueprints into firmware storage, and test them instantly in the destructible sandbox!",
                color = if (isHighContrast) Color.White else SleekTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Section 1: Specifications Builder Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPECIFY PROTOTYPE BLUEPRINT",
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

            // Builder Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHighContrast) Color.Black else SleekSurface
                ),
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else SleekBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Weapon Name Input Field
                    OutlinedTextField(
                        value = modName,
                        onValueChange = { modName = it },
                        label = { Text("Prototype Designator Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            unfocusedBorderColor = SleekBorder,
                            focusedBorderColor = SleekBlueLight,
                            focusedLabelColor = SleekBlueLight,
                            unfocusedLabelColor = SleekTextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mod_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Projectile Type Segment Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PROJECTILE SHELL TYPE", 
                            color = if (isHighContrast) Color.White else SleekTextSecondary, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val types = listOf("PLASMA", "LASER", "VORTEX")
                            for (t in types) {
                                val isSel = projectileType == t
                                Button(
                                    onClick = { projectileType = t },
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("mod_type_tab_$t"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(t, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Speed Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MUZZLE ACCELERATION SPEED", color = if (isHighContrast) Color.White else SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${velocityMult.toInt()} PPS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = velocityMult,
                            onValueChange = { velocityMult = it },
                            valueRange = 8f..30f,
                            modifier = Modifier.testTag("mod_speed_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = SleekBlueLight,
                                activeTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }

                    // Size Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SHELL MASS CALIBER SIZE", color = if (isHighContrast) Color.White else SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${radiusSize.toInt()} dp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = radiusSize,
                            onValueChange = { radiusSize = it },
                            valueRange = 5f..25f,
                            modifier = Modifier.testTag("mod_size_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = SleekBlueLight,
                                activeTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }

                    // Bounce Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("WALL ELASTIC REBOUND LIMIT", color = if (isHighContrast) Color.White else SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$bounceLimit bounces", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = bounceLimit.toFloat(),
                            onValueChange = { bounceLimit = it.toInt() },
                            valueRange = 0f..5f,
                            steps = 4,
                            modifier = Modifier.testTag("mod_bounce_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = SleekBlueLight,
                                activeTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }

                    // Damage Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("EXPLOSION BLAST DAMAGE", color = if (isHighContrast) Color.White else SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$damageValue hp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Slider(
                            value = damageValue.toFloat(),
                            onValueChange = { damageValue = it.toInt() },
                            valueRange = 20f..100f,
                            modifier = Modifier.testTag("mod_damage_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = SleekBlueLight,
                                activeTrackColor = SleekBlueActiveContainer
                            )
                        )
                    }

                    // RGB Neon Glow color builder block
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "NEON GLOW COLOR CONSTRUCT", 
                            color = if (isHighContrast) Color.White else SleekTextSecondary, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        // Preview capsule
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(android.graphics.Color.parseColor(computedHexColor)))
                                .border(1.dp, if (isHighContrast) Color.White else SleekBorder, RoundedCornerShape(8.dp)),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PREVIEW HEX: $computedHexColor",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Red slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                            Slider(
                                value = rSlider, 
                                onValueChange = { rSlider = it }, 
                                valueRange = 0f..255f, 
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red.copy(alpha = 0.5f))
                            )
                        }
                        // Green slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                            Slider(
                                value = gSlider, 
                                onValueChange = { gSlider = it }, 
                                valueRange = 0f..255f, 
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green.copy(alpha = 0.5f))
                            )
                        }
                        // Blue slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("B", color = Color.Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                            Slider(
                                value = bSlider, 
                                onValueChange = { bSlider = it }, 
                                valueRange = 0f..255f, 
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue.copy(alpha = 0.5f))
                            )
                        }
                    }

                    // Save blueprint button (CTA style)
                    Button(
                        onClick = {
                            if (modName.isNotBlank()) {
                                viewModel.createAndSaveMod(
                                    name = modName,
                                    projType = projectileType,
                                    speed = velocityMult,
                                    size = radiusSize,
                                    color = computedHexColor,
                                    damage = damageValue,
                                    bounce = bounceLimit
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_mod_blueprint_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else SleekBlueLight,
                            contentColor = if (isHighContrast) Color.Black else SleekBlueDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COMPILE & DOWNLOAD TO FIRMWARE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Section 2: Active Blueprints List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE FIRMWARE BLUEPRINTS (${mods.size})",
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

            if (mods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isHighContrast) Color.Black else SleekSurface)
                        .border(1.dp, if (isHighContrast) Color.White else SleekBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No custom weapon mods compiled yet. Design one above!",
                        color = SleekTextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                for (m in mods) {
                    val modIdStr = "mod_${m.id}"
                    val isCurrentEquipped = profile.equippedWeaponId == modIdStr

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mod_item_card_${m.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHighContrast) Color.Black else SleekSurface
                        ),
                        border = BorderStroke(
                            width = if (isCurrentEquipped) 2.dp else 1.dp,
                            color = if (isHighContrast) Color.White
                            else if (isCurrentEquipped) SleekBlueLight
                            else SleekBorder
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp), 
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(android.graphics.Color.parseColor(m.colorHex)))
                                            .border(0.5.dp, Color.White, RoundedCornerShape(4.dp))
                                    )
                                    Text(
                                        text = m.name.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteMod(m.id) },
                                    modifier = Modifier.testTag("delete_mod_button_${m.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete, 
                                        contentDescription = "Remove Mod", 
                                        tint = SleekError
                                    )
                                }
                            }

                            // Stats Grid / Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isHighContrast) Color.Black else SleekBg, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("VEL: ${m.speed.toInt()}", color = SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("SIZE: ${m.size.toInt()}dp", color = SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("BOUND: ${m.bounceCount}", color = SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("DMG: ${m.damage}", color = SleekTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.setEquippedWeapon(modIdStr) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("equip_mod_${m.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrentEquipped) {
                                            if (isHighContrast) Color.White else SleekBlueActiveContainer
                                        } else {
                                            if (isHighContrast) Color.Black else SleekBg
                                        },
                                        contentColor = if (isCurrentEquipped) {
                                            if (isHighContrast) Color.Black else Color.White
                                        } else {
                                            SleekTextPrimary
                                        }
                                    ),
                                    border = if (!isCurrentEquipped) BorderStroke(1.dp, SleekBorder) else null,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (isCurrentEquipped) "EQUIPPED" else "EQUIP BLUEPRINT", 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.setEquippedWeapon(modIdStr)
                                        viewModel.playMission("SENTRY", isSandbox = true)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("sandbox_test_mod_${m.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHighContrast) Color.White else SleekBlueLight,
                                        contentColor = if (isHighContrast) Color.Black else SleekBlueDark
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("TEST IN SANDBOX", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
