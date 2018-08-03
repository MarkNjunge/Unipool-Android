package com.marknkamau.unipool.ui.main

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class SelectVehicleDialog : DialogFragment() {
    private var onSelected: ((item: String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Choose vehicle")

        arguments?.let {
            val items = it.getStringArray(ITEMS)

            builder.setItems(items) { _, which ->
                onSelected?.invoke(items[which])
            }
        }

        return builder.create()
    }

    fun setListener(onSelected: (item: String) -> Unit) {
        this.onSelected = onSelected
    }

    companion object {
        val ITEMS = "items"
    }
}