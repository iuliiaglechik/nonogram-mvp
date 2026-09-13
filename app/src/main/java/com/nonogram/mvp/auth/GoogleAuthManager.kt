package com.nonogram.mvp.auth

  import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Base64

/**
* Wraps Google Sign-In using the AndroidX Credential Manager and
* exchanges the resulting ID token with Firebase Authentication.
  *
  * SETUP REQUIRED before this works (see README.md): create a Firebase
* project, add this Android app, download google-services.json into app/,
* enable Google sign-in method, then paste the Web client ID below.
  */
object AuthConfig {
  const val WEB_CLIENT_ID = "REPLACE_WITH_YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
}

sealed class AuthState {
data object SignedOut : AuthState()
data class SignedIn(val displayName: String?, val email: String?, val photoUrl: String?) : AuthState()
}

class GoogleAuthManager(private val context: Context) {

private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
private val credentialManager: CredentialManager = CredentialManager.create(context)

private val _authState = MutableStateFlow(currentAuthState())
val authState: StateFlow<AuthState> = _authState

private fun currentAuthState(): AuthState {
val user: FirebaseUser? = firebaseAuth.currentUser
return if (user == null) {
AuthState.SignedOut
} else {
AuthState.SignedIn(user.displayName, user.email, user.photoUrl?.toString())
}
}

suspend fun signIn(): Result<AuthState> {
return try {
val nonce = generateNonce()
val googleIdOption = GetGoogleIdOption.Builder()
.setFilterByAuthorizedAccounts(false)
.setServerClientId(AuthConfig.WEB_CLIENT_ID)
.setAutoSelectEnabled(false)
.setNonce(nonce)
.build()

val request = GetCredentialRequest.Builder()
.addCredentialOption(googleIdOption)
.build()

val result = credentialManager.getCredential(context, request)
val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
firebaseAuth.signInWithCredential(firebaseCredential).await()

val newState = currentAuthState()
_authState.value = newState
Result.success(newState)
} catch (e: GetCredentialException) {
Result.failure(e)
} catch (e: Exception) {
Result.failure(e)
}
}

suspend fun signOut() {
firebaseAuth.signOut()
try {
credentialManager.clearCredentialState(ClearCredentialStateRequest())
} catch (_: Exception) {
}
_authState.value = AuthState.SignedOut
}

private fun generateNonce(): String {
val bytes = ByteArray(16)
SecureRandom().nextBytes(bytes)
return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}
}
