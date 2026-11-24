package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.game.PieceCatalog
import com.example.model.BOARD_SIZE
import com.example.model.BlockColor
import com.example.model.BoardCell
import com.example.model.GemType
import com.example.model.TrayPiece
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "block_blast_prefs")

data class StoredPreferences(
  val bestScore: Int = 0,
  val adventureUnlockedLevel: Int = 1,
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val savedClassicSnapshot: ClassicSnapshot? = null,
)

data class ClassicSnapshot(
  val board: List<List<BoardCell>>,
  val score: Int,
  val comboStreak: Int,
  val movesWithoutClear: Int,
  val tray: List<TrayPiece?>,
  val reviveUsed: Boolean,
)

class GamePreferencesRepository(private val context: Context) {

  private object Keys {
    val BEST_SCORE = intPreferencesKey("best_score")
    val ADVENTURE_LEVEL = intPreferencesKey("adventure_unlocked_level")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val SAVED_CLASSIC = stringPreferencesKey("saved_classic_state")
  }

  val preferencesFlow: Flow<StoredPreferences> =
    context.dataStore.data.map { prefs ->
      StoredPreferences(
        bestScore = prefs[Keys.BEST_SCORE] ?: 0,
        adventureUnlockedLevel = prefs[Keys.ADVENTURE_LEVEL] ?: 1,
        soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
        hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
        savedClassicSnapshot = prefs[Keys.SAVED_CLASSIC]?.let(::decodeSnapshot),
      )
    }

  suspend fun saveBestScore(score: Int) {
    context.dataStore.edit { prefs ->
      val current = prefs[Keys.BEST_SCORE] ?: 0
      if (score > current) prefs[Keys.BEST_SCORE] = score
    }
  }

  suspend fun saveAdventureLevel(level: Int) {
    context.dataStore.edit { prefs ->
      val current = prefs[Keys.ADVENTURE_LEVEL] ?: 1
      if (level > current) prefs[Keys.ADVENTURE_LEVEL] = level
    }
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
  }

  suspend fun setHapticsEnabled(enabled: Boolean) {
    context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
  }

  suspend fun saveClassicSnapshot(snapshot: ClassicSnapshot?) {
    context.dataStore.edit { prefs ->
      if (snapshot == null) {
        prefs.remove(Keys.SAVED_CLASSIC)
      } else {
        prefs[Keys.SAVED_CLASSIC] = encodeSnapshot(snapshot)
      }
    }
  }

  private fun encodeSnapshot(snapshot: ClassicSnapshot): String {
    val boardStr =
      snapshot.board.joinToString(";") { row ->
        row.joinToString(",") { cell ->
          if (!cell.occupied || cell.color == null) "."
          else "${cell.color.ordinal}:${cell.gem.ordinal}"
        }
      }
    val trayStr =
      snapshot.tray.joinToString(";") { piece ->
        if (piece == null) "EMPTY"
        else "${piece.instanceId}|${piece.shape.id}|${piece.color.ordinal}|${piece.gem.ordinal}"
      }
    return listOf(
        snapshot.score.toString(),
        snapshot.comboStreak.toString(),
        snapshot.movesWithoutClear.toString(),
        snapshot.reviveUsed.toString(),
        boardStr,
        trayStr,
      )
      .joinToString("#")
  }

  private fun decodeSnapshot(raw: String): ClassicSnapshot? =
    runCatching {
      val parts = raw.split("#")
      if (parts.size != 6) return null
      val score = parts[0].toInt()
      val combo = parts[1].toInt()
      val movesNoClear = parts[2].toInt()
      val reviveUsed = parts[3].toBoolean()

      val rowStrings = parts[4].split(";")
      if (rowStrings.size != BOARD_SIZE) return null
      val board =
        rowStrings.map { rowStr ->
          val cellStrings = rowStr.split(",")
          if (cellStrings.size != BOARD_SIZE) return null
          cellStrings.map { token ->
            if (token == ".") BoardCell()
            else {
              val sub = token.split(":")
              val color = BlockColor.entries[sub[0].toInt()]
              val gem = GemType.entries[sub[1].toInt()]
              BoardCell(occupied = true, color = color, gem = gem)
            }
          }
        }

      val trayTokens = parts[5].split(";")
      if (trayTokens.size != 3) return null
      val tray =
        trayTokens.map { token ->
          if (token == "EMPTY") null
          else {
            val fields = token.split("|")
            val id = fields[0].toLong()
            val shape = PieceCatalog.findShape(fields[1]) ?: return null
            val color = BlockColor.entries[fields[2].toInt()]
            val gem = GemType.entries[fields[3].toInt()]
            TrayPiece(instanceId = id, shape = shape, color = color, gem = gem)
          }
        }

      ClassicSnapshot(
        board = board,
        score = score,
        comboStreak = combo,
        movesWithoutClear = movesNoClear,
        tray = tray,
        reviveUsed = reviveUsed,
      )
    }
      .getOrNull()
}
