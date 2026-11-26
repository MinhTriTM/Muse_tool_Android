package com.musetool.superapp.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.musetool.superapp.MainActivity
import com.musetool.superapp.R
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.repository.BillingDashboardSummary
import java.util.Locale

data class AppShortcutItem(
    val shortcutId: String,
    val action: String,
    val shortLabel: String,
    val longLabel: String,
    val subtitleDetail: String,
    val targetModelId: String? = null
)

object MuseAppShortcutManager {

    const val ACTION_CHECK_BALANCE = "com.musetool.superapp.SHORTCUT_CHECK_BALANCE"
    const val ACTION_OPEN_LAST_MODEL = "com.musetool.superapp.SHORTCUT_OPEN_LAST_MODEL"
    const val ACTION_QUICK_LOG_USAGE = "com.musetool.superapp.SHORTCUT_QUICK_LOG_USAGE"
    const val ACTION_WIFI_OTA_SYNC = "com.musetool.superapp.SHORTCUT_WIFI_OTA_SYNC"

    const val EXTRA_SHORTCUT_MODEL_ID = "extra_shortcut_model_id"

    fun buildLiveShortcutsList(
        summary: BillingDashboardSummary,
        profile: UserProfileState
    ): List<AppShortcutItem> {
        val lastLog = summary.filteredLogs.firstOrNull()
        val lastModelId = lastLog?.modelId
            ?: summary.modelBreakdowns.firstOrNull { it.totalCostUsd > 0 }?.modelSpec?.modelId
            ?: MuseModelCatalog.models.first().modelId
        val lastModelSpec = MuseModelCatalog.findById(lastModelId)

        val freeStr = "$%.2f".format(Locale.US, summary.freeCreditsRemainingUsd)
        val debtStr = "$%.2f".format(Locale.US, summary.netOutstandingBalanceUsd)

        return listOf(
            AppShortcutItem(
                shortcutId = "shortcut_check_balance",
                action = ACTION_CHECK_BALANCE,
                shortLabel = "Số dư: $freeStr Free",
                longLabel = "Free: $freeStr · Nợ: $debtStr",
                subtitleDetail = "Mở ngay Trung tâm Tính tiền & Đối soát Free Credits / Outstanding Balance"
            ),
            AppShortcutItem(
                shortcutId = "shortcut_last_ai_model",
                action = ACTION_OPEN_LAST_MODEL,
                shortLabel = "Mở ${lastModelSpec.displayName}",
                longLabel = "Chạy ${lastModelSpec.displayName}",
                subtitleDetail = "Mở AI Studio chọn sẵn model gần nhất (${lastModelSpec.displayName} • In $${lastModelSpec.inputPricePer1MUsd}/1M)",
                targetModelId = lastModelSpec.modelId
            ),
            AppShortcutItem(
                shortcutId = "shortcut_quick_log_cost",
                action = ACTION_QUICK_LOG_USAGE,
                shortLabel = "Ghi nhận Phiên phí",
                longLabel = "Tính tiền nhanh phiên AI mới",
                subtitleDetail = "Bật hộp thoại ghi nhận & khấu trừ tự động Free Credit / Outstanding"
            ),
            AppShortcutItem(
                shortcutId = "shortcut_wifi_ota_sync",
                action = ACTION_WIFI_OTA_SYNC,
                shortLabel = "OTA v${profile.activeVersionName}",
                longLabel = "Wi-Fi OTA & Hồ sơ Zero-Logout",
                subtitleDetail = "Kiểm tra Silent HTTP Manifest & trạng thái bảo toàn Session Token"
            )
        )
    }

    /**
     * Dynamically updates the Android Launcher long-press app icon shortcuts with real-time
     * Free Credits Remaining, Outstanding Balance, and the actual Last Used AI Model name.
     */
    fun syncDynamicShortcuts(
        context: Context,
        summary: BillingDashboardSummary,
        profile: UserProfileState
    ) {
        runCatching {
            val items = buildLiveShortcutsList(summary, profile)

            val balanceItem = items[0]
            val balanceIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_CHECK_BALANCE
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val balanceShortcut = ShortcutInfoCompat.Builder(context, balanceItem.shortcutId)
                .setShortLabel(balanceItem.shortLabel)
                .setLongLabel(balanceItem.longLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_balance))
                .setIntent(balanceIntent)
                .setRank(0)
                .build()

            val modelItem = items[1]
            val modelIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_LAST_MODEL
                putExtra(EXTRA_SHORTCUT_MODEL_ID, modelItem.targetModelId ?: "gemini-3.8-flash")
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val modelShortcut = ShortcutInfoCompat.Builder(context, modelItem.shortcutId)
                .setShortLabel(modelItem.shortLabel)
                .setLongLabel(modelItem.longLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_ai_model))
                .setIntent(modelIntent)
                .setRank(1)
                .build()

            val quickLogItem = items[2]
            val quickLogIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_QUICK_LOG_USAGE
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val quickLogShortcut = ShortcutInfoCompat.Builder(context, quickLogItem.shortcutId)
                .setShortLabel(quickLogItem.shortLabel)
                .setLongLabel(quickLogItem.longLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_quick_log))
                .setIntent(quickLogIntent)
                .setRank(2)
                .build()

            val otaItem = items[3]
            val otaIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_WIFI_OTA_SYNC
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val otaShortcut = ShortcutInfoCompat.Builder(context, otaItem.shortcutId)
                .setShortLabel(otaItem.shortLabel)
                .setLongLabel(otaItem.longLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_ota_sync))
                .setIntent(otaIntent)
                .setRank(3)
                .build()

            ShortcutManagerCompat.setDynamicShortcuts(
                context,
                listOf(balanceShortcut, modelShortcut, quickLogShortcut, otaShortcut)
            )
        }
    }

    /**
     * Requests the Android Launcher (e.g., Samsung One UI) to pin a specific quick-action shortcut
     * directly onto the user's home screen.
     */
    fun requestPinAppShortcut(
        context: Context,
        item: AppShortcutItem
    ): Boolean {
        return runCatching {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false
            val iconRes = when (item.action) {
                ACTION_CHECK_BALANCE -> R.drawable.ic_shortcut_balance
                ACTION_OPEN_LAST_MODEL -> R.drawable.ic_shortcut_ai_model
                ACTION_QUICK_LOG_USAGE -> R.drawable.ic_shortcut_quick_log
                else -> R.drawable.ic_shortcut_ota_sync
            }
            val intent = Intent(context, MainActivity::class.java).apply {
                action = item.action
                if (item.targetModelId != null) {
                    putExtra(EXTRA_SHORTCUT_MODEL_ID, item.targetModelId)
                }
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "${item.shortcutId}_pinned")
                .setShortLabel(item.shortLabel)
                .setLongLabel(item.longLabel)
                .setIcon(IconCompat.createWithResource(context, iconRes))
                .setIntent(intent)
                .build()

            ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
        }.getOrDefault(false)
    }
}
