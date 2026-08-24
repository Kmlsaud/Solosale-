package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.solosale.utils.CurrencyUtils
import com.example.solosale.utils.InvoiceNumberGenerator
import com.example.solosale.utils.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SoloSale", appName)
  }

  @Test
  fun `test currency formatting`() {
    assertEquals("Rs. 1,500.00", CurrencyUtils.formatNpr(1500.0))
    assertEquals("Rs. 15.0 K", CurrencyUtils.formatNprShort(15000.0))
  }

  @Test
  fun `test invoice number generator`() {
    val inv = InvoiceNumberGenerator.generate("INV-", 5, 1001)
    assertTrue(inv.startsWith("INV-"))
    assertTrue(inv.endsWith("-1006"))
  }

  @Test
  fun `test password hashing and verification`() {
    val hash = PasswordHasher.hashPassword("admin123")
    assertTrue(PasswordHasher.verifyPassword("admin123", hash))
  }
}
