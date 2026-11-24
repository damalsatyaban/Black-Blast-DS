package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.TrayPiece
import com.example.ui.theme.TraySlotShadow

@Composable
fun PieceTray(
  tray: List<TrayPiece?>,
  activeDragIndex: Int?,
  isInteractionEnabled: Boolean,
  onDragStart: (index: Int, pointerRoot: Offset) -> Unit,
  onDragMove: (pointerRoot: Offset) -> Unit,
  onDragEnd: () -> Unit,
  onDragCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(112.dp)
        .padding(horizontal = 4.dp)
        .testTag("piece_tray"),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    for (slotIndex in 0 until 3) {
      val piece = tray.getOrNull(slotIndex)
      val isBeingDragged = activeDragIndex == slotIndex
      TraySlot(
        slotIndex = slotIndex,
        piece = piece,
        isBeingDragged = isBeingDragged,
        isInteractionEnabled = isInteractionEnabled,
        onDragStart = onDragStart,
        onDragMove = onDragMove,
        onDragEnd = onDragEnd,
        onDragCancel = onDragCancel,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
  }
}

@Composable
private fun TraySlot(
  slotIndex: Int,
  piece: TrayPiece?,
  isBeingDragged: Boolean,
  isInteractionEnabled: Boolean,
  onDragStart: (index: Int, pointerRoot: Offset) -> Unit,
  onDragMove: (pointerRoot: Offset) -> Unit,
  onDragEnd: () -> Unit,
  onDragCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var slotCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val currentEnabled by rememberUpdatedState(isInteractionEnabled)
  val currentPiece by rememberUpdatedState(piece)
  val currentOnStart by rememberUpdatedState(onDragStart)
  val currentOnMove by rememberUpdatedState(onDragMove)
  val currentOnEnd by rememberUpdatedState(onDragEnd)
  val currentOnCancel by rememberUpdatedState(onDragCancel)

  Box(
    modifier =
      modifier
        .onGloballyPositioned { slotCoords = it }
        .testTag("tray_slot_$slotIndex")
        .pointerInput(piece?.instanceId) {
          if (piece == null) return@pointerInput
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (!currentEnabled || currentPiece == null) return@awaitEachGesture
            val coords = slotCoords ?: return@awaitEachGesture
            down.consume()
            currentOnStart(slotIndex, coords.localToWindow(down.position))

            var finished = false
            while (!finished) {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
              if (change == null) {
                finished = true
                currentOnCancel()
              } else if (!change.pressed) {
                change.consume()
                finished = true
                currentOnEnd()
              } else {
                change.consume()
                val latestCoords = slotCoords
                if (latestCoords != null) {
                  currentOnMove(latestCoords.localToWindow(change.position))
                }
              }
            }
          }
        },
    contentAlignment = Alignment.Center,
  ) {
    if (piece != null && !isBeingDragged) {
      Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val maxDim = maxOf(piece.shape.rows, piece.shape.cols, 3)
        val cellSize = minOf(size.width / maxDim, size.height / maxDim).coerceAtMost(size.minDimension / 2.6f)
        val totalW = piece.shape.cols * cellSize
        val totalH = piece.shape.rows * cellSize
        val originX = (size.width - totalW) / 2f
        val originY = (size.height - totalH) / 2f
        val gemIdx = if (piece.gem != com.example.model.GemType.NONE) 0 else -1

        // Subtle drop shadow beneath tray piece
        for (offset in piece.shape.cells) {
          drawRect(
            color = TraySlotShadow,
            topLeft =
              Offset(
                originX + offset.col * cellSize + cellSize * 0.08f,
                originY + offset.row * cellSize + cellSize * 0.14f,
              ),
            size = androidx.compose.ui.geometry.Size(cellSize * 0.94f, cellSize * 0.94f),
          )
        }

        piece.shape.cells.forEachIndexed { idx, offset ->
          drawBeveledBlockCell(
            topLeft = Offset(originX + offset.col * cellSize, originY + offset.row * cellSize),
            cellSize = cellSize,
            color = piece.color,
            gem = if (idx == gemIdx) piece.gem else com.example.model.GemType.NONE,
          )
        }
      }
    } else if (isBeingDragged) {
      // Subtle empty slot indicator while dragging
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
          color = Color.White.copy(alpha = 0.06f),
          radius = size.minDimension * 0.24f,
          center = center,
        )
      }
    }
  }
}
