package com.ksensor.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

fun Double.toTwoDecimalString(): String {
    val integerPart = this.toLong()
    val decimalPart = ((this - integerPart) * 100).toLong()
    return "$integerPart.${decimalPart.toString().padStart(2, '0')}"
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colors.primary
) {
    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.h5, color = MaterialTheme.colors.onSurface)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.primary)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) MaterialTheme.colors.primary.copy(alpha = 0.12f) else MaterialTheme.colors.error.copy(alpha = 0.12f)
    val contentColor = if (isActive) MaterialTheme.colors.primary else MaterialTheme.colors.error

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(contentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.caption,
                color = contentColor
            )
        }
    }
}

@Composable
fun SparklineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colors.primary
) {
    val points = remember(values) { values.takeLast(30) }
    if (points.isEmpty()) return

    val minVal = points.minOrNull() ?: 0f
    val maxVal = points.maxOrNull() ?: 1f
    val range = if (maxVal == minVal) 1f else (maxVal - minVal)

    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth().height(140.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.size < 2) return@Canvas
                val widthStep = size.width / (points.size - 1)
                val path = Path()

                points.forEachIndexed { index, fl ->
                    val x = index * widthStep
                    val normalized = (fl - minVal) / range
                    val y = size.height - (normalized * size.height * 0.8f + size.height * 0.1f)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}
