package com.nonogram.mvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nonogram.mvp.data.PuzzleRepository
import com.nonogram.mvp.model.Puzzle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PuzzleListItem(
  val puzzle: Puzzle,
  val completed: Boolean,
  )

data class PuzzleListUiState(
  val items: List<PuzzleListItem> = emptyList(),
  val isLoading: Boolean = true,
  )

class PuzzleListViewModel(private val repository: PuzzleRepository) : ViewModel() {

  private val puzzlesFlow = MutableStateFlow<List<Puzzle>>(emptyList())
  private val isLoadingFlow = MutableStateFlow(true)

  val uiState: StateFlow<PuzzleListUiState> = combine(
    puzzlesFlow,
    repository.completedIds,
    isLoadingFlow,
    ) { puzzles, completed, loading ->
    PuzzleListUiState(
      items = puzzles.map { PuzzleListItem(it, completed.contains(it.id)) },
      isLoading = loading,
      )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PuzzleListUiState())

  init {
    viewModelScope.launch {
      val puzzles = repository.loadPuzzles()
      puzzlesFlow.value = puzzles
      isLoadingFlow.value = false
    }
  }

  class Factory(private val repository: PuzzleRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
    PuzzleListViewModel(repository) as T
  }
}
