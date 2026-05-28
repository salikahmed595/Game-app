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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.data.WeaponUpgradeEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeHub(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity,
    weapons: List<WeaponUpgradeEntity>
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
            // Upper Header Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier.testTag("exit_upgrade_hub_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back To Home Menu",
                        tint = if (isHighContrast) Color.White else SleekTextPrimary
                    )
                }
                Text(
                    text = "HEAVY WEAPON ARMORY",
                    color = if (isHighContrast) Color.White else SleekTextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Text(
                text = "Improve mechanical performance specifications of planetary defense weapons using gained Space War credits. Equip your optimized loadout below.",
                color = if (isHighContrast) Color.White else SleekTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Render each weapon card block
            for (w in weapons) {
                val isSelected = profile.equippedWeaponId == w.weaponId
                val unlockCost = 250
                val statUpgradeCost = 80

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weapon_upgrade_card_${w.weaponId}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHighContrast) Color.Black else SleekSurface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isHighContrast) Color.White
                        else if (isSelected) SleekBlueLight
                        else SleekBorder
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card Header: Title and Equip status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = w.name.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "DESIGNATOR SYSTEM REF: ${w.weaponId.uppercase()}",
                                    color = SleekTextMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!w.isUnlocked) {
                                Row(
                                    modifier = Modifier
                                        .background(
                                            if (isHighContrast) Color.Black else Color(0x22FF5252),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isHighContrast) Color.White else SleekError,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = SleekError,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "LOCKED",
                                        color = SleekError,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            } else {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isHighContrast) Color.White else SleekBlueActiveContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isHighContrast) Color.Black else SleekBlueLight,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "EQUIPPED",
                                            color = if (isHighContrast) Color.Black else Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    // Clickable Equip Box
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isHighContrast) Color.Black else SleekBg,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isHighContrast) Color.White else SleekBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.setEquippedWeapon(w.weaponId) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "EQUIP CORE",
                                            fontSize = 10.sp,
                                            color = if (isHighContrast) Color.White else SleekTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (!w.isUnlocked) {
                            // Locked Purchase button: CTA style matching Deploys
                            Button(
                                onClick = {
                                    viewModel.upgradeWeaponStat(w.weaponId, "unlock", unlockCost)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHighContrast) Color.White else SleekBlueLight,
                                    contentColor = if (isHighContrast) Color.Black else SleekBlueDark
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("unlock_weapon_${w.weaponId}"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "UNLOCK ARMORY DECK • $unlockCost CREDITS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            // Display status meters and upgrade slots using beautiful custom indicators
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Damage Force
                                StatRow(
                                    label = "INTEGRAL DAMAGE FORCE (Tier ${w.damageTier})",
                                    levelValue = w.damageTier,
                                    onUpgradeClick = {
                                        viewModel.upgradeWeaponStat(w.weaponId, "damage", statUpgradeCost)
                                    },
                                    upgradesLeft = w.damageTier < 5,
                                    upgradeCost = statUpgradeCost,
                                    isHighContrast = isHighContrast
                                )

                                // Fire Rate Meter
                                StatRow(
                                    label = "SHELL CYCLING CYCLE FREQUENCY (Tier ${w.fireRateTier})",
                                    levelValue = w.fireRateTier,
                                    onUpgradeClick = {
                                        viewModel.upgradeWeaponStat(w.weaponId, "fireRate", statUpgradeCost)
                                    },
                                    upgradesLeft = w.fireRateTier < 5,
                                    upgradeCost = statUpgradeCost,
                                    isHighContrast = isHighContrast
                                )

                                // Speed Muzzle
                                StatRow(
                                    label = "MUZZLE VELOCITY CHARGE (Tier ${w.speedTier})",
                                    levelValue = w.speedTier,
                                    onUpgradeClick = {
                                        viewModel.upgradeWeaponStat(w.weaponId, "speed", statUpgradeCost)
                                    },
                                    upgradesLeft = w.speedTier < 5,
                                    upgradeCost = statUpgradeCost,
                                    isHighContrast = isHighContrast
                                )

                                // Rebounds
                                StatRow(
                                    label = "TACTICAL SURFACE REBOUND CAPACITY (Tier ${w.bounceTier})",
                                    levelValue = w.bounceTier,
                                    onUpgradeClick = {
                                        viewModel.upgradeWeaponStat(w.weaponId, "bounce", statUpgradeCost)
                                    },
                                    upgradesLeft = w.bounceTier < 4,
                                    upgradeCost = statUpgradeCost,
                                    isHighContrast = isHighContrast
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(
    label: String,
    levelValue: Int,
    onUpgradeClick: () -> Unit,
    upgradesLeft: Boolean,
    upgradeCost: Int,
    isHighContrast: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label, 
                color = if (isHighContrast) Color.White else SleekTextSecondary, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Bold
            )
            if (upgradesLeft) {
                // Interactive pill
                Row(
                    modifier = Modifier
                        .background(
                            if (isHighContrast) Color.White else SleekBlueActiveContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onUpgradeClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UP +$upgradeCost U",
                        color = if (isHighContrast) Color.Black else SleekBlueLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = "MAX LEVEL", 
                    color = SleekTextMuted, 
                    fontSize = 9.sp, 
                    fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Progress segments
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp), 
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (i <= levelValue) {
                                if (isHighContrast) Color.White else SleekBlueLight
                            } else {
                                if (isHighContrast) Color.DarkGray else Color(0x22FFFFFF)
                            }
                        )
                )
            }
        }
    }
}
