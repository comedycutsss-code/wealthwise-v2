package com.wealthwise.app.presentation.common.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/**
 * The dashboard's signature element: net worth / cash flow trend line that draws itself in,
 * stroke-by-stroke, like a pen tracing a ledger line — rather than a generic fade-in chart.
 * Uses PathMeasure to trim the path progressively as [progress] animates 0f -> 1f.
 */
@Composable
fun InkStrokeLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillBelow: Boolean = true
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1100))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (values.size < 2) return@Canvas
        val maxV = values.max()
        val minV = values.min()
        val range = (maxV - minV).let { if (it == 0f) 1f else it }
        val stepX = size.width / (values.size - 1)

        val path = Path().apply {
            values.forEachIndexed { index, v ->
                val x = index * stepX
                val normalized = (v - minV) / range
                val y = size.height - (normalized * size.height)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val measure = PathMeasure()
        measure.setPath(path, false)
        val animatedPath = Path()
        measure.getSegment(0f, measure.length * progress.value, animatedPath, true)

        drawPath(
            path = animatedPath,
            color = lineColor,
            style = Stroke(width = 5f, pathEffect = PathEffect.cornerPathEffect(20f))
        )

        if (fillBelow) {
            val fillPath = Path().apply {
                addPath(animatedPath)
                lineTo(values.size.let { (it - 1) * stepX * progress.value }, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = fillPath, color = lineColor.copy(alpha = 0.12f))
        }
    }
}
