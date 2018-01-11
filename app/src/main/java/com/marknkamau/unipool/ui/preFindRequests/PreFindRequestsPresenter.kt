package com.marknkamau.unipool.ui.preFindRequests

import com.marknkamau.unipool.domain.data.local.LocalStorageService

class PreFindRequestsPresenter(val view: PreFindRequestsView, val localStorageService: LocalStorageService) {

    fun getUser(){
        val user = localStorageService.getUser()
        user?.let {
            view.onUserRetrieved(user)
        }
    }

}