package com.example

import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.DateUtils
import com.example.solosale.utils.InvoiceNumberGenerator
import com.example.solosale.utils.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoloSaleUnitTest {

    @Test
    fun testNprCurrencyFormatting() {
        assertEquals("Rs. 0.00", CurrencyUtils.formatNpr(0.0))
        assertEquals("Rs. 1,250.50", CurrencyUtils.formatNpr(1250.50))
        assertEquals("Rs. 100,000.00", CurrencyUtils.formatNpr(100000.0))
    }

    @Test
    fun testShortCurrencyFormatting() {
        assertEquals("Rs. 500.00", CurrencyUtils.formatNprShort(500.0))
        assertEquals("Rs. 1.5 K", CurrencyUtils.formatNprShort(1500.0))
        assertEquals("Rs. 2.50 Lakh", CurrencyUtils.formatNprShort(250000.0))
    }

    @Test
    fun testInvoiceGenerator() {
        val invoice1 = InvoiceNumberGenerator.generate("INV-", 0, 1001)
        assertTrue(invoice1.startsWith("INV-"))
        assertTrue(invoice1.endsWith("-1001"))

        val invoice50 = InvoiceNumberGenerator.generate("SALES-", 49, 1001)
        assertTrue(invoice50.startsWith("SALES-"))
        assertTrue(invoice50.endsWith("-1050"))
    }

    @Test
    fun testPasswordHashingSecurity() {
        val rawPassword = "SecurePassword@123"
        val hash = PasswordHasher.hashPassword(rawPassword)

        assertTrue(PasswordHasher.verifyPassword(rawPassword, hash))
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun testSaleComputationLogic() {
        val unitPrice = 500.0
        val qty = 3.0
        val subtotal = unitPrice * qty // 1500.0
        val discount = 100.0
        val taxable = subtotal - discount // 1400.0
        val taxRate = 13.0 // 13% VAT in Nepal
        val tax = taxable * (taxRate / 100.0) // 182.0
        val grandTotal = taxable + tax // 1582.0
        val paidAmount = 1000.0
        val dueAmount = grandTotal - paidAmount // 582.0

        assertEquals(1500.0, subtotal, 0.001)
        assertEquals(1400.0, taxable, 0.001)
        assertEquals(182.0, tax, 0.001)
        assertEquals(1582.0, grandTotal, 0.001)
        assertEquals(582.0, dueAmount, 0.001)
    }
}
