package com.nonogram.mvp.ui.theme

  import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
primary = InkPrimary,
onPrimary = PaperBackground,
secondary = Accent,
onSecondary = PaperBackground,
background = PaperBackground,
onBackground = InkPrimary,
surface = PaperSurface,
onSurface = InkPrimary,
surfaceVariant = AccentSoft,
onSurfaceVariant = InkSecondary,
outline = GridLine,
error = ErrorSoft,
)

private val DarkColors = darkColorScheme(
primary = InkPrimaryDark,
onPrimary = CharcoalBackground,
secondary = AccentDark,
onSecondary = CharcoalBackground,
background = CharcoalBackground,
onBackground = InkPrimaryDark,
surface = CharcoalSurface,
onSurface = InkPrimaryDark,
surfaceVariant = AccentSoftDark,
onSurfaceVariant = InkSecondaryDark,
outline = GridLineDark,
error = ErrorSoft,
)

@Composable
fun NonogramTheme(
darkTheme: Boolean = isSystemInDarkTheme(),
content: @Composable () -> Unit,
  ) {
  val colorScheme = if (darkTheme) DarkColors else LightColors
val view = LocalView.current
  if (!view.isInEditMode) {
SideEffect {
  val window = (view.context as Activity).window
  window.statusBarColor = colorScheme.background.toArgb()
  WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
  }
  }

MaterialTheme(
colorScheme = colorScheme,
typography = NonogramTypography,
content = content,
)
}
