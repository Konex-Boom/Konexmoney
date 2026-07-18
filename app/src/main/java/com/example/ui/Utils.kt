package com.example.ui

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    private val currencyFormat = DecimalFormat("#,###")

    fun formatAriary(amount: Double): String {
        // Formats currency nicely like 1.250.000
        val formatted = currencyFormat.format(amount)
        return formatted.replace(",", ".").replace("\u00A0", ".")
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    fun formatDateToHuman(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }

    fun getDaysRemaining(targetTimestamp: Long): Int {
        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)

        val targetCal = Calendar.getInstance()
        targetCal.timeInMillis = targetTimestamp
        targetCal.set(Calendar.HOUR_OF_DAY, 0)
        targetCal.set(Calendar.MINUTE, 0)
        targetCal.set(Calendar.SECOND, 0)
        targetCal.set(Calendar.MILLISECOND, 0)

        val diff = targetCal.timeInMillis - todayCal.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}
