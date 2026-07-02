package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.Weather
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun RaceScreen(viewModel: GameViewModel) {
    val activeBike by viewModel.activeBike.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val weather by viewModel.selectedWeather.collectAsState()
    val speed by viewModel.playerSpeed.collectAsState()
    val rpm by viewModel.playerRpm.collectAsState()
    val gear by viewModel.playerGear.collectAsState()
    val playerX by viewModel.playerX.collectAsState()
    val distanceTravelled by viewModel.distanceTravelled.collectAsState()
    val health by viewModel.playerHealth.collectAsState()
    val combatMessage by viewModel.combatMessage.collectAsState()
    val batterySaver by viewModel.batterySaverMode.collectAsState()

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val textMeasurer = rememberTextMeasurer()

    // Handle background tick for animations
    var animFrame by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            animFrame = (animFrame + 1) % 100
            delay(if (batterySaver) 50 else 25) // throttle slightly in battery saver
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Stats Bar (Speedometer & Gear in Marathi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .glassCard(
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        borderColor = Color.White.copy(alpha = 0.2f),
                        backgroundColor = GlassWhiteBg
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button / Resign
                    IconButton(onClick = { viewModel.setScreen(GameScreen.MENU) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = Color.Red)
                    }

                    // Live Speed HUD (Marathi translated style)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${speed.toInt()} किमी/तास",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = NitroBlue
                        )
                        Text("वेग (Speed)", fontSize = 9.sp, color = Color.Gray)
                    }

                    // Gear Indicator
                    Box(
                        modifier = Modifier.glassCard(
                            shape = RoundedCornerShape(8.dp),
                            borderWidth = 1.dp,
                            borderColor = NeonOrange,
                            backgroundColor = Color(0x26FFFFFF)
                        )
                    ) {
                        Text(
                            text = "गिअर $gear",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = NeonOrange,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Distance indicator
                    selectedTrack?.let { track ->
                        val progressPercent = (distanceTravelled / track.distance * 100).coerceIn(0f, 100f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.1f", distanceTravelled / 1000f)} / ${(track.distance / 1000f)} किमी",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToxicGreen
                            )
                            LinearProgressIndicator(
                                progress = { progressPercent / 100f },
                                color = ToxicGreen,
                                trackColor = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            // DYNAMIC GAMEPLAY DRAWING CANVAS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("race_canvas")
                ) {
                    val w = size.width
                    val h = size.height
                    val horizon = h * 0.4f

                    // 1. Draw Sky & Horizon Environment
                    drawEnvironment(w, h, horizon, weather, animFrame)

                    // 2. Draw Pseudo-3D Perspective Road
                    drawRoad(w, h, horizon, animFrame, speed, playerX)

                    // 3. Draw Side Scenery / Pune boards & landmarks
                    drawScenery(w, h, horizon, animFrame, speed, textMeasurer)

                    // 4. Draw Traffic Cars/Buses/Rickshaws
                    drawTraffic(w, h, horizon, viewModel.traffic, distanceTravelled, playerX, textMeasurer)

                    // 5. Draw Rival Racers
                    drawRivals(w, h, horizon, viewModel.rivals, distanceTravelled, playerX, textMeasurer)

                    // 6. Draw Player's Handlebar Dashboard at bottom (with custom neon glow!)
                    drawPlayerHandlebar(w, h, rpm, activeBike?.paintColor ?: 0, activeBike?.neonColor ?: 0, animFrame)

                    // 7. Draw Weather Overlay effect (Rain, Fog, Sunset glow)
                    drawWeatherEffects(w, h, horizon, weather, animFrame)
                }

                // Health & Damage indicators overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Health bar
                    Box(
                        modifier = Modifier.glassCard(
                            shape = RoundedCornerShape(8.dp),
                            borderColor = Color.White.copy(alpha = 0.2f),
                            backgroundColor = GlassWhiteBg
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Health", tint = PuneCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.width(110.dp)) {
                                    Text("आरोग्य (HP)", fontSize = 8.sp, color = Color.Gray)
                                    Text("${health.toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (health < 40) Color.Red else ToxicGreen)
                                }
                                LinearProgressIndicator(
                                    progress = { health / 100f },
                                    color = if (health < 40) Color.Red else ToxicGreen,
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }

                    // Combat commentary / Alerts
                    if (combatMessage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .glassCard(
                                    shape = RoundedCornerShape(12.dp),
                                    borderColor = PuneCrimson,
                                    backgroundColor = Color(0x66B71C1C) // crimson frosted glass
                                )
                        ) {
                            Text(
                                text = combatMessage,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // GAMEPLAY CONTROLS CONSOLE (Bottom HUD)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .glassCard(
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        borderColor = Color.White.copy(alpha = 0.2f),
                        backgroundColor = GlassWhiteBg
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT CONTROLS: STEERING SLIDERS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("दिशा बदला (Steering)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.5.dp, NitroBlue, CircleShape)
                                    .clickable {
                                        viewModel.steerLeft()
                                        triggerShortVibration(vibrator)
                                    }
                                    .testTag("steer_left_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowLeft, contentDescription = "Left", tint = NitroBlue, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.5.dp, NitroBlue, CircleShape)
                                    .clickable {
                                        viewModel.steerRight()
                                        triggerShortVibration(vibrator)
                                    }
                                    .testTag("steer_right_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowRight, contentDescription = "Right", tint = NitroBlue, modifier = Modifier.size(40.dp))
                            }
                        }
                    }

                    // CENTER CONTROLS: ROAD RASH COMBAT FIGHT ACTIONS!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("राडा / फाईट (Combat)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // PUNCH BUTTON
                            Button(
                                onClick = {
                                    viewModel.performCombatAction("punch")
                                    triggerCombatVibration(vibrator)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PuneCrimson, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("punch_button")
                            ) {
                                Text("टोला (Punch)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // KICK BUTTON
                            Button(
                                onClick = {
                                    viewModel.performCombatAction("kick")
                                    triggerCombatVibration(vibrator)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("kick_button")
                            ) {
                                Text("लाथ (Kick)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Weapon status info (stick)
                        Text("हत्यार: लाकडी दंडा (Stick) 🪵", fontSize = 10.sp, color = LuxuryGold, fontWeight = FontWeight.Bold)
                    }

                    // RIGHT CONTROLS: GEAR SHIFTERS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("गिअर बदला (Gears)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            // Downshift
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.shiftGearDown()
                                        triggerShortVibration(vibrator)
                                    }
                                    .testTag("gear_down_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("- G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            // Upshift
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.5.dp, ToxicGreen, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.shiftGearUp()
                                        triggerShortVibration(vibrator)
                                    }
                                    .testTag("gear_up_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+ G", color = ToxicGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ENVIRONMENT RENDERING FUNCTION
private fun DrawScope.drawEnvironment(w: Float, h: Float, horizon: Float, weather: Weather, animFrame: Int) {
    // Ground color (base road shoulder)
    val groundColor = when (weather) {
        Weather.NIGHT -> Color(0xFF070B10)
        Weather.RAINY -> Color(0xFF14191C)
        Weather.FOGGY -> Color(0xFF1F2428)
        else -> Color(0xFF1D3212) // green grass/ground
    }
    drawRect(color = groundColor, topLeft = Offset(0f, horizon), size = Size(w, h - horizon))

    // Sky gradient
    val skyBrush = when (weather) {
        Weather.NIGHT -> Brush.verticalGradient(colors = listOf(Color(0xFF020205), Color(0xFF0C0E17)))
        Weather.RAINY -> Brush.verticalGradient(colors = listOf(Color(0xFF2C3238), Color(0xFF424A52)))
        Weather.FOGGY -> Brush.verticalGradient(colors = listOf(Color(0xFF70777D), Color(0xFF8A939A)))
        else -> Brush.verticalGradient(colors = listOf(Color(0xFFFF7B36), Color(0xFFFFB366))) // stunning sunset
    }
    drawRect(brush = skyBrush, topLeft = Offset(0f, 0f), size = Size(w, horizon))

    // Draw Pune city outline / mountain silhouette on sunset
    if (weather == Weather.SUNNY) {
        val silhouetteColor = Color(0xFF4F2722)
        val path = Path().apply {
            moveTo(0f, horizon)
            quadraticTo(w * 0.15f, horizon - 50f, w * 0.3f, horizon)
            lineTo(w * 0.45f, horizon)
            quadraticTo(w * 0.6f, horizon - 80f, w * 0.75f, horizon - 10f)
            quadraticTo(w * 0.85f, horizon - 30f, w * 0.95f, horizon)
            lineTo(w, horizon)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = path, color = silhouetteColor)
    }
}

// ROAD PERSPECTIVE RENDERING
private fun DrawScope.drawRoad(w: Float, h: Float, horizon: Float, animFrame: Int, speed: Float, playerX: Float) {
    val roadWidthBottom = w * 0.82f
    val roadWidthTop = w * 0.05f
    val centerX = w * 0.5f - (playerX * w * 0.2f)

    // Main asphalt road polygon
    val roadPath = Path().apply {
        moveTo(centerX - roadWidthTop / 2f, horizon)
        lineTo(centerX + roadWidthTop / 2f, horizon)
        lineTo(w * 0.5f - (playerX * w * 0.2f) + roadWidthBottom / 2f, h)
        lineTo(w * 0.5f - (playerX * w * 0.2f) - roadWidthBottom / 2f, h)
        close()
    }
    drawPath(roadPath, color = Color(0xFF24262E))

    // Side safety stripes (red/white curbs) scrolling to simulate motion
    val stripesCount = 8
    val scrollPhase = (animFrame * (speed / 40f)) % 100f

    for (i in 0 until stripesCount) {
        // Perspective ratio (non-linear spacing)
        val pyStart = (i.toFloat() / stripesCount)
        val pyEnd = ((i + 0.5f) / stripesCount)

        // Exponential mapping for depth feeling
        val yStart = horizon + (h - horizon) * (pyStart * pyStart)
        val yEnd = horizon + (h - horizon) * (pyEnd * pyEnd)

        val wStart = roadWidthTop + (roadWidthBottom - roadWidthTop) * (pyStart * pyStart)
        val wEnd = roadWidthTop + (roadWidthBottom - roadWidthTop) * (pyEnd * pyEnd)

        // Center shift for curving effect
        val xStart = w * 0.5f - (playerX * w * 0.2f) + sin((pyStart * 3f) + (animFrame * 0.05f)) * 30f
        val xEnd = w * 0.5f - (playerX * w * 0.2f) + sin((pyEnd * 3f) + (animFrame * 0.05f)) * 30f

        val curbColor = if ((i + animFrame / 10) % 2 == 0) PuneCrimson else Color.White

        // Left curb
        drawLine(
            color = curbColor,
            start = Offset(xStart - wStart / 2f, yStart),
            end = Offset(xEnd - wEnd / 2f, yEnd),
            strokeWidth = 4f + pyStart * 12f
        )
        // Right curb
        drawLine(
            color = curbColor,
            start = Offset(xStart + wStart / 2f, yStart),
            end = Offset(xEnd + wEnd / 2f, yEnd),
            strokeWidth = 4f + pyStart * 12f
        )

        // Center dotted divider line
        if (i % 2 == 0) {
            drawLine(
                color = Color.Yellow,
                start = Offset(xStart, yStart),
                end = Offset(xEnd, yEnd),
                strokeWidth = 1f + pyStart * 6f
            )
        }
    }
}

// DRAW SCENERY BOARDS AND LANDMARKS
private fun DrawScope.drawScenery(w: Float, h: Float, horizon: Float, animFrame: Int, speed: Float, textMeasurer: TextMeasurer) {
    // Drawn on left and right borders of the screen
    val scale = 0.5f
    val lx = w * 0.08f
    val rx = w * 0.92f

    // Landmark text layout: "सिंहगड", "स्वारगेट"
    val puneriPatya = listOf("पुणेकर शांत डोक्याने चालवा!", "PCMC किंग", "एकच नंबर भावा!", "शपथ पाळा वेग टाळा")
    val textIndex = (animFrame / 30) % puneriPatya.size

    val patyaStyle = TextStyle(
        color = Color.Black,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        background = LuxuryGold
    )

    // Left Bill Board
    drawRect(color = Color.Black, topLeft = Offset(lx - 20f, horizon + 40f), size = Size(50f, 60f))
    drawRect(color = LuxuryGold, topLeft = Offset(lx - 18f, horizon + 42f), size = Size(46f, 36f))
    drawLine(color = Color.Gray, start = Offset(lx + 5f, horizon + 78f), end = Offset(lx + 5f, horizon + 120f), strokeWidth = 4f)

    drawText(
        textMeasurer = textMeasurer,
        text = "पुणेरी\nपाटी",
        style = patyaStyle,
        topLeft = Offset(lx - 12f, horizon + 45f)
    )

    // Right Milestone board with KM remaining
    drawRect(color = Color.White, topLeft = Offset(rx - 10f, h * 0.7f), size = Size(32f, 40f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
    drawRect(color = Color.Yellow, topLeft = Offset(rx - 8f, h * 0.7f + 2f), size = Size(28f, 15f))
    drawText(
        textMeasurer = textMeasurer,
        text = "PUNE",
        style = TextStyle(color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold),
        topLeft = Offset(rx - 6f, h * 0.7f + 2f)
    )
}

// DRAW ROAD TRAFFIC (Indian Cars, Trucks, Rickshaws scaling from horizon)
private fun DrawScope.drawTraffic(
    w: Float, h: Float, horizon: Float,
    trafficList: List<com.example.ui.VehicleState>,
    playerDist: Float, playerX: Float, textMeasurer: TextMeasurer
) {
    for (v in trafficList) {
        val relativeDist = v.distance - playerDist
        // Render only if within visible depth range (0 to 350 meters ahead)
        if (relativeDist in 0f..350f) {
            // Perspective math for scale and position
            val scaleRatio = 1f - (relativeDist / 350f) // 1.0 close-up, 0.0 horizon
            val yPos = horizon + (h - horizon) * (scaleRatio * scaleRatio)

            val roadWidthAtY = w * 0.05f + (w * 0.82f - w * 0.05f) * (scaleRatio * scaleRatio)
            val centerAtY = w * 0.5f - (playerX * w * 0.2f)

            // Lane coordinate
            val laneWidth = roadWidthAtY / 3f
            val xPos = centerAtY + (v.lane * laneWidth)

            // Vehicle visual sizing
            val vWidth = 32f + scaleRatio * 130f
            val vHeight = 24f + scaleRatio * 90f

            // Drawing custom Indian Vehicles using layered canvas rects
            when (v.model) {
                "PMPML Bus" -> {
                    // Famous Red/White Pune PMPML bus
                    drawRect(
                        color = Color(0xFFD32F2F), // PMPML red
                        topLeft = Offset(xPos - vWidth / 2f, yPos - vHeight),
                        size = Size(vWidth, vHeight)
                    )
                    // Bus Windows
                    drawRect(
                        color = Color(0xE8B3E5FC),
                        topLeft = Offset(xPos - vWidth / 2.2f, yPos - vHeight * 0.82f),
                        size = Size(vWidth * 0.9f, vHeight * 0.35f)
                    )
                    // Headlights
                    drawCircle(color = Color.Yellow, radius = 2f + scaleRatio * 8f, center = Offset(xPos - vWidth * 0.35f, yPos - vHeight * 0.15f))
                    drawCircle(color = Color.Yellow, radius = 2f + scaleRatio * 8f, center = Offset(xPos + vWidth * 0.35f, yPos - vHeight * 0.15f))

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "लाल डबा",
                        style = TextStyle(color = Color.White, fontSize = (5f + scaleRatio * 11f).sp, fontWeight = FontWeight.Bold),
                        topLeft = Offset(xPos - vWidth * 0.4f, yPos - vHeight * 0.7f)
                    )
                }
                "Thar" -> {
                    // Jeep/Thar
                    drawRect(
                        color = Color(0xFF1A1A1A), // black jeep
                        topLeft = Offset(xPos - vWidth / 2f, yPos - vHeight),
                        size = Size(vWidth, vHeight)
                    )
                    // Grill + windshield
                    drawRect(color = Color.Gray, topLeft = Offset(xPos - vWidth / 2.3f, yPos - vHeight * 0.9f), size = Size(vWidth * 0.85f, vHeight * 0.4f))
                    // Wheels
                    drawRect(color = Color.Black, topLeft = Offset(xPos - vWidth * 0.45f, yPos - 10f), size = Size(vWidth * 0.2f, 15f))
                    drawRect(color = Color.Black, topLeft = Offset(xPos + vWidth * 0.25f, yPos - 10f), size = Size(vWidth * 0.2f, 15f))
                }
                "Auto" -> {
                    // Pune Auto Rickshaw (Yellow top, black body)
                    // Bottom body
                    drawRect(color = Color.Black, topLeft = Offset(xPos - vWidth / 2f, yPos - vHeight * 0.5f), size = Size(vWidth, vHeight * 0.5f))
                    // Top yellow cabin
                    drawRect(color = Color(0xFFFFD600), topLeft = Offset(xPos - vWidth / 2f, yPos - vHeight), size = Size(vWidth, vHeight * 0.5f))
                    // Cabin windshield cut
                    drawRect(color = Color.LightGray, topLeft = Offset(xPos - vWidth / 2.4f, yPos - vHeight * 0.85f), size = Size(vWidth * 0.8f, vHeight * 0.3f))
                    // Headlight
                    drawCircle(color = Color.Yellow, radius = 1f + scaleRatio * 6f, center = Offset(xPos, yPos - vHeight * 0.15f))
                }
                else -> {
                    // Standard Tata Truck or swift
                    drawRect(color = Color(0xFF1E88E5), topLeft = Offset(xPos - vWidth / 2f, yPos - vHeight), size = Size(vWidth, vHeight))
                    drawRect(color = LuxuryGold, topLeft = Offset(xPos - vWidth / 2.5f, yPos - vHeight * 0.95f), size = Size(vWidth * 0.8f, vHeight * 0.4f))
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "TATA",
                        style = TextStyle(color = Color.White, fontSize = (4f + scaleRatio * 10f).sp, fontWeight = FontWeight.Bold),
                        topLeft = Offset(xPos - vWidth * 0.2f, yPos - vHeight * 0.75f)
                    )
                }
            }
        }
    }
}

// DRAW COMBAT RIVAL RACERS
private fun DrawScope.drawRivals(
    w: Float, h: Float, horizon: Float,
    rivalList: List<com.example.ui.RivalState>,
    playerDist: Float, playerX: Float, textMeasurer: TextMeasurer
) {
    for (r in rivalList) {
        val relativeDist = r.distance - playerDist
        if (relativeDist in -30f..350f) {
            val scaleRatio = 1f - (relativeDist.coerceIn(0f, 350f) / 350f)
            val yPos = horizon + (h - horizon) * (scaleRatio * scaleRatio)

            val roadWidthAtY = w * 0.05f + (w * 0.82f - w * 0.05f) * (scaleRatio * scaleRatio)
            val centerAtY = w * 0.5f - (playerX * w * 0.2f)

            // Linear position on road
            val xPos = centerAtY + (r.laneX * roadWidthAtY / 2f)

            val rWidth = 16f + scaleRatio * 74f
            val rHeight = 28f + scaleRatio * 110f

            if (!r.isKnockedOut) {
                // Draw opponent rider (Custom colored bike)
                val bikeColor = when (r.bikeModel) {
                    "ktm" -> Color(0xFFFF5722)
                    "bullet" -> LuxuryGold
                    "pulsar" -> NitroBlue
                    else -> Color.Gray
                }

                // Bike body
                drawRect(color = bikeColor, topLeft = Offset(xPos - rWidth * 0.3f, yPos - rHeight * 0.5f), size = Size(rWidth * 0.6f, rHeight * 0.4f))
                // Rider body (Black jacket, helmet)
                drawCircle(color = Color.Black, radius = rWidth * 0.35f, center = Offset(xPos, yPos - rHeight * 0.7f))
                drawCircle(color = Color.Red, radius = rWidth * 0.22f, center = Offset(xPos, yPos - rHeight * 0.88f)) // Helmet

                // Name tag & HP above opponent
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${r.name}\nHP: ${r.health.toInt()}%",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (6f + scaleRatio * 8f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        background = Color.Black.copy(alpha = 0.6f)
                    ),
                    topLeft = Offset(xPos - rWidth * 0.8f, yPos - rHeight * 1.35f)
                )
            } else {
                // Drawn flat on road (Knocked out!)
                drawOval(
                    color = Color.Red.copy(alpha = 0.8f),
                    topLeft = Offset(xPos - rWidth, yPos - 10f),
                    size = Size(rWidth * 2f, 16f)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "कोलकटला!",
                    style = TextStyle(color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold, background = Color.Black),
                    topLeft = Offset(xPos - rWidth * 0.5f, yPos - 22f)
                )
            }
        }
    }
}

// DRAW ACTIVE BIKE COCKPIT COCKY METER & HANDLEBAR
private fun DrawScope.drawPlayerHandlebar(w: Float, h: Float, rpm: Float, paintIndex: Int, neonIndex: Int, animFrame: Int) {
    val handleY = h * 0.94f
    val handleWidth = w * 0.65f

    // Subtle vibration effect proportional to RPM
    val vibrationShift = (sin(animFrame * 1.5) * (rpm / 2000f)).toFloat()

    // Determine Paint Accent Color
    val paintColor = when (paintIndex) {
        1 -> PuneCrimson
        2 -> NitroBlue
        3 -> LuxuryGold
        4 -> Color.Black
        else -> NeonOrange
    }

    // Draw Custom Neon Underglow around bike center if neon installed
    if (neonIndex > 0) {
        val neonColor = when (neonIndex) {
            1 -> ToxicGreen
            2 -> NitroBlue
            3 -> PuneCrimson
            4 -> LuxuryGold
            else -> Color.Transparent
        }
        // Glowing aura under cockpit
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(neonColor.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.9f)
            ),
            topLeft = Offset(w * 0.3f, h * 0.85f),
            size = Size(w * 0.4f, h * 0.15f)
        )
    }

    // Handlebars Left and Right
    drawLine(
        color = Color(0xFF2A2E35),
        start = Offset(w * 0.5f - handleWidth / 2f + vibrationShift, handleY + vibrationShift),
        end = Offset(w * 0.5f + handleWidth / 2f + vibrationShift, handleY + vibrationShift),
        strokeWidth = 24f
    )

    // Handgrips
    drawRect(color = Color.Black, topLeft = Offset(w * 0.5f - handleWidth / 2f + vibrationShift, handleY - 16f + vibrationShift), size = Size(80f, 32f))
    drawRect(color = Color.Black, topLeft = Offset(w * 0.5f + handleWidth / 2f - 80f + vibrationShift, handleY - 16f + vibrationShift), size = Size(80f, 32f))

    // Handlebar Base Crown
    val path = Path().apply {
        moveTo(w * 0.4f + vibrationShift, h + vibrationShift)
        lineTo(w * 0.46f + vibrationShift, h * 0.84f + vibrationShift)
        lineTo(w * 0.54f + vibrationShift, h * 0.84f + vibrationShift)
        lineTo(w * 0.6f + vibrationShift, h + vibrationShift)
        close()
    }
    drawPath(path = path, color = Color(0xFF14171D))

    // Custom Colored Fuel Tank / Cover Accent
    drawRect(
        color = paintColor,
        topLeft = Offset(w * 0.46f + vibrationShift, h * 0.86f + vibrationShift),
        size = Size(w * 0.08f, h * 0.14f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(6f)
    )
}

// ENVIRONMENT WEATHER EFFECT LAYER
private fun DrawScope.drawWeatherEffects(w: Float, h: Float, horizon: Float, weather: Weather, animFrame: Int) {
    when (weather) {
        Weather.RAINY -> {
            // Draw diagonal rain lines falling
            for (i in 0..40) {
                val rx = (i * 97) % w
                val ry = (i * 123 + animFrame * 22) % h
                if (ry > horizon) { // rain drops hit the windshield/lens
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(rx, ry),
                        end = Offset(rx - 8f, ry + 24f),
                        strokeWidth = 2f
                    )
                }
            }
            // Screen edge rainy splash vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x33445566)),
                    center = Offset(w * 0.5f, h * 0.5f)
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, h)
            )
        }
        Weather.FOGGY -> {
            // Radial white fog gradient fading down from horizon
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xE0FFFFFF), Color.Transparent),
                    startY = 0f,
                    endY = h * 0.8f
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, h)
            )

            // Headlight beams glowing from bottom center forward to horizon
            val lightPath = Path().apply {
                moveTo(w * 0.5f - 10f, h * 0.85f)
                lineTo(w * 0.15f, horizon + 20f)
                lineTo(w * 0.85f, horizon + 20f)
                lineTo(w * 0.5f + 10f, h * 0.85f)
                close()
            }
            drawPath(
                path = lightPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xAAFFF176), Color.Transparent),
                    startY = h * 0.85f,
                    endY = horizon
                )
            )
        }
        Weather.NIGHT -> {
            // Heavy black overlay except the bike headlight path
            drawRect(color = Color.Black.copy(alpha = 0.45f), topLeft = Offset(0f, 0f), size = Size(w, h))

            // Bright yellow headlamp path
            val lightPath = Path().apply {
                moveTo(w * 0.5f - 14f, h * 0.85f)
                lineTo(w * 0.22f, horizon + 10f)
                lineTo(w * 0.78f, horizon + 10f)
                lineTo(w * 0.5f + 14f, h * 0.85f)
                close()
            }
            drawPath(
                path = lightPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xEEFFFF8D), Color.Transparent),
                    startY = h * 0.82f,
                    endY = horizon
                )
            )
        }
        else -> {
            // Beautiful Sunset gold glare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3FEEFF41), Color.Transparent)
                ),
                radius = 160f,
                center = Offset(w * 0.5f, horizon - 20f)
            )
        }
    }
}

// VIBRATION FEEDBACK UTILS
private fun triggerShortVibration(vibrator: Vibrator?) {
    if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }
}

private fun triggerCombatVibration(vibrator: Vibrator?) {
    if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(120, 220))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(120)
        }
    }
}
