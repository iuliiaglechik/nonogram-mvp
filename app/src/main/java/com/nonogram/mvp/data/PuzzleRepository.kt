package com.nonogram.mvp.data

  import android.content.Context
  import androidx.datastore.preferences.core.edit
  import androidx.datastore.preferences.core.stringSetPreferencesKey
  import androidx.datastore.preferences.preferencesDataStore
  import com.nonogram.mvp.model.Puzzle
  import com.nonogram.mvp.model.PuzzleDto
  import kotlinx.coroutines.flow.Flow
  import kotlinx.coroutines.flow.first
  import kotlinx.coroutines.flow.map
  import kotlinx.serialization.json.Json

  private val Context.dataStore by preferencesDataStore(name = "nonogram_progress")

  class PuzzleRepository(private val context: Context) {

  private var cache: List<Puzzle>? = null

    suspend fun loadPuzzles(): List<Puzzle> {
    cache?.let { return it }
    val json = context.assets.open("puzzles.json").bufferedReader().use { it.readText() }
    val dtos = Json { ignoreUnknownKeys = true }.decodeFromString<List<PuzzleDto>>(json)
      val puzzles = dtos.map { Puzzle.fromDto(it) }
    cache = puzzles
      return puzzles
  }

  private val completedKey = stringSetPreferencesKey("completed_puzzle_ids")

    val completedIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
      prefs[completedKey] ?: emptySet()
                                                                     }

  suspend fun isCompleted(puzzleId: String): Boolean =
    completedIds.first().contains(puzzleId)

    suspend fun markCompleted(puzzleId: String) {
    context.dataStore.edit { prefs ->
      val current = prefs[completedKey] ?: emptySet()
      prefs[completedKey] = current + puzzleId
                           }
  }

  private fun progressKey(puzzleId: String) = androidx.datastore.preferences.core.stringPreferencesKey("progress_$puzzleId")

    suspend fun saveProgress(puzzleId: String, encoded: String) {
    context.dataStore.edit { prefs -> prefs[progressKey(puzzleId)] = encoded }
  }

  suspend fun loadProgress(puzzleId: String): String? {
    return context.dataStore.data.first()[progressKey(puzzleId)]
  }

  suspend fun clearProgress(puzzleId: String) {
    context.dataStore.edit { prefs -> prefs.remove(progressKey(puzzleId)) }
  }
}
