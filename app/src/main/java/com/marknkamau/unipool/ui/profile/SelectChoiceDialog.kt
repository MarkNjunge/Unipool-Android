package com.marknkamau.unipool.ui.profile

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import timber.log.Timber

fun choiceDialog(title: String, items: Array<String>, onSelected: (item: String) -> Unit): DialogFragment {
    Timber.i(items.size.toString())
    val bundle = Bundle()
    bundle.putStringArray(SelectChoiceDialog.ITEMS_KEY, items)
    bundle.putString(SelectChoiceDialog.TITLE, title)

    val dialog = SelectChoiceDialog()
    dialog.arguments = bundle
    dialog.setDialogListener(onSelected)

    return dialog
}

class SelectChoiceDialog : DialogFragment() {
    private var onSelected: ((item: String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val items = arguments!!.getStringArray(ITEMS_KEY)
        val title = arguments!!.getString(TITLE)

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(title)
        builder.setItems(items) { _, which -> onSelected?.invoke(items[which]) }

        // Single-choice dialogs don't need buttons because they
        // auto-dismiss when the user makes a choice

        return builder.create()
    }

    fun setDialogListener(onSelected: (item: String) -> Unit) {
        this.onSelected = onSelected
    }

    companion object {
        const val ITEMS_KEY = "items"
        const val TITLE = "title"
    }
}
