package com.example.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShootingModeIcon(
    mode: CaptureMode,
    tint: Color = Color.White,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (mode) {
            CaptureMode.NIGHT -> drawNightIcon(tint, w, h)
            CaptureMode.HI_RES -> drawHiResIcon(tint, w, h)
            CaptureMode.PANO -> drawPanoIcon(tint, w, h)
            CaptureMode.MACRO -> drawMacroIcon(tint, w, h)
            CaptureMode.SLO_MO -> drawSloMoIcon(tint, w, h)
            CaptureMode.TIME_LAPSE -> drawTimeLapseIcon(tint, w, h)
            CaptureMode.DUAL_VIDEO -> drawDualVideoIcon(tint, w, h)
            CaptureMode.UNDERWATER -> drawUnderwaterIcon(tint, w, h)
            CaptureMode.STICKER -> drawStickerIcon(tint, w, h)
            CaptureMode.DOC_SCANNER -> drawDocScannerIcon(tint, w, h)
            CaptureMode.PORTRAIT -> drawPortraitIcon(tint, w, h)
            CaptureMode.PRO -> drawProIcon(tint, w, h)
            CaptureMode.PHOTO -> drawPhotoIcon(tint, w, h)
            CaptureMode.VIDEO -> drawVideoIcon(tint, w, h)
            CaptureMode.HDR -> drawHdrIcon(tint, w, h)
        }
    }
}

// 1. Crescent Moon Icon for NIGHT
private fun DrawScope.drawNightIcon(color: Color, w: Float, h: Float) {
    val path = Path().apply {
        moveTo(w * 0.65f, h * 0.18f)
        cubicTo(
            w * 0.25f, h * 0.22f,
            w * 0.20f, h * 0.78f,
            w * 0.68f, h * 0.82f
        )
        cubicTo(
            w * 0.40f, h * 0.72f,
            w * 0.42f, h * 0.32f,
            w * 0.65f, h * 0.18f
        )
        close()
    }
    drawPath(path, color = color, style = Fill)
    // Small star accent
    drawCircle(color, radius = w * 0.04f, center = Offset(w * 0.76f, h * 0.32f))
}

// 2. High-Res Sensor Matrix for HI-RES
private fun DrawScope.drawHiResIcon(color: Color, w: Float, h: Float) {
    // Outer rounded square
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.18f, h * 0.18f),
        size = Size(w * 0.64f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.07f)
    )
    // 3x3 pixel sensor dots
    val dotR = w * 0.045f
    val positions = listOf(0.34f, 0.50f, 0.66f)
    for (x in positions) {
        for (y in positions) {
            drawCircle(
                color = color,
                radius = dotR,
                center = Offset(w * x, h * y)
            )
        }
    }
}

