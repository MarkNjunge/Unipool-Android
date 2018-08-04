package com.marknkamau.unipool.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.marknkamau.unipool.R
import com.marknkamau.unipool.utils.getTrimmedText
import com.marknkamau.unipool.utils.isValidRegistrationNumber
import kotlinx.android.synthetic.main.dialog_add_vehicle.view.*

class AddVehicleDialog : DialogFragment() {
    private var listener: AddVehicleListener? = null

    interface AddVehicleListener {
        fun onAdded(regNo: String, make: String, passengerCount: Int, color: String)
        fun onError(message: String)
    }

    fun addListener(listener: AddVehicleListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_vehicle, null)

        arguments?.let {
            val passedRegNo = it.getString(REG_NO, "")
            if (passedRegNo.isNotEmpty()) {
                view.etRegNo.setText(passedRegNo)
                view.etRegNo.isEnabled = false
            }

            val passedMake = it.getString(MAKE, "")
            if (passedMake.isNotEmpty()) {
                view.etMake.setText(passedMake)
            }

            val passedColor = it.getString(COLOR, "")
            if (passedColor.isNotEmpty()) {
                view.etColor.setText(passedColor)
            }

            val passedCapacity = it.getString(CAPACITY, "")
            if (passedCapacity.isNotEmpty()) {
                view.etPassengers.setText(passedCapacity)
            }
        }

        view.btnAdd.setOnClickListener {
            val regNo = view.etRegNo.getTrimmedText()
            val make = view.etMake.getTrimmedText()
            val passengerCount = view.etPassengers.getTrimmedText()
            val color = view.etColor.getTrimmedText()

            listener?.let {
                if (regNo.isEmpty() || make.isEmpty() || passengerCount.isEmpty() || color.isEmpty()) {
                    it.onError("All fields are required")
                } else if (!regNo.isValidRegistrationNumber()) {
                    it.onError("Registration number is invalid")
                } else if (passengerCount.toInt() == 0) {
                    it.onError("A vehicle requires at least 1 passenger space")
                } else if (passengerCount.toInt() > 7) {
                    it.onError("A vehicle can't have more than 7 passengers")
                } else {
                    it.onAdded(regNo, make, passengerCount.toInt(), color)
                    dismiss()
                }
            }
        }

        builder.setView(view)

        return builder.create()
    }

    companion object {
        const val REG_NO = "reg_no"
        const val MAKE = "make"
        const val CAPACITY = "capacity"
        const val COLOR = "color"
    }
}