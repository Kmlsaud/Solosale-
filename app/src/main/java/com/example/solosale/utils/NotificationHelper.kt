package com.example.solosale.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_STOCK = "solosale_stock_alerts"
    private const val CHANNEL_NAME = "SoloSale Alerts"
    private const val CHANNEL_DESC = "Notifications for stock alerts and business reminders"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_STOCK,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendLowStockAlert(context: Context, productName: String, currentStock: Double, unit: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_STOCK)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Low Stock Alert: $productName")
            .setContentText("$productName has only $currentStock $unit remaining. Please restock soon.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(productName.hashCode(), notification)
    }

    fun sendDueReminder(context: Context, customerName: String, dueAmount: Double) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_STOCK)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Due Payment Reminder")
            .setContentText("$customerName has pending due balance of ${CurrencyUtils.formatNpr(dueAmount)}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(customerName.hashCode(), notification)
    }
}
