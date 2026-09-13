package com.nonogram.mvp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.nonogram.mvp.model.CellState
import kotlin.math.floor

@Composable
fun NonogramGrid(
  size: Int,
  rowClues: List<List<Int>>,
  colClues: List<List<Int>>,
  cells: List<List<CellState>>,
  onCellTap: (row: Int, col: Int) -> Unit,
  modifier: Modifier = Modifier,
  ) {
  val maxRowClueLen = rowClues.maxOf { it.size }
  val maxColClueLen = colClues.maxOf { it.size }

  val gridLineColor = MaterialTheme.colorScheme.outline
  val strongLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
  val filledColor = MaterialTheme.colorScheme.onBackground
  val markColor = MaterialTheme.colorScheme.onSurfaceVariant
  val clueTextColor = MaterialTheme.colorScheme.onBackground
  val clueTextColorDone = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
  val surfaceColor = MaterialTheme.colorScheme.surface

  val satisfiedRows = remember(cells) { rowClues.indices.map { r -> rowSatisfied(rowClues[r], cells[r]) } }
  val satisfiedCols = remember(cells) {
    colClues.indices.map { c -> rowSatisfied(colClues[c], cells.map { it[c] }) }
  }

  var lastDragCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
  var dragPaintValue by remember { mutableStateOf(true) }

  Box(modifier = modifier.fillMaxWidth().aspectRatio(1f)) {
    Canvas(
      modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .pointerInput(size) {
        detectTapGestures { offset ->
          cellAt(offset, size.toFloat(), this.size.width.toFloat(), maxRowClueLen, maxColClueLen)
          ?.let { (r, c) -> onCellTap(r, c) }
        }
      }
      .pointerInput(size) {
        detectDragGestures(
          onDragStart = { offset ->
            lastDragCell = cellAt(offset, size.toFloat(), this.size.width.toFloat(), maxRowClueLen, maxColClueLen)
            lastDragCell?.let { (r, c) -> dragPaintValue = cells[r][c] != CellState.FILLED }
          },
          onDrag = { change, _ ->
            val cell = cellAt(change.position, size.toFloat(), this.size.width.toFloat(), maxRowClueLen, maxColClueLen)
            if (cell != null && cell != lastDragCell) {
              lastDragCell = cell
              onCellTap(cell.first, cell.second)
            }
          },
          onDragEnd = { lastDragCell = null },
          )
      },
      ) {
      drawNonogram(
        size = size,
        rowClues = rowClues,
        colClues = colClues,
        cells = cells,
        maxRowClueLen = maxRowClueLen,
        maxColClueLen = maxColClueLen,
        gridLineColor = gridLineColor,
        strongLineColor = strongLineColor,
        filledColor = filledColor,
        markColor = markColor,
        clueTextColor = clueTextColor,
        clueTextColorDone = clueTextColorDone,
        surfaceColor = surfaceColor,
        satisfiedRows = satisfiedRows,
        satisfiedCols = satisfiedCols,
        )
    }
  }
}

private fun rowSatisfied(clue: List<Int>, line: List<CellState>): Boolean {
  val runs = mutableListOf<Int>()
  var run = 0
  for (cell in line) {
    if (cell == CellState.FILLED) {
      run++
    } else if (run > 0) {
      runs.add(run)
      run = 0
    }
  }
  if (run > 0) runs.add(run)
  val normalized = if (runs.isEmpty()) listOf(0) else runs
  return normalized == clue
}

private fun cellAt(
  offset: Offset,
  gridSize: Float,
  canvasSize: Float,
  maxRowClueLen: Int,
  maxColClueLen: Int,
  ): Pair<Int, Int>? {
  val clueUnit = canvasSize / (gridSize + maxRowClueLen.coerceAtMost(4) + 0.4f)
  val gutterLeft = clueUnit * (maxRowClueLen.coerceAtMost(4) + 0.4f)
  val gutterTop = clueUnit * (maxColClueLen.coerceAtMost(4) + 0.4f)
  val cellPx = (canvasSize - gutterLeft) / gridSize
  if (offset.x < gutterLeft || offset.y < gutterTop) return null
  val col = floor((offset.x - gutterLeft) / cellPx).toInt()
  val row = floor((offset.y - gutterTop) / cellPx).toInt()
  if (row !in 0 until gridSize.toInt() || col !in 0 until gridSize.toInt()) return null
  return row to col
}

