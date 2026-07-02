package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.*
import com.example.ui.theme.*

@Composable
fun MenuScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val activeBike by viewModel.activeBike.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val selectedWeather by viewModel.selectedWeather.collectAsState()
    val batterySaver by viewModel.batterySaverMode.collectAsState()

    // Smooth entry transition of menu elements
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        // Set default track initially if nothing selected
        if (selectedTrack == null && viewModel.tracks.isNotEmpty()) {
            viewModel.selectTrack(viewModel.tracks.first())
        }
    }

    FrostedGlassBackground {
        // Dynamic background cover image generated earlier
        Image(
            painter = painterResource(id = R.drawable.img_game_cover_1782978736667),
            contentDescription = "Pune Shaniwar Wada Cover",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // Gradient fade out for background image transition
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.43f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GlassBlackBg),
                        startY = 150f
                    )
                )
        )

        // Scrollable menu contents
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Game Logo / Marathi Branding
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .glassCard(
                        shape = RoundedCornerShape(16.dp),
                        borderColor = LuxuryGold.copy(alpha = 0.4f),
                        backgroundColor = Color(0x22000000)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "पुणे रोड रॅश",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonOrange,
                            fontSize = 36.sp,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "PUNE ROAD RASH • PCMC ARCADE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NitroBlue,
                            letterSpacing = 2.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Player Info Bar
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cash Balance Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .glassCard(shape = RoundedCornerShape(12.dp), borderColor = ToxicGreen.copy(alpha = 0.35f), backgroundColor = GlassWhiteBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Cash Icon",
                                tint = ToxicGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("तुमची शिल्लक (Cash)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("₹${profile?.cash ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ToxicGreen)
                            }
                        }
                    }

                    // Battery & Settings Toggle Card
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .glassCard(
                                shape = RoundedCornerShape(12.dp),
                                borderColor = if (batterySaver) ToxicGreen.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                                backgroundColor = if (batterySaver) Color(0x1A39FF14) else GlassWhiteBg
                            )
                            .clickable { viewModel.toggleBatterySaver() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (batterySaver) Icons.Default.BatterySaver else Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery Saver",
                                tint = if (batterySaver) ToxicGreen else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("बॅटरी बचत (Saver)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(
                                    text = if (batterySaver) "शुरू (ON)" else "बंद (OFF)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (batterySaver) ToxicGreen else Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Bike Section
            activeBike?.let { bike ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .glassCard(shape = RoundedCornerShape(16.dp), borderColor = NitroBlue.copy(alpha = 0.35f), backgroundColor = GlassWhiteBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = "Bike Icon",
                                    tint = NeonOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = bike.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { viewModel.setScreen(GameScreen.GARAGE) },
                                colors = ButtonDefaults.buttonColors(containerColor = NitroBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("garage_button")
                            ) {
                                Icon(Icons.Default.Build, contentDescription = "Garage", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("गॅरेज (Garage)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (bike.stickerText.isNotEmpty()) {
                            Text(
                                text = "स्टीकर: \"${bike.stickerText}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = ToxicGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress performance bars
                        StatProgressRow("वेग (Max Speed)", (bike.baseSpeed + 20f) * (1f + bike.engineStage * 0.12f) / 130f, NitroBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        StatProgressRow("पिकअप (Acceleration)", (bike.baseAcceleration + 15f) * (1f + bike.ecuStage * 0.1f) / 120f, NeonOrange)
                        Spacer(modifier = Modifier.height(6.dp))
                        StatProgressRow("नियंत्रण (Handling)", bike.baseHandling / 100f, ToxicGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Route / Track Selection Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "टॅग रूट निवडा (Select Route in Pune):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.tracks) { track ->
                        val isUnlocked = profile?.unlockedTracks?.split(",")?.contains(track.id) == true
                        val isSelected = selectedTrack?.id == track.id

                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(160.dp)
                                .glassCard(
                                    shape = RoundedCornerShape(16.dp),
                                    borderWidth = if (isSelected) 2.dp else 1.dp,
                                    borderColor = if (isSelected) NeonOrange else if (isUnlocked) Color.White.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.3f),
                                    backgroundColor = if (isSelected) Color(0x22FFFFFF) else GlassWhiteBg
                                )
                                .clickable(enabled = isUnlocked) {
                                    viewModel.selectTrack(track)
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = track.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUnlocked) Color.White else Color.Gray,
                                                maxLines = 1
                                            )
                                            if (!isUnlocked) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = track.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray,
                                            maxLines = 3,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "अंतर: ${(track.distance / 1000f)} किमी",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NitroBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "बक्षीस: ₹${track.reward}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ToxicGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (!isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("मागील रेस जिंकून अनलॉक करा!", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weather Selection Overrides
            selectedTrack?.let { track ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .glassCard(shape = RoundedCornerShape(16.dp), borderColor = Color.White.copy(alpha = 0.15f), backgroundColor = GlassWhiteBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "हवामान निवडा (Weather Controls):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WeatherButton("सूर्यप्रकाश", Weather.SUNNY, selectedWeather == Weather.SUNNY) {
                                viewModel.overrideWeather(Weather.SUNNY)
                            }
                            WeatherButton("पाऊस", Weather.RAINY, selectedWeather == Weather.RAINY) {
                                viewModel.overrideWeather(Weather.RAINY)
                            }
                            WeatherButton("धुके", Weather.FOGGY, selectedWeather == Weather.FOGGY) {
                                viewModel.overrideWeather(Weather.FOGGY)
                            }
                            WeatherButton("रात्र", Weather.NIGHT, selectedWeather == Weather.NIGHT) {
                                viewModel.overrideWeather(Weather.NIGHT)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cheat Codes for Testing Cash
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { viewModel.cheatAddCash() }) {
                    Text("पैसे हवेत? +₹५,००० मिळवा 💰", color = ToxicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Offline Mode Active 🎮",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        // STICKY BOTTOM PLAY BAR OVERLAY (Always visible so players never miss the play button!)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GlassBlackBg.copy(alpha = 0.95f)),
                        startY = 0f
                    )
                )
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.setScreen(GameScreen.IGNITION) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_race_button")
            ) {
                Icon(Icons.Default.SportsMotorsports, contentDescription = "Play", modifier = Modifier.size(24.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "खेळायला सुरुवात करा (Let's Ride!)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun StatProgressRow(label: String, progress: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text("${(progress * 100).toInt()}%", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.Black
        )
    }
}

@Composable
fun WeatherButton(label: String, weather: Weather, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier
            .height(38.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        val icon = when (weather) {
            Weather.SUNNY -> Icons.Default.WbSunny
            Weather.RAINY -> Icons.Default.WaterDrop
            Weather.FOGGY -> Icons.Default.Cloud
            Weather.NIGHT -> Icons.Default.NightsStay
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (isSelected) NeonOrange else Color.LightGray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.LightGray
        )
    }
}
