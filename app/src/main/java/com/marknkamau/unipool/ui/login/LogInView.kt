package com.marknkamau.unipool.ui.login

import com.marknkamau.unipool.ui.BaseView

interface LogInView : BaseView {
    fun userDoesNotExist()
    fun userExists()
}
