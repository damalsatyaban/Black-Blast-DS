package com.example.game

import com.example.model.BOARD_SIZE
import com.example.model.BlockColor
import com.example.model.BlockShape
import com.example.model.BoardCell
import com.example.model.CellOffset
import com.example.model.GemType
import com.example.model.TrayPiece
import kotlin.random.Random

object PieceCatalog {

  private fun shape(id: String, vararg coords: Pair<Int, Int>): BlockShape {
    val cells = coords.map { CellOffset(it.first, it.second) }
    val rows = (cells.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (cells.maxOfOrNull { it.col } ?: 0) + 1
    return BlockShape(id = id, cells = cells, rows = rows, cols = cols)
  }

  val smallShapes: List<BlockShape> =
    listOf(
      shape("dot_1x1", 0 to 0),
      shape("line_1x2", 0 to 0, 0 to 1),
      shape("line_2x1", 0 to 0, 1 to 0),
      shape("line_1x3", 0 to 0, 0 to 1, 0 to 2),
      shape("line_3x1", 0 to 0, 1 to 0, 2 to 0),
      shape("l2_tl", 0 to 0, 0 to 1, 1 to 0),
      shape("l2_tr", 0 to 0, 0 to 1, 1 to 1),
      shape("l2_bl", 0 to 0, 1 to 0, 1 to 1),
      shape("l2_br", 0 to 1, 1 to 0, 1 to 1),
      shape("sq_2x2", 0 to 0, 0 to 1, 1 to 0, 1 to 1),
    )

  val mediumShapes: List<BlockShape> =
    listOf(
      shape("line_1x4", 0 to 0, 0 to 1, 0 to 2, 0 to 3),
      shape("line_4x1", 0 to 0, 1 to 0, 2 to 0, 3 to 0),
      shape("t_up", 0 to 1, 1 to 0, 1 to 1, 1 to 2),
      shape("t_down", 0 to 0, 0 to 1, 0 to 2, 1 to 1),
      shape("t_left", 0 to 1, 1 to 0, 1 to 1, 2 to 1),
      shape("t_right", 0 to 0, 1 to 0, 1 to 1, 2 to 0),
      shape("z_h1", 0 to 0, 0 to 1, 1 to 1, 1 to 2),
      shape("z_h2", 0 to 1, 0 to 2, 1 to 0, 1 to 1),
      shape("z_v1", 0 to 1, 1 to 0, 1 to 1, 2 to 0),
      shape("z_v2", 0 to 0, 1 to 0, 1 to 1, 2 to 1),
      shape("l_a", 0 to 0, 1 to 0, 2 to 0, 2 to 1),
      shape("l_b", 0 to 1, 1 to 1, 2 to 0, 2 to 1),
      shape("l_c", 0 to 0, 0 to 1, 1 to 0, 2 to 0),
      shape("l_d", 0 to 0, 0 to 1, 1 to 1, 2 to 1),
      shape("l_e", 0 to 0, 1 to 0, 1 to 1, 1 to 2),
      shape("l_f", 0 to 2, 1 to 0, 1 to 1, 1 to 2),
      shape("l_g", 0 to 0, 0 to 1, 0 to 2, 1 to 0),
      shape("l_h", 0 to 0, 0 to 1, 0 to 2, 1 to 2),
    )

  val largeShapes: List<BlockShape> =
    listOf(
      shape("line_1x5", 0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4),
      shape("line_5x1", 0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0),
      shape("rect_2x3", 0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2),
      shape("rect_3x2", 0 to 0, 0 to 1, 1 to 0, 1 to 1, 2 to 0, 2 to 1),
      shape("sq_3x3", 0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2, 2 to 0, 2 to 1, 2 to 2),
      shape("bigL_tl", 0 to 0, 0 to 1, 0 to 2, 1 to 0, 2 to 0),
      shape("bigL_tr", 0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2),
      shape("bigL_bl", 0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2),
      shape("bigL_br", 0 to 2, 1 to 2, 2 to 0, 2 to 1, 2 to 2),
    )

  val allShapes: List<BlockShape> = smallShapes + mediumShapes + largeShapes

  private val shapeById: Map<String, BlockShape> = allShapes.associateBy { it.id }

  fun findShape(id: String): BlockShape? = shapeById[id]

  fun emptyBoard(): List<List<BoardCell>> =
    List(BOARD_SIZE) { List(BOARD_SIZE) { BoardCell() } }

  fun canPlace(
    board: List<List<BoardCell>>,
    shape: BlockShape,
    anchorRow: Int,
    anchorCol: Int,
  ): Boolean {
    for (cell in shape.cells) {
      val r = anchorRow + cell.row
      val c = anchorCol + cell.col
      if (r !in 0 until BOARD_SIZE || c !in 0 until BOARD_SIZE) return false
      if (board[r][c].occupied) return false
    }
    return true
  }

  fun canPlaceAnywhere(board: List<List<BoardCell>>, shape: BlockShape): Boolean {
    for (r in 0..BOARD_SIZE - shape.rows) {
      for (c in 0..BOARD_SIZE - shape.cols) {
        if (canPlace(board, shape, r, c)) return true
      }
    }
    return false
  }

  fun findPreviewClears(
    board: List<List<BoardCell>>,
    shape: BlockShape,
    anchorRow: Int,
    anchorCol: Int,
  ): Pair<Set<Int>, Set<Int>> {
    if (!canPlace(board, shape, anchorRow, anchorCol)) return emptySet<Int>() to emptySet()
    val placedCoords = shape.cells.map { (anchorRow + it.row) to (anchorCol + it.col) }.toSet()

    val fullRows = mutableSetOf<Int>()
    for (r in 0 until BOARD_SIZE) {
      var full = true
      for (c in 0 until BOARD_SIZE) {
        if (!board[r][c].occupied && (r to c) !in placedCoords) {
          full = false
          break
        }
      }
      if (full) fullRows.add(r)
    }

    val fullCols = mutableSetOf<Int>()
    for (c in 0 until BOARD_SIZE) {
      var full = true
      for (r in 0 until BOARD_SIZE) {
        if (!board[r][c].occupied && (r to c) !in placedCoords) {
          full = false
          break
        }
      }
      if (full) fullCols.add(c)
    }

    return fullRows to fullCols
  }

  data class PlacementOutcome(
    val boardAfterPlace: List<List<BoardCell>>,
    val boardAfterClear: List<List<BoardCell>>,
    val clearedRows: Set<Int>,
    val clearedCols: Set<Int>,
    val clearedCellPositions: List<Pair<Int, Int>>,
    val collectedGems: Map<GemType, Int>,
  )

  fun placeAndClear(
    board: List<List<BoardCell>>,
    piece: TrayPiece,
    anchorRow: Int,
    anchorCol: Int,
  ): PlacementOutcome {
    val mutableAfterPlace = board.map { it.toMutableList() }
    val gemTargetIndex =
      if (piece.gem != GemType.NONE && piece.shape.cells.isNotEmpty()) 0 else -1

    piece.shape.cells.forEachIndexed { idx, offset ->
      val r = anchorRow + offset.row
      val c = anchorCol + offset.col
      val cellGem = if (idx == gemTargetIndex) piece.gem else GemType.NONE
      mutableAfterPlace[r][c] = BoardCell(occupied = true, color = piece.color, gem = cellGem)
    }

    val fullRows = mutableSetOf<Int>()
    for (r in 0 until BOARD_SIZE) {
      if (mutableAfterPlace[r].all { it.occupied }) fullRows.add(r)
    }
    val fullCols = mutableSetOf<Int>()
    for (c in 0 until BOARD_SIZE) {
      var allOccupied = true
      for (r in 0 until BOARD_SIZE) {
        if (!mutableAfterPlace[r][c].occupied) {
          allOccupied = false
          break
        }
      }
      if (allOccupied) fullCols.add(c)
    }

    val mutableAfterClear = mutableAfterPlace.map { it.toMutableList() }
    val clearedPositions = mutableListOf<Pair<Int, Int>>()
    val collectedGems = mutableMapOf<GemType, Int>()

    for (r in 0 until BOARD_SIZE) {
      for (c in 0 until BOARD_SIZE) {
        if (r in fullRows || c in fullCols) {
          clearedPositions.add(r to c)
          val gem = mutableAfterPlace[r][c].gem
          if (gem != GemType.NONE) {
            collectedGems[gem] = (collectedGems[gem] ?: 0) + 1
          }
          mutableAfterClear[r][c] = BoardCell()
        }
      }
    }

    return PlacementOutcome(
      boardAfterPlace = mutableAfterPlace.map { it.toList() },
      boardAfterClear = mutableAfterClear.map { it.toList() },
      clearedRows = fullRows,
      clearedCols = fullCols,
      clearedCellPositions = clearedPositions,
      collectedGems = collectedGems,
    )
  }

  fun generateSmartTray(
    board: List<List<BoardCell>>,
    nextInstanceId: () -> Long,
    neededGemsOnBoard: Map<GemType, Int> = emptyMap(),
  ): List<TrayPiece> {
    val occupiedCount = board.sumOf { row -> row.count { it.occupied } }
    val fillRatio = occupiedCount / 64f

    fun pickWeightedShape(): BlockShape {
      val roll = Random.nextFloat()
      return when {
        fillRatio > 0.65f -> {
          if (roll < 0.65f) smallShapes.random() else mediumShapes.random()
        }
        fillRatio > 0.42f -> {
          when {
            roll < 0.45f -> smallShapes.random()
            roll < 0.85f -> mediumShapes.random()
            else -> largeShapes.random()
          }
        }
        else -> {
          when {
            roll < 0.34f -> smallShapes.random()
            roll < 0.78f -> mediumShapes.random()
            else -> largeShapes.random()
          }
        }
      }
    }

    val picked = MutableList(3) { pickWeightedShape() }

    // Ensure at least one piece in the batch can fit on the current board (if any shape fits at all)
    val anyFits = picked.any { canPlaceAnywhere(board, it) }
    if (!anyFits) {
      val fittingSmall = smallShapes.filter { canPlaceAnywhere(board, it) }
      val fittingMedium = mediumShapes.filter { canPlaceAnywhere(board, it) }
      val fallback =
        fittingSmall.randomOrNull() ?: fittingMedium.randomOrNull() ?: allShapes.firstOrNull {
          canPlaceAnywhere(board, it)
        }
      if (fallback != null) {
        picked[Random.nextInt(3)] = fallback
      }
    }

    // In Adventure mode, if a gem type still needs collecting and fewer exist on board than needed,
    // spawn one on a piece in the tray so the stage is always completable!
    val gemToInject: GemType? =
      neededGemsOnBoard.entries.firstOrNull { it.value > 0 }?.let { (gem, _) ->
        if (Random.nextFloat() < 0.7f) gem else null
      }

    val colors = BlockColor.entries
    val injectSlot = if (gemToInject != null) Random.nextInt(3) else -1

    return picked.mapIndexed { idx, shape ->
      TrayPiece(
        instanceId = nextInstanceId(),
        shape = shape,
        color = colors.random(),
        gem = if (idx == injectSlot) gemToInject ?: GemType.NONE else GemType.NONE,
      )
    }
  }
}
