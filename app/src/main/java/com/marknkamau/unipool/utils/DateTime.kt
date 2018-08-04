package com.marknkamau.unipool.utils

import java.text.SimpleDateFormat
import java.util.*

data class DateTime(var hourOfDay: Int, var minute: Int, var year: Int, var month: Int, var dayOfMonth: Int, var isAm: Boolean) {

    fun format(format: String): String {
        val now = Calendar.getInstance()
        now.set(this.year, this.month, this.dayOfMonth, this.hourOfDay, this.minute)
        return now.time.format(format)
    }

    fun asTimestamp(): Long {
        val now = Calendar.getInstance()
        now.set(this.year, this.month, this.dayOfMonth, this.hourOfDay, this.minute)
        return now.time.time
    }

    companion object {
        const val TIME_FORMAT = "h:mm a" // Display as 12Hr from 1-12 e.g. 6:00 AM
        const val DATE_FORMAT = "dd MMM yy" // e.g. 27 Sep 17

        fun getNow() = Calendar.getInstance().time.toDateTime()

        fun fromTimestamp(timestamp: Long): DateTime = Date(timestamp).toDateTime()

        private fun Date.toDateTime(): DateTime {
            val hourOfDay = this.format("H").toInt() // Format according to 24Hr from 0-23
            val minute = this.format("m").toInt()
            val year = this.format("yyyy").toInt()
            val month = this.format("M").toInt()
            val dayOfMonth = this.format("dd").toInt()
            val isAm = this.format("a") == "AM"

            return DateTime(hourOfDay, minute, year, month, dayOfMonth, isAm)
        }

        fun Date.format(pattern: String): String = SimpleDateFormat(pattern).format(this)
    }
}
