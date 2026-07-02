package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BikeEntity
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@Composable
fun GarageScreen(viewModel: GameViewModel) {
    val bikes by viewModel.allBikes.collectAsState()
    val activeBike by viewModel.activeBike.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: गाड्या खरेदी (Bikes Shop), 1: इंजिन ट्युनिंग (Engine Tuning), 2: रंग आणि स्टिकर्स (Aesthetics)

    FrostedGlassBackground(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.setScreen(GameScreen.MENU) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "पुणेकर गॅरेज (Garage)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Balance display
                Box(
                    modifier = Modifier.glassCard(shape = RoundedCornerShape(12.dp), borderColor = ToxicGreen.copy(alpha = 0.35f), backgroundColor = GlassWhiteBg)
                ) {
                    Text(
                        text = "₹${profile?.cash ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ToxicGreen,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Custom Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabButton("गाड्या खरेदी", activeTab == 0) { activeTab = 0 }
                TabButton("इंजिन ट्युनिंग", activeTab == 1) { activeTab = 1 }
                TabButton("रंग आणि डेकल्स", activeTab == 2) { activeTab = 2 }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TAB CONTENTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    0 -> BikesShopTab(bikes, activeBike, profile?.cash ?: 0, viewModel)
                    1 -> EngineTuningTab(activeBike, profile?.cash ?: 0, viewModel)
                    2 -> AestheticsTab(activeBike, viewModel)
                }
            }
        }
    }
}

@Composable
fun TabButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        modifier = Modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) NeonOrange else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.LightGray
        )
    }
}

