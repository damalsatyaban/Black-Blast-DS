package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BOARD_SIZE
import com.example.model.BoardCell
import com.example.model.ClearingCellEffect
import com.example.model.FloatingCallout
import com.example.model.GemType
import com.example.model.TrayPiece
import com.example.ui.theme.BoardBackground
import com.example.ui.theme.BoardCellEmpty
import com.example.ui.theme.BoardFrameBorder
import com.example.ui.theme.BoardFrameOuter
import com.example.ui.theme.BoardGridLine
import com.example.ui.theme.CrownGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GameBoard(
  board: List<List<BoardCell>>,
  hoverPiece: TrayPiece?,
  hoverRow: Int?,
  hoverCol: Int?,
  isValidHover: Boolean,
  previewClearedRows: Set<Int>,
  previewClearedCols: Set<Int>,
  clearingCells: List<ClearingCellEffect>,
  clearingWaveId: Long,
  floatingCallout: FloatingCallout?,
  onGridBoundsChanged: (Rect) -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "boardPulse")
  val ghostPulse by
    infiniteTransition.animateFloat(
      initialValue = 0.72f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
      label = "ghostPulse",
    )

  val clearProgress = remember(clearingWaveId) { Animatable(0f) }
  LaunchedEffect(clearingWaveId) {
    if (clearingWaveId > 0L) {
      clearProgress.snapTo(0f)
      clearProgress.animateTo(1f, animationSpec = tween(560, easing = FastOutSlowInEasing))
    }
  }

  val calloutProgress = remember(floatingCallout?.id) { Animatable(0f) }
  LaunchedEffect(floatingCallout?.id) {
    if (floatingCallout != null) {
      calloutProgress.snapTo(0f)
      calloutProgress.animateTo(1f, animationSpec = tween(660, easing = FastOutSlowInEasing))
    }
  }

  Box(
    modifier =
      modifier
        .aspectRatio(1f)
        .shadow(14.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black, spotColor = Color.Black)
        .clip(RoundedCornerShape(16.dp))
        .background(BoardFrameOuter)
        .border(2.5.dp, BoardFrameBorder.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
        .padding(7.dp)
        .testTag("game_board")
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .clip(RoundedCornerShape(10.dp))
          .background(BoardBackground)
          .onGloballyPositioned { coords -> onGridBoundsChanged(coords.boundsInWindow()) }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / BOARD_SIZE

        // 1. Empty recessed 8x8 grid cells
        for (r in 0 until BOARD_SIZE) {
          for (c in 0 until BOARD_SIZE) {
            val pad = cellSize * 0.035f
            val topLeft = Offset(c * cellSize + pad, r * cellSize + pad)
            val s = cellSize - pad * 2f
            drawRoundRect(
              color = BoardCellEmpty,
              topLeft = topLeft,
              size = Size(s, s),
              cornerRadius = CornerRadius(cellSize * 0.08f, cellSize * 0.08f),
            )
            drawRoundRect(
              color = BoardGridLine.copy(alpha = 0.5f),
              topLeft = topLeft,
              size = Size(s, s),
              cornerRadius = CornerRadius(cellSize * 0.08f, cellSize * 0.08f),
              style = Stroke(width = 1.2f),
            )
          }
        }

        // 2. Highlight rows/cols that will clear if hovered piece is dropped
        if (isValidHover && hoverPiece != null && (previewClearedRows.isNotEmpty() || previewClearedCols.isNotEmpty())) {
          for (r in previewClearedRows) {
            drawRect(
              brush =
                Brush.horizontalGradient(
                  colors =
                    listOf(
                      hoverPiece.color.palette.topHighlight.copy(alpha = 0.25f * ghostPulse),
                      Color.White.copy(alpha = 0.34f * ghostPulse),
                      hoverPiece.color.palette.topHighlight.copy(alpha = 0.25f * ghostPulse),
                    )
                ),
              topLeft = Offset(0f, r * cellSize),
              size = Size(size.width, cellSize),
            )
          }
          for (c in previewClearedCols) {
            drawRect(
              brush =
                Brush.verticalGradient(
                  colors =
                    listOf(
                      hoverPiece.color.palette.topHighlight.copy(alpha = 0.25f * ghostPulse),
                      Color.White.copy(alpha = 0.34f * ghostPulse),
                      hoverPiece.color.palette.topHighlight.copy(alpha = 0.25f * ghostPulse),
                    )
                ),
              topLeft = Offset(c * cellSize, 0f),
              size = Size(cellSize, size.height),
            )
          }
        }

        // 3. Placed blocks on board
        for (r in 0 until BOARD_SIZE) {
          for (c in 0 until BOARD_SIZE) {
            val cell = board[r][c]
            if (cell.occupied && cell.color != null) {
              val willClear = r in previewClearedRows || c in previewClearedCols
              val displayColor =
                if (willClear && isValidHover && hoverPiece != null) hoverPiece.color else cell.color
              drawBeveledBlockCell(
                topLeft = Offset(c * cellSize, r * cellSize),
                cellSize = cellSize,
                color = displayColor,
                gem = cell.gem,
                alpha = 1f,
              )
              if (willClear && isValidHover) {
                drawRect(
                  color = Color.White.copy(alpha = 0.22f * ghostPulse),
                  topLeft = Offset(c * cellSize, r * cellSize),
                  size = Size(cellSize, cellSize),
                )
              }
            }
          }
        }

        // 4. Ghost preview cells where dragged piece will snap
        if (isValidHover && hoverPiece != null && hoverRow != null && hoverCol != null) {
          for (offset in hoverPiece.shape.cells) {
            val r = hoverRow + offset.row
            val c = hoverCol + offset.col
            if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) {
              drawGhostBlockCell(
                topLeft = Offset(c * cellSize, r * cellSize),
                cellSize = cellSize,
                color = hoverPiece.color,
                pulseAlpha = ghostPulse,
              )
            }
          }
        }

        // 5. Line clear particle burst & swirling star explosions
        if (clearingCells.isNotEmpty()) {
          val p = clearProgress.value
          val alpha = (1f - p).coerceIn(0f, 1f)
          val shrink = (1f - p * 0.72f).coerceIn(0.15f, 1f)

          for (effect in clearingCells) {
            val center =
              Offset((effect.col + 0.5f) * cellSize, (effect.row + 0.5f) * cellSize)

            // Shrinking glowing block flash
            drawBeveledBlockCell(
              topLeft = Offset(effect.col * cellSize, effect.row * cellSize),
              cellSize = cellSize,
              color = effect.color,
              gem = effect.gem,
              alpha = alpha * 0.85f,
              scale = shrink,
            )

            // Radial burst sparks
            val sparkCount = if (effect.gem != GemType.NONE) 6 else 3
            for (i in 0 until sparkCount) {
              val angle = (2.0 * PI * i / sparkCount) + p * 2.2
              val dist = cellSize * (0.35f + p * 1.15f)
              val sx = center.x + (dist * cos(angle)).toFloat()
              val sy = center.y + (dist * sin(angle)).toFloat()
              if (effect.gem != GemType.NONE) {
                drawGemSymbol(
                  center = Offset(sx, sy),
                  radius = cellSize * 0.22f * alpha,
                  gem = effect.gem,
                  alpha = alpha,
                )
              } else {
                drawCircle(
                  color = effect.color.palette.topHighlight.copy(alpha = alpha),
                  radius = cellSize * 0.11f * alpha,
                  center = Offset(sx, sy),
                )
              }
            }
          }
        }
      }

      // 6. Floating Combo / Score Callout
      if (floatingCallout != null) {
        val p = calloutProgress.value
        val alpha = if (p < 0.7f) 1f else ((1f - p) / 0.3f).coerceIn(0f, 1f)
        val riseDp = (-36f * p).dp
        Box(
          modifier =
            Modifier.fillMaxSize().offset {
              IntOffset(x = 0, y = riseDp.roundToPx())
            },
          contentAlignment = Alignment.Center,
        ) {
          androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = floatingCallout.text,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
              ),
              color = CrownGold.copy(alpha = alpha),
            )
            if (floatingCallout.subtext != null) {
              Text(
                text = floatingCallout.subtext,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = Color.White.copy(alpha = alpha),
              )
            }
          }
        }
      }
    }
  }
}
