package com.nonogram.mvp.model

  import kotlinx.serialization.Serializable

  @Serializable
  data class PuzzleDto(
  val id: String,
  val title: String,
  val size: Int,
  val grid: List<String>,
  val filledCount: Int,
  val difficulty: String,
  )

  enum class Difficulty { EASY, MEDIUM, HARD;

  companion object {
  fun fromString(value: String): Difficulty = when (value.lowercase()) {
  "easy" -> EASY
  "medium" -> MEDIUM
  else -> HARD
  }
  }
  }

  data class Puzzle(
  val id: String,
  val title: String,
  val size: Int,
  val solution: List<List<Boolean>>,
  val difficulty: Difficulty,
  ) {
  val rowClues: List<List<Int>> by lazy { solution.map { row -> clueOf(row) } }
  val colClues: List<List<Int>> by lazy {
  (0 until size).map { c -> clueOf((0 until size).map { r -> solution[r][c] }) }
  }

  private fun clueOf(line: List<Boolean>): List<Int> {
  val clues = mutableListOf<Int>()
  var run = 0
  for (cell in line) {
  if (cell) {
  run++
  } else if (run > 0) {
  clues.add(run)
  run = 0
  }
  }
  if (run > 0) clues.add(run)
  return if (clues.isEmpty()) listOf(0) else clues
  }

  companion object {
  fun fromDto(dto: PuzzleDto): Puzzle = Puzzle(
  id = dto.id,
  title = dto.title,
  size = dto.size,
  solution = dto.grid.map { row -> row.map { it == '1' } },
  difficulty = Difficulty.fromString(dto.difficulty),
  )
  }
  }

  enum class CellState { EMPTY, FILLED, MARKED_X }