private fun DrawScope.drawNonogram(
  size: Int,
  rowClues: List<List<Int>>,
  colClues: List<List<Int>>,
  cells: List<List<CellState>>,
  maxRowClueLen: Int,
  maxColClueLen: Int,
  gridLineColor: Color,
  strongLineColor: Color,
  filledColor: Color,
  markColor: Color,
  clueTextColor: Color,
  clueTextColorDone: Color,
  surfaceColor: Color,
  satisfiedRows: List<Boolean>,
  satisfiedCols: List<Boolean>,
  ) {
  val canvasSize = this.size.width
  val clueUnit = canvasSize / (size + maxRowClueLen.coerceAtMost(4) + 0.4f)
  val gutterLeft = clueUnit * (maxRowClueLen.coerceAtMost(4) + 0.4f)
  val gutterTop = clueUnit * (maxColClueLen.coerceAtMost(4) + 0.4f)
  val cellPx = (canvasSize - gutterLeft) / size

  drawRect(
    color = surfaceColor,
    topLeft = Offset(gutterLeft, gutterTop),
    size = androidx.compose.ui.geometry.Size(canvasSize - gutterLeft, canvasSize - gutterTop),
    )

  for (r in 0 until size) {
    for (c in 0 until size) {
      val x = gutterLeft + c * cellPx
      val y = gutterTop + r * cellPx
      when (cells[r][c]) {
        CellState.FILLED -> drawRect(
          color = filledColor,
          topLeft = Offset(x + cellPx * 0.06f, y + cellPx * 0.06f),
          size = androidx.compose.ui.geometry.Size(cellPx * 0.88f, cellPx * 0.88f),
          )
        CellState.MARKED_X -> {
          val pad = cellPx * 0.32f
          drawLine(markColor, Offset(x + pad, y + pad), Offset(x + cellPx - pad, y + cellPx - pad), strokeWidth = cellPx * 0.08f)
          drawLine(markColor, Offset(x + cellPx - pad, y + pad), Offset(x + pad, y + cellPx - pad), strokeWidth = cellPx * 0.08f)
        }
        CellState.EMPTY -> {}
      }
    }
  }

  for (i in 0..size) {
    val isThick = i % 5 == 0
    val strokeW = if (isThick) 2.4f else 1f
    val color = if (isThick) strongLineColor else gridLineColor
    drawLine(color, Offset(gutterLeft, gutterTop + i * cellPx), Offset(gutterLeft + size * cellPx, gutterTop + i * cellPx), strokeWidth = strokeW)
    drawLine(color, Offset(gutterLeft + i * cellPx, gutterTop), Offset(gutterLeft + i * cellPx, gutterTop + size * cellPx), strokeWidth = strokeW)
  }
  drawRect(strongLineColor, topLeft = Offset(gutterLeft, gutterTop), size = androidx.compose.ui.geometry.Size(size * cellPx, size * cellPx), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.6f))

  val textSizePx = cellPx * 0.42f
  for (r in 0 until size) {
    val clue = rowClues[r]
    val text = clue.joinToString(" ")
    val paint = android.graphics.Paint().apply {
      textSize = textSizePx
      color = (if (satisfiedRows[r]) clueTextColorDone else clueTextColor).toArgbCompat()
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.RIGHT
      typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
    }
    val y = gutterTop + r * cellPx + cellPx / 2f + textSizePx * 0.35f
    drawContext.canvas.nativeCanvas.drawText(text, gutterLeft - cellPx * 0.25f, y, paint)
  }
  for (c in 0 until size) {
    val clue = colClues[c]
    val paint = android.graphics.Paint().apply {
      textSize = textSizePx
      color = (if (satisfiedCols[c]) clueTextColorDone else clueTextColor).toArgbCompat()
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.CENTER
    }
    val x = gutterLeft + c * cellPx + cellPx / 2f
    val lineHeight = textSizePx * 1.15f
    val startY = gutterTop - (clue.size - 1) * lineHeight - textSizePx * 0.55f
    clue.forEachIndexed { idx, num ->
      drawContext.canvas.nativeCanvas.drawText(num.toString(), x, startY + idx * lineHeight, paint)
    }
  }
}

private fun Color.toArgbCompat(): Int {
  val a = (alpha * 255).toInt()
  val r = (red * 255).toInt()
  val g = (green * 255).toInt()
  val b = (blue * 255).toInt()
  return (a shl 24) or (r shl 16) or (g shl 8) or b
}
