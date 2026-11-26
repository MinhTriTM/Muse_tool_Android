package com.musetool.superapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.musetool.superapp.MainActivity
import com.musetool.superapp.R
import java.text.NumberFormat
import java.util.Locale

class MuseNote20UltraWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_NOTE20_WIDGET) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, MuseNote20UltraWidgetProvider::class.java)
            )
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH_NOTE20_WIDGET = "com.musetool.superapp.ACTION_REFRESH_NOTE20_WIDGET"
        private const val PREFS_NAME = "muse_note20_ultra_widget_cache"

        fun syncDataToNote20Widget(
            context: Context,
            freeRemainingUsd: Double,
            freeUsedUsd: Double,
            outstandingUsd: Double,
            todayUsd: Double,
            weekUsd: Double,
            monthUsd: Double,
            yearUsd: Double,
            totalUsd: Double,
            vndRate: Double,
            profileName: String,
            versionName: String,
            topModelName: String
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat("free_rem", freeRemainingUsd.toFloat())
                .putFloat("free_used", freeUsedUsd.toFloat())
                .putFloat("outstanding", outstandingUsd.toFloat())
                .putFloat("today", todayUsd.toFloat())
                .putFloat("week", weekUsd.toFloat())
                .putFloat("month", monthUsd.toFloat())
                .putFloat("year", yearUsd.toFloat())
                .putFloat("total", totalUsd.toFloat())
                .putFloat("vnd_rate", vndRate.toFloat())
                .putString("profile_name", profileName)
                .putString("version_name", versionName)
                .putString("top_model", topModelName)
                .apply()

            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, MuseNote20UltraWidgetProvider::class.java)
            )
            for (id in ids) {
                updateSingleWidget(context, manager, id)
            }
        }

        fun requestPinNote20Widget(context: Context): Boolean {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, MuseNote20UltraWidgetProvider::class.java)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
            } else {
                false
            }
        }

        private fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val freeRem = prefs.getFloat("free_rem", 6.40f).toDouble()
            val outstanding = prefs.getFloat("outstanding", 4.85f).toDouble()
            val today = prefs.getFloat("today", 1.85f).toDouble()
            val week = prefs.getFloat("week", 6.42f).toDouble()
            val month = prefs.getFloat("month", 14.90f).toDouble()
            val total = prefs.getFloat("total", 28.45f).toDouble()
            val vndRate = prefs.getFloat("vnd_rate", 25450f).toDouble()
            val profileName = prefs.getString("profile_name", "Kazuto Yui") ?: "Kazuto Yui"
            val versionName = prefs.getString("version_name", "2.4.0") ?: "2.4.0"
            val topModel = prefs.getString("top_model", "Gemini 3.8 Flash") ?: "Gemini 3.8 Flash"

            val freeVnd = NumberFormat.getNumberInstance(Locale.US).format((freeRem * vndRate).toLong())
            val outVnd = NumberFormat.getNumberInstance(Locale.US).format((outstanding * vndRate).toLong())

            val views = RemoteViews(context.packageName, R.layout.widget_muse_note20_ultra)
            views.setTextViewText(R.id.widget_version_badge, "v$versionName • Wi-Fi OTA")
            views.setTextViewText(R.id.widget_free_credits_value, "$%.2f".format(Locale.US, freeRem))
            views.setTextViewText(R.id.widget_free_credits_sub, "≈ ${freeVnd}₫ • Còn lại")
            views.setTextViewText(R.id.widget_outstanding_value, "$%.2f".format(Locale.US, outstanding))
            views.setTextViewText(R.id.widget_outstanding_sub, "≈ ${outVnd}₫ • Khi hết Free")
            views.setTextViewText(
                R.id.widget_period_breakdown,
                "Ngày: $%.2f | Tuần: $%.2f | Tháng: $%.2f | Tổng: $%.2f".format(
                    Locale.US,
                    today,
                    week,
                    month,
                    total
                )
            )
            views.setTextViewText(
                R.id.widget_profile_status,
                "$profileName • Model: $topModel • Zero-Logout"
            )

            // Launch app on tap
            val launchIntent = Intent(context, MainActivity::class.java)
            val launchPending = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root_container, launchPending)

            // Refresh widget on tap
            val refreshIntent = Intent(context, MuseNote20UltraWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_NOTE20_WIDGET
            }
            val refreshPending = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
