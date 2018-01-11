package com.marknkamau.unipool.domain.authentication

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.marknkamau.unipool.BuildConfig
import com.marknkamau.unipool.UnipoolApp
import timber.log.Timber

class AuthenticationServiceImpl(private val googleApiClient: GoogleApiClient) : AuthenticationService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun signIn(result: GoogleSignInResult, listener: AuthenticationService.SignInListener) {
        val account = result.signInAccount
        account?.let {
            val email = account.email!!

            if (!BuildConfig.DEBUG && !email.contains("@strathmore.edu")) {
                listener.onError("Only Strathmore email addresses are allowed")
                signOut(object: AuthenticationService.SignOutListener{
                    override fun onError(reason: String) {

                    }

                    override fun onSuccess() {

                    }
                })
                return
            }

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        listener.onSuccess(email)
                    }
                    .addOnFailureListener {
                        listener.onError(it.message ?: "Failed to sign in with ${account.email}")
                    }

        }
    }

    override fun signOut(listener: AuthenticationService.SignOutListener) {
        Auth.GoogleSignInApi.signOut(googleApiClient)
                .setResultCallback { status: Status ->
                    val success = status.isSuccess
                    Timber.i(success.toString())

                    if (success) {
                        firebaseAuth.signOut()
                        listener.onSuccess()
                        Timber.i("Signed out")
                    } else {
                        listener.onError(status.statusMessage ?: "Error signing out")
                    }
                }
    }

    override fun isSignedIn() = firebaseAuth.currentUser != null

    override fun currentUserEmail(): String {
        return if (isSignedIn()) {
            firebaseAuth.currentUser?.email!! // It cant be null
        } else {
            "null"
        }
    }

    override fun currentUserId(): String {
        return if (isSignedIn()) {
            firebaseAuth.currentUser?.uid!! // It cant be null
        } else {
            "null"
        }
    }

}