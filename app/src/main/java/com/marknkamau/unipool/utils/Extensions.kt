package com.marknkamau.unipool.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.marknkamau.unipool.UnipoolApp
import java.math.BigDecimal

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, message, length).show()

val Activity.app: UnipoolApp
    get() = application as UnipoolApp

fun EditText.getTrimmedText() = this.text.toString().trim()
fun Double.roundTo(decimals: Int) =
        BigDecimal(this).setScale(decimals, BigDecimal.ROUND_HALF_UP).toDouble()

fun Context.start(cls: Class<*>) {
    this.startActivity(Intent(this, cls))
}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun String.containsNumber() = this.contains(Regex("(?:\\d*\\.)?\\d+"))

fun String.isValidRegistrationNumber() = this.replace(" ", "").matches(Regex("[kK][a-zA-Z]{2}\\d{3}[a-zA-Z]"))