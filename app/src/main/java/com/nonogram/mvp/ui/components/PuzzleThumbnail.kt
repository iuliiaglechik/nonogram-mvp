package com.nonogram.mvp.ui.components

  import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

@Composable
fun PuzzleThumbnail(solution: List<List<Boolean>>, modifier: Modifier = Modifier) {
  val filledColor = MaterialTheme.colorScheme.onSurface
val bgColor = MaterialTheme.colorScheme.surface
Canvas(modifier = modifier.fillMaxSize().aspectRatio(1f)) {
val n = solution.size
val cell = this.size.width / n
drawRect(bgColor, size = this.size)
for (r in 0 until n) {
for (c in 0 until n) {
if (solution[r][c]) {
drawRect(
color = filledColor,
topLeft = Offset(c * cell, r * cell),
size = Size(cell, cell),
)
}
}
}
}
}
