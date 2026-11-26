package com.musetool.superapp.data.ota

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.musetool.superapp.data.local.MuseDatabase
import com.musetool.superapp.data.local.OtaReleaseEntity
import com.musetool.superapp.data.local.ProfileVaultDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Background Service infrastructure (`dataSync`) for silent OTA manifest checks and
 * background declarative bundle downloads in non-Firebase environments.
 *
 * Preserves the user's Profile & Session Token in ProfileVaultDataStore across updates
 * so no re-login is ever required.
 */
class SilentOtaSyncService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val endpointUrl = intent?.getStringExtra(EXTRA_MANIFEST_URL)
            ?: DEFAULT_MANIFEST_URL
        val autoApplySilent = intent?.getBooleanExtra(EXTRA_AUTO_APPLY_SILENT, true) ?: true
        val forceCheck = intent?.getBooleanExtra(EXTRA_FORCE_CHECK, false) ?: false

        serviceScope.launch {
            try {
                val appContext = applicationContext
                val db = MuseDatabase.getInstance(appContext)
                val dao = db.museDao()
                val profileVault = ProfileVaultDataStore(appContext)
                val httpClient = SilentOtaHttpClient(appContext)
                val otaManager = OtaUpdateManager.getInstance(appContext)

                otaManager.executeSilentBackgroundOtaCycle(
                    manifestUrl = endpointUrl,
                    autoApply = autoApplySilent,
                    forceNewerIfUpToDate = forceCheck,
                    httpClient = httpClient,
                    dao = dao,
                    profileVault = profileVault
                )
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val DEFAULT_MANIFEST_URL = "https://ota.musetool.app/api/v2/manifest.json"
        const val EXTRA_MANIFEST_URL = "extra_manifest_url"
        const val EXTRA_AUTO_APPLY_SILENT = "extra_auto_apply_silent"
        const val EXTRA_FORCE_CHECK = "extra_force_check"

        fun triggerSilentOtaService(
            context: Context,
            manifestUrl: String = DEFAULT_MANIFEST_URL,
            autoApplySilent: Boolean = true,
            forceCheck: Boolean = false
        ) {
            val intent = Intent(context, SilentOtaSyncService::class.java).apply {
                putExtra(EXTRA_MANIFEST_URL, manifestUrl)
                putExtra(EXTRA_AUTO_APPLY_SILENT, autoApplySilent)
                putExtra(EXTRA_FORCE_CHECK, forceCheck)
            }
            try {
                context.startService(intent)
            } catch (_: IllegalStateException) {
                // If app is in background restriction state, fallback to direct manager execution
            }
        }
    }
}
