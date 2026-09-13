package com.nonogram.mvp

  import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nonogram.mvp.auth.GoogleAuthManager
import com.nonogram.mvp.data.PuzzleRepository
import com.nonogram.mvp.ui.navigation.NonogramNavHost
import com.nonogram.mvp.ui.theme.NonogramTheme

class MainActivity : ComponentActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
enableEdgeToEdge()

val repository = PuzzleRepository(applicationContext)
val authManager = GoogleAuthManager(applicationContext)

setContent {
  NonogramTheme {
  NonogramNavHost(repository = repository, authManager = authManager)
  }
  }
  }
}
