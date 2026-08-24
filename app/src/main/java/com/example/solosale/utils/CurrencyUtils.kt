package com.example.solosale.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val format = DecimalFormat("#,##,##0.00") // South Asian / Nepali grouping

    fun formatNpr(amount: Double, symbol: String = "Rs."): String {
        return try {
            val formatted = format.format(amount)
            "$symbol $formatted"
        } catch (e: Exception) {
            String.format(Locale.US, "$symbol %.2f", amount)
        }
    }

    fun formatNprShort(amount: Double, symbol: String = "Rs."): String {
        return when {
            amount >= 10_000_000 -> String.format(Locale.US, "$symbol %.2f Cr", amount / 10_000_000)
            amount >= 100_000 -> String.format(Locale.US, "$symbol %.2f Lakh", amount / 100_000)
            amount >= 1_000 -> String.format(Locale.US, "$symbol %.1f K", amount / 1_000)
            else -> formatNpr(amount, symbol)
        }
    }
}
