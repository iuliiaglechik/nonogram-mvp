package com.nonogram.mvp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nonogram.mvp.auth.AuthState
import com.nonogram.mvp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: AuthViewModel,
  onBack: () -> Unit,
  ) {
  val state by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Profile") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )
    },
    containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      ) {
      Spacer(Modifier.height(32.dp))

      when (val authState = state.authState) {
        is AuthState.SignedIn -> SignedInContent(authState, onSignOut = viewModel::signOut)
        is AuthState.SignedOut -> SignedOutContent(
          isSigningIn = state.isSigningIn,
          onSignIn = viewModel::signIn,
          )
      }

      state.errorMessage?.let { message ->
        Spacer(Modifier.height(16.dp))
        Text(
          message,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          )
      }
    }
  }
}

@Composable
private fun SignedOutContent(isSigningIn: Boolean, onSignIn: () -> Unit) {
  Icon(
    Icons.Filled.Person,
    contentDescription = null,
    modifier = Modifier.size(72.dp),
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  Spacer(Modifier.height(16.dp))
  Text(
    "Sign in to sync your progress",
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onBackground,
    )
  Spacer(Modifier.height(4.dp))
  Text(
    "Your solved puzzles stay on this device unless you sign in.",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
    )
  Spacer(Modifier.height(28.dp))

  OutlinedButton(
    onClick = onSignIn,
    enabled = !isSigningIn,
    shape = RoundedCornerShape(24.dp),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
    modifier = Modifier.fillMaxWidth(0.85f).height(52.dp),
    ) {
    if (isSigningIn) {
      CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
      Spacer(Modifier.width(10.dp))
      Text("Signing in...")
    } else {
      Text("Continue with Google")
    }
  }
}

@Composable
private fun SignedInContent(state: AuthState.SignedIn, onSignOut: () -> Unit) {
  Surface(
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier.size(84.dp),
    ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = (state.displayName?.firstOrNull() ?: state.email?.firstOrNull() ?: '*').uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
  }
  Spacer(Modifier.height(16.dp))
  Text(
    state.displayName ?: "Signed in",
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onBackground,
    )
  state.email?.let {
    Spacer(Modifier.height(2.dp))
    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
  Spacer(Modifier.height(28.dp))
  Button(
    onClick = onSignOut,
    shape = RoundedCornerShape(24.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
    modifier = Modifier.fillMaxWidth(0.85f).height(52.dp),
    ) {
    Text("Sign out")
  }
}