// 3. Curved Panorama Landscape for PANO
private fun DrawScope.drawPanoIcon(color: Color, w: Float, h: Float) {
    val framePath = Path().apply {
        moveTo(w * 0.15f, h * 0.30f)
        cubicTo(w * 0.35f, h * 0.22f, w * 0.65f, h * 0.22f, w * 0.85f, h * 0.30f)
        lineTo(w * 0.85f, h * 0.70f)
        cubicTo(w * 0.65f, h * 0.78f, w * 0.35f, h * 0.78f, w * 0.15f, h * 0.70f)
        close()
    }
    drawPath(framePath, color = color, style = Stroke(width = w * 0.065f))

    // Mountain peaks inside
    val mountainPath = Path().apply {
        moveTo(w * 0.22f, h * 0.64f)
        lineTo(w * 0.42f, h * 0.44f)
        lineTo(w * 0.56f, h * 0.58f)
        lineTo(w * 0.72f, h * 0.38f)
        lineTo(w * 0.80f, h * 0.62f)
    }
    drawPath(
        mountainPath,
        color = color,
        style = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

// 4. Tulip / Flower for MACRO
private fun DrawScope.drawMacroIcon(color: Color, w: Float, h: Float) {
    // Flower petals
    val petalLeft = Path().apply {
        moveTo(w * 0.50f, h * 0.58f)
        cubicTo(w * 0.20f, h * 0.50f, w * 0.25f, h * 0.20f, w * 0.50f, h * 0.35f)
        close()
    }
    drawPath(petalLeft, color = color, style = Fill)

    val petalRight = Path().apply {
        moveTo(w * 0.50f, h * 0.58f)
        cubicTo(w * 0.80f, h * 0.50f, w * 0.75f, h * 0.20f, w * 0.50f, h * 0.35f)
        close()
    }
    drawPath(petalRight, color = color, style = Fill)

    val centerBud = Path().apply {
        moveTo(w * 0.50f, h * 0.18f)
        cubicTo(w * 0.42f, h * 0.28f, w * 0.42f, h * 0.50f, w * 0.50f, h * 0.58f)
        cubicTo(w * 0.58f, h * 0.50f, w * 0.58f, h * 0.28f, w * 0.50f, h * 0.18f)
        close()
    }
    drawPath(centerBud, color = color, style = Fill)

    // Stem
    drawLine(
        color = color,
        start = Offset(w * 0.50f, h * 0.58f),
        end = Offset(w * 0.50f, h * 0.85f),
        strokeWidth = w * 0.065f,
        cap = StrokeCap.Round
    )

    // Side leaves
    val leafLeft = Path().apply {
        moveTo(w * 0.50f, h * 0.75f)
        cubicTo(w * 0.32f, h * 0.70f, w * 0.30f, h * 0.60f, w * 0.35f, h * 0.58f)
    }
    drawPath(leafLeft, color = color, style = Stroke(width = w * 0.055f, cap = StrokeCap.Round))
}

// 5. Shutter Speed / Offset Circles for SLO-MO
private fun DrawScope.drawSloMoIcon(color: Color, w: Float, h: Float) {
    // Large center ring
    drawCircle(
        color = color,
        radius = w * 0.26f,
        center = Offset(w * 0.54f, h * 0.50f),
        style = Fill
    )
    // Motion trail dotted/dash circle behind
    drawCircle(
        color = color.copy(alpha = 0.5f),
        radius = w * 0.16f,
        center = Offset(w * 0.32f, h * 0.40f),
        style = Stroke(width = w * 0.06f)
    )
    // Motion speed lines
    for (i in 0 until 6) {
        val angle = i * (Math.PI / 3.0)
        val x1 = w * 0.54f + (w * 0.32f) * cos(angle).toFloat()
        val y1 = h * 0.50f + (w * 0.32f) * sin(angle).toFloat()
        val x2 = w * 0.54f + (w * 0.38f) * cos(angle).toFloat()
        val y2 = h * 0.50f + (w * 0.38f) * sin(angle).toFloat()
        drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    }
}

// 6. Clock Sweep for TIME-LAPSE
private fun DrawScope.drawTimeLapseIcon(color: Color, w: Float, h: Float) {
    val center = Offset(w * 0.50f, h * 0.52f)
    val r = w * 0.30f

    // Outer clock face with gap
    drawCircle(
        color = color,
        radius = r,
        center = center,
        style = Stroke(width = w * 0.07f)
    )

    // Sweep arc filled (fast quarter sector)
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 100f,
        useCenter = true,
        topLeft = Offset(center.x - r * 0.85f, center.y - r * 0.85f),
        size = Size(r * 1.7f, r * 1.7f)
    )

    // Top stopwatch button
    drawLine(
        color = color,
        start = Offset(w * 0.50f, h * 0.12f),
        end = Offset(w * 0.50f, h * 0.20f),
        strokeWidth = w * 0.07f,
        cap = StrokeCap.Round
    )
}

// 7. Video Frame & Play Indicator for VIEW VIDEO (DUAL-VIEW)
private fun DrawScope.drawDualVideoIcon(color: Color, w: Float, h: Float) {
    // Outer video monitor
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.18f, h * 0.24f),
        size = Size(w * 0.64f, h * 0.52f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.065f)
    )

    // Center play triangle
    val playPath = Path().apply {
        moveTo(w * 0.44f, h * 0.40f)
        lineTo(w * 0.62f, h * 0.50f)
        lineTo(w * 0.44f, h * 0.60f)
        close()
    }
    drawPath(playPath, color = color, style = Fill)
}

