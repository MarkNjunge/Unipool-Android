package com.marknkamau.unipool.ui.profile

import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.marknkamau.type.Gender
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.Vehicle
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.ui.setUpUser.SetUpUserActivity
import com.marknkamau.unipool.utils.containsNumber
import com.marknkamau.unipool.utils.getTrimmedText
import com.marknkamau.unipool.utils.start
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_profile.*
import timber.log.Timber

class ProfileActivity : BaseActivity(), ProfileView {
    private val viewToBeEnabled = mutableListOf<View>()
    private var isInEditMode = false
    private lateinit var presenter: ProfilePresenter
    private var vehiclesAdapter: VehiclesAdapter? = null
    private var vehicles = mutableListOf<Vehicle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this, paperService, apiRepository, authenticationService)
        presenter.getUser()

        viewToBeEnabled.add(etFullName)
        viewToBeEnabled.add(etPhone)
        viewToBeEnabled.add(tvGender)

        rvVehicles.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvVehicles.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        tvGender.setOnClickListener {
            choiceDialog("Select gender", arrayOf("Male", "Female"), { item ->
                tvGender.text = item
            }).show(supportFragmentManager, "SelectGender")
        }

        imgAddVehicle.setOnClickListener {
            val dialog = AddVehicleDialog()

            dialog.addListener(object : AddVehicleDialog.AddVehicleListener {
                override fun onAdded(regNo: String, make: String, passengerCount: Int, color: String) {
                    Timber.i("$regNo $make $passengerCount $color")
                    saveVehicle(regNo, make, passengerCount, color)
                }

                override fun onError(message: String) {
                    toast(message)
                }
            })

            dialog.show(supportFragmentManager, "AddVehicle")
        }

        btnEditSave.setOnClickListener {
            if (isInEditMode) {
                disableEditMode()
                save()
            } else {
                enableEditing()
            }

            isInEditMode = !isInEditMode
        }

        btnCancel.setOnClickListener {
            isInEditMode = false
            disableEditMode()
        }
    }

    private fun saveVehicle(regNo: String, make: String, passengerCount: Int, color: String) {
        pbLoading.visibility = View.VISIBLE
        presenter.saveVehicle(Vehicle(regNo, make, color, passengerCount))
    }

    private fun save() {
        val fullName = etFullName.getTrimmedText()
        val phone = etPhone.getTrimmedText()
        val gender = tvGender.text.toString()

        if (fullName.isEmpty() || phone.isEmpty()) {
            toast("All fields are required")
            return
        }

        if (fullName.containsNumber()) {
            toast("A fullname cannot contain a number")
            return
        }

        presenter.updateUser(fullName, gender, phone.toInt())
    }

    fun updateVehicle(regNo: String, make: String, passengerCount: Int, color: String) {
        val vehicle = Vehicle(regNo, make, color, passengerCount)
        presenter.updateVehicle(vehicle)
    }

    override fun userRetrieved(user: User) {
        etFullName.setText(user.fullName)
        tvStudentNumber.text = user.studentNumber.toString()
        tvEmail.text = user.email
        etPhone.setText("0${user.phone}")
        tvGender.text = if (user.gender == Gender.M) "Male" else "Female"

        if (user.vehicles.isNotEmpty()) {
            setVehiclesAdapter(user.vehicles)
            vehicles.addAll(user.vehicles)

            tvNoVehicles.visibility = View.GONE
        } else {
            rvVehicles.visibility = View.GONE
            tvNoVehicles.visibility = View.VISIBLE
        }
    }

    private fun setVehiclesAdapter(vehicles: MutableList<Vehicle>) {
        vehiclesAdapter = VehiclesAdapter(vehicles, { vehicle, vehicleClickType ->
            if (vehicleClickType == VehiclesAdapter.VehicleClickType.EDIT) {
                val dialog = AddVehicleDialog()

                val bundle = Bundle()
                bundle.putString(AddVehicleDialog.REG_NO, vehicle.registrationNumber)
                bundle.putString(AddVehicleDialog.MAKE, vehicle.make)
                bundle.putString(AddVehicleDialog.COLOR, vehicle.color)
                bundle.putString(AddVehicleDialog.CAPACITY, vehicle.capacity.toString())

                dialog.arguments = bundle

                dialog.addListener(object : AddVehicleDialog.AddVehicleListener {
                    override fun onAdded(regNo: String, make: String, passengerCount: Int, color: String) {
                        updateVehicle(regNo, make, passengerCount, color)
                    }

                    override fun onError(message: String) {
                        toast(message)
                    }
                })

                dialog.show(supportFragmentManager, "UpdateVehicle")
            } else {
                pbLoading.visibility = View.VISIBLE
                presenter.deleteVehicle(vehicle)
            }
        })

        rvVehicles.adapter = vehiclesAdapter
    }

    override fun userDoesNotExist() {
        toast("There are no user details!")
        start(SetUpUserActivity::class.java)
        finish()
    }

    override fun onVehicleSaved(vehicle: Vehicle) {
        toast("The vehicle has been saved")
        pbLoading.visibility = View.INVISIBLE
    }

    override fun onVehicleDeleted(vehicle: Vehicle) {
        pbLoading.visibility = View.INVISIBLE
        toast("Vehicle deleted")
    }

    override fun onVehicleUpdated(vehicle: Vehicle) {
        pbLoading.visibility = View.INVISIBLE
        toast("Vehicle updated")
    }

    override fun displayMessage(message: String) {
        toast(message)
        pbLoading.visibility = View.INVISIBLE
    }

    private fun enableEditing() {
        TransitionManager.beginDelayedTransition(sceneRoot)
        btnEditSave.text = "Save"
        btnCancel.visibility = View.VISIBLE

        viewToBeEnabled.forEach { view ->
            view.isEnabled = true
        }
    }

    private fun disableEditMode() {
        TransitionManager.beginDelayedTransition(sceneRoot)
        btnEditSave.text = "Edit"
        btnCancel.visibility = View.GONE

        viewToBeEnabled.forEach { view ->
            view.isEnabled = false
        }

    }
}
