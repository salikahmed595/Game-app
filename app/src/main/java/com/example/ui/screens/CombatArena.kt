package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfileEntity
import com.example.data.WeaponUpgradeEntity
import com.example.data.ModdedWeaponEntity
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.SoundSynthesisManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Physics data classes
data class Bullet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val r: Float,
    var bouncesLeft: Int,
    val damage: Float,
    val trail: MutableList<Offset> = mutableListOf()
)

data class EnemyMissile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val r: Float = 12f,
    var hp: Float = 1f
)

data class BlockSegment(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    var hp: Int,
    val maxHp: Int = 3
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 down to 0
    val color: Color,
    val r: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatArena(
    viewModel: GameViewModel,
    profile: PlayerProfileEntity,
    weapons: List<WeaponUpgradeEntity>,
    mods: List<ModdedWeaponEntity>
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    // Selected Weapon
    val equippedId = profile.equippedWeaponId
    val normalWeapon = weapons.find { it.weaponId == equippedId }
    val modWeapon = mods.find { "mod_${it.id}" == equippedId }

    // Weapon physics variables derived from upgrades or mod config
    val weaponName = normalWeapon?.name ?: modWeapon?.name ?: "Plasma Burst"
    val bulletColor = when {
        modWeapon != null -> Color(android.graphics.Color.parseColor(modWeapon.colorHex))
        equippedId.contains("laser") -> Color(0xFF00E676)
        equippedId.contains("scatter") -> Color(0xFFFF9100)
        equippedId.contains("vortex") -> Color(0xFFD500F9)
        else -> Color(0xFFFF1744)
    }

    val baseDamage = 10f + (normalWeapon?.damageTier ?: 1) * 5f + (modWeapon?.damage?.toFloat() ?: 0f)
    val baseSpeed = 15f + (normalWeapon?.speedTier ?: 1) * 3f + (modWeapon?.speed ?: 0f)
    val baseBounces = (normalWeapon?.bounceTier ?: 1) - 1 + (modWeapon?.bounceCount ?: 0)
    val fireIntervalMs = maxOf(100L, 500L - (normalWeapon?.fireRateTier ?: 1) * 75L)

    // Game state tracking
    var score by remember { mutableStateOf(0) }
    var hits by remember { mutableStateOf(0) }
    var shotsFired by remember { mutableStateOf(0) }
    var bossHp by remember { mutableStateOf(1000f) }
    val maxBossHp = 1000f
    var bossShieldHp by remember { mutableStateOf(300f) }
    val maxBossShieldHp = 300f

    // Canvas game components
    val bullets = remember { mutableStateListOf<Bullet>() }
    val enemyMissiles = remember { mutableStateListOf<EnemyMissile>() }
    val barriers = remember { mutableStateListOf<BlockSegment>() }
    val particles = remember { mutableStateListOf<Particle>() }

    // Screen Shake Offset
    var screenShakeAmount by remember { mutableStateOf(0f) }

    // Joystick & Aiming Angle (degrees, 0 is straight right)
    var aimAngle by remember { mutableStateOf(0f) }
    var isAiming by remember { mutableStateOf(false) }
    var cooldownLeft by remember { mutableStateOf(0L) }
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }

    // Simulated Speech Out / Caption
    var combatMessage by remember { mutableStateOf("TREATY GRID INITIATED. Boss Armor loaded.") }

    val perfMode = profile.performanceMode
    val isHighContrast = profile.highContrast

    // Setup environments (destructible obstacles) once screen size is known
    var initialized by remember { mutableStateOf(false) }
    var wSize by remember { mutableStateOf(Offset(1000f, 600f)) }

    fun setupArena(width: Float, height: Float) {
        bullets.clear()
        enemyMissiles.clear()
        barriers.clear()
        particles.clear()
        bossHp = maxBossHp
        bossShieldHp = maxBossShieldHp
        score = 0
        hits = 0
        shotsFired = 0
        gameOver = false
        gameWon = false
        aimAngle = 0f
        combatMessage = "Targeting initialized. Fire at the Boss Shield!"

        // Create destructible grid barrier in center
        val barrierCenterX = width * 0.5f
        val barrierWidth = 40f
        val barrierHeight = 40f
        val rows = 8
        val cols = 3
        val startY = height * 0.15f

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                barriers.add(
                    BlockSegment(
                        x = barrierCenterX - (cols * barrierWidth * 0.5f) + (c * barrierWidth),
                        y = startY + (r * barrierHeight * 1.1f),
                        w = barrierWidth,
                        h = barrierHeight,
                        hp = if (viewModel.activeMissionType.value == "REACTOR") 4 else 3
                    )
                )
            }
        }
        initialized = true
    }

    // Fire function
    fun fireBullet() {
        if (cooldownLeft > 0 || gameOver || gameWon) return
        cooldownLeft = fireIntervalMs
        shotsFired++
        SoundSynthesisManager.playLaser()

        val rad = Math.toRadians(aimAngle.toDouble())
        val barrelLen = 60f
        val launchX = 100f + barrelLen * cos(rad).toFloat()
        val launchY = (wSize.y * 0.5f) + barrelLen * sin(rad).toFloat()

        bullets.add(
            Bullet(
                x = launchX,
                y = launchY,
                vx = cos(rad).toFloat() * baseSpeed,
                vy = sin(rad).toFloat() * baseSpeed,
                color = bulletColor,
                r = if (modWeapon != null) (8f + modWeapon.size) else 12f,
                bouncesLeft = baseBounces,
                damage = baseDamage
            )
        )
        combatMessage = "Plasma bolt launched. Angle: ${aimAngle.toInt()}°"
        viewModel.announce("Fired $weaponName at ${aimAngle.toInt()} degrees.")
    }

    // Running continuous dynamic simulation coroutine
    LaunchedEffect(initialized, gameOver, gameWon, perfMode) {
        if (!initialized) return@LaunchedEffect
        while (!gameOver && !gameWon) {
            val stepTime = if (perfMode == "LOW_END_OPTIMIZED") 32L else 16L
            delay(stepTime)

            if (cooldownLeft > 0) {
                cooldownLeft -= stepTime
            }

            // Screen shake dampening
            if (screenShakeAmount > 0) {
                screenShakeAmount -= 1.5f
            }

            // 1. Update Player bullets
            val bIterator = bullets.iterator()
            while (bIterator.hasNext()) {
                val b = bIterator.next()
                b.x += b.vx
                b.y += b.vy

                // Append trail offsets
                if (perfMode != "LOW_END_OPTIMIZED") {
                    b.trail.add(Offset(b.x, b.y))
                    if (b.trail.size > 8) b.trail.removeAt(0)
                }

                // Bounce top/bottom boundaries
                if (b.y - b.r < 0f) {
                    b.y = b.r
                    b.vy = -b.vy
                    if (b.bouncesLeft > 0) b.bouncesLeft-- else {
                        bIterator.remove()
                        continue
                    }
                    SoundSynthesisManager.playHit()
                } else if (b.y + b.r > wSize.y) {
                    b.y = wSize.y - b.r
                    b.vy = -b.vy
                    if (b.bouncesLeft > 0) b.bouncesLeft-- else {
                        bIterator.remove()
                        continue
                    }
                    SoundSynthesisManager.playHit()
                }

                // Hit destructible barriers
                var hitBarrier = false
                val barIter = barriers.iterator()
                while (barIter.hasNext()) {
                    val bar = barIter.next()
                    if (b.x + b.r > bar.x && b.x - b.r < bar.x + bar.w &&
                        b.y + b.r > bar.y && b.y - b.r < bar.y + bar.h
                    ) {
                        // Impact! Damage barrier
                        bar.hp--
                        hitBarrier = true
                        screenShakeAmount = 5f
                        SoundSynthesisManager.playHit()

                        // Explode particle splashes
                        val numParts = if (perfMode == "LOW_END_OPTIMIZED") 2 else 6
                        for (i in 0 until numParts) {
                            particles.add(
                                Particle(
                                    x = b.x, y = b.y,
                                    vx = (Random.nextFloat() - 0.5f) * 6f,
                                    vy = (Random.nextFloat() - 0.5f) * 6f,
                                    life = 1.0f,
                                    color = Color(0xFFFFD54F),
                                    r = 4f
                                )
                            )
                        }

                        if (bar.hp <= 0) {
                            barIter.remove()
                            score += 50
                            SoundSynthesisManager.playExplosion()
                            combatMessage = "Fortress barrier segment demolished! Reward: +50 pts."
                            viewModel.announce("Barrier segment destroyed.")
                        }
                        break
                    }
                }

                if (hitBarrier) {
                    if (b.bouncesLeft > 0) {
                        b.vx = -b.vx
                        b.bouncesLeft--
                    } else {
                        bIterator.remove()
                        continue
                    }
                }

                // Hit Boss Mech (Boss resides at width - 150f, center horizontal, body height wSize.y * 0.7f)
                val bossX = wSize.x - 140f
                if (b.x + b.r > bossX && b.x - b.r < wSize.x - 30f &&
                    b.y > wSize.y * 0.15f && b.y < wSize.y * 0.85f
                ) {
                    hits++
                    screenShakeAmount = 8f
                    bullets.remove(b)

                    if (bossShieldHp > 0) {
                        bossShieldHp = maxOf(0f, bossShieldHp - b.damage)
                        SoundSynthesisManager.playHit()
                        combatMessage = "Boss Shields absorb impact! Shields: ${bossShieldHp.toInt()} HP"
                    } else {
                        bossHp = maxOf(0f, bossHp - b.damage)
                        SoundSynthesisManager.playExplosion()
                        score += 150
                        combatMessage = "DIRECT HIT ON REACTOR CORE! Carbon Integrity: ${((bossHp / maxBossHp) * 100).toInt()}%"
                        if (bossHp <= 0) {
                            gameWon = true
                            combatMessage = "CRITICAL METALLIC OVERLOAD! Core neutralized. Victory!"
                            viewModel.announce("Victory! Boss defeated. Score: $score")
                            SoundSynthesisManager.playChime()
                            // Save stats in Room!
                            viewModel.addCredits((score * 0.2f).toInt() + 100)
                            viewModel.submitLocalScore(
                                score = score,
                                accuracy = if (shotsFired > 0) (hits.toFloat() / shotsFired * 100).coerceIn(0f..100f) else 100f,
                                stageCleared = viewModel.activeMissionType.value
                            )
                        }
                    }
                    break
                }

                // Clean loose runaways
                if (b.x > wSize.x || b.x < 0f) {
                    bIterator.remove()
                }
            }

            // 2. Boss movements and offensive firing loops
            val bossFreq = 0.03f
            val elapsed = System.currentTimeMillis()
            val bossTargetY = (wSize.y * 0.5f) + sin(elapsed * bossFreq) * (wSize.y * 0.25f)

            // Random Boss counter-measures (fires homing missiles!)
            if (Random.nextFloat() < 0.025f && enemyMissiles.size < 3) {
                enemyMissiles.add(
                    EnemyMissile(
                        x = wSize.x - 150f,
                        y = bossTargetY.toFloat(),
                        vx = -5f - Random.nextFloat() * 3f,
                        vy = (Random.nextFloat() - 0.5f) * 4f
                    )
                )
                SoundSynthesisManager.playLaser()
                combatMessage = "Warning! Boss is deploying thermonuclear flares."
            }

            // 3. Update Enemy missiles
            val mIterator = enemyMissiles.iterator()
            while (mIterator.hasNext()) {
                val m = mIterator.next()
                m.x += m.vx
                m.y += m.vy

                // Slightly track player's general Y center to be challenging!
                val playerCenterY = wSize.y * 0.5f
                val dy = playerCenterY - m.y
                m.vy += if (dy > 0) 0.1f else -0.1f
                m.vy = m.vy.coerceIn(-3f..3f)

                // Hitting player base (at left 100f boundary)
                if (m.x < 120f) {
                    mIterator.remove()
                    screenShakeAmount = 15f
                    score = maxOf(0, score - 200)
                    SoundSynthesisManager.playExplosion()
                    combatMessage = "ALERT! Core Hull impacted by flares! Remaining score degraded."
                    viewModel.announce("Colony hull impacted. Integrity compromise.")
                    continue
                }

                // Check collision with player bullets to blow up missiles!
                val bHit = bullets.find {
                    Math.hypot((it.x - m.x).toDouble(), (it.y - m.y).toDouble()) < (it.r + m.r)
                }
                if (bHit != null) {
                    bullets.remove(bHit)
                    mIterator.remove()
                    score += 100
                    SoundSynthesisManager.playExplosion()
                    combatMessage = "Counter-fire successful. Homing flare intercepted!"
                    viewModel.announce("Interception confirmed.")
                    // Debris
                    val partsCount = if (perfMode == "LOW_END_OPTIMIZED") 3 else 8
                    for (i in 0 until partsCount) {
                        particles.add(
                            Particle(
                                x = m.x, y = m.y,
                                vx = (Random.nextFloat() - 0.5f) * 10f,
                                vy = (Random.nextFloat() - 0.5f) * 10f,
                                life = 1f,
                                color = Color(0xFFFF5252),
                                r = 5f
                            )
                        )
                    }
                    continue
                }
            }

            // 4. Update Particle effects
            val pIterator = particles.iterator()
            while (pIterator.hasNext()) {
                val p = pIterator.next()
                p.x += p.vx
                p.y += p.vy
                p.life -= 0.05f
                if (p.life <= 0f) {
                    pIterator.remove()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isHighContrast) Modifier.background(Color.Black)
                else Modifier.background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
                        center = Offset(wSize.x * 0.5f, wSize.y * 0.5f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Title & Accents
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        if (isHighContrast) Color.Black else Color(0x33000000),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isHighContrast) Color.White else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.navigateTo(ScreenState.CAMPAIGN_BRANCHES)
                    },
                    modifier = Modifier.testTag("exit_combat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Abort Mission and Exit",
                        tint = if (isHighContrast) Color.White else Color.Red
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (viewModel.isSandboxMode.value) "MOD WEAPON SANDBOX" else "ASTRA DEEP-SPACE MISSION",
                        color = if (isHighContrast) Color.White else Color(0xFF81D4FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Tactical Target: Sentinel Carrier",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SCORE: $score",
                        color = if (isHighContrast) Color.White else Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Boss Status Health Bars
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Boss Core HP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BOSS HP ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.width(70.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = bossHp / maxBossHp)
                                .background(if (isHighContrast) Color.White else Color(0xFFFF1744))
                        )
                    }
                    Text(
                        text = " ${((bossHp / maxBossHp) * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Shield Health
                if (bossShieldHp > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SHIELD HP ",
                            color = Color(0xFF64B5F6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(70.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0x33FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = bossShieldHp / maxBossShieldHp)
                                    .background(Color(0xFF2979FF))
                            )
                        }
                        Text(
                            text = " ${((bossShieldHp / maxBossShieldHp) * 100).toInt()}%",
                            color = Color(0xFF64B5F6),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Live Speech/Narrator Assist captions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color(0xD9102027), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .border(
                        1.dp,
                        if (isHighContrast) Color.White else Color(0xFF00E676),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = "📢 [Tactical Narrator]: $combatMessage",
                    fontSize = 13.sp,
                    color = if (isHighContrast) Color.White else Color(0xFF00E676),
                    fontWeight = FontWeight.Bold
                )
            }

            // Real-Time Canvas Combat Renderer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .border(
                        width = 2.dp,
                        color = if (isHighContrast) Color.White else Color(0x33FFFFFF)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isAiming = true
                                val dy = offset.y - (size.height * 0.5f)
                                val dx = offset.x - 100f
                                aimAngle = Math
                                    .toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                                    .toFloat()
                            },
                            onDrag = { change, _ ->
                                val dy = change.position.y - (size.height * 0.5f)
                                val dx = change.position.x - 100f
                                aimAngle = Math
                                    .toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                                    .toFloat()
                                    .coerceIn(-65f..65f)
                            },
                            onDragEnd = {
                                isAiming = false
                            }
                        )
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(elevation = screenShakeAmount.dp)
                ) {
                    if (!initialized) {
                        wSize = Offset(size.width, size.height)
                        setupArena(size.width, size.height)
                    }

                    val cy = size.height * 0.5f

                    // Draw Background grid lines if High Detail
                    if (perfMode != "LOW_END_OPTIMIZED" && !isHighContrast) {
                        val gridSpace = 80f
                        for (gx in 0..(size.width / gridSpace).toInt()) {
                            drawLine(
                                color = Color(0x0CFFFFFF),
                                start = Offset(gx * gridSpace, 0f),
                                end = Offset(gx * gridSpace, size.height),
                                strokeWidth = 1f
                            )
                        }
                        for (gy in 0..(size.height / gridSpace).toInt()) {
                            drawLine(
                                color = Color(0x0CFFFFFF),
                                start = Offset(0f, gy * gridSpace),
                                end = Offset(size.width, gy * gridSpace),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // 1. Draw Player Cannon Reactor Base on left
                    drawCircle(
                        color = if (isHighContrast) Color.White else Color(0xFF263238),
                        radius = 50f,
                        center = Offset(50f, cy)
                    )
                    drawCircle(
                        color = if (isHighContrast) Color.Black else Color(0xFFFF1744),
                        radius = 25f,
                        center = Offset(50f, cy),
                        style = Stroke(width = 6f)
                    )

                    // Draw aim vector barrel
                    val rad = Math.toRadians(aimAngle.toDouble())
                    val barrelLength = 60f
                    val bEndX = 100f + barrelLength * cos(rad).toFloat()
                    val bEndY = cy + barrelLength * sin(rad).toFloat()
                    drawLine(
                        color = if (isHighContrast) Color.White else Color(0xFFCFD8DC),
                        start = Offset(50f, cy),
                        end = Offset(bEndX, bEndY),
                        strokeWidth = 24f
                    )

                    // Draw realistic aiming trajectory path line if aiming
                    if (isAiming || true) {
                        var tempX = bEndX
                        var tempY = bEndY
                        var tempVx = cos(rad).toFloat() * baseSpeed
                        var tempVy = sin(rad).toFloat() * baseSpeed
                        val pathPoints = mutableListOf<Offset>()
                        pathPoints.add(Offset(tempX, tempY))

                        // Trace 15 simulation frames for clean resolution details!
                        for (i in 0..15) {
                            tempX += tempVx
                            tempY += tempVy

                            // Bounce simulations
                            if (tempY < 0f) {
                                tempY = 10f
                                tempVy = -tempVy
                            } else if (tempY > size.height) {
                                tempY = size.height - 10f
                                tempVy = -tempVy
                            }
                            pathPoints.add(Offset(tempX, tempY))
                        }

                        // Draw dotted path line
                        for (p in 0 until pathPoints.size - 1) {
                            drawLine(
                                color = if (isHighContrast) Color.White else Color(0xFF00E676),
                                start = pathPoints[p],
                                end = pathPoints[p + 1],
                                strokeWidth = 4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                        }
                    }

                    // 2. Draw Destructible Obstacles / Barriers with structural health colors!
                    for (b in barriers) {
                        val hpPercent = b.hp.toFloat() / b.maxHp.toFloat()
                        val color = if (isHighContrast) {
                            Color.White
                        } else {
                            when {
                                hpPercent > 0.6f -> Color(0xFF00ACC1)
                                hpPercent > 0.3f -> Color(0xFFFFB300)
                                else -> Color(0xFFE53935)
                            }
                        }
                        
                        // Draw outer rectangle
                        drawRect(
                            color = color,
                            topLeft = Offset(b.x, b.y),
                            size = androidx.compose.ui.geometry.Size(b.w, b.h),
                            style = if (isHighContrast) Stroke(width = 2f) else Stroke(width = 3f)
                        )
                        // Fill semi-transparent core
                        if (!isHighContrast) {
                            drawRect(
                                color = color.copy(alpha = 0.25f),
                                topLeft = Offset(b.x, b.y),
                                size = androidx.compose.ui.geometry.Size(b.w, b.h)
                            )
                        }
                    }

                    // 3. Draw Player Bullets
                    for (blt in bullets) {
                        // Trail
                        if (perfMode != "LOW_END_OPTIMIZED") {
                            for (tIdx in 0 until blt.trail.size) {
                                val tAlpha = (tIdx.toFloat() / blt.trail.size) * 0.4f
                                drawCircle(
                                    color = blt.color.copy(alpha = tAlpha),
                                    radius = blt.r * (tIdx.toFloat() / blt.trail.size),
                                    center = blt.trail[tIdx]
                                )
                            }
                        }
                        // Core Bullet
                        drawCircle(
                            color = if (isHighContrast) Color.White else blt.color,
                            radius = blt.r,
                            center = Offset(blt.x, blt.y)
                        )
                    }

                    // 4. Draw Boss Giant Goliath Mech
                    val elapsed = System.currentTimeMillis()
                    val bossFreq = 0.003f
                    val bossCenterY = cy + sin(elapsed * bossFreq) * (size.height * 0.25f)
                    val bossX = size.width - 140f
                    val bossHeight = size.height * 0.45f

                    // Shield visual fence
                    if (bossShieldHp > 0) {
                        drawArc(
                            color = if (isHighContrast) Color.White else Color(0xFF2979FF).copy(alpha = 0.5f),
                            startAngle = 90f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(bossX - 45f, bossCenterY.toFloat() - (bossHeight * 0.5f) - 20f),
                            size = androidx.compose.ui.geometry.Size(120f, bossHeight + 40f),
                            style = Stroke(width = if (isHighContrast) 2f else 8f)
                        )
                    }

                    // Boss Main core box
                    drawRect(
                        color = if (isHighContrast) Color.White else Color(0xFF37474F),
                        topLeft = Offset(bossX, bossCenterY.toFloat() - (bossHeight * 0.5f)),
                        size = androidx.compose.ui.geometry.Size(110f, bossHeight),
                        style = Stroke(width = 4f)
                    )
                    drawRect(
                        color = if (isHighContrast) Color.Black else Color(0xFF212121),
                        topLeft = Offset(bossX, bossCenterY.toFloat() - (bossHeight * 0.5f)),
                        size = androidx.compose.ui.geometry.Size(110f, bossHeight)
                    )

                    // Overheating core lights
                    val coreColor = if (bossShieldHp <= 0) Color(0xFFFF1744) else Color(0xFF00E676)
                    drawCircle(
                        color = coreColor,
                        radius = 20f,
                        center = Offset(bossX + 55f, bossCenterY.toFloat())
                    )
                    drawCircle(
                        color = coreColor.copy(alpha = 0.3f),
                        radius = 35f,
                        center = Offset(bossX + 55f, bossCenterY.toFloat())
                    )

                    // Target indicator
                    if (isHighContrast) {
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(bossX - 10f, bossCenterY.toFloat() - 30f),
                            size = androidx.compose.ui.geometry.Size(20f, 60f),
                            style = Stroke(width = 2f)
                        )
                    }

                    // 5. Draw Enemy Flares/Missiles
                    for (msl in enemyMissiles) {
                        drawCircle(
                            color = if (isHighContrast) Color.White else Color(0xFFFF5252),
                            radius = msl.r,
                            center = Offset(msl.x, msl.y)
                        )
                        // Fire flare tail
                        drawLine(
                            color = Color(0xFFFFAB40),
                            start = Offset(msl.x, msl.y),
                            end = Offset(msl.x + 20f, msl.y),
                            strokeWidth = 4f
                        )
                    }

                    // 6. Draw Spark particles
                    for (p in particles) {
                        drawCircle(
                            color = p.color.copy(alpha = p.life),
                            radius = p.r * p.life,
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }

            // Controls Footer area (Responsive for Portrait/Landscape)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xE6050C10), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Aim Slider or Left/Right remapping indicators
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VIRTUAL ELEVATION CONTROLS [DRAG SCENE TO AIM]",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                aimAngle = (aimAngle - 5f).coerceIn(-65f..65f)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("aim_up_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aim Up")
                        }
                        
                        Text(
                            text = "ANGLE: ${aimAngle.toInt()}°",
                            color = if (isHighContrast) Color.White else Color(0xFF80DEEA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.width(90.dp)
                        )

                        Button(
                            onClick = {
                                aimAngle = (aimAngle + 5f).coerceIn(-65f..65f)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("aim_down_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aim Down")
                        }
                    }
                }

                // Tactile FIRE trigger
                Button(
                    onClick = { fireBullet() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else Color(0xFFFF1744),
                        contentColor = if (isHighContrast) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .height(55.dp)
                        .padding(start = 12.dp)
                        .testTag("fire_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Shoot Weapon")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FIRE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Overlay modals for Win / Loss game results
        if (gameOver || gameWon) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF0000000))
                    .clickable(enabled = false) {}, // consume clicks
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .background(Color(0xFF102027), shape = RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = if (gameWon) Color(0xFF00E676) else Color(0xFFFF1744),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (gameWon) Icons.Default.CheckCircle else Icons.Default.Warning,
                        tint = if (gameWon) Color(0xFF00E676) else Color(0xFFFF1744),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (gameWon) "MISSION ACCOMPLISHED" else "COSMIC CORE CRITICAL",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (gameWon) "You have shattered the Sentinel Carrier. Power core recovered."
                        else "Colony systems depleted. Recharge reactor grid and try again.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33000000), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACCURACY", color = Color.Gray, fontSize = 10.sp)
                            val acc = if (shotsFired > 0) (hits.toFloat() / shotsFired * 100).toInt() else 100
                            Text("$acc%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        VerticalDivider(color = Color.Gray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CREDITS", color = Color.Gray, fontSize = 10.sp)
                            val bonus = if (gameWon) (score * 0.2f).toInt() + 100 else 10
                            Text("+$bonus U", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { setupArena(wSize.x, wSize.y) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retry_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238))
                        ) {
                            Text("RETRY")
                        }
                        Button(
                            onClick = {
                                viewModel.navigateTo(ScreenState.CAMPAIGN_BRANCHES)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("continue_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = if (gameWon) Color(0xFF00E676) else Color.Red)
                        ) {
                            Text("PROCEED")
                        }
                    }
                }
            }
        }
    }
}
