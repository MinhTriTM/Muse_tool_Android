package com.musetool.superapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musetool.superapp.data.ai.AiGenerationResult
import com.musetool.superapp.data.ai.PlaceholderAiRepository
import com.musetool.superapp.data.local.MuseDatabase
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.OtaReleaseEntity
import com.musetool.superapp.data.local.ProfileVaultDataStore
import com.musetool.superapp.data.local.PromptTemplateEntity
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.ota.OtaInstallState
import com.musetool.superapp.data.ota.OtaUpdateManager
import com.musetool.superapp.data.ota.WifiConnectionStatus
import com.musetool.superapp.data.repository.BillingCalculatorEngine
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.data.repository.BillingPeriod
import com.musetool.superapp.data.repository.MuseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MuseSuperAppViewModel(
    private val repository: MuseRepository,
    private val otaUpdateManager: OtaUpdateManager
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(BillingPeriod.ALL)
    val selectedPeriod: StateFlow<BillingPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedModelIdFilter = MutableStateFlow("ALL")
    val selectedModelIdFilter: StateFlow<String> = _selectedModelIdFilter.asStateFlow()

    private val _showVndPrimary = MutableStateFlow(false)
    val showVndPrimary: StateFlow<Boolean> = _showVndPrimary.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _lastAiResult = MutableStateFlow<AiGenerationResult?>(null)
    val lastAiResult: StateFlow<AiGenerationResult?> = _lastAiResult.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _wifiStatus = MutableStateFlow(otaUpdateManager.checkLiveWifiStatus())
    val wifiStatus: StateFlow<WifiConnectionStatus> = _wifiStatus.asStateFlow()

    val otaInstallState: StateFlow<OtaInstallState> = otaUpdateManager.installState
    val silentOtaTelemetry = otaUpdateManager.silentTelemetry

    val profileState: StateFlow<UserProfileState> = repository.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileState()
        )

    val otaReleases: StateFlow<List<OtaReleaseEntity>> = repository.otaReleasesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val promptTemplates: StateFlow<List<PromptTemplateEntity>> = repository.promptTemplatesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val billingSummary: StateFlow<BillingDashboardSummary> = combine(
        repository.usageLogsFlow,
        repository.profileFlow,
        _selectedPeriod,
        _selectedModelIdFilter
    ) { logs, profile, period, modelFilter ->
        BillingCalculatorEngine.computeDashboardSummary(
            allLogsDesc = logs,
            profile = profile,
            selectedPeriod = period,
            selectedModelFilter = modelFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BillingCalculatorEngine.computeDashboardSummary(
            allLogsDesc = emptyList(),
            profile = UserProfileState(),
            selectedPeriod = BillingPeriod.ALL,
            selectedModelFilter = "ALL"
        )
    )

    init {
        viewModelScope.launch {
            repository.ensureSeedDataInitialized()
            refreshWifiStatus()
        }
    }

    fun selectBillingPeriod(period: BillingPeriod) {
        _selectedPeriod.value = period
    }

    fun selectModelFilter(modelId: String) {
        _selectedModelIdFilter.value = modelId
    }

    fun toggleCurrencyDisplay() {
        _showVndPrimary.value = !_showVndPrimary.value
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }

    fun refreshWifiStatus() {
        _wifiStatus.value = otaUpdateManager.checkLiveWifiStatus()
    }

    fun runAiStudioPrompt(
        modelId: String,
        taskCategory: String,
        prompt: String,
        expectedOutputTokens: Int
    ) {
        if (prompt.isBlank() || _isGeneratingAi.value) return
        viewModelScope.launch {
            _isGeneratingAi.value = true
            try {
                val res = repository.executeAiAndLogCost(
                    modelId = modelId,
                    taskCategory = taskCategory,
                    prompt = prompt,
                    targetOutputTokens = expectedOutputTokens
                )
                _lastAiResult.value = res
                _statusBannerMessage.value =
                    "Đã tính phí ${res.modelName}: $%.5f USD (%,d in / %,d out tokens)".format(
                        res.costUsd,
                        res.inputTokens,
                        res.outputTokens
                    )
            } finally {
                _isGeneratingAi.value = false
            }
        }
    }

    fun logManualModelUsage(
        modelId: String,
        category: String,
        note: String,
        inputTokens: Int,
        outputTokens: Int,
        daysOffset: Int = 0
    ) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis() - (daysOffset.toLong() * 86_400_000L)
            val entry = repository.recordModelUsage(
                modelId = modelId,
                taskCategory = category,
                promptSummary = note.ifBlank { "Phiên xử lý ${MuseModelCatalog.findById(modelId).displayName}" },
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                customTimestamp = timestamp
            )
            val splitMsg = if (entry.chargedToOutstandingUsd > 0.0) {
                "Free Credit: $%.4f | Outstanding Balance: +$%.4f".format(
                    entry.coveredByFreeCreditUsd,
                    entry.chargedToOutstandingUsd
                )
            } else {
                "Trừ vào Free Credit In Use: $%.4f".format(entry.coveredByFreeCreditUsd)
            }
            _statusBannerMessage.value = "Đã ghi nhận ${entry.modelName} ($%.4f) • $splitMsg".format(entry.totalCostUsd)
        }
    }

    fun deleteUsageRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteUsageLog(id)
            _statusBannerMessage.value = "Đã xóa bản ghi chi phí #$id"
        }
    }

    fun topUpFreeCredits(amountUsd: Double) {
        viewModelScope.launch {
            repository.topUpFreeCredits(amountUsd)
            _statusBannerMessage.value = "Đã cộng thêm +$%.2f USD vào Free Credits In Use!".format(amountUsd)
        }
    }

    fun simulateExhaustFreeCredits() {
        viewModelScope.launch {
            val usedFree = billingSummary.value.freeCreditsUsedUsd
            repository.setFreeCreditAllocation(usedFree)
            _statusBannerMessage.value =
                "Đã đặt Free Credits còn lại = $0.00! Mọi chi phí mới sẽ tính 100% vào Outstanding Balance."
        }
    }

    fun settleOutstandingBalance(amountUsd: Double) {
        if (amountUsd <= 0.0) return
        viewModelScope.launch {
            repository.settleOutstandingBalance(amountUsd)
            _statusBannerMessage.value =
                "Đã thanh toán $%.2f USD dư nợ Outstanding Balance!".format(amountUsd)
        }
    }

    fun updateBillingConfig(freeCreditUsd: Double, monthlyBudgetUsd: Double, vndRate: Double) {
        viewModelScope.launch {
            repository.updateBillingSettings(freeCreditUsd, monthlyBudgetUsd, vndRate)
            _statusBannerMessage.value = "Đã cập nhật định mức Free Credit & Tỷ giá VNĐ!"
        }
    }

    fun updateProfileDetails(
        displayName: String,
        email: String,
        roleTitle: String,
        workspaceName: String
    ) {
        viewModelScope.launch {
            repository.updateProfile(displayName, email, roleTitle, workspaceName)
            _statusBannerMessage.value = "Đã lưu Profile vào Session Vault (Bảo toàn qua mọi bản Update)!"
        }
    }

    fun updateOtaSettings(channel: String, autoWifiOta: Boolean, keepProfileOnUpdate: Boolean) {
        viewModelScope.launch {
            repository.updateOtaPreferences(channel, autoWifiOta, keepProfileOnUpdate)
        }
    }

    fun startWifiOtaUpdate(release: OtaReleaseEntity) {
        viewModelScope.launch {
            refreshWifiStatus()
            val profile = profileState.value
            otaUpdateManager.performWifiHotBundleUpdate(
                targetRelease = release,
                currentProfile = profile,
                onCommitVersion = { verName, verCode ->
                    repository.commitOtaVersionInstalled(verName, verCode)
                }
            )
            _statusBannerMessage.value =
                "Đã cập nhật OTA lên v${release.versionName} qua Wi-Fi! Giữ nguyên Profile ${profile.displayName} (Không cần đăng nhập lại)."
        }
    }

    fun resetOtaInstallBanner() {
        otaUpdateManager.resetInstallState()
    }

    fun checkAndSimulateNewCloudRelease() {
        viewModelScope.launch {
            val currentMaxCode = otaReleases.value.maxOfOrNull { it.versionCode } ?: 26
            val nextCode = currentMaxCode + 1
            val nextMajor = 2
            val nextMinor = nextCode - 20
            val newVersionName = "$nextMajor.$nextMinor.0-HotBundle"
            repository.publishNewRemoteOtaRelease(
                versionName = newVersionName,
                versionCode = nextCode,
                channel = profileState.value.otaChannel,
                sizeMb = 5.2,
                changelog = "• Bản cập nhật OTA phát hiện qua Wi-Fi Cloud (v$newVersionName)\n• Tối ưu hóa bộ đếm Free Credits & Outstanding Balance theo thời gian thực\n• Tương thích 100% cơ chế Zero-Logout giữ nguyên Profile & Token hiện tại"
            )
            _statusBannerMessage.value = "Phát hiện bản cập nhật mới v$newVersionName qua Wi-Fi (Không cần tải APK)!"
        }
    }

    fun updateSilentManifestConfig(endpointUrl: String, autoApply: Boolean) {
        otaUpdateManager.updateManifestEndpoint(endpointUrl, autoApply)
    }

    fun triggerSilentHttpOtaService(
        context: Context,
        manifestUrl: String,
        autoApplySilent: Boolean,
        forceCheck: Boolean = true
    ) {
        refreshWifiStatus()
        com.musetool.superapp.data.ota.SilentOtaSyncService.triggerSilentOtaService(
            context = context,
            manifestUrl = manifestUrl,
            autoApplySilent = autoApplySilent,
            forceCheck = forceCheck
        )
    }

    fun saveCustomPromptTemplate(
        title: String,
        category: String,
        promptBody: String,
        modelId: String,
        inTokens: Int,
        outTokens: Int
    ) {
        viewModelScope.launch {
            repository.savePromptTemplate(title, category, promptBody, modelId, inTokens, outTokens)
            _statusBannerMessage.value = "Đã lưu mẫu Prompt '$title' vào thư viện SuperApp!"
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleTheme(enabled)
        }
    }

    fun selectThemePreset(preset: com.musetool.superapp.ui.theme.MuseThemePreset) {
        viewModelScope.launch {
            repository.selectThemePreset(preset)
        }
    }

    fun cycleNextThemePreset() {
        viewModelScope.launch {
            val all = com.musetool.superapp.ui.theme.MuseThemePreset.entries
            val current = com.musetool.superapp.ui.theme.MuseThemePreset.fromId(profileState.value.themePresetId)
            val next = all[(current.ordinal + 1) % all.size]
            repository.selectThemePreset(next)
            _statusBannerMessage.value = "Đã đổi sang giao diện: ${next.titleVi} (${next.modeLabel})"
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val db = MuseDatabase.getInstance(appContext)
                    val vault = ProfileVaultDataStore(appContext)
                    val aiRepo = PlaceholderAiRepository()
                    val repo = MuseRepository(db.museDao(), vault, aiRepo)
                    val otaManager = OtaUpdateManager.getInstance(appContext)
                    return MuseSuperAppViewModel(repo, otaManager) as T
                }
            }
    }
}
