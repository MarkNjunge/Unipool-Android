package com.marknkamau.unipool.ui.preFindRequests

import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.ui.BaseView

interface PreFindRequestsView : BaseView {
    fun onUserRetrieved(user: User)
}