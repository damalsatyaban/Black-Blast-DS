package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClassicSnapshot
import com.example.data.GamePreferencesRepository
import com.example.game.AdventureLevels
import com.example.game.PieceCatalog
import com.example.game.SoundEffects
import com.example.model.BOARD_SIZE
import com.example.model.BlockColor
import com.example.model.BoardCell
import com.example.model.ClearingCellEffect
import com.example.model.FloatingCallout
import com.example.model.GameMode
import com.example.model.GemType
import com.example.model.TrayPiece
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
  val mode: GameMode = GameMode.CLASSIC,
  val board: List<List<BoardCell>> = PieceCatalog.emptyBoard(),
  val tray: List<TrayPiece?> = emptyList(),
  val score: Int = 0,
  val bestScore: Int = 0,
  val isNewBest: Boolean = false,
  val comboStreak: Int = 0,
  val movesWithoutClear: Int = 0,
  val adventureLevel: Int = 1,
  val adventureUnlockedLevel: Int = 1,
  val adventureLevelTitle: String = "",
  val adventureTargets: Map<GemType, Int> = emptyMap(),
  val adventureCollected: Map<GemType, Int> = emptyMap(),
  val clearingCells: List<ClearingCellEffect> = emptyList(),
  val clearingWaveId: Long = 0L,
  val floatingCallout: FloatingCallout? = null,
  val isGameOver: Boolean = false,
  val isStageClear: Boolean = false,
  val reviveUsed: Boolean = false,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val showSettingsDialog: Boolean = false,
  val showModeSheet: Boolean = false,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = GamePreferencesRepository(application.applicationContext)
  private val soundEffects = SoundEffects()

  private var nextPieceInstanceId = 1L
  private var nextEffectId = 1L

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      val prefs = repository.preferencesFlow.first()
      val snapshot = prefs.savedClassicSnapshot
      if (snapshot != null && snapshot.tray.any { it != null }) {
        val maxId = snapshot.tray.filterNotNull().maxOfOrNull { it.instanceId } ?: 0L
        nextPieceInstanceId = maxOf(nextPieceInstanceId, maxId + 1L)
        val hasMove =
          snapshot.tray.filterNotNull().any { PieceCatalog.canPlaceAnywhere(snapshot.board, it.shape) }
        _uiState.update {
          it.copy(
            mode = GameMode.CLASSIC,
            board = snapshot.board,
            tray = snapshot.tray,
            score = snapshot.score,
            bestScore = prefs.bestScore,
            comboStreak = snapshot.comboStreak,
            movesWithoutClear = snapshot.movesWithoutClear,
            reviveUsed = snapshot.reviveUsed,
            adventureUnlockedLevel = prefs.adventureUnlockedLevel,
            soundEnabled = prefs.soundEnabled,
            hapticsEnabled = prefs.hapticsEnabled,
            isGameOver = !hasMove,
          )
        }
      } else {
        val emptyBoard = PieceCatalog.emptyBoard()
        val initialTray = PieceCatalog.generateSmartTray(emptyBoard, ::allocatePieceId)
        _uiState.update {
          it.copy(
            mode = GameMode.CLASSIC,
            board = emptyBoard,
            tray = initialTray,
            bestScore = prefs.bestScore,
            adventureUnlockedLevel = prefs.adventureUnlockedLevel,
            soundEnabled = prefs.soundEnabled,
            hapticsEnabled = prefs.hapticsEnabled,
          )
        }
      }

      repository.preferencesFlow.collect { latest ->
        _uiState.update { state ->
          state.copy(
            bestScore = maxOf(state.bestScore, latest.bestScore),
            adventureUnlockedLevel = maxOf(state.adventureUnlockedLevel, latest.adventureUnlockedLevel),
            soundEnabled = latest.soundEnabled,
            hapticsEnabled = latest.hapticsEnabled,
          )
        }
      }
    }
  }

  private fun allocatePieceId(): Long = nextPieceInstanceId++

  fun onPiecePickedUp() {
    if (_uiState.value.soundEnabled) soundEffects.playPickup()
  }

  fun placePiece(trayIndex: Int, anchorRow: Int, anchorCol: Int): Boolean {
    val state = _uiState.value
    if (state.isGameOver || state.isStageClear) return false
    val piece = state.tray.getOrNull(trayIndex) ?: return false
    if (!PieceCatalog.canPlace(state.board, piece.shape, anchorRow, anchorCol)) return false

    val outcome = PieceCatalog.placeAndClear(state.board, piece, anchorRow, anchorCol)
    val linesCleared = outcome.clearedRows.size + outcome.clearedCols.size

    val newCombo: Int
    val newMovesWithoutClear: Int
    if (linesCleared > 0) {
      newCombo = state.comboStreak + 1
      newMovesWithoutClear = 0
    } else {
      val nextMiss = state.movesWithoutClear + 1
      if (nextMiss >= 3) {
        newCombo = 0
        newMovesWithoutClear = 0
      } else {
        newCombo = state.comboStreak
        newMovesWithoutClear = nextMiss
      }
    }

    val placePoints = piece.shape.cells.size
    val clearPoints =
      if (linesCleared > 0) {
        val lineMultiplier =
          when (linesCleared) {
            1 -> 10
            2 -> 30
            3 -> 60
            4 -> 100
            else -> linesCleared * 35
          }
        lineMultiplier * maxOf(1, newCombo)
      } else 0

    val totalGained = placePoints + clearPoints
    val newScore = state.score + totalGained
    val newBest = maxOf(state.bestScore, newScore)
    val hitNewBest = state.mode == GameMode.CLASSIC && newScore > state.bestScore && state.bestScore > 0

    // Update Adventure gem progress
    val updatedCollected = state.adventureCollected.toMutableMap()
    if (state.mode == GameMode.ADVENTURE) {
      for ((gem, count) in outcome.collectedGems) {
        val target = state.adventureTargets[gem] ?: 0
        val current = updatedCollected[gem] ?: 0
        updatedCollected[gem] = minOf(target, current + count)
      }
    }

    val stageCleared =
      state.mode == GameMode.ADVENTURE &&
        state.adventureTargets.isNotEmpty() &&
        state.adventureTargets.all { (gem, target) -> (updatedCollected[gem] ?: 0) >= target }

    // Update tray
    val updatedTray = state.tray.toMutableList()
    updatedTray[trayIndex] = null
    val finalBoard = outcome.boardAfterClear

    val finalTray =
      if (updatedTray.all { it == null } && !stageCleared) {
        val neededGems =
          if (state.mode == GameMode.ADVENTURE) {
            computeMissingGemsOnBoard(finalBoard, state.adventureTargets, updatedCollected)
          } else emptyMap()
        PieceCatalog.generateSmartTray(finalBoard, ::allocatePieceId, neededGems)
      } else {
        updatedTray.toList()
      }

    val hasAnyMove =
      stageCleared ||
        finalTray.filterNotNull().any { PieceCatalog.canPlaceAnywhere(finalBoard, it.shape) }

    // Audio
    if (state.soundEnabled) {
      if (stageCleared) {
        soundEffects.playStageClear()
      } else if (linesCleared > 0) {
        soundEffects.playClear(newCombo)
      } else {
        soundEffects.playPlace()
      }
      if (!hasAnyMove && !stageCleared) {
        soundEffects.playGameOver()
      }
    }

    // Visual effects for clearing cells & callout
    val clearEffects =
      outcome.clearedCellPositions.map { (r, c) ->
        val cell = outcome.boardAfterPlace[r][c]
        ClearingCellEffect(
          row = r,
          col = c,
          color = cell.color ?: piece.color,
          gem = cell.gem,
        )
      }

    val callout =
      if (linesCleared > 0) {
        val headline =
          when {
            linesCleared >= 4 -> "UNBELIEVABLE!"
            linesCleared == 3 -> "AMAZING!"
            newCombo >= 3 -> "COMBO x$newCombo!"
            linesCleared == 2 -> "GREAT!"
            newCombo == 2 -> "COMBO x2!"
            else -> "+$clearPoints"
          }
        val sub = if (headline.startsWith("+")) null else "+$totalGained"
        val avgRow =
          outcome.clearedCellPositions.map { it.first }.average().toFloat().coerceIn(1f, 6f)
        val avgCol =
          outcome.clearedCellPositions.map { it.second }.average().toFloat().coerceIn(1.5f, 5.5f)
        FloatingCallout(
          id = nextEffectId++,
          text = headline,
          subtext = sub,
          anchorRow = avgRow,
          anchorCol = avgCol,
          color = piece.color,
        )
      } else null

    val waveId = if (clearEffects.isNotEmpty()) nextEffectId++ else state.clearingWaveId

    _uiState.update {
      it.copy(
        board = finalBoard,
        tray = finalTray,
        score = newScore,
        bestScore = newBest,
        isNewBest = it.isNewBest || hitNewBest,
        comboStreak = newCombo,
        movesWithoutClear = newMovesWithoutClear,
        adventureCollected = updatedCollected,
        clearingCells = clearEffects,
        clearingWaveId = waveId,
        floatingCallout = callout,
        isStageClear = stageCleared,
        isGameOver = !hasAnyMove && !stageCleared,
      )
    }

    // Clear transient particles after animation duration
    if (clearEffects.isNotEmpty() || callout != null) {
      val currentWave = waveId
      val currentCalloutId = callout?.id
      viewModelScope.launch {
        delay(700L)
        _uiState.update { current ->
          current.copy(
            clearingCells =
              if (current.clearingWaveId == currentWave) emptyList() else current.clearingCells,
            floatingCallout =
              if (current.floatingCallout?.id == currentCalloutId) null else current.floatingCallout,
          )
        }
      }
    }

    // Persist progress
    viewModelScope.launch {
      if (state.mode == GameMode.CLASSIC) {
        if (newScore >= newBest) repository.saveBestScore(newBest)
        if (!hasAnyMove) {
          repository.saveClassicSnapshot(null)
        } else {
          repository.saveClassicSnapshot(
            ClassicSnapshot(
              board = finalBoard,
              score = newScore,
              comboStreak = newCombo,
              movesWithoutClear = newMovesWithoutClear,
              tray = finalTray,
              reviveUsed = state.reviveUsed,
            )
          )
        }
      } else if (stageCleared) {
        repository.saveAdventureLevel(state.adventureLevel + 1)
      }
    }

    return true
  }

  private fun computeMissingGemsOnBoard(
    board: List<List<BoardCell>>,
    targets: Map<GemType, Int>,
    collected: Map<GemType, Int>,
  ): Map<GemType, Int> {
    val onBoard = mutableMapOf<GemType, Int>()
    for (row in board) {
      for (cell in row) {
        if (cell.occupied && cell.gem != GemType.NONE) {
          onBoard[cell.gem] = (onBoard[cell.gem] ?: 0) + 1
        }
      }
    }
    val missing = mutableMapOf<GemType, Int>()
    for ((gem, target) in targets) {
      val remainingNeeded = (target - (collected[gem] ?: 0)).coerceAtLeast(0)
      val boardCount = onBoard[gem] ?: 0
      if (remainingNeeded > boardCount) {
        missing[gem] = remainingNeeded - boardCount
      }
    }
    return missing
  }

  fun startClassicGame() {
    val emptyBoard = PieceCatalog.emptyBoard()
    val tray = PieceCatalog.generateSmartTray(emptyBoard, ::allocatePieceId)
    _uiState.update {
      it.copy(
        mode = GameMode.CLASSIC,
        board = emptyBoard,
        tray = tray,
        score = 0,
        isNewBest = false,
        comboStreak = 0,
        movesWithoutClear = 0,
        clearingCells = emptyList(),
        floatingCallout = null,
        isGameOver = false,
        isStageClear = false,
        reviveUsed = false,
        showSettingsDialog = false,
        showModeSheet = false,
      )
    }
    viewModelScope.launch { repository.saveClassicSnapshot(null) }
  }

  fun startAdventureLevel(levelNumber: Int) {
    val level = AdventureLevels.getLevel(levelNumber)
    val initialBoard = level.initialBoard
    val tray = PieceCatalog.generateSmartTray(initialBoard, ::allocatePieceId)
    _uiState.update {
      it.copy(
        mode = GameMode.ADVENTURE,
        board = initialBoard,
        tray = tray,
        score = 0,
        isNewBest = false,
        comboStreak = 0,
        movesWithoutClear = 0,
        adventureLevel = level.levelNumber,
        adventureLevelTitle = level.title,
        adventureTargets = level.targets,
        adventureCollected = level.targets.keys.associateWith { 0 },
        clearingCells = emptyList(),
        floatingCallout = null,
        isGameOver = false,
        isStageClear = false,
        reviveUsed = false,
        showSettingsDialog = false,
        showModeSheet = false,
      )
    }
  }

  fun reviveByClearingCenter() {
    val state = _uiState.value
    if (state.reviveUsed || !state.isGameOver) return

    val clearedList = mutableListOf<ClearingCellEffect>()
    val mutableBoard = state.board.map { it.toMutableList() }
    for (r in 2..5) {
      for (c in 2..5) {
        val cell = mutableBoard[r][c]
        if (cell.occupied) {
          clearedList.add(ClearingCellEffect(r, c, cell.color ?: BlockColor.CYAN, cell.gem))
          // Preserve gems in Adventure mode? Better to collect or keep board playable; let's collect!
          mutableBoard[r][c] = BoardCell()
        }
      }
    }
    val newBoard = mutableBoard.map { it.toList() }
    val trayHasMove =
      state.tray.filterNotNull().any { PieceCatalog.canPlaceAnywhere(newBoard, it.shape) }
    val finalTray =
      if (trayHasMove) state.tray
      else PieceCatalog.generateSmartTray(newBoard, ::allocatePieceId)

    val waveId = nextEffectId++
    if (state.soundEnabled) soundEffects.playClear(2)

    _uiState.update {
      it.copy(
        board = newBoard,
        tray = finalTray,
        clearingCells = clearedList,
        clearingWaveId = waveId,
        isGameOver = false,
        reviveUsed = true,
      )
    }
    viewModelScope.launch {
      delay(650L)
      _uiState.update { current ->
        if (current.clearingWaveId == waveId) current.copy(clearingCells = emptyList()) else current
      }
    }
  }

  fun setSoundEnabled(enabled: Boolean) {
    _uiState.update { it.copy(soundEnabled = enabled) }
    viewModelScope.launch { repository.setSoundEnabled(enabled) }
  }

  fun setHapticsEnabled(enabled: Boolean) {
    _uiState.update { it.copy(hapticsEnabled = enabled) }
    viewModelScope.launch { repository.setHapticsEnabled(enabled) }
  }

  fun setSettingsVisible(visible: Boolean) {
    _uiState.update { it.copy(showSettingsDialog = visible) }
  }

  fun setModeSheetVisible(visible: Boolean) {
    _uiState.update { it.copy(showModeSheet = visible) }
  }
}
