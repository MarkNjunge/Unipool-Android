package com.marknkamau.unipool.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import com.google.android.gms.auth.api.Auth
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.ui.main.MainActivity
import com.marknkamau.unipool.ui.setUpUser.SetUpUserActivity
import com.marknkamau.unipool.utils.app
import com.marknkamau.unipool.utils.start
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity : BaseActivity(), AuthenticationService.SignInListener, LogInView {
    private val SIGN_IN_REQUEST = 0
    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this, authenticationService, apiRepository, paperService)

        btnSignIn.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(app.googleApiClient)
            startActivityForResult(signInIntent, SIGN_IN_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST && resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {
                TransitionManager.beginDelayedTransition(activityLogIn)
                pbLoading.visibility = View.VISIBLE
                authenticationService.signIn(result, this)
            } else {
                Timber.e("Sign in result was empty")
                toast("Sign in failed")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    override fun onSuccess(email: String) {
        presenter.getCurrentUser()
    }

    override fun onError(reason: String) {
        toast(reason)
        pbLoading.visibility = View.GONE
    }

    override fun displayMessage(message: String) = toast(message)

    override fun userDoesNotExist() {
        pbLoading.visibility = View.GONE
        start(SetUpUserActivity::class.java)
        finish()
    }


    override fun userExists() {
        toast("You have been signed in")
        start(MainActivity::class.java)
        finish()
    }
}
