package com.marknkamau.unipool.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.ui.login.LoginActivity
import com.marknkamau.unipool.ui.pastRides.PastRidesActivity
import com.marknkamau.unipool.ui.profile.ProfileActivity
import com.marknkamau.unipool.utils.app
import com.marknkamau.unipool.utils.start
import com.marknkamau.unipool.utils.toast
import timber.log.Timber

@SuppressLint("Registered") // Not required in manifest
open class BaseActivity : AppCompatActivity(), AuthenticationService.SignOutListener {
    protected val paperService by lazy { app.localStorage }
    protected val apiRepository by lazy { app.apiService }
    protected val directionsHelper by lazy { app.directionsHelper }
    protected val authenticationService by lazy { app.authService }
    protected val mqttHelper by lazy { app.mqttHelper }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (this is ProfileActivity) {
            menu?.findItem(R.id.menu_profile)?.isVisible = false
        }
        if (this is PastRidesActivity) {
            menu?.findItem(R.id.menu_past_rides)?.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign_out -> {
                authenticationService.signOut(this)
                paperService.deleteUser()
                true
            }
            R.id.menu_profile -> {
                start(ProfileActivity::class.java)
                true
            }
            R.id.menu_past_rides -> {
                start(PastRidesActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSuccess() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()
    }

    override fun onError(reason: String) {
        toast(reason)
        Timber.e(reason)
    }
}