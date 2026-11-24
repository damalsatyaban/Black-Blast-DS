package com.example.game

import com.example.model.AdventureLevel
import com.example.model.BOARD_SIZE
import com.example.model.BlockColor
import com.example.model.BoardCell
import com.example.model.GemType

object AdventureLevels {

  private fun buildBoard(rows: List<String>): List<List<BoardCell>> {
    val grid = MutableList(BOARD_SIZE) { MutableList(BOARD_SIZE) { BoardCell() } }
    for (r in 0 until minOf(rows.size, BOARD_SIZE)) {
      val line = rows[r]
      for (c in 0 until minOf(line.length, BOARD_SIZE)) {
        val ch = line[c]
        grid[r][c] =
          when (ch) {
            'R' -> BoardCell(true, BlockColor.RED)
            'O' -> BoardCell(true, BlockColor.ORANGE)
            'Y' -> BoardCell(true, BlockColor.YELLOW)
            'G' -> BoardCell(true, BlockColor.GREEN)
            'C' -> BoardCell(true, BlockColor.CYAN)
            'B' -> BoardCell(true, BlockColor.BLUE)
            'P' -> BoardCell(true, BlockColor.PURPLE)
            '*' -> BoardCell(true, BlockColor.YELLOW, GemType.GOLD_STAR)
            'H' -> BoardCell(true, BlockColor.RED, GemType.RED_HEX)
            'D' -> BoardCell(true, BlockColor.CYAN, GemType.CYAN_DIAMOND)
            else -> BoardCell()
          }
      }
    }
    return grid.map { it.toList() }
  }

  val levels: List<AdventureLevel> =
    listOf(
      AdventureLevel(
        levelNumber = 1,
        title = "Starlight Debut",
        initialBoard =
          buildBoard(
            listOf(
              "........",
              "..*..*..",
              "..BB.BB.",
              "........",
              "........",
              ".CC..CC.",
              "..*..*..",
              "....*...",
            )
          ),
        targets = mapOf(GemType.GOLD_STAR to 5),
      ),
      AdventureLevel(
        levelNumber = 2,
        title = "Ruby Cascade",
        initialBoard =
          buildBoard(
            listOf(
              "D..BB..*",
              "B..PP..B",
              ".H....H.",
              "........",
              "........",
              ".G....G.",
              "*..PP..H",
              "B..**..D",
            )
          ),
        targets = mapOf(GemType.GOLD_STAR to 4, GemType.RED_HEX to 4),
      ),
      AdventureLevel(
        levelNumber = 3,
        title = "Prism Vault",
        initialBoard =
          buildBoard(
            listOf(
              "D.BB..BB",
              "B*P....*",
              "........",
              "..H..H..",
              "..P..P..",
              "........",
              "*....P.D",
              "BB..BB.*",
            )
          ),
        targets = mapOf(GemType.GOLD_STAR to 5, GemType.RED_HEX to 5),
      ),
      AdventureLevel(
        levelNumber = 4,
        title = "Sapphire Cross",
        initialBoard =
          buildBoard(
            listOf(
              "...DD...",
              "...BB...",
              ".*....*.",
              "CC....GG",
              "GG....CC",
              ".H....H.",
              "...PP...",
              "...**...",
            )
          ),
        targets = mapOf(GemType.GOLD_STAR to 4, GemType.CYAN_DIAMOND to 4),
      ),
      AdventureLevel(
        levelNumber = 5,
        title = "Crimson Matrix",
        initialBoard =
          buildBoard(
            listOf(
              "H.OO..*.",
              ".OO..YY.",
              "........",
              "..D..D..",
              "..C..C..",
              "........",
              ".YY..OO.",
              "*..OO.H.",
            )
          ),
        targets = mapOf(GemType.RED_HEX to 5, GemType.CYAN_DIAMOND to 4),
      ),
      AdventureLevel(
        levelNumber = 6,
        title = "Royal Crown",
        initialBoard =
          buildBoard(
            listOf(
              "*..BB..*",
              "B.P..P.B",
              "..H..H..",
              "...DD...",
              "........",
              ".GG..GG.",
              "C.*..*.C",
              ".H....H.",
            )
          ),
        targets =
          mapOf(GemType.GOLD_STAR to 5, GemType.RED_HEX to 4, GemType.CYAN_DIAMOND to 3),
      ),
      AdventureLevel(
        levelNumber = 7,
        title = "Neon Fortress",
        initialBoard =
          buildBoard(
            listOf(
              "D.CCCC.D",
              "P......P",
              "P.*..*.P",
              "..H..H..",
              "..G..G..",
              "P.*..*.P",
              "P......P",
              "H.YYYY.D",
            )
          ),
        targets = mapOf(GemType.GOLD_STAR to 6, GemType.RED_HEX to 5, GemType.CYAN_DIAMOND to 4),
      ),
      AdventureLevel(
        levelNumber = 8,
        title = "Galaxy Spiral",
        initialBoard =
          buildBoard(
            listOf(
              "*.BBBB.*",
              "..P..P..",
              "H.D..D.H",
              "..C..C..",
              "..G..G..",
              "H.Y..Y.H",
              "..O..O..",
              "*.RRRR.*",
            )
          ),
        targets = mapOf(GemType.GOLD_STAR to 6, GemType.RED_HEX to 6, GemType.CYAN_DIAMOND to 4),
      ),
    )

  fun getLevel(levelNumber: Int): AdventureLevel =
    levels.firstOrNull { it.levelNumber == levelNumber }
      ?: levels[(levelNumber - 1).mod(levels.size)].copy(levelNumber = levelNumber)
}
