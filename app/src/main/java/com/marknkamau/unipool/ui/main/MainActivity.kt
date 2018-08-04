package com.marknkamau.unipool.ui.main

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.support.design.widget.BottomNavigationView
import android.view.View
import com.marknkamau.unipool.R
import com.marknkamau.unipool.UnipoolApp
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.ui.login.LoginActivity
import com.marknkamau.unipool.ui.main.driver.DriverMainFragment
import com.marknkamau.unipool.ui.main.rider.RiderMainFragment
import com.marknkamau.unipool.ui.setUpUser.SetUpUserActivity
import com.marknkamau.unipool.utils.start
import com.marknkamau.unipool.utils.toast
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : BaseActivity(), MainView {
    private lateinit var presenter: MainPresenter
    private var user: User? = null
    private val riderFragment by lazy { RiderMainFragment() }
    private val driverFragment by lazy { DriverMainFragment() }
    private val riderFragmentId = 1
    private val driverFragmentId = 2
    private var currentFragment = 0

    private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_rider -> {
                changeFragment(riderFragmentId)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_driver -> {
                changeFragment(driverFragmentId)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!UnipoolApp.authService.isSignedIn()) {
            start(LoginActivity::class.java)
            finish()
            return
        }

        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener)

        changeFragment(riderFragmentId)

        presenter = MainPresenter(this, UnipoolApp.authService, UnipoolApp.apiRepository, UnipoolApp.localStorage)

        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        presenter.getCurrentUser()
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    private fun changeFragment(fragmentId: Int) {
        if (currentFragment != fragmentId) {
            if (fragmentId == riderFragmentId) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, riderFragment)
                        .commit()
            } else if (fragmentId == driverFragmentId) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, driverFragment)
                        .commit()
            }
        }
        currentFragment = fragmentId
    }

    override fun displayMessage(message: String) {
        toast(message)
        pbLoading.visibility = View.GONE
    }

    override fun userDoesNotExist() {
        pbLoading.visibility = View.GONE
        toast("Sign up has not been completed")
        start(SetUpUserActivity::class.java)
        finish()
    }

    override fun onUserRetrieved(user: User) {
        this.user = user
        displayViews()
        driverFragment.user = user
    }

    private fun hideViews() {
        fragmentHolder.visibility = View.GONE
    }

    private fun displayViews() {
        fragmentHolder.visibility = View.VISIBLE
        pbLoading.visibility = View.GONE
    }

    private fun requestPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe { granted ->
                    if (granted) {
                        presenter.getCurrentUser()
                        displayViews()
                    } else {
                        toast("Location permission is required!")
                        hideViews()
                    }
                }
    }

    fun canViewMap(): Boolean {
        return if (canConnectToInternet() && isLocationEnabled()) {
            true
        } else if (!isLocationEnabled()) {
            toast("Location is not enabled")
            false
        } else if (!canConnectToInternet()) {
            toast("There is no internet connection")
            false
        } else {
            false
        }
    }

    private fun canConnectToInternet(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun isLocationEnabled(): Boolean {
        return try {
            val locationMode: Int = Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE)
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } catch (e: SettingNotFoundException) {
            Timber.e(e)
            false
        }
    }
}
