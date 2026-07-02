package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@Composable
fun FinishScreen(viewModel: GameViewModel) {
    val won by viewModel.raceFinishStatus.collectAsState()
    val earning by viewModel.raceEarning.collectAsState()
    val timeElapsed by viewModel.raceTimeElapsed.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()

    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIn = true
    }

    FrostedGlassBackground(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Result Title
            AnimatedVisibility(
                visible = animateIn,
                enter = fadeIn(animationSpec = tween(600)) + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (won) Icons.Default.EmojiEvents else Icons.Default.Dangerous,
                        contentDescription = "Result",
                        tint = if (won) LuxuryGold else PuneCrimson,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (won) "विजयी! (VICTORY)" else "अपघात झाला! (CRASHED)",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (won) ToxicGreen else PuneCrimson,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Fun local Marathi commentary card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(
                        shape = RoundedCornerShape(16.dp),
                        borderColor = if (won) ToxicGreen.copy(alpha = 0.4f) else PuneCrimson.copy(alpha = 0.4f),
                        backgroundColor = GlassWhiteBg
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (won) {
                            "एकच नंबर रायडिंग भावा! तू तर पुण्याच्या रस्त्यांचा खरा राजा निघालास! PMPML बस आणि स्थानिक वाहतुकीला मागे टाकून तू यश संपादन केलेस. 🏍️🔥"
                        } else {
                            "राडा झाला भावा! पुण्याच्या गल्लीबोळात जास्त धडका लागल्या की गाडी डॅमेज होते! इंजिन मजबूत करण्यासाठी गॅरेजमध्ये जाऊन पिस्टन आणि सायलेन्सर अपग्रेड करून घे. 🔧🤕"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Performance breakdown score cards
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "कामगिरीचा तक्ता (Race Performance):",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time stat
                    StatCard(
                        title = "एकूण वेळ",
                        value = "$timeElapsed सेकंद",
                        icon = Icons.Default.Timer,
                        color = NitroBlue,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    // Earning stat
                    StatCard(
                        title = "मिळालेले पैसे",
                        value = "₹$earning",
                        icon = Icons.Default.MonetizationOn,
                        color = ToxicGreen,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }
            }

            // Navigation shortcuts
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { viewModel.startRace() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_retry_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("पुन्हा शर्यत खेळा (Retry Sharyat)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Go to Garage for modifications
                    Button(
                        onClick = { viewModel.setScreen(GameScreen.GARAGE) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(1.dp, NitroBlue.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                            .testTag("finish_garage_button")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Garage", tint = NitroBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("गॅरेज (Garage)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Go to main menu
                    Button(
                        onClick = { viewModel.setScreen(GameScreen.MENU) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1.05f)
                            .height(50.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .testTag("finish_menu_button")
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("मुख्य मेनू (Menu)", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(12.dp), borderColor = Color.White.copy(alpha = 0.15f), backgroundColor = GlassWhiteBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 10.sp, color = Color.Gray)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}