// TAB 1: BIKES SHOP (Buy Pulsar, Bullet, KTM RC)
@Composable
fun BikesShopTab(bikes: List<BikeEntity>, activeBike: BikeEntity?, currentCash: Int, viewModel: GameViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            text = "तुमची सवारी निवडा (Select/Buy Indian Bikes):",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        for (bike in bikes) {
            val isActive = activeBike?.id == bike.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .glassCard(
                        shape = RoundedCornerShape(16.dp),
                        borderColor = if (isActive) NeonOrange else Color.White.copy(alpha = 0.15f),
                        backgroundColor = if (isActive) Color(0x22FFFFFF) else GlassWhiteBg
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bike.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = bike.englishName,
                                style = MaterialTheme.typography.bodySmall,
                                color = NitroBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Ownership actions
                        if (bike.owned) {
                            if (isActive) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.White.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("सध्याची गाडी (Active)", fontSize = 10.sp, color = Color.Gray)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.selectBike(bike.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NitroBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("equip_${bike.id}")
                                ) {
                                    Text("वापरा (Equip)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Buy button
                            val canAfford = currentCash >= bike.price
                            Button(
                                onClick = { viewModel.buyBike(bike) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ToxicGreen,
                                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("buy_${bike.id}")
                            ) {
                                Text("खरेदी करा ₹${bike.price}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (canAfford) Color.Black else Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bike.description,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Stats
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("टॉप स्पीड: ${bike.baseSpeed.toInt()} km/h", fontSize = 11.sp, color = Color.Gray)
                        Text("ताकद: ${bike.baseAcceleration.toInt()}/100", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// TAB 2: ENGINE TUNING (Piston, ECU, Air Filter, Exhaust stages)
@Composable
fun EngineTuningTab(bike: BikeEntity?, currentCash: Int, viewModel: GameViewModel) {
    if (bike == null) return

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            text = "${bike.name} ट्युनिंग कस्टमायझेशन:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Upgrade category list helper
        UpgradeCategoryCard(
            title = "१. हाय-परफॉर्मन्स पिस्टन (High-Performance Piston)",
            desc = "इंजिन कॉम्प्रेशन वाढवून गाडीचा टॉप स्पीड तुफान वाढवतो.",
            stage = bike.engineStage,
            baseCost = 2000,
            currentCash = currentCash,
            onUpgrade = { cost -> viewModel.buyUpgrade(bike.id, "engine", cost) },
            tag = "engine_upgrade"
        )

        UpgradeCategoryCard(
            title = "२. इलेक्ट्रॉनिक कंट्रोल युनिट (ECU Remap)",
            desc = "पिकअप आणि आरपीएम रिॲक्शन सुधारून गतीशील प्रवेग देतो.",
            stage = bike.ecuStage,
            baseCost = 1500,
            currentCash = currentCash,
            onUpgrade = { cost -> viewModel.buyUpgrade(bike.id, "ecu", cost) },
            tag = "ecu_upgrade"
        )

        UpgradeCategoryCard(
            title = "३. स्पोर्ट्स सायलेन्सर (Exhaust Thump Tuner)",
            desc = "सायलेन्सर कस्टमायझेशन! आवाज अधिक खणखणीत करतो आणि हॉर्सपॉवर वाढवतो.",
            stage = bike.exhaustStage,
            baseCost = 3000,
            currentCash = currentCash,
            onUpgrade = { cost -> viewModel.buyUpgrade(bike.id, "exhaust", cost) },
            tag = "exhaust_upgrade"
        )

        UpgradeCategoryCard(
            title = "४. रेसिंग एअर फिल्टर (High-Flow Air Filter)",
            desc = "इंजिन हवेचा पुरवठा सुधारून स्थिर व वेगवान कामगिरी सुनिश्चित करतो.",
            stage = bike.airFilterStage,
            baseCost = 1000,
            currentCash = currentCash,
            onUpgrade = { cost -> viewModel.buyUpgrade(bike.id, "airFilter", cost) },
            tag = "filter_upgrade"
        )
    }
}

@Composable
fun UpgradeCategoryCard(
    title: String,
    desc: String,
    stage: Int,
    baseCost: Int,
    currentCash: Int,
    onUpgrade: (Int) -> Unit,
    tag: String
) {
    val isMax = stage >= 3
    val cost = baseCost * (stage + 1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .glassCard(shape = RoundedCornerShape(12.dp), borderColor = Color.White.copy(alpha = 0.15f), backgroundColor = GlassWhiteBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Stage indicators
                Row {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(horizontal = 1.5.dp)
                                .clip(CircleShape)
                                .background(if (i <= stage) ToxicGreen else Color.White.copy(alpha = 0.15f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMax) "कमाल ट्यूनिंग झाली! (Stage Max)" else "पुढील स्टेज: स्टेज ${stage + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMax) ToxicGreen else LuxuryGold
                )

                if (!isMax) {
                    val canAfford = currentCash >= cost
                    Button(
                        onClick = { onUpgrade(cost) },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag(tag)
                    ) {
                        Text("ट्यून करा: ₹$cost", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (canAfford) Color.Black else Color.Gray)
                    }
                }
            }
        }
    }
}

// TAB 3: AESTHETICS (Paint color, neon, Stickers!)
@Composable
fun AestheticsTab(bike: BikeEntity?, viewModel: GameViewModel) {
    if (bike == null) return

    var selectedPaint by remember { mutableIntStateOf(bike.paintColor) }
    var selectedNeon by remember { mutableIntStateOf(bike.neonColor) }
    var stickerInput by remember { mutableStateOf(bike.stickerText) }

    val paints = listOf("मूळ रंग (Stock)", "लाल (Crimson Red)", "निळा (Nitro Blue)", "सोनेरी (Luxury Gold)", "काळा (Stealth Black)")
    val neons = listOf("नेऑन बंद (None)", "हिरवा (Toxic Green)", "निळा (Neon Blue)", "गुलाबी (Neon Pink)", "पिवळा (Neon Gold)")
    val stickers = listOf("", "पुणेकर", "एकच राजा", "नाद करायचा नाय!", "आईचा आशीर्वाद", "PCMC KING")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("१. गाडीचा रंग निवडा (Paint Color):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in paints.indices) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            when (i) {
                                1 -> PuneCrimson
                                2 -> NitroBlue
                                3 -> LuxuryGold
                                4 -> Color.Black
                                else -> NeonOrange // stock orange
                            }
                        )
                        .border(
                            width = if (selectedPaint == i) 2.5.dp else 1.dp,
                            color = if (selectedPaint == i) Color.White else Color.Gray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { selectedPaint = i }
                )
            }
        }
        Text(paints[selectedPaint], fontSize = 11.sp, color = NitroBlue, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text("२. निऑन अंडरग्लो (Neon Underglow):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in neons.indices) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (i) {
                                1 -> ToxicGreen
                                2 -> NitroBlue
                                3 -> PuneCrimson
                                4 -> LuxuryGold
                                else -> Color.DarkGray // closed
                            }
                        )
                        .border(
                            width = if (selectedNeon == i) 2.5.dp else 1.dp,
                            color = if (selectedNeon == i) Color.White else Color.Gray.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedNeon = i }
                )
            }
        }
        Text(neons[selectedNeon], fontSize = 11.sp, color = ToxicGreen, modifier = Modifier.padding(vertical = 4.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text("३. मराठी स्लोगन स्टिकर्स (Stickers):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (st in stickers) {
                val label = if (st.isEmpty()) "काहीही नाही" else st
                val isSel = stickerInput == st
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .glassCard(
                            shape = RoundedCornerShape(10.dp),
                            borderWidth = if (isSel) 1.5.dp else 1.dp,
                            borderColor = if (isSel) NeonOrange else Color.White.copy(alpha = 0.15f),
                            backgroundColor = if (isSel) Color(0x33FFFFFF) else GlassWhiteBg
                        )
                        .clickable { stickerInput = st }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else Color.LightGray,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save modification buttons
        Button(
            onClick = {
                viewModel.saveCustomizations(bike.id, selectedPaint, selectedNeon, stickerInput)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_customs_button")
        ) {
            Text("बदल जतन करा (Save Aesthetics Mods)", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}
