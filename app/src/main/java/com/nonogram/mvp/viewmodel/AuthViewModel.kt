package com.nonogram.mvp.viewmodel

  import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
      import androidx.lifecycle.viewModelScope
        import com.nonogram.mvp.auth.AuthState
          import com.nonogram.mvp.auth.GoogleAuthManager
            import kotlinx.coroutines.flow.MutableStateFlow
              import kotlinx.coroutines.flow.StateFlow
                import kotlinx.coroutines.flow.asStateFlow
                  import kotlinx.coroutines.launch

                    data class AuthUiState(
                      val authState: AuthState = AuthState.SignedOut,
                      val isSigningIn: Boolean = false,
                      val errorMessage: String? = null,
                    )

                      class AuthViewModel(private val authManager: GoogleAuthManager) : ViewModel() {

                        private val _uiState = MutableStateFlow(AuthUiState(authState = authManager.authState.value))
                        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

                        fun signIn() {
                          if (_uiState.value.isSigningIn) return
                            _uiState.value = _uiState.value.copy(isSigningIn = true, errorMessage = null)
                            viewModelScope.launch {
                              val result = authManager.signIn()
                              _uiState.value = result.fold(
                                onSuccess = { state -> AuthUiState(authState = state) },
                                onFailure = { e ->
                                             AuthUiState(
                                               authState = AuthState.SignedOut,
                                               errorMessage = e.message ?: "Sign-in failed. Please try again.",
                                             )
                                            },
                              )
                            }
                        }

                            fun signOut() {
                              viewModelScope.launch {
                                authManager.signOut()
                                _uiState.value = AuthUiState(authState = AuthState.SignedOut)
                              }
                            }

                            fun dismissError() {
                              _uiState.value = _uiState.value.copy(errorMessage = null)
                            }

                            class Factory(private val authManager: GoogleAuthManager) : ViewModelProvider.Factory {
                              @Suppress("UNCHECKED_CAST")
                              override fun <T : ViewModel> create(modelClass: Class<T>): T =
                              AuthViewModel(authManager) as T
                            }
                              }
