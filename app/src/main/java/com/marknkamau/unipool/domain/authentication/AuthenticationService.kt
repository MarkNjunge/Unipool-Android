package com.marknkamau.unipool.domain.authentication

import com.google.android.gms.auth.api.signin.GoogleSignInResult

interface AuthenticationService {
    fun signIn(result: GoogleSignInResult, listener: SignInListener)
    fun signOut(listener: SignOutListener)
    fun isSignedIn(): Boolean
    fun currentUserId(): String
    fun currentUserEmail(): String

    interface SignOutListener {
        fun onSuccess()
        fun onError(reason: String)
    }

    interface SignInListener {
        fun onSuccess(email: String)
        fun onError(reason: String)
    }
}
