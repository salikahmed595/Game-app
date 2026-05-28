package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.ui.theme.*

@Composable
fun SleekPlayerHeader(
    profile: PlayerProfileEntity,
    modifier: Modifier = Modifier
) {
    val isHighContrast = profile.highContrast

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player info (Avatar & Level & Username)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar Bubble V
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(
                        if (isHighContrast) {
                            Modifier
                                .background(Color.White)
                                .border(2.dp, Color.Black, CircleShape)
                        } else {
                            Modifier
                                .background(
                                    Brush.sweepGradient(
                                        listOf(SleekOrangePrimary, SleekOrangeLight, SleekOrangePrimary)
                                    )
                                )
                                .border(2.dp, SleekTextPrimary, CircleShape)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    color = if (isHighContrast) Color.Black else SleekBg,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Username and Level
            Column {
                Text(
                    text = "LEVEL 42",
                    color = if (isHighContrast) Color.White else SleekTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Viper_Strike",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Stats Badge (Credits, Offline Mode indicator)
        Row(
            modifier = Modifier
                .background(
                    if (isHighContrast) Color.Black else SleekSurface,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isHighContrast) Color.White else SleekBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Credits
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🪙",
                    fontSize = 14.sp
                )
                Text(
                    text = String.format("%,d", profile.credits),
                    color = if (isHighContrast) Color.White else SleekTextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            // Separator line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(if (isHighContrast) Color.White else SleekBorder)
            )

            // Status label
            Text(
                text = "OFFLINE",
                color = if (isHighContrast) Color.White else SleekTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
