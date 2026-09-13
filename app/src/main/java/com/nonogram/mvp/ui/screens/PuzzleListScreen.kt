package com.nonogram.mvp.ui.screens

  import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nonogram.mvp.model.Difficulty
import com.nonogram.mvp.ui.components.PuzzleThumbnail
import com.nonogram.mvp.viewmodel.PuzzleListItem
import com.nonogram.mvp.viewmodel.PuzzleListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleListScreen(
viewModel: PuzzleListViewModel,
onOpenPuzzle: (String) -> Unit,
onOpenProfile: () -> Unit,
) {
  val state by viewModel.uiState.collectAsState()

    Scaffold(
topBar = {
LargeTopAppBar(
title = { Text("Nonogram") },
actions = {
IconButton(onClick = onOpenProfile) {
Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
}
},
colors = TopAppBarDefaults.largeTopAppBarColors(
containerColor = MaterialTheme.colorScheme.background,
),
)
},
containerColor = MaterialTheme.colorScheme.background,
) { padding ->
    if (state.isLoading) {
Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
}
return@Scaffold
  }

LazyVerticalGrid(
columns = GridCells.Fixed(2),
modifier = Modifier.fillMaxSize().padding(padding),
contentPadding = PaddingValues(16.dp),
horizontalArrangement = Arrangement.spacedBy(14.dp),
verticalArrangement = Arrangement.spacedBy(14.dp),
) {
items(state.items, key = { it.puzzle.id }) { item ->
  PuzzleCard(item = item, onClick = { onOpenPuzzle(item.puzzle.id) })
  }
  }
}
}

@Composable
private fun PuzzleCard(item: PuzzleListItem, onClick: () -> Unit) {
  Card(
onClick = onClick,
shape = RoundedCornerShape(18.dp),
colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
) {
Column(Modifier.padding(12.dp)) {
Box(
Modifier
.fillMaxWidth()
.aspectRatio(1f)
.clip(RoundedCornerShape(10.dp)),
) {
  PuzzleThumbnail(solution = item.puzzle.solution, modifier = Modifier.fillMaxSize())
if (item.completed) {
Icon(
Icons.Filled.CheckCircle,
  contentDescription = "Completed",
  tint = MaterialTheme.colorScheme.secondary,
  modifier = Modifier
  .align(Alignment.TopEnd)
  .padding(6.dp),
  )
  }
}
Spacer(Modifier.height(10.dp))
Text(
item.puzzle.title,
  style = MaterialTheme.typography.titleMedium,
  color = MaterialTheme.colorScheme.onSurface,
  maxLines = 1,
  overflow = TextOverflow.Ellipsis,
  )
  Spacer(Modifier.height(4.dp))
  Row(verticalAlignment = Alignment.CenterVertically) {
  DifficultyDot(item.puzzle.difficulty)
  Spacer(Modifier.width(6.dp))
  Text(
  item.puzzle.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
  style = MaterialTheme.typography.labelSmall,
  color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
  }
  }
  }
  }

@Composable
private fun DifficultyDot(difficulty: Difficulty) {
  val color = when (difficulty) {
Difficulty.EASY -> Color(0xFF7C9473)
  Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
  Difficulty.HARD -> MaterialTheme.colorScheme.error
  }
Box(
Modifier
.height(7.dp)
.aspectRatio(1f)
.clip(RoundedCornerShape(50))
.background(color),
)
}
