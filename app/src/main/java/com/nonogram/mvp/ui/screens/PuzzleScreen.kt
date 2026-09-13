package com.nonogram.mvp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nonogram.mvp.ui.components.NonogramGrid
import com.nonogram.mvp.viewmodel.DrawTool
import com.nonogram.mvp.viewmodel.PuzzleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleScreen(
  viewModel: PuzzleViewModel,
  onBack: () -> Unit,
  ) {
  val state by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(state.puzzle?.title ?: "") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = viewModel::undo, enabled = state.canUndo) {
            Icon(Icons.Filled.Undo, contentDescription = "Undo")
          }
          IconButton(onClick = viewModel::resetPuzzle) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reset")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )
    },
    containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
    if (state.isLoading || state.puzzle == null) {
      Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
      }
      return@Scaffold
    }

    val puzzle = state.puzzle!!

    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      ) {
      NonogramGrid(
        size = puzzle.size,
        rowClues = puzzle.rowClues,
        colClues = puzzle.colClues,
        cells = state.cells,
        onCellTap = viewModel::onCellTap,
        modifier = Modifier.fillMaxWidth(),
        )

      Spacer(Modifier.height(28.dp))

      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth(0.8f)) {
        SegmentedButton(
          selected = state.tool == DrawTool.FILL,
          onClick = { viewModel.selectTool(DrawTool.FILL) },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
          ) { Text("Fill") }
        SegmentedButton(
          selected = state.tool == DrawTool.MARK_X,
          onClick = { viewModel.selectTool(DrawTool.MARK_X) },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
          ) { Text("Mark") }
      }

      Spacer(Modifier.height(20.dp))

      AnimatedVisibility(visible = state.isSolved, enter = fadeIn(), exit = fadeOut()) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth(),
          ) {
          Text(
            "Solved -- well done.",
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            )
        }
      }
    }
  }
}
