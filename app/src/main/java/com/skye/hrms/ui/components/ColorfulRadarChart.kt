package com.skye.hrms.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ColorfulRadarChart(
    labels: List<String>,
    values: List<Float>,
    maxValue: Float = 100f,
    animationDuration: Int = 900,
) {
    require(labels.size == values.size)

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val outline = MaterialTheme.colorScheme.outline
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    val segmentColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary,
        MaterialTheme.colorScheme.surfaceTint
    )

    val animatedValues = values.mapIndexed { index, v ->
        animateFloatAsState(
            targetValue = v,
            animationSpec = tween(
                durationMillis = animationDuration,
                delayMillis = index * 80,
                easing = FastOutSlowInEasing
            )
        ).value
    }

    Canvas(
        modifier = Modifier
            .size(350.dp)
            .padding(20.dp)
    ) {
        val count = labels.size
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.5f
        val angle = (2 * Math.PI / count)
        val gridLayers = 5

        // ---------- BACKGROUND COLOR SEGMENTS ----------
        repeat(gridLayers) { layer ->
            val rInner = radius * (layer / gridLayers.toFloat())
            val rOuter = radius * ((layer + 1) / gridLayers.toFloat())

            for (i in 0 until count) {
                val segmentColor = segmentColors[i % segmentColors.size].copy(alpha = 0.25f)
                val path = Path()

                val x1 = center.x + rInner * cos(angle * i).toFloat()
                val y1 = center.y + rInner * sin(angle * i).toFloat()
                val x2 = center.x + rOuter * cos(angle * i).toFloat()
                val y2 = center.y + rOuter * sin(angle * i).toFloat()
                val x3 = center.x + rOuter * cos(angle * (i + 1)).toFloat()
                val y3 = center.y + rOuter * sin(angle * (i + 1)).toFloat()
                val x4 = center.x + rInner * cos(angle * (i + 1)).toFloat()
                val y4 = center.y + rInner * sin(angle * (i + 1)).toFloat()

                path.moveTo(x1, y1)
                path.lineTo(x2, y2)
                path.lineTo(x3, y3)
                path.lineTo(x4, y4)
                path.close()
                drawPath(path, segmentColor)
            }
        }

        // ---------- SPIDER-WEB GRID (visible again) ----------
        repeat(gridLayers) { step ->
            val r = radius * ((step + 1) / gridLayers.toFloat())
            val gridPath = Path()
            for (i in 0 until count) {
                val x = center.x + r * cos(angle * i).toFloat()
                val y = center.y + r * sin(angle * i).toFloat()
                if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
            }
            gridPath.close()
            drawPath(gridPath, color = outline.copy(alpha = 0.7f), style = Stroke(2f))
        }

        // Axis lines
        for (i in 0 until count) {
            val x = center.x + radius * cos(angle * i).toFloat()
            val y = center.y + radius * sin(angle * i).toFloat()
            drawLine(
                outlineVariant.copy(alpha = 0.7f),
                center,
                Offset(x, y),
                strokeWidth = 2f
            )
        }

        // ---------- VALUE POLYGON ----------
        val shapePath = Path()
        val points = mutableListOf<Offset>()

        animatedValues.forEachIndexed { i, v ->
            val r = (v / maxValue) * radius
            val x = center.x + r * cos(angle * i).toFloat()
            val y = center.y + r * sin(angle * i).toFloat()
            points.add(Offset(x, y))
            if (i == 0) shapePath.moveTo(x, y) else shapePath.lineTo(x, y)
        }
        shapePath.close()

        // Gradient outline
        points.forEachIndexed { i, point ->
            val next = points[(i + 1) % count]
            drawLine(
                brush = Brush.linearGradient(
                    listOf(segmentColors[i % segmentColors.size], segmentColors[(i + 1) % segmentColors.size])
                ),
                start = point,
                end = next,
                strokeWidth = 6f
            )
        }

        // Dots
        points.forEachIndexed { i, p ->
            drawCircle(
                color = segmentColors[i % segmentColors.size],
                radius = 9f,
                center = p
            )
        }

        // ---------- LABELS ----------
        labels.forEachIndexed { i, label ->
            val textR = radius * 1.25f
            val x = center.x + textR * cos(angle * i).toFloat()
            val y = center.y + textR * sin(angle * i).toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                y,
                android.graphics.Paint().apply {
                    color = onSurfaceColor
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 36f
                }
            )
        }
    }
}