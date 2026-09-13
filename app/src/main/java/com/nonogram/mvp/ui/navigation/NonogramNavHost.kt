package com.nonogram.mvp.ui.navigation

  import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nonogram.mvp.auth.GoogleAuthManager
import com.nonogram.mvp.data.PuzzleRepository
import com.nonogram.mvp.ui.screens.ProfileScreen
import com.nonogram.mvp.ui.screens.PuzzleListScreen
import com.nonogram.mvp.ui.screens.PuzzleScreen
import com.nonogram.mvp.viewmodel.AuthViewModel
import com.nonogram.mvp.viewmodel.PuzzleListViewModel
import com.nonogram.mvp.viewmodel.PuzzleViewModel

private object Routes {
  const val LIST = "list"
const val PROFILE = "profile"
const val PUZZLE = "puzzle/{puzzleId}"
fun puzzle(id: String) = "puzzle/$id"
}

@Composable
fun NonogramNavHost(
repository: PuzzleRepository,
authManager: GoogleAuthManager,
navController: NavHostController = rememberNavController(),
) {
NavHost(navController = navController, startDestination = Routes.LIST) {
composable(Routes.LIST) {
val listViewModel: PuzzleListViewModel = viewModel(factory = PuzzleListViewModel.Factory(repository))
PuzzleListScreen(
viewModel = listViewModel,
onOpenPuzzle = { id -> navController.navigate(Routes.puzzle(id)) },
onOpenProfile = { navController.navigate(Routes.PROFILE) },
)
}
composable(Routes.PUZZLE) { backStackEntry ->
val puzzleId = backStackEntry.arguments?.getString("puzzleId") ?: return@composable
val puzzleViewModel: PuzzleViewModel = viewModel(
key = puzzleId,
factory = PuzzleViewModel.Factory(repository, puzzleId),
)
PuzzleScreen(viewModel = puzzleViewModel, onBack = { navController.popBackStack() })
}
composable(Routes.PROFILE) {
val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authManager))
ProfileScreen(viewModel = authViewModel, onBack = { navController.popBackStack() })
}
}
}
