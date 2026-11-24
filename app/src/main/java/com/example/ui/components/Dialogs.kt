package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.AdventureLevels
import com.example.model.GameMode
import com.example.model.GemType
import com.example.ui.theme.ActionAmber
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.BoardFrameBorder
import com.example.ui.theme.CrownGold
import com.example.ui.theme.DialogCardSurface
import com.example.ui.theme.DialogSurface
import com.example.ui.theme.HudMuted
import com.example.ui.theme.OverlayScrim

@Composable
fun SettingsDialog(
  soundEnabled: Boolean,
  hapticsEnabled: Boolean,
  mode: GameMode,
  onToggleSound: (Boolean) -> Unit,
  onToggleHaptics: (Boolean) -> Unit,
  onRestart: () -> Unit,
  onOpenModes: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .widthIn(max = 380.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(DialogSurface)
          .border(2.dp, BoardFrameBorder.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
          .padding(20.dp)
          .testTag("settings_dialog"),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Settings",
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
        )
        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
          Icon(Icons.Default.Close, contentDescription = "Close settings", tint = HudMuted)
        }
      }

      // Offline badge
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DialogCardSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Default.WifiOff,
          contentDescription = null,
          tint = Color(0xFF4ADE80),
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = "Offline Ready • Progress Saved Locally",
          style = MaterialTheme.typography.bodyMedium,
          color = HudMuted,
        )
      }

      // Sound Toggle
      SettingToggleRow(
        icon =
          if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp
          else Icons.AutoMirrored.Filled.VolumeOff,
        label = "Sound Effects",
        checked = soundEnabled,
        onCheckedChange = onToggleSound,
        testTag = "sound_toggle",
      )

      // Haptics Toggle
      SettingToggleRow(
        icon = Icons.Default.Vibration,
        label = "Haptic Feedback",
        checked = hapticsEnabled,
        onCheckedChange = onToggleHaptics,
        testTag = "haptics_toggle",
      )

      Spacer(modifier = Modifier.height(2.dp))

      Button(
        onClick = onOpenModes,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("switch_mode_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B6BFF)),
      ) {
        Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text =
            if (mode == GameMode.CLASSIC) "Play Adventure Stages" else "Modes & Stage Select",
          style = MaterialTheme.typography.labelLarge,
        )
      }

      Button(
        onClick = onRestart,
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("restart_game_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Restart Game", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun SettingToggleRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(DialogCardSurface)
        .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(icon, contentDescription = null, tint = CrownGold)
      Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.testTag(testTag),
      colors =
        SwitchDefaults.colors(
          checkedThumbColor = Color.White,
          checkedTrackColor = ActionGreen,
          uncheckedThumbColor = HudMuted,
          uncheckedTrackColor = DialogSurface,
        ),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeSelectDialog(
  currentMode: GameMode,
  currentAdventureLevel: Int,
  unlockedLevel: Int,
  onSelectClassic: () -> Unit,
  onSelectAdventureLevel: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .widthIn(max = 400.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(DialogSurface)
          .border(2.dp, BoardFrameBorder.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
          .padding(20.dp)
          .testTag("mode_select_dialog"),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Game Modes", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = HudMuted)
        }
      }

      // Classic Mode Card
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
              if (currentMode == GameMode.CLASSIC) Color(0xFF2B6BFF) else DialogCardSurface
            )
            .border(
              width = 1.5.dp,
              color = if (currentMode == GameMode.CLASSIC) CrownGold else Color.Transparent,
              shape = RoundedCornerShape(16.dp),
            )
            .clickable { onSelectClassic() }
            .padding(14.dp)
            .testTag("select_classic_mode"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          Icons.Default.GridView,
          contentDescription = null,
          tint = CrownGold,
          modifier = Modifier.size(28.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text("Classic Endless", style = MaterialTheme.typography.titleLarge, color = Color.White)
          Text(
            "Relaxing 8x8 high-score chase",
            style = MaterialTheme.typography.bodyMedium,
            color = HudMuted,
          )
        }
        Icon(Icons.Default.PlayArrow, contentDescription = "Play Classic", tint = Color.White)
      }

      Text(
        text = "Adventure Stages (Collect Stars & Gems)",
        style = MaterialTheme.typography.titleMedium,
        color = CrownGold,
      )

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 4,
      ) {
        for (level in AdventureLevels.levels) {
          val isUnlocked = level.levelNumber <= unlockedLevel
          val isSelected =
            currentMode == GameMode.ADVENTURE && currentAdventureLevel == level.levelNumber
          Box(
            modifier =
              Modifier.size(68.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                  when {
                    isSelected -> Color(0xFF2B6BFF)
                    isUnlocked -> DialogCardSurface
                    else -> Color(0xFF141E3D)
                  }
                )
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color =
                    when {
                      isSelected -> CrownGold
                      isUnlocked -> BoardFrameBorder.copy(alpha = 0.6f)
                      else -> Color.Transparent
                    },
                  shape = RoundedCornerShape(14.dp),
                )
                .clickable(enabled = isUnlocked) { onSelectAdventureLevel(level.levelNumber) }
                .testTag("adventure_stage_${level.levelNumber}"),
            contentAlignment = Alignment.Center,
          ) {
            if (isUnlocked) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "${level.levelNumber}",
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                )
                Text(
                  text = "Stage",
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                  color = HudMuted,
                )
              }
            } else {
              Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = HudMuted.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun GameOverOverlay(
  score: Int,
  bestScore: Int,
  isNewBest: Boolean,
  canRevive: Boolean,
  onRevive: () -> Unit,
  onPlayAgain: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize().background(OverlayScrim).padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .widthIn(max = 360.dp)
          .clip(RoundedCornerShape(26.dp))
          .background(
            Brush.verticalGradient(listOf(Color(0xFF2A428A), DialogSurface))
          )
          .border(2.dp, BoardFrameBorder, RoundedCornerShape(26.dp))
          .padding(24.dp)
          .testTag("game_over_overlay"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      if (isNewBest) {
        Row(
          modifier =
            Modifier.clip(CircleShape)
              .background(CrownGold)
              .padding(horizontal = 14.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFF3A2300),
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = stringResource(R.string.new_best_badge),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
            color = Color(0xFF3A2300),
          )
        }
      }

      Text(
        text = stringResource(R.string.game_over_title),
        style = MaterialTheme.typography.displayMedium,
        color = Color.White,
        textAlign = TextAlign.Center,
      )

      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DialogCardSurface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text("SCORE", style = MaterialTheme.typography.bodyMedium, color = HudMuted)
        Text(
          text = score.toString(),
          style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
          color = Color.White,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = CrownGold,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Best: $bestScore",
            style = MaterialTheme.typography.titleMedium,
            color = CrownGold,
          )
        }
      }

      if (canRevive) {
        Button(
          onClick = onRevive,
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("revive_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ActionAmber),
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF281900))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.revive_board),
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF281900),
          )
        }
      }

      Button(
        onClick = onPlayAgain,
        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("play_again_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.play_again),
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
        )
      }
    }
  }
}

