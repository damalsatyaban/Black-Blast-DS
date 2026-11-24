package com.example

import com.example.game.PieceCatalog
import com.example.model.BOARD_SIZE
import com.example.model.BlockColor
import com.example.model.BoardCell
import com.example.model.GemType
import com.example.model.TrayPiece
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockBlastLogicTest {

  @Test
  fun placeAndClear_clearsFullRowAndColSimultaneouslyAndCollectsGems() {
    val grid = PieceCatalog.emptyBoard().map { it.toMutableList() }
    // Fill row 0 except col 7, and fill col 7 except row 0
    for (c in 0 until BOARD_SIZE - 1) {
      grid[0][c] = BoardCell(occupied = true, color = BlockColor.BLUE, gem = if (c == 2) GemType.GOLD_STAR else GemType.NONE)
    }
    for (r in 1 until BOARD_SIZE) {
      grid[r][7] = BoardCell(occupied = true, color = BlockColor.GREEN, gem = if (r == 4) GemType.RED_HEX else GemType.NONE)
    }

    val dotShape = PieceCatalog.findShape("dot_1x1")!!
    val piece = TrayPiece(instanceId = 1L, shape = dotShape, color = BlockColor.YELLOW)

    assertTrue(PieceCatalog.canPlace(grid, dotShape, 0, 7))
    val outcome = PieceCatalog.placeAndClear(grid, piece, 0, 7)

    assertEquals(setOf(0), outcome.clearedRows)
    assertEquals(setOf(7), outcome.clearedCols)
    assertEquals(1, outcome.collectedGems[GemType.GOLD_STAR])
    assertEquals(1, outcome.collectedGems[GemType.RED_HEX])

    for (c in 0 until BOARD_SIZE) {
      assertFalse(outcome.boardAfterClear[0][c].occupied)
    }
    for (r in 0 until BOARD_SIZE) {
      assertFalse(outcome.boardAfterClear[r][7].occupied)
    }
  }

  @Test
  fun smartTray_ensuresAtLeastOneFittablePieceOnCrowdedBoard() {
    val grid = PieceCatalog.emptyBoard().map { it.toMutableList() }
    for (r in 0 until BOARD_SIZE) {
      for (c in 0 until BOARD_SIZE) {
        grid[r][c] = BoardCell(occupied = true, color = BlockColor.PURPLE)
      }
    }
    // Leave only a single 1x1 cell open at (3, 3)
    grid[3][3] = BoardCell()

    var id = 1L
    val tray = PieceCatalog.generateSmartTray(grid, { id++ })
    assertEquals(3, tray.size)
    assertTrue(tray.any { PieceCatalog.canPlaceAnywhere(grid, it.shape) })
  }
}
