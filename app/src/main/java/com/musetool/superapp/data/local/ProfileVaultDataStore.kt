package com.musetool.superapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.profileDataStore by preferencesDataStore(name = "muse_persistent_profile_vault")

/**
 * Persistent Vault that preserves user profile, session token, billing credits, and app state
 * across Wi-Fi OTA updates so the user never has to log in again after updating.
 */
class ProfileVaultDataStore(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val ROLE_TITLE = stringPreferencesKey("role_title")
        val WORKSPACE_NAME = stringPreferencesKey("workspace_name")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val SESSION_CREATED_AT = longPreferencesKey("session_created_at")
        val FREE_CREDIT_USD = doublePreferencesKey("free_credit_usd")
        val PAID_AMOUNT_USD = doublePreferencesKey("paid_amount_usd")
        val MONTHLY_BUDGET_USD = doublePreferencesKey("monthly_budget_usd")
        val VND_RATE = doublePreferencesKey("vnd_rate")
        val ACTIVE_VERSION_NAME = stringPreferencesKey("active_version_name")
        val ACTIVE_VERSION_CODE = intPreferencesKey("active_version_code")
        val OTA_CHANNEL = stringPreferencesKey("ota_channel")
        val AUTO_WIFI_OTA = booleanPreferencesKey("auto_wifi_ota")
        val KEEP_PROFILE_ON_UPDATE = booleanPreferencesKey("keep_profile_on_update")
        val SNAPSHOT_HASH = stringPreferencesKey("snapshot_hash")
        val SNAPSHOT_COUNT = intPreferencesKey("snapshot_count")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val THEME_PRESET_ID = stringPreferencesKey("theme_preset_id")
    }

    val profileFlow: Flow<UserProfileState> = context.profileDataStore.data.map { prefs ->
        UserProfileState(
            userId = prefs[Keys.USER_ID] ?: "MUSE-PRO-8842",
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Kazuto Yui",
            email = prefs[Keys.EMAIL] ?: "kazutoyui20125@gmail.com",
            roleTitle = prefs[Keys.ROLE_TITLE] ?: "AI Architect & Lead Developer",
            workspaceName = prefs[Keys.WORKSPACE_NAME] ?: "Muse Tool Cloud Workspace",
            sessionToken = prefs[Keys.SESSION_TOKEN] ?: "jwt_muse_persistent_994f8a21c7e4",
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: true,
            sessionCreatedAt = prefs[Keys.SESSION_CREATED_AT] ?: 1732000000000L,
            freeCreditAllocationUsd = prefs[Keys.FREE_CREDIT_USD] ?: 25.00,
            paidAmountUsd = prefs[Keys.PAID_AMOUNT_USD] ?: 0.0,
            monthlyBudgetLimitUsd = prefs[Keys.MONTHLY_BUDGET_USD] ?: 50.00,
            vndExchangeRate = prefs[Keys.VND_RATE] ?: 25450.0,
            activeVersionName = prefs[Keys.ACTIVE_VERSION_NAME] ?: "2.4.0",
            activeVersionCode = prefs[Keys.ACTIVE_VERSION_CODE] ?: 24,
            otaChannel = prefs[Keys.OTA_CHANNEL] ?: "Stable",
            autoWifiOtaEnabled = prefs[Keys.AUTO_WIFI_OTA] ?: true,
            keepProfileOnUpdate = prefs[Keys.KEEP_PROFILE_ON_UPDATE] ?: true,
            lastProfileSnapshotHash = prefs[Keys.SNAPSHOT_HASH] ?: "SHA256:9F86D081884C7D659A2FEAA0",
            snapshotRestoreCount = prefs[Keys.SNAPSHOT_COUNT] ?: 3,
            darkThemeEnabled = prefs[Keys.DARK_THEME] ?: true,
            themePresetId = prefs[Keys.THEME_PRESET_ID] ?: "OBSIDIAN_MONO"
        )
    }

    suspend fun updateProfileInfo(
        displayName: String,
        email: String,
        roleTitle: String,
        workspaceName: String
    ) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName
            prefs[Keys.EMAIL] = email
            prefs[Keys.ROLE_TITLE] = roleTitle
            prefs[Keys.WORKSPACE_NAME] = workspaceName
            prefs[Keys.SNAPSHOT_HASH] = computeHash("$displayName|$email|$roleTitle")
        }
    }

    suspend fun updateBillingSettings(
        freeCreditUsd: Double,
        monthlyBudgetUsd: Double,
        vndRate: Double
    ) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.FREE_CREDIT_USD] = freeCreditUsd.coerceAtLeast(0.0)
            prefs[Keys.MONTHLY_BUDGET_USD] = monthlyBudgetUsd.coerceAtLeast(1.0)
            prefs[Keys.VND_RATE] = vndRate.coerceAtLeast(1000.0)
        }
    }

    suspend fun settleOutstandingBalance(amountPaidUsd: Double) {
        context.profileDataStore.edit { prefs ->
            val currentPaid = prefs[Keys.PAID_AMOUNT_USD] ?: 0.0
            prefs[Keys.PAID_AMOUNT_USD] = currentPaid + amountPaidUsd
        }
    }

    suspend fun resetPaidAmount() {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.PAID_AMOUNT_USD] = 0.0
        }
    }

    suspend fun updateOtaPreferences(
        channel: String,
        autoWifiOta: Boolean,
        keepProfileOnUpdate: Boolean
    ) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.OTA_CHANNEL] = channel
            prefs[Keys.AUTO_WIFI_OTA] = autoWifiOta
            prefs[Keys.KEEP_PROFILE_ON_UPDATE] = keepProfileOnUpdate
        }
    }

    /**
     * Performs a hot-bundle version update while preserving the user's Profile, Session Token,
     * and billing credits without requiring re-login.
     */
    suspend fun applyOtaVersionPreserveSession(
        newVersionName: String,
        newVersionCode: Int
    ): String {
        var newHash = ""
        context.profileDataStore.edit { prefs ->
            val uid = prefs[Keys.USER_ID] ?: "MUSE-PRO-8842"
            val token = prefs[Keys.SESSION_TOKEN] ?: "jwt_muse_persistent_994f8a21c7e4"
            val count = (prefs[Keys.SNAPSHOT_COUNT] ?: 3) + 1
            newHash = computeHash("$uid|$token|$newVersionName|$newVersionCode|$count")
            prefs[Keys.ACTIVE_VERSION_NAME] = newVersionName
            prefs[Keys.ACTIVE_VERSION_CODE] = newVersionCode
            prefs[Keys.IS_LOGGED_IN] = true // Guaranteed session continuation
            prefs[Keys.SNAPSHOT_HASH] = newHash
            prefs[Keys.SNAPSHOT_COUNT] = count
        }
        return newHash
    }

    suspend fun toggleDarkTheme(enabled: Boolean) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
            val currentId = prefs[Keys.THEME_PRESET_ID] ?: "OBSIDIAN_MONO"
            val currentPreset = com.musetool.superapp.ui.theme.MuseThemePreset.fromId(currentId)
            if (currentPreset.isDark != enabled) {
                prefs[Keys.THEME_PRESET_ID] = if (enabled) "OBSIDIAN_MONO" else "ALABASTER_LIGHT"
            }
        }
    }

    suspend fun setThemePreset(preset: com.musetool.superapp.ui.theme.MuseThemePreset) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.THEME_PRESET_ID] = preset.id
            prefs[Keys.DARK_THEME] = preset.isDark
        }
    }

    private fun computeHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val hex = digest.take(12).joinToString("") { "%02X".format(it) }
        return "SHA256:$hex"
    }
}
