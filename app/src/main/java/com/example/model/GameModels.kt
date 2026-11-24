package com.example.model

import com.example.ui.theme.BlockPalette
import com.example.ui.theme.BlueBlockPalette
import com.example.ui.theme.CyanBlockPalette
import com.example.ui.theme.GemTilePalette
import com.example.ui.theme.GreenBlockPalette
import com.example.ui.theme.OrangeBlockPalette
import com.example.ui.theme.PurpleBlockPalette
import com.example.ui.theme.RedBlockPalette
import com.example.ui.theme.YellowBlockPalette

const val BOARD_SIZE = 8

enum class BlockColor(val palette: BlockPalette) {
  RED(RedBlockPalette),
  ORANGE(OrangeBlockPalette),
  YELLOW(YellowBlockPalette),
  GREEN(GreenBlockPalette),
  CYAN(CyanBlockPalette),
  BLUE(BlueBlockPalette),
  PURPLE(PurpleBlockPalette),
}

enum class GemType {
  NONE,
  GOLD_STAR,
  RED_HEX,
  CYAN_DIAMOND,
}

data class BoardCell(
  val occupied: Boolean = false,
  val color: BlockColor? = null,
  val gem: GemType = GemType.NONE,
) {
  val effectivePalette: BlockPalette
    get() =
      when {
        gem != GemType.NONE -> GemTilePalette
        color != null -> color.palette
        else -> BlueBlockPalette
      }
}

data class CellOffset(val row: Int, val col: Int)

data class BlockShape(
  val id: String,
  val cells: List<CellOffset>,
  val rows: Int,
  val cols: Int,
)

data class TrayPiece(
  val instanceId: Long,
  val shape: BlockShape,
  val color: BlockColor,
  val gem: GemType = GemType.NONE,
)

enum class GameMode {
  CLASSIC,
  ADVENTURE,
}

data class AdventureLevel(
  val levelNumber: Int,
  val title: String,
  val initialBoard: List<List<BoardCell>>,
  val targets: Map<GemType, Int>,
)

data class FloatingCallout(
  val id: Long,
  val text: String,
  val subtext: String? = null,
  val anchorRow: Float,
  val anchorCol: Float,
  val color: BlockColor,
)

data class ClearingCellEffect(
  val row: Int,
  val col: Int,
  val color: BlockColor,
  val gem: GemType = GemType.NONE,
)