// 8. Waves & Water Droplet for UNDERWATER
private fun DrawScope.drawUnderwaterIcon(color: Color, w: Float, h: Float) {
    // Water droplet on top
    val dropPath = Path().apply {
        moveTo(w * 0.55f, h * 0.20f)
        cubicTo(w * 0.42f, h * 0.30f, w * 0.42f, h * 0.46f, w * 0.55f, h * 0.48f)
        cubicTo(w * 0.68f, h * 0.46f, w * 0.68f, h * 0.30f, w * 0.55f, h * 0.20f)
        close()
    }
    drawPath(dropPath, color = color, style = Fill)

    // Two wavy lines
    val wave1 = Path().apply {
        moveTo(w * 0.18f, h * 0.60f)
        cubicTo(w * 0.32f, h * 0.54f, w * 0.42f, h * 0.66f, w * 0.56f, h * 0.60f)
        cubicTo(w * 0.70f, h * 0.54f, w * 0.78f, h * 0.66f, w * 0.82f, h * 0.60f)
    }
    drawPath(wave1, color = color, style = Stroke(width = w * 0.065f, cap = StrokeCap.Round))

    val wave2 = Path().apply {
        moveTo(w * 0.18f, h * 0.75f)
        cubicTo(w * 0.32f, h * 0.69f, w * 0.42f, h * 0.81f, w * 0.56f, h * 0.75f)
        cubicTo(w * 0.70f, h * 0.69f, w * 0.78f, h * 0.81f, w * 0.82f, h * 0.75f)
    }
    drawPath(wave2, color = color, style = Stroke(width = w * 0.065f, cap = StrokeCap.Round))
}

// 9. Photo Frame with Star/Smiley for STICKER
private fun DrawScope.drawStickerIcon(color: Color, w: Float, h: Float) {
    // Slanted photo card
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.18f, h * 0.22f),
        size = Size(w * 0.64f, h * 0.54f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.065f)
    )

    // Smiley face inside
    drawCircle(color, radius = w * 0.04f, center = Offset(w * 0.40f, h * 0.44f))
    drawCircle(color, radius = w * 0.04f, center = Offset(w * 0.60f, h * 0.44f))

    val smile = Path().apply {
        moveTo(w * 0.38f, h * 0.56f)
        cubicTo(w * 0.45f, h * 0.66f, w * 0.55f, h * 0.66f, w * 0.62f, h * 0.56f)
    }
    drawPath(smile, color = color, style = Stroke(width = w * 0.05f, cap = StrokeCap.Round))
}

// 10. Corner Brackets & Sheet for DOC SCANNER
private fun DrawScope.drawDocScannerIcon(color: Color, w: Float, h: Float) {
    val bSize = w * 0.18f
    val stroke = w * 0.065f

    // Top Left bracket
    val tl = Path().apply {
        moveTo(w * 0.16f, h * 0.16f + bSize)
        lineTo(w * 0.16f, h * 0.16f)
        lineTo(w * 0.16f + bSize, h * 0.16f)
    }
    drawPath(tl, color, style = Stroke(width = stroke, cap = StrokeCap.Round))

    // Top Right bracket
    val tr = Path().apply {
        moveTo(w * 0.84f - bSize, h * 0.16f)
        lineTo(w * 0.84f, h * 0.16f)
        lineTo(w * 0.84f, h * 0.16f + bSize)
    }
    drawPath(tr, color, style = Stroke(width = stroke, cap = StrokeCap.Round))

    // Bottom Left bracket
    val bl = Path().apply {
        moveTo(w * 0.16f, h * 0.84f - bSize)
        lineTo(w * 0.16f, h * 0.84f)
        lineTo(w * 0.16f + bSize, h * 0.84f)
    }
    drawPath(bl, color, style = Stroke(width = stroke, cap = StrokeCap.Round))

    // Bottom Right bracket
    val br = Path().apply {
        moveTo(w * 0.84f - bSize, h * 0.84f)
        lineTo(w * 0.84f, h * 0.84f)
        lineTo(w * 0.84f, h * 0.84f - bSize)
    }
    drawPath(br, color, style = Stroke(width = stroke, cap = StrokeCap.Round))

    // Document sheet inside
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.32f, h * 0.28f),
        size = Size(w * 0.36f, h * 0.44f),
        cornerRadius = CornerRadius(w * 0.04f),
        style = Stroke(width = w * 0.05f)
    )
    // Document text lines
    drawLine(color, Offset(w * 0.38f, h * 0.40f), Offset(w * 0.58f, h * 0.40f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.38f, h * 0.50f), Offset(w * 0.62f, h * 0.50f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.38f, h * 0.60f), Offset(w * 0.52f, h * 0.60f), strokeWidth = w * 0.04f, cap = StrokeCap.Round)
}

