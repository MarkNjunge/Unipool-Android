package com.marknkamau.unipool.ui.main

import com.marknkamau.unipool.ui.BaseView
import com.marknkamau.unipool.domain.User

interface MainView : BaseView {
    fun userDoesNotExist()
    fun onUserRetrieved(user: User)
}