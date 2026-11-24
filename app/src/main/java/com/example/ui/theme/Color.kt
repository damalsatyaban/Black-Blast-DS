package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Block Blast Royal Blue Palette (from reference screenshots)
val RoyalBlueTop = Color(0xFF3964D8)
val RoyalBlueMid = Color(0xFF325BCE)
val RoyalBlueBottom = Color(0xFF284CB8)
val DeepSapphire = Color(0xFF1A3485)

// Board & Surface Colors
val BoardFrameBorder = Color(0xFF4C76E8)
val BoardFrameOuter = Color(0xFF213363)
val BoardBackground = Color(0xFF1A2547)
val BoardCellEmpty = Color(0xFF202D54)
val BoardGridLine = Color(0xFF2A3A6A)

// HUD & Accent Colors
val CrownGold = Color(0xFFFFD028)
val CrownGoldDark = Color(0xFFE09E00)
val ScoreWhite = Color(0xFFFFFFFF)
val HudMuted = Color(0xFFD6E2FF)
val TraySlotShadow = Color(0x330B1433)
val OverlayScrim = Color(0xB80E1733)
val DialogSurface = Color(0xFF1E2D5A)
val DialogCardSurface = Color(0xFF283B73)
val ActionGreen = Color(0xFF22C55E)
val ActionGreenDark = Color(0xFF15803D)
val ActionAmber = Color(0xFFFFA800)

// 3D Glossy Beveled Block Color Palettes
data class BlockPalette(
  val core: Color,
  val topHighlight: Color,
  val leftHighlight: Color,
  val rightShadow: Color,
  val bottomShadow: Color,
  val outline: Color,
  val glow: Color,
)

val RedBlockPalette =
  BlockPalette(
    core = Color(0xFFED2B2B),
    topHighlight = Color(0xFFFF7A7A),
    leftHighlight = Color(0xFFFF5252),
    rightShadow = Color(0xFFC41818),
    bottomShadow = Color(0xFF8E0B0B),
    outline = Color(0xFF750707),
    glow = Color(0xAAFF4D4D),
  )

val OrangeBlockPalette =
  BlockPalette(
    core = Color(0xFFFF7E0A),
    topHighlight = Color(0xFFFFBE66),
    leftHighlight = Color(0xFFFF9E3B),
    rightShadow = Color(0xFFD65A00),
    bottomShadow = Color(0xFFA34000),
    outline = Color(0xFF873200),
    glow = Color(0xAAFF952B),
  )

val YellowBlockPalette =
  BlockPalette(
    core = Color(0xFFFFC300),
    topHighlight = Color(0xFFFFEA78),
    leftHighlight = Color(0xFFFFD63D),
    rightShadow = Color(0xFFD99600),
    bottomShadow = Color(0xFFA86F00),
    outline = Color(0xFF8A5900),
    glow = Color(0xAAFFD93B),
  )

val GreenBlockPalette =
  BlockPalette(
    core = Color(0xFF1FC626),
    topHighlight = Color(0xFF80F484),
    leftHighlight = Color(0xFF47E04D),
    rightShadow = Color(0xFF139919),
    bottomShadow = Color(0xFF0A6E0F),
    outline = Color(0xFF06540A),
    glow = Color(0xAA3BE842),
  )

val CyanBlockPalette =
  BlockPalette(
    core = Color(0xFF16C3FF),
    topHighlight = Color(0xFF8BE5FF),
    leftHighlight = Color(0xFF49D3FF),
    rightShadow = Color(0xFF0093CC),
    bottomShadow = Color(0xFF006A99),
    outline = Color(0xFF005178),
    glow = Color(0xAA3AD3FF),
  )

val BlueBlockPalette =
  BlockPalette(
    core = Color(0xFF286BFF),
    topHighlight = Color(0xFF7CA8FF),
    leftHighlight = Color(0xFF4F88FF),
    rightShadow = Color(0xFF1749C9),
    bottomShadow = Color(0xFF0C3199),
    outline = Color(0xFF072378),
    glow = Color(0xAA4C86FF),
  )

val PurpleBlockPalette =
  BlockPalette(
    core = Color(0xFFA442F5),
    topHighlight = Color(0xFFD69EFF),
    leftHighlight = Color(0xFFBD6BFF),
    rightShadow = Color(0xFF7E24C7),
    bottomShadow = Color(0xFF5A1296),
    outline = Color(0xFF430975),
    glow = Color(0xAABF6BFF),
  )

val GemTilePalette =
  BlockPalette(
    core = Color(0xFFD8AF6F),
    topHighlight = Color(0xFFFFE5B8),
    leftHighlight = Color(0xFFF2CFA0),
    rightShadow = Color(0xFFAD8246),
    bottomShadow = Color(0xFF825D2A),
    outline = Color(0xFF634318),
    glow = Color(0xAAFFE28A),
  )
