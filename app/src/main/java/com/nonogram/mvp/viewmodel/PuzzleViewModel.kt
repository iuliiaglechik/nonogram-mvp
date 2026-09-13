package com.nonogram.mvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nonogram.mvp.data.PuzzleRepository
import com.nonogram.mvp.model.CellState
import com.nonogram.mvp.model.Puzzle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DrawTool { FILL, MARK_X }

data class PuzzleUiState(
  val puzzle: Puzzle? = null,
  val cells: List<List<CellState>> = emptyList(),
  val isSolved: Boolean = false,
  val tool: DrawTool = DrawTool.FILL,
  val canUndo: Boolean = false,
  val isLoading: Boolean = true,
  )

class PuzzleViewModel(
  private val repository: PuzzleRepository,
  private val puzzleId: String,
  ) : ViewModel() {

  private val _uiState = MutableStateFlow(PuzzleUiState())
  val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

  private val undoStack = ArrayDeque<List<List<CellState>>>()

  init {
    viewModelScope.launch {
      val puzzle = repository.loadPuzzles().first { it.id == puzzleId }
      val saved = repository.loadProgress(puzzleId)
      val cells = saved?.let { decode(it, puzzle.size) }
      ?: List(puzzle.size) { List(puzzle.size) { CellState.EMPTY } }
      _uiState.value = PuzzleUiState(
        puzzle = puzzle,
        cells = cells,
        isSolved = isSolved(puzzle, cells),
        isLoading = false,
        )
    }
  }

  fun selectTool(tool: DrawTool) {
    _uiState.value = _uiState.value.copy(tool = tool)
  }

  fun onCellTap(row: Int, col: Int) {
    val state = _uiState.value
    if (state.isSolved || state.puzzle == null) return

    pushUndo(state.cells)
    val current = state.cells[row][col]
    val newState = when (state.tool) {
      DrawTool.FILL -> if (current == CellState.FILLED) CellState.EMPTY else CellState.FILLED
      DrawTool.MARK_X -> if (current == CellState.MARKED_X) CellState.EMPTY else CellState.MARKED_X
    }
    applyChange(row, col, newState)
  }

  fun undo() {
    val previous = undoStack.removeLastOrNull() ?: return
    val puzzle = _uiState.value.puzzle ?: return
    _uiState.value = _uiState.value.copy(
      cells = previous,
      isSolved = isSolved(puzzle, previous),
      canUndo = undoStack.isNotEmpty(),
      )
    persist(previous)
  }

  fun resetPuzzle() {
    val puzzle = _uiState.value.puzzle ?: return
    val blank = List(puzzle.size) { List(puzzle.size) { CellState.EMPTY } }
    undoStack.clear()
    _uiState.value = _uiState.value.copy(cells = blank, isSolved = false, canUndo = false)
    viewModelScope.launch { repository.clearProgress(puzzleId) }
  }

  private fun pushUndo(cells: List<List<CellState>>) {
    undoStack.addLast(cells)
    if (undoStack.size > 50) undoStack.removeFirst()
  }

  private fun applyChange(row: Int, col: Int, newState: CellState) {
    val puzzle = _uiState.value.puzzle ?: return
    val updated = _uiState.value.cells.mapIndexed { r, line ->
      if (r != row) line else line.mapIndexed { c, v -> if (c == col) newState else v }
    }
    val solved = isSolved(puzzle, updated)
    _uiState.value = _uiState.value.copy(cells = updated, isSolved = solved, canUndo = true)
    persist(updated)
    if (solved) {
      viewModelScope.launch { repository.markCompleted(puzzleId) }
    }
  }

  private fun isSolved(puzzle: Puzzle, cells: List<List<CellState>>): Boolean {
    for (r in cells.indices) {
      for (c in cells[r].indices) {
        val shouldBeFilled = puzzle.solution[r][c]
        val isFilled = cells[r][c] == CellState.FILLED
        if (shouldBeFilled != isFilled) return false
      }
    }
    return true
  }

  private fun persist(cells: List<List<CellState>>) {
    viewModelScope.launch { repository.saveProgress(puzzleId, encode(cells)) }
  }

  private fun encode(cells: List<List<CellState>>): String =
  cells.joinToString("") { row ->
    row.joinToString("") {
      when (it) {
        CellState.EMPTY -> "E"
        CellState.FILLED -> "F"
        CellState.MARKED_X -> "X"
      }
    }
  }

  private fun decode(encoded: String, size: Int): List<List<CellState>> {
    if (encoded.length != size * size) return List(size) { List(size) { CellState.EMPTY } }
    return (0 until size).map { r ->
      (0 until size).map { c ->
        when (encoded[r * size + c]) {
          'F' -> CellState.FILLED
          'X' -> CellState.MARKED_X
          else -> CellState.EMPTY
        }
      }
    }
  }

  class Factory(
    private val repository: PuzzleRepository,
    private val puzzleId: String,
    ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
    PuzzleViewModel(repository, puzzleId) as T
  }
}
