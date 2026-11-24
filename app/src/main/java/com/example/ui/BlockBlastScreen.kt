package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.game.PieceCatalog
import com.example.model.BOARD_SIZE
import com.example.model.GameMode
import com.example.model.GemType
import com.example.model.TrayPiece
import com.example.ui.components.GameBoard
import com.example.ui.components.GameOverOverlay
import com.example.ui.components.ModeSelectDialog
import com.example.ui.components.PieceTray
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StageClearOverlay
import com.example.ui.components.drawBeveledBlockCell
import com.example.ui.components.drawGemSymbol
import com.example.ui.theme.CrownGold
import com.example.ui.theme.HudMuted
import com.example.ui.theme.RoyalBlueBottom
import com.example.ui.theme.RoyalBlueMid
import com.example.ui.theme.RoyalBlueTop
import kotlin.math.roundToInt

@Composable
fun BlockBlastScreen(viewModel: GameViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val haptics = LocalHapticFeedback.current
  val density = LocalDensity.current

  var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var boardGridRect by remember { mutableStateOf(Rect.Zero) }

  // Active drag state in root coordinates
  var activeDragIndex by remember { mutableStateOf<Int?>(null) }
  var dragPointerRoot by remember { mutableStateOf<Offset?>(null) }

  val animatedScore by
    animateIntAsState(
      targetValue = uiState.score,
      animationSpec = tween(durationMillis = 240),
      label = "scoreAnim",
    )

  val fingerLiftPx = with(density) { 80.dp.toPx() }
  val activePiece: TrayPiece? = activeDragIndex?.let { uiState.tray.getOrNull(it) }

  val cellSizePx =
    if (boardGridRect.width > 0f) boardGridRect.width / BOARD_SIZE else 0f

  val floatingPieceCenterRoot: Offset? =
    if (activePiece != null && dragPointerRoot != null) {
      Offset(dragPointerRoot!!.x, dragPointerRoot!!.y - fingerLiftPx)
    } else null

  val hoverPlacement: Pair<Int, Int>? =
    if (activePiece != null && floatingPieceCenterRoot != null && cellSizePx > 0f) {
      val pieceTotalW = activePiece.shape.cols * cellSizePx
      val pieceTotalH = activePiece.shape.rows * cellSizePx
      val topLeftX = floatingPieceCenterRoot.x - pieceTotalW / 2f
      val topLeftY = floatingPieceCenterRoot.y - pieceTotalH / 2f
      val col = ((topLeftX - boardGridRect.left) / cellSizePx).roundToInt()
      val row = ((topLeftY - boardGridRect.top) / cellSizePx).roundToInt()
      row to col
    } else null

  val isValidHover =
    if (activePiece != null && hoverPlacement != null) {
      PieceCatalog.canPlace(
        uiState.board,
        activePiece.shape,
        hoverPlacement.first,
        hoverPlacement.second,
      )
    } else false

  val (previewClearRows, previewClearCols) =
    if (isValidHover && activePiece != null && hoverPlacement != null) {
      PieceCatalog.findPreviewClears(
        uiState.board,
        activePiece.shape,
        hoverPlacement.first,
        hoverPlacement.second,
      )
    } else {
      emptySet<Int>() to emptySet()
    }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(listOf(RoyalBlueTop, RoyalBlueMid, RoyalBlueBottom))
        )
        .onGloballyPositioned { rootCoords = it }
  ) {
    // Subtle checkerboard atmospheric texture behind the royal blue arena
    Canvas(modifier = Modifier.fillMaxSize()) {
      val tile = 44.dp.toPx()
      val cols = (size.width / tile).toInt() + 1
      val rows = (size.height / tile).toInt() + 1
      for (r in 0..rows) {
        for (c in 0..cols) {
          if ((r + c) % 2 == 0) {
            drawRect(
              color = Color.White.copy(alpha = 0.028f),
              topLeft = Offset(c * tile, r * tile),
              size = androidx.compose.ui.geometry.Size(tile, tile),
            )
          }
        }
      }
    }

    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.TopCenter,
    ) {
      val maxBoardWidth =
        minOf(maxWidth, (maxHeight * 0.56f).coerceAtLeast(240.dp)).coerceAtMost(520.dp)

      Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Top HUD: Crown + Best Score on Left, Mode Pill + Settings Gear on Right
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Best Score Crown Pill
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 4.dp).testTag("best_score_hud"),
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = stringResource(R.string.best_score_desc),
              tint = CrownGold,
              modifier = Modifier.size(30.dp),
            )
            Text(
              text =
                if (uiState.mode == GameMode.CLASSIC) uiState.bestScore.toString()
                else "Lv.${uiState.adventureLevel}",
              style =
                MaterialTheme.typography.headlineMedium.copy(
                  fontSize = 24.sp,
                  shadow = Shadow(Color(0x55000000), Offset(0f, 3f), 4f),
                ),
              color = CrownGold,
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            // Mode Switch Pill (Classic <-> Adventure)
            Row(
              modifier =
                Modifier.clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.14f))
                  .border(1.dp, Color.White.copy(alpha = 0.24f), CircleShape)
                  .clickable { viewModel.setModeSheetVisible(true) }
                  .minimumInteractiveComponentSize()
                  .padding(horizontal = 12.dp, vertical = 6.dp)
                  .testTag("mode_pill_button"),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector =
                  if (uiState.mode == GameMode.CLASSIC) Icons.Default.GridView
                  else Icons.Default.Explore,
                contentDescription = "Switch mode",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text =
                  if (uiState.mode == GameMode.CLASSIC) stringResource(R.string.mode_classic)
                  else stringResource(R.string.mode_adventure),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
              )
            }

            IconButton(
              onClick = { viewModel.setSettingsVisible(true) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_desc),
                tint = HudMuted.copy(alpha = 0.92f),
                modifier = Modifier.size(30.dp),
              )
            }
          }
        }

        // Score / Adventure Gem Targets Area (matches screenshots 1-5)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (uiState.mode == GameMode.ADVENTURE && uiState.adventureTargets.isNotEmpty()) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(22.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.testTag("adventure_targets_row"),
            ) {
              for ((gem, targetCount) in uiState.adventureTargets) {
                val collected = uiState.adventureCollected[gem] ?: 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Canvas(modifier = Modifier.size(34.dp)) {
                    drawGemSymbol(
                      center = Offset(size.width / 2f, size.height / 2f),
                      radius = size.minDimension * 0.46f,
                      gem = gem,
                    )
                  }
                  Text(
                    text = "${(targetCount - collected).coerceAtLeast(0)}",
                    style =
                      MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        shadow = Shadow(Color(0x55000000), Offset(0f, 3f), 4f),
                      ),
                    color = if (collected >= targetCount) Color(0xFF4ADE80) else Color.White,
                  )
                }
              }
            }
          } else {
            Text(
              text = animatedScore.toString(),
              style =
                MaterialTheme.typography.displayLarge.copy(
                  fontSize = 52.sp,
                  shadow = Shadow(Color(0x66000000), Offset(0f, 4f), 8f),
                ),
              color = Color.White,
              modifier = Modifier.testTag("current_score_text"),
            )
            if (uiState.comboStreak >= 2) {
              Text(
                text = "STREAK x${uiState.comboStreak}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = CrownGold,
                modifier =
                  Modifier.clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33000000))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
              )
            }
          }
        }

        // Center 8x8 Game Board
        GameBoard(
          board = uiState.board,
          hoverPiece = activePiece,
          hoverRow = hoverPlacement?.first,
          hoverCol = hoverPlacement?.second,
          isValidHover = isValidHover,
          previewClearedRows = previewClearRows,
          previewClearedCols = previewClearCols,
          clearingCells = uiState.clearingCells,
          clearingWaveId = uiState.clearingWaveId,
          floatingCallout = uiState.floatingCallout,
          onGridBoundsChanged = { boardGridRect = it },
          modifier = Modifier.size(maxBoardWidth),
        )

        // Bottom 3-Piece Tray
        PieceTray(
          tray = uiState.tray,
          activeDragIndex = activeDragIndex,
          isInteractionEnabled = !uiState.isGameOver && !uiState.isStageClear,
          onDragStart = { index, pointerRoot ->
            activeDragIndex = index
            dragPointerRoot = pointerRoot
            viewModel.onPiecePickedUp()
            if (uiState.hapticsEnabled) {
              haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          },
          onDragMove = { pointerRoot -> dragPointerRoot = pointerRoot },
          onDragEnd = {
            val idx = activeDragIndex
            val hover = hoverPlacement
            if (idx != null && hover != null && isValidHover) {
              val placed = viewModel.placePiece(idx, hover.first, hover.second)
              if (placed && uiState.hapticsEnabled) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              }
            }
            activeDragIndex = null
            dragPointerRoot = null
          },
          onDragCancel = {
            activeDragIndex = null
            dragPointerRoot = null
          },
          modifier = Modifier.widthIn(max = maxBoardWidth + 24.dp),
        )

        Spacer(modifier = Modifier.height(2.dp))
      }
    }

    // Floating dragged piece rendered on top of everything in exact root coordinates
    if (activePiece != null && floatingPieceCenterRoot != null && cellSizePx > 0f && rootCoords != null) {
      val localCenter = rootCoords!!.windowToLocal(floatingPieceCenterRoot)
      Canvas(modifier = Modifier.fillMaxSize()) {
        val totalW = activePiece.shape.cols * cellSizePx
        val totalH = activePiece.shape.rows * cellSizePx
        val startX = localCenter.x - totalW / 2f
        val startY = localCenter.y - totalH / 2f
        val gemIdx = if (activePiece.gem != GemType.NONE) 0 else -1

        // Elevated shadow underneath floating piece
        for (offset in activePiece.shape.cells) {
          drawRect(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft =
              Offset(
                startX + offset.col * cellSizePx + cellSizePx * 0.1f,
                startY + offset.row * cellSizePx + cellSizePx * 0.18f,
              ),
            size = androidx.compose.ui.geometry.Size(cellSizePx, cellSizePx),
          )
        }

        activePiece.shape.cells.forEachIndexed { idx, offset ->
          drawBeveledBlockCell(
            topLeft =
              Offset(
                startX + offset.col * cellSizePx,
                startY + offset.row * cellSizePx,
              ),
            cellSize = cellSizePx,
            color = activePiece.color,
            gem = if (idx == gemIdx) activePiece.gem else GemType.NONE,
            scale = 1.04f,
          )
        }
      }
    }

    // Settings Dialog
    if (uiState.showSettingsDialog) {
      SettingsDialog(
        soundEnabled = uiState.soundEnabled,
        hapticsEnabled = uiState.hapticsEnabled,
        mode = uiState.mode,
        onToggleSound = viewModel::setSoundEnabled,
        onToggleHaptics = viewModel::setHapticsEnabled,
        onRestart = {
          if (uiState.mode == GameMode.CLASSIC) viewModel.startClassicGame()
          else viewModel.startAdventureLevel(uiState.adventureLevel)
        },
        onOpenModes = {
          viewModel.setSettingsVisible(false)
          viewModel.setModeSheetVisible(true)
        },
        onDismiss = { viewModel.setSettingsVisible(false) },
      )
    }

    // Mode & Stage Selection Dialog
    if (uiState.showModeSheet) {
      ModeSelectDialog(
        currentMode = uiState.mode,
        currentAdventureLevel = uiState.adventureLevel,
        unlockedLevel = uiState.adventureUnlockedLevel,
        onSelectClassic = { viewModel.startClassicGame() },
        onSelectAdventureLevel = { lvl -> viewModel.startAdventureLevel(lvl) },
        onDismiss = { viewModel.setModeSheetVisible(false) },
      )
    }

    // Game Over Overlay
    AnimatedVisibility(
      visible = uiState.isGameOver,
      enter = fadeIn(tween(260)),
      exit = fadeOut(tween(180)),
    ) {
      GameOverOverlay(
        score = uiState.score,
        bestScore = uiState.bestScore,
        isNewBest = uiState.isNewBest,
        canRevive = !uiState.reviveUsed,
        onRevive = viewModel::reviveByClearingCenter,
        onPlayAgain = {
          if (uiState.mode == GameMode.CLASSIC) viewModel.startClassicGame()
          else viewModel.startAdventureLevel(uiState.adventureLevel)
        },
      )
    }

    // Stage Clear Overlay
    AnimatedVisibility(
      visible = uiState.isStageClear,
      enter = fadeIn(tween(260)),
      exit = fadeOut(tween(180)),
    ) {
      StageClearOverlay(
        levelNumber = uiState.adventureLevel,
        levelTitle = uiState.adventureLevelTitle,
        score = uiState.score,
        targets = uiState.adventureTargets,
        onNextStage = { viewModel.startAdventureLevel(uiState.adventureLevel + 1) },
        onReplay = { viewModel.startAdventureLevel(uiState.adventureLevel) },
      )
    }
  }
}
