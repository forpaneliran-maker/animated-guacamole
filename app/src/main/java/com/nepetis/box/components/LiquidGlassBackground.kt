package com.nepetis.box.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

@Composable
fun LiquidGlassBackground(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            time += 0.016f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // پس‌زمینه مات سیاه
        drawRect(Color.Black)
        
        // عناصر Liquid Glass متحرک
        drawLiquidBlobs(time)
        
        // لایه محافظ
        drawRect(Color.Black.copy(alpha = 0.1f))
    }
}

private fun DrawScope.drawLiquidBlobs(time: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    // Blob های رنگی متحرک
    val colors = listOf(
        Color(0xFF00D4FF),
        Color(0xFFFF00FF),
        Color(0xFF00FF88),
        Color(0xFFFFAA00),
    )
    
    colors.forEachIndexed { index, color ->
        val angle = (time * 0.5f + index * 90) * PI.toFloat() / 180f
        val radius = 150 + sin(time + index) * 50
        
        val x = centerX + cos(angle) * radius
        val y = centerY + sin(angle) * radius
        
        // رسم blob درخشنده
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = 100f,
            center = Offset(x, y)
        )
        
        // هاله درخشنده
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = 200f,
            center = Offset(x, y)
        )
    }
    
    // خطوط متحرک
    for (i in 0..3) {
        val x1 = size.width * sin(time * 0.3f + i) / 2 + size.width / 2
        val y1 = size.height * cos(time * 0.2f + i) / 2 + size.height / 2
        val x2 = size.width * sin(time * 0.2f + i + 2) / 2 + size.width / 2
        val y2 = size.height * cos(time * 0.15f + i + 2) / 2 + size.height / 2
        
        drawLine(
            color = Color(0xFF00D4FF).copy(alpha = 0.15f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 2f
        )
    }
}
