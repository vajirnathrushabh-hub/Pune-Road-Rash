package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Glass Theme Color Tokens
val GlassBlackBg = Color(0xFF050505)
val GlassWhiteBg = Color(0x0EFFFFFF) // 5.5% white
val GlassWhiteBgStrong = Color(0x19FFFFFF) // 10% white
val GlassWhiteBgSelected = Color(0x2BFFFFFF) // 17% white
val GlassWhiteBorder = Color(0x1AFFFFFF) // 10% white border
val GlassWhiteBorderStrong = Color(0x26FFFFFF) // 15% white border

// Accents from "Frosted Glass" design
val FrostedIndigo = Color(0xFF818CF8) // indigo-400
val FrostedEmerald = Color(0xFF34D399) // emerald-400
val FrostedAmber = Color(0xFFFBBF24) // amber-400
val FrostedSlate = Color(0xFF94A3B8) // slate-400
val FrostedIndigoDark = Color(0xFF4F46E5) // indigo-600
val FrostedEmeraldDark = Color(0xFF059669) // emerald-600

@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBlackBg)
    ) {
        // Ambient soft blur halos
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Indigo Glow at Top-Left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FrostedIndigoDark.copy(alpha = 0.22f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(-size.width * 0.1f, size.height * 0.15f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = androidx.compose.ui.geometry.Offset(-size.width * 0.1f, size.height * 0.15f)
            )

            // Emerald Glow at Bottom-Right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FrostedEmeraldDark.copy(alpha = 0.18f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * 0.85f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * 0.85f)
            )
        }

        content()
    }
}

// Custom Extension Modifiers for Frosted Glass styling
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = GlassWhiteBorder,
    backgroundColor: Color = GlassWhiteBg
) = this
    .clip(shape)
    .background(backgroundColor)
    .border(borderWidth, borderColor, shape)

fun Modifier.glassCardSelected(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.2.dp,
    borderColor: Color = GlassWhiteBorderStrong,
    backgroundColor: Color = GlassWhiteBgSelected
) = this
    .clip(shape)
    .background(backgroundColor)
    .border(borderWidth, borderColor, shape)