// 11. Viewfinder Person for PORTRAIT
private fun DrawScope.drawPortraitIcon(color: Color, w: Float, h: Float) {
    val bSize = w * 0.16f
    val stroke = w * 0.06f

    // 4 Corner brackets
    drawPath(Path().apply {
        moveTo(w * 0.16f, h * 0.16f + bSize); lineTo(w * 0.16f, h * 0.16f); lineTo(w * 0.16f + bSize, h * 0.16f)
    }, color, style = Stroke(stroke, cap = StrokeCap.Round))

    drawPath(Path().apply {
        moveTo(w * 0.84f - bSize, h * 0.16f); lineTo(w * 0.84f, h * 0.16f); lineTo(w * 0.84f, h * 0.16f + bSize)
    }, color, style = Stroke(stroke, cap = StrokeCap.Round))

    drawPath(Path().apply {
        moveTo(w * 0.16f, h * 0.84f - bSize); lineTo(w * 0.16f, h * 0.84f); lineTo(w * 0.16f + bSize, h * 0.84f)
    }, color, style = Stroke(stroke, cap = StrokeCap.Round))

    drawPath(Path().apply {
        moveTo(w * 0.84f - bSize, h * 0.84f); lineTo(w * 0.84f, h * 0.84f); lineTo(w * 0.84f, h * 0.84f - bSize)
    }, color, style = Stroke(stroke, cap = StrokeCap.Round))

    // Head
    drawCircle(color, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.38f), style = Fill)

    // Shoulders
    val shoulders = Path().apply {
        moveTo(w * 0.30f, h * 0.72f)
        cubicTo(w * 0.32f, h * 0.56f, w * 0.68f, h * 0.56f, w * 0.70f, h * 0.72f)
        close()
    }
    drawPath(shoulders, color, style = Fill)
}

// 12. Camera with PRO Text for PRO
private fun DrawScope.drawProIcon(color: Color, w: Float, h: Float) {
    // Camera body
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.16f, h * 0.28f),
        size = Size(w * 0.68f, h * 0.48f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.065f)
    )
    // Camera top bump
    val topBump = Path().apply {
        moveTo(w * 0.34f, h * 0.28f)
        lineTo(w * 0.40f, h * 0.20f)
        lineTo(w * 0.60f, h * 0.20f)
        lineTo(w * 0.66f, h * 0.28f)
    }
    drawPath(topBump, color, style = Stroke(width = w * 0.055f, cap = StrokeCap.Round))

    // Center "PRO" emblem
    // Letter P
    drawLine(color, Offset(w * 0.32f, h * 0.40f), Offset(w * 0.32f, h * 0.64f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
    drawArc(color, startAngle = -90f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(w * 0.24f, h * 0.40f), size = Size(w * 0.20f, h * 0.14f),
        style = Stroke(width = w * 0.05f))

    // Letter R
    drawLine(color, Offset(w * 0.48f, h * 0.40f), Offset(w * 0.48f, h * 0.64f), strokeWidth = w * 0.055f, cap = StrokeCap.Round)
    drawArc(color, startAngle = -90f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(w * 0.40f, h * 0.40f), size = Size(w * 0.18f, h * 0.13f),
        style = Stroke(width = w * 0.05f))
    drawLine(color, Offset(w * 0.50f, h * 0.53f), Offset(w * 0.58f, h * 0.64f), strokeWidth = w * 0.05f, cap = StrokeCap.Round)

    // Letter O
    drawCircle(color, radius = w * 0.085f, center = Offset(w * 0.68f, h * 0.52f), style = Stroke(width = w * 0.05f))
}

// Additional helpers for standard PHOTO, VIDEO, HDR
private fun DrawScope.drawPhotoIcon(color: Color, w: Float, h: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.16f, h * 0.25f),
        size = Size(w * 0.68f, h * 0.50f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.065f)
    )
    drawCircle(color, radius = w * 0.13f, center = Offset(w * 0.50f, h * 0.50f), style = Stroke(width = w * 0.06f))
}

private fun DrawScope.drawVideoIcon(color: Color, w: Float, h: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.16f, h * 0.28f),
        size = Size(w * 0.48f, h * 0.44f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.065f)
    )
    val triangle = Path().apply {
        moveTo(w * 0.64f, h * 0.40f)
        lineTo(w * 0.84f, h * 0.28f)
        lineTo(w * 0.84f, h * 0.72f)
        lineTo(w * 0.64f, h * 0.60f)
        close()
    }
    drawPath(triangle, color, style = Fill)
}

private fun DrawScope.drawHdrIcon(color: Color, w: Float, h: Float) {
    drawCircle(color, radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.50f), style = Stroke(width = w * 0.065f))
    drawArc(color, startAngle = -90f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(w * 0.22f, h * 0.22f), size = Size(w * 0.56f, h * 0.56f))
}