@Composable
fun StageClearOverlay(
  levelNumber: Int,
  levelTitle: String,
  score: Int,
  targets: Map<GemType, Int>,
  onNextStage: () -> Unit,
  onReplay: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize().background(OverlayScrim).padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .widthIn(max = 360.dp)
          .clip(RoundedCornerShape(26.dp))
          .background(Brush.verticalGradient(listOf(Color(0xFF2A428A), DialogSurface)))
          .border(2.dp, CrownGold, RoundedCornerShape(26.dp))
          .padding(24.dp)
          .testTag("stage_clear_overlay"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = "STAGE $levelNumber",
        style = MaterialTheme.typography.titleMedium,
        color = CrownGold,
      )
      Text(
        text = stringResource(R.string.level_complete_title),
        style = MaterialTheme.typography.displayMedium,
        color = Color.White,
        textAlign = TextAlign.Center,
      )
      Text(
        text = levelTitle,
        style = MaterialTheme.typography.bodyLarge,
        color = HudMuted,
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for ((gem, count) in targets) {
          Row(
            modifier =
              Modifier.clip(RoundedCornerShape(12.dp))
                .background(DialogCardSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Canvas(modifier = Modifier.size(24.dp)) {
              drawGemSymbol(
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.minDimension * 0.45f,
                gem = gem,
              )
            }
            Text(
              text = "$count/$count",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
            )
          }
        }
      }

      Text(
        text = "Score: $score",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
      )

      Button(
        onClick = onNextStage,
        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("next_stage_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.next_stage), style = MaterialTheme.typography.titleLarge)
      }

      OutlinedButton(
        onClick = onReplay,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp),
      ) {
        Text("Replay Stage", color = HudMuted, style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}
