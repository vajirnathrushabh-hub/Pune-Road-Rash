package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IgnitionScreen(viewModel: GameViewModel) {
    val activeBike by viewModel.activeBike.collectAsState()
    val isKeyInserted by viewModel.isKeyInserted.collectAsState()
    val isIgnitionOn by viewModel.isIgnitionOn.collectAsState()
    val isEngineRunning by viewModel.isEngineRunning.collectAsState()
    val isStarterPressed by viewModel.isStarterPressed.collectAsState()
    val statusText by viewModel.ignitionCockpitStatus.collectAsState()

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val scope = rememberCoroutineScope()

    // Gauge Sweep animation state on ignition on
    var sweepActive by remember { mutableStateOf(false) }
    val sweepRpm by animateFloatAsState(
        targetValue = if (sweepActive) 9000f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "rpm_sweep"
    )

    // Handle Gauge Sweep effect when ignition goes on
    LaunchedEffect(isIgnitionOn) {
        if (isIgnitionOn) {
            sweepActive = true
            delay(1000)
            sweepActive = false
        }
    }

    // Manual revving/throttle input by player when engine is running
    var throttleValue by remember { mutableFloatStateOf(0f) } // 0f to 1f
    val smoothRpm by animateFloatAsState(
        targetValue = if (isEngineRunning) {
            1200f + (throttleValue * 6800f)
        } else {
            0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "smooth_rpm"
    )

    // Trigger sound updates and vibration on RPM changes
    LaunchedEffect(smoothRpm, isEngineRunning) {
        if (isEngineRunning) {
            // Update VM RPM
            viewModel.setRpm(smoothRpm)

            // Vibrate phone slightly based on RPM level
            if (smoothRpm > 1500f && vibrator != null && vibrator.hasVibrator()) {
                val intensity = (smoothRpm / 9000f * 255).toInt().coerceIn(10, 255)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, intensity))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(40)
                }
            }
        }
    }

    FrostedGlassBackground(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setScreen(GameScreen.MENU) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "स्टार्टिंग कॉकपिट (Starter Cockpit)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium Meter Console Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .glassCard(
                        shape = RoundedCornerShape(24.dp),
                        borderColor = LuxuryGold.copy(alpha = 0.5f),
                        backgroundColor = Color(0x26000000)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeBike?.name?.uppercase() ?: "BIKE CONSOLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isIgnitionOn) ToxicGreen else Color.White.copy(alpha = 0.15f))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "N (Neutral)",
                                fontSize = 8.sp,
                                color = if (isIgnitionOn) ToxicGreen else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Simulated Digital Odometer + RPM display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Digital speedometer (000 in starting cockpit)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isEngineRunning) "००" else "--",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 54.sp,
                                    color = if (isIgnitionOn) NitroBlue else Color.DarkGray
                                )
                            )
                            Text("km/h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.width(48.dp))

                        // RPM progress bar
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val activeRpm = if (sweepActive) sweepRpm else smoothRpm
                            Text(
                                text = "${activeRpm.toInt()} RPM",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIgnitionOn) NeonOrange else Color.DarkGray
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (activeRpm / 9000f).coerceIn(0f, 1f) },
                                color = if (activeRpm > 7000) PuneCrimson else NeonOrange,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                            Text("आरपीएम सुई", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    // Diagnostic indicators (Engine check, battery status)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatusLed(
                            label = "चावी (KEY)",
                            active = isKeyInserted,
                            color = NitroBlue
                        )
                        StatusLed(
                            label = "इग्निशन (IGN)",
                            active = isIgnitionOn,
                            color = NeonOrange
                        )
                        StatusLed(
                            label = "इंजिन (RUN)",
                            active = isEngineRunning,
                            color = ToxicGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step status box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .glassCard(shape = RoundedCornerShape(12.dp), borderColor = NeonOrange.copy(alpha = 0.35f), backgroundColor = GlassWhiteBg)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // PHYSICAL HARDWARE CONTROLS UI LAYOUT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // STEP 1 & 2: KEY IGNITION SLIDER/DRAG
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "पायरी १: चावी चालू करा",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isIgnitionOn) NeonOrange else Color.White.copy(alpha = 0.08f))
                            .border(2.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .clickable {
                                if (!isKeyInserted) {
                                    viewModel.insertKey()
                                } else if (!isIgnitionOn) {
                                    viewModel.turnIgnitionOn()
                                }
                            }
                            .testTag("key_ignition_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Key",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(if (isIgnitionOn) 90f else 0f)
                        )
                    }
                    Text(
                        text = if (isIgnitionOn) "ऑन (ON)" else if (isKeyInserted) "चावी फिरवा" else "चावी लावा",
                        color = if (isIgnitionOn) ToxicGreen else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // STEP 3: RED START SWITCH BUTTON
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "पायरी २: सेल्फ स्टार्ट",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEngineRunning) ToxicGreen
                                else if (isIgnitionOn) PuneCrimson
                                else Color.White.copy(alpha = 0.08f)
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .pointerInput(isIgnitionOn, isEngineRunning) {
                                detectDragGestures(
                                    onDragStart = {
                                        if (isIgnitionOn && !isEngineRunning) {
                                            viewModel.pressStarter(true)
                                        }
                                    },
                                    onDragEnd = {
                                        viewModel.pressStarter(false)
                                    },
                                    onDragCancel = {
                                        viewModel.pressStarter(false)
                                    },
                                    onDrag = { _, _ -> }
                                )
                            }
                            .testTag("self_start_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = if (isEngineRunning) "चालू (RUNNING)" else "दाबून धरा (HOLD)",
                        color = if (isEngineRunning) ToxicGreen else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // THROTTLE ROTATION GRIPPER
            if (isEngineRunning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏍️ थ्रॉटल फिरवून इंजिन रेस करा (Rev Engine):",
                        fontSize = 13.sp,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = throttleValue,
                        onValueChange = { throttleValue = it },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonOrange,
                            activeTrackColor = NeonOrange.copy(alpha = 0.8f),
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("throttle_slider")
                    )
                    Text(
                        text = "RPM: ${smoothRpm.toInt()} / ९०००",
                        color = if (smoothRpm > 7500) PuneCrimson else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // GO TO RACING TRACK BUTTON
            Button(
                onClick = { viewModel.startRace() },
                enabled = isEngineRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEngineRunning) Color.White else Color.White.copy(alpha = 0.05f),
                    contentColor = if (isEngineRunning) Color.Black else Color.White.copy(alpha = 0.25f),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (isEngineRunning) Color.White else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("go_to_race_track_button")
            ) {
                Text(
                    text = if (isEngineRunning) "रेस सुरू करा! (Start Race)" else "इंजिन सुरू होण्याची वाट पहा...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isEngineRunning) Color.Black else Color.White.copy(alpha = 0.25f)
                )
            }
        }
    }
}

@Composable
fun StatusLed(label: String, active: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (active) color else Color.White.copy(alpha = 0.15f))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 9.sp, color = if (active) color else Color.Gray, fontWeight = FontWeight.Bold)
    }
}
