package com.marknkamau.unipool.ui.setUpUser

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.ui.login.LoginActivity
import com.marknkamau.unipool.ui.main.MainActivity
import com.marknkamau.unipool.utils.*
import kotlinx.android.synthetic.main.activity_set_up_user.*

class SetUpUserActivity : BaseActivity(), SetUpUserView {
    private lateinit var presenter: SetUpUserPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_user)

        presenter = SetUpUserPresenter(this, paperService, authenticationService, apiRepository)

        etFullName.setText(extractNameFromEmail(authenticationService.currentUserEmail()))

        btnContinue.setOnClickListener {
            addUser()
        }

        btnLogout.setOnClickListener {
            authenticationService.signOut(object : AuthenticationService.SignOutListener {
                override fun onSuccess() {
                    start(LoginActivity::class.java)
                    finish()
                }

                override fun onError(reason: String) {
                    toast(reason)
                }
            })
        }
    }

    private fun addUser() {
        val fullName = etFullName.getTrimmedText()
        val phone = etPhone.getTrimmedText()
        val studentNumber = etStudentNumber.getTrimmedText()
        val gender = findViewById<RadioButton>(rgGender.checkedRadioButtonId).text.toString()

        if (fullName.isEmpty() || phone.isEmpty() || studentNumber.isEmpty()) {
            toast("All fields are required")
            return
        }

        if (fullName.containsNumber()) {
            toast("A fullname cannot contain a number")
            return
        }

        presenter.addUser(fullName, phone.toInt(), studentNumber.toInt(), gender)
        pbLoading.visibility = View.VISIBLE
    }

    override fun onUserCreated() {
        toast("Sign up complete!")
        pbLoading.visibility = View.GONE
        start(MainActivity::class.java)
        finish()
    }

    override fun displayMessage(message: String) {
        pbLoading.visibility = View.GONE
        toast(message)
    }
}
