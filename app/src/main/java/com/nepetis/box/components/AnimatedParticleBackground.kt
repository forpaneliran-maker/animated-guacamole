package com.nepetis.box.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val angle: Float = 0f
)

@Composable
fun AnimatedParticleBackground(modifier: Modifier = Modifier) {
    val particles = remember { mutableStateOf(generateParticles()) }
    var animationTime by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16) // ~60 FPS
            animationTime += 0.016f
            updateParticles(particles.value, animationTime)
        }
    }
    
    Canvas(modifier = modifier) {
        // پس‌زمینه مات سیاه
        drawRect(Color.Black)
        
        // نقطه‌های متحرک
        particles.value.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = 0.7f),
                radius = particle.size,
                center = androidx.compose.ui.geometry.Offset(particle.x, particle.y)
            )
            
            // هاله درخشنده
            drawCircle(
                color = particle.color.copy(alpha = 0.3f),
                radius = particle.size * 3,
                center = androidx.compose.ui.geometry.Offset(particle.x, particle.y)
            )
        }
    }
}

private fun generateParticles(count: Int = 30): List<Particle> {
    return (0 until count).map {
        val colors = listOf(
            Color(0xFF00D4FF), // آبی روشن
            Color(0xFFFF00FF), // مجنتا
            Color(0xFF00FF88), // سبز آبی
            Color(0xFFFFAA00), // نارنجی
            Color(0xFF00FFFF), // سیان
            Color(0xFFFF1493), // صورتی عمیق
            Color(0xFF1E90FF), // رنگ رویال
        )
        
        Particle(
            x = Math.random().toFloat() * 1080,
            y = Math.random().toFloat() * 2340,
            vx = (Math.random() - 0.5).toFloat() * 2,
            vy = (Math.random() - 0.5).toFloat() * 2,
            size = (Math.random() * 8 + 2).toFloat(),
            color = colors.random(),
            angle = (Math.random() * 360).toFloat()
        )
    }
}

private fun updateParticles(particles: List<Particle>, time: Float) {
    particles.forEachIndexed { index, particle ->
        // حرکت موجی
        val waveX = particle.x + sin(time * 2 + index) * 2
        val waveY = particle.y + cos(time + index * 0.5f) * 2
        
        // بروز‌رسانی موقعیت
        particles as MutableList
        particles[index] = particle.copy(
            x = (waveX + particle.vx) % 1080,
            y = (waveY + particle.vy) % 2340,
            angle = particle.angle + 2f
        )
        
        // بازگشت به داخل صفحه اگر خارج رفت
        if (particles[index].x < 0) particles[index] = particles[index].copy(x = 1080f)
        if (particles[index].y < 0) particles[index] = particles[index].copy(y = 2340f)
    }
}
