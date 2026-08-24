package com.example.solosale.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoiceNumberGenerator {
    fun generate(prefix: String = "INV-", lastId: Long = 0, startNumber: Long = 1001): String {
        val nextNum = if (lastId <= 0) startNumber else (startNumber + lastId)
        val datePart = SimpleDateFormat("yyMM", Locale.US).format(Date())
        return "$prefix$datePart-$nextNum"
    }
}
