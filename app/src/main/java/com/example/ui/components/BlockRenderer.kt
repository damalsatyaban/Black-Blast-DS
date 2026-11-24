package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.BlockColor
import com.example.model.GemType
import com.example.ui.theme.BlockPalette
import com.example.ui.theme.CrownGold
import com.example.ui.theme.CyanBlockPalette
import com.example.ui.theme.GemTilePalette
import com.example.ui.theme.RedBlockPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawBeveledBlockCell(
  topLeft: Offset,
  cellSize: Float,
  color: BlockColor,
  gem: GemType = GemType.NONE,
  alpha: Float = 1f,
  scale: Float = 1f,
) {
  val palette: BlockPalette = if (gem != GemType.NONE) GemTilePalette else color.palette
  val actualSize = cellSize * scale
  val inset = (cellSize - actualSize) / 2f
  val pad = actualSize * 0.025f
  val x0 = topLeft.x + inset + pad
  val y0 = topLeft.y + inset + pad
  val s = actualSize - pad * 2f
  if (s <= 1f) return

  val bevel = s * 0.165f
  val x1 = x0 + s
  val y1 = y0 + s
  val ix0 = x0 + bevel
  val iy0 = y0 + bevel
  val ix1 = x1 - bevel
  val iy1 = y1 - bevel

  // Top bevel trapezoid
  val topPath =
    Path().apply {
      moveTo(x0, y0)
      lineTo(x1, y0)
      lineTo(ix1, iy0)
      lineTo(ix0, iy0)
      close()
    }
  drawPath(topPath, color = palette.topHighlight.copy(alpha = alpha))

  // Left bevel trapezoid
  val leftPath =
    Path().apply {
      moveTo(x0, y0)
      lineTo(ix0, iy0)
      lineTo(ix0, iy1)
      lineTo(x0, y1)
      close()
    }
  drawPath(leftPath, color = palette.leftHighlight.copy(alpha = alpha))

  // Right bevel trapezoid
  val rightPath =
    Path().apply {
      moveTo(x1, y0)
      lineTo(x1, y1)
      lineTo(ix1, iy1)
      lineTo(ix1, iy0)
      close()
    }
  drawPath(rightPath, color = palette.rightShadow.copy(alpha = alpha))

  // Bottom bevel trapezoid
  val bottomPath =
    Path().apply {
      moveTo(x0, y1)
      lineTo(ix0, iy1)
      lineTo(ix1, iy1)
      lineTo(x1, y1)
      close()
    }
  drawPath(bottomPath, color = palette.bottomShadow.copy(alpha = alpha))

  // Inner raised face with subtle glossy top-left sheen
  drawRect(
    brush =
      Brush.linearGradient(
        colors = listOf(palette.leftHighlight.copy(alpha = 0.28f * alpha), Color.Transparent),
        start = Offset(ix0, iy0),
        end = Offset(ix1, iy1),
      ),
    topLeft = Offset(ix0, iy0),
    size = Size(ix1 - ix0, iy1 - iy0),
  )
  drawRect(
    color = palette.core.copy(alpha = alpha),
    topLeft = Offset(ix0, iy0),
    size = Size(ix1 - ix0, iy1 - iy0),
  )
  // Glossy sheen overlay on upper corner
  val sheenPath =
    Path().apply {
      moveTo(ix0, iy0)
      lineTo(ix1, iy0)
      lineTo(ix0, iy0 + (iy1 - iy0) * 0.45f)
      close()
    }
  drawPath(sheenPath, color = Color.White.copy(alpha = 0.14f * alpha))

  // Crisp outer edge border
  drawRoundRect(
    color = palette.outline.copy(alpha = 0.72f * alpha),
    topLeft = Offset(x0, y0),
    size = Size(s, s),
    cornerRadius = CornerRadius(s * 0.06f, s * 0.06f),
    style = Stroke(width = (s * 0.032f).coerceAtLeast(1f)),
  )

  if (gem != GemType.NONE) {
    val center = Offset((x0 + x1) / 2f, (y0 + y1) / 2f)
    drawGemSymbol(center = center, radius = s * 0.29f, gem = gem, alpha = alpha)
  }
}

fun DrawScope.drawGhostBlockCell(
  topLeft: Offset,
  cellSize: Float,
  color: BlockColor,
  pulseAlpha: Float = 0.85f,
) {
  val pad = cellSize * 0.055f
  val s = cellSize - pad * 2f
  val pos = Offset(topLeft.x + pad, topLeft.y + pad)
  val radius = CornerRadius(cellSize * 0.1f, cellSize * 0.1f)

  drawRoundRect(
    color = color.palette.core.copy(alpha = 0.22f * pulseAlpha),
    topLeft = pos,
    size = Size(s, s),
    cornerRadius = radius,
  )
  drawRoundRect(
    color = color.palette.topHighlight.copy(alpha = 0.92f * pulseAlpha),
    topLeft = pos,
    size = Size(s, s),
    cornerRadius = radius,
    style = Stroke(width = (cellSize * 0.055f).coerceAtLeast(2f)),
  )
}

fun DrawScope.drawGemSymbol(
  center: Offset,
  radius: Float,
  gem: GemType,
  alpha: Float = 1f,
) {
  when (gem) {
    GemType.GOLD_STAR -> {
      val path = buildStarPath(center, outerRadius = radius, innerRadius = radius * 0.46f, points = 5)
      drawPath(path, color = Color(0xFFB36B00).copy(alpha = alpha), style = Stroke(width = radius * 0.16f))
      drawPath(
        path,
        brush =
          Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF08A).copy(alpha = alpha), CrownGold.copy(alpha = alpha)),
            startY = center.y - radius,
            endY = center.y + radius,
          ),
      )
    }
    GemType.RED_HEX -> {
      val path = buildStarPath(center, outerRadius = radius, innerRadius = radius * 0.56f, points = 6)
      drawPath(
        path,
        color = RedBlockPalette.bottomShadow.copy(alpha = alpha),
        style = Stroke(width = radius * 0.16f),
      )
      drawPath(
        path,
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                RedBlockPalette.topHighlight.copy(alpha = alpha),
                RedBlockPalette.core.copy(alpha = alpha),
              ),
            startY = center.y - radius,
            endY = center.y + radius,
          ),
      )
    }
    GemType.CYAN_DIAMOND -> {
      val path =
        Path().apply {
          moveTo(center.x, center.y - radius)
          lineTo(center.x + radius * 0.85f, center.y)
          lineTo(center.x, center.y + radius)
          lineTo(center.x - radius * 0.85f, center.y)
          close()
        }
      drawPath(
        path,
        color = CyanBlockPalette.bottomShadow.copy(alpha = alpha),
        style = Stroke(width = radius * 0.16f),
      )
      drawPath(
        path,
        brush =
          Brush.verticalGradient(
            colors =
              listOf(
                CyanBlockPalette.topHighlight.copy(alpha = alpha),
                CyanBlockPalette.core.copy(alpha = alpha),
              ),
            startY = center.y - radius,
            endY = center.y + radius,
          ),
      )
    }
    GemType.NONE -> Unit
  }
}

private fun buildStarPath(
  center: Offset,
  outerRadius: Float,
  innerRadius: Float,
  points: Int,
): Path {
  val path = Path()
  val totalVertices = points * 2
  val startAngle = -PI / 2.0
  for (i in 0 until totalVertices) {
    val r = if (i % 2 == 0) outerRadius else innerRadius
    val angle = startAngle + (PI * i) / points
    val x = center.x + (r * cos(angle)).toFloat()
    val y = center.y + (r * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  return path
}
