package com.musetool.superapp.data.repository

import com.musetool.superapp.data.ai.AiGenerationResult
import com.musetool.superapp.data.ai.AiRepository
import com.musetool.superapp.data.local.MuseDao
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.OtaReleaseEntity
import com.musetool.superapp.data.local.ProfileVaultDataStore
import com.musetool.superapp.data.local.PromptTemplateEntity
import com.musetool.superapp.data.local.UsageLogEntity
import com.musetool.superapp.data.local.UserProfileState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MuseRepository(
    private val dao: MuseDao,
    private val profileVault: ProfileVaultDataStore,
    private val aiRepository: AiRepository
) {
    val usageLogsFlow: Flow<List<UsageLogEntity>> = dao.observeAllUsageLogs()
    val otaReleasesFlow: Flow<List<OtaReleaseEntity>> = dao.observeOtaReleases()
    val promptTemplatesFlow: Flow<List<PromptTemplateEntity>> = dao.observePromptTemplates()
    val profileFlow: Flow<UserProfileState> = profileVault.profileFlow

    suspend fun ensureSeedDataInitialized() {
        if (dao.getUsageLogCount() == 0) {
            seedHistoricalUsageLogs()
        }
        if (dao.getOtaReleaseCount() == 0) {
            seedOtaReleases()
        }
        if (dao.getPromptTemplateCount() == 0) {
            seedPromptTemplates()
        }
    }

    /**
     * Executes an AI model request (or manual cost log), calculates exact token pricing,
     * deducts from remaining Free Credits In Use first, and spills over any uncovered cost
     * into Outstanding Balance automatically.
     */
    suspend fun executeAiAndLogCost(
        modelId: String,
        taskCategory: String,
        prompt: String,
        targetOutputTokens: Int
    ): AiGenerationResult {
        val result = aiRepository.generateResponse(
            modelId = modelId,
            taskCategory = taskCategory,
            prompt = prompt,
            simulatedOutputTokensTarget = targetOutputTokens
        )
        recordModelUsage(
            modelId = result.modelId,
            taskCategory = taskCategory,
            promptSummary = prompt.take(90),
            inputTokens = result.inputTokens,
            outputTokens = result.outputTokens,
            latencyMs = result.latencyMs,
            customTimestamp = System.currentTimeMillis()
        )
        return result
    }

    suspend fun recordModelUsage(
        modelId: String,
        taskCategory: String,
        promptSummary: String,
        inputTokens: Int,
        outputTokens: Int,
        latencyMs: Long = 420L,
        customTimestamp: Long = System.currentTimeMillis()
    ): UsageLogEntity {
        val profile = profileFlow.first()
        val existingLogs = usageLogsFlow.first()
        val spec = MuseModelCatalog.findById(modelId)
        val totalCost = MuseModelCatalog.calculateCostUsd(modelId, inputTokens, outputTokens)

        val usedFreeBefore = existingLogs.sumOf { it.coveredByFreeCreditUsd }
        val remainingFreeBefore = (profile.freeCreditAllocationUsd - usedFreeBefore).coerceAtLeast(0.0)

        val coveredByFree = totalCost.coerceAtMost(remainingFreeBefore)
        val chargedToOutstanding = (totalCost - coveredByFree).coerceAtLeast(0.0)

        val entity = UsageLogEntity(
            timestamp = customTimestamp,
            modelId = spec.modelId,
            modelName = spec.displayName,
            taskCategory = taskCategory,
            promptSummary = promptSummary,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            totalCostUsd = totalCost,
            coveredByFreeCreditUsd = coveredByFree,
            chargedToOutstandingUsd = chargedToOutstanding,
            latencyMs = latencyMs,
            appVersionAtUse = profile.activeVersionName
        )
        dao.insertUsageLog(entity)
        return entity
    }

    suspend fun deleteUsageLog(id: Long) {
        dao.deleteUsageLogById(id)
    }

    suspend fun topUpFreeCredits(additionalUsd: Double) {
        val current = profileFlow.first()
        profileVault.updateBillingSettings(
            freeCreditUsd = current.freeCreditAllocationUsd + additionalUsd,
            monthlyBudgetUsd = current.monthlyBudgetLimitUsd,
            vndRate = current.vndExchangeRate
        )
    }

    suspend fun setFreeCreditAllocation(newLimitUsd: Double) {
        val current = profileFlow.first()
        profileVault.updateBillingSettings(
            freeCreditUsd = newLimitUsd,
            monthlyBudgetUsd = current.monthlyBudgetLimitUsd,
            vndRate = current.vndExchangeRate
        )
    }

    suspend fun updateBillingSettings(
        freeCreditUsd: Double,
        monthlyBudgetUsd: Double,
        vndRate: Double
    ) {
        profileVault.updateBillingSettings(freeCreditUsd, monthlyBudgetUsd, vndRate)
    }

    suspend fun settleOutstandingBalance(amountUsd: Double) {
        profileVault.settleOutstandingBalance(amountUsd)
    }

    suspend fun updateProfile(
        displayName: String,
        email: String,
        roleTitle: String,
        workspaceName: String
    ) {
        profileVault.updateProfileInfo(displayName, email, roleTitle, workspaceName)
    }

    suspend fun updateOtaPreferences(
        channel: String,
        autoWifiOta: Boolean,
        keepProfileOnUpdate: Boolean
    ) {
        profileVault.updateOtaPreferences(channel, autoWifiOta, keepProfileOnUpdate)
    }

    suspend fun commitOtaVersionInstalled(versionName: String, versionCode: Int): String {
        val now = System.currentTimeMillis()
        dao.markOtaVersionInstalled(versionName, now)
        return profileVault.applyOtaVersionPreserveSession(versionName, versionCode)
    }

    suspend fun publishNewRemoteOtaRelease(
        versionName: String,
        versionCode: Int,
        channel: String,
        sizeMb: Double,
        changelog: String
    ) {
        val release = OtaReleaseEntity(
            versionName = versionName,
            versionCode = versionCode,
            channel = channel,
            releaseDate = "Vừa phát hành qua Wi-Fi Cloud",
            bundleSizeMb = sizeMb,
            changelog = changelog,
            patchSignature = "SHA256:OTA_${versionCode}_${System.currentTimeMillis().toString().takeLast(6)}",
            isInstalled = false,
            installedTimestamp = null
        )
        dao.insertOtaRelease(release)
    }

    suspend fun savePromptTemplate(
        title: String,
        category: String,
        promptBody: String,
        modelId: String,
        inputTokens: Int,
        outputTokens: Int
    ) {
        dao.insertPromptTemplate(
            PromptTemplateEntity(
                title = title,
                category = category,
                promptBody = promptBody,
                recommendedModelId = modelId,
                defaultInputTokens = inputTokens,
                defaultOutputTokens = outputTokens,
                isPinned = true
            )
        )
    }

    suspend fun toggleTheme(darkTheme: Boolean) {
        profileVault.toggleDarkTheme(darkTheme)
    }

    suspend fun selectThemePreset(preset: com.musetool.superapp.ui.theme.MuseThemePreset) {
        profileVault.setThemePreset(preset)
    }

    private suspend fun seedHistoricalUsageLogs() {
        val now = System.currentTimeMillis()
        val hourMs = 3_600_000L
        val dayMs = 24 * hourMs

        data class RawSeed(
            val offsetMs: Long,
            val modelId: String,
            val category: String,
            val summary: String,
            val inTokens: Int,
            val outTokens: Int,
            val forceOutstandingSplit: Boolean = false,
            val ver: String = "2.4.0"
        )

        val seeds = listOf(
            // Today (Multiple runs across models)
            RawSeed(1 * hourMs, "gemini-3.8-flash", "Multimodal & Code", "Tối ưu kiến trúc Jetpack Compose & Billing Engine cho Muse Tool", 42_000, 18_500),
            RawSeed(3 * hourMs, "gemini-4-argon", "Deep Reasoning", "Phân tích thuật toán định giá đa tầng Token & dự báo ngân sách Q4", 125_000, 64_000, forceOutstandingSplit = true),
            RawSeed(5 * hourMs, "gemini-nano-banana-2.1", "Vision Studio", "Render bộ 12 banner UI Cyber-FinTech 2K cho SuperApp", 18_000, 12_000),
            RawSeed(7 * hourMs, "claude-3.7-sonnet", "Code Architect", "Refactor hệ thống Wi-Fi OTA Delta Bundle & Session Vault", 95_000, 48_000, forceOutstandingSplit = true),
            RawSeed(9 * hourMs, "gemini-3.5-flash-lite", "Fast Batch", "Tóm tắt 450 log giao dịch API theo ngày/tuần/tháng/năm", 210_000, 85_000),

            // Earlier This Week (Days 1 to 6 ago)
            RawSeed(1 * dayMs + 2 * hourMs, "gpt-4.5-omni", "General AI", "Tổng hợp báo cáo tài chính đa tiền tệ USD/VND tự động", 140_000, 72_000, forceOutstandingSplit = true),
            RawSeed(1 * dayMs + 6 * hourMs, "gemini-3.8-flash", "Multimodal & Code", "Tạo unit test cho bộ lọc chi phí theo từng model AI", 85_000, 32_000),
            RawSeed(2 * dayMs + 4 * hourMs, "deepseek-r1-reasoner", "Math & Logic", "Kiểm chứng công thức khấu trừ Free Credits & Outstanding Balance", 190_000, 110_000),
            RawSeed(3 * dayMs + 3 * hourMs, "gemini-4-argon", "Deep Reasoning", "Thiết kế giao thức Hot-Swap OTA không cần cài lại APK", 160_000, 92_000, forceOutstandingSplit = true),
            RawSeed(4 * dayMs + 5 * hourMs, "claude-3.7-sonnet", "Code Architect", "Xây dựng module lưu trữ Profile Snapshot SHA-256", 130_000, 68_000),
            RawSeed(5 * dayMs + 2 * hourMs, "gemini-nano-banana-2.1", "Vision Studio", "Thiết kế bộ icon 3D Adaptive & đồ họa Dashboard", 28_000, 16_000),
            RawSeed(6 * dayMs + 4 * hourMs, "gemini-3.8-flash", "Multimodal & Code", "Tự động hóa pipeline kiểm thử hiệu năng Room Database", 110_000, 54_000),

            // Earlier This Month (Days 9 to 22 ago)
            RawSeed(9 * dayMs, "gemini-4-argon", "Deep Reasoning", "Huấn luyện prompt hệ thống phân tích dữ liệu lớn", 280_000, 145_000, forceOutstandingSplit = true, ver = "2.3.8"),
            RawSeed(12 * dayMs, "gpt-4.5-omni", "General AI", "Dịch thuật tài liệu kỹ thuật đa ngôn ngữ Việt - Anh - Nhật", 240_000, 130_000, ver = "2.3.8"),
            RawSeed(15 * dayMs, "claude-3.7-sonnet", "Code Architect", "Kiểm toán bảo mật Zero-Trust cho kho lưu trữ Token", 175_000, 88_000, forceOutstandingSplit = true, ver = "2.3.5"),
            RawSeed(18 * dayMs, "gemini-3.5-flash-lite", "Fast Batch", "Phân loại 2.500 yêu cầu khách hàng theo thời gian thực", 520_000, 190_000, ver = "2.3.5"),
            RawSeed(21 * dayMs, "deepseek-r1-reasoner", "Math & Logic", "Tối ưu chi phí routing giữa 7 model AI khác nhau", 310_000, 155_000, ver = "2.3.5"),

            // Earlier This Year (Months ago)
            RawSeed(42 * dayMs, "gemini-4-argon", "Deep Reasoning", "Kiến trúc lõi Muse Tool SuperApp v2.3", 410_000, 210_000, forceOutstandingSplit = true, ver = "2.3.0"),
            RawSeed(75 * dayMs, "claude-3.7-sonnet", "Code Architect", "Tích hợp hệ thống cảnh báo vượt ngưỡng ngân sách tháng", 290_000, 165_000, ver = "2.2.4"),
            RawSeed(120 * dayMs, "gpt-4.5-omni", "General AI", "Phân tích xu hướng tiêu dùng Token quý 2 & quý 3", 380_000, 180_000, forceOutstandingSplit = true, ver = "2.1.0"),
            RawSeed(185 * dayMs, "gemini-3.8-flash", "Multimodal & Code", "Khởi tạo hệ thống theo dõi thời gian thực đa model", 640_000, 320_000, ver = "2.0.5"),

            // Previous Year (For multi-year comparison)
            RawSeed(380 * dayMs, "gemini-4-argon", "Deep Reasoning", "Nghiên cứu nền tảng Muse Tool thế hệ đầu tiên (Năm trước)", 520_000, 275_000, ver = "1.9.2"),
            RawSeed(410 * dayMs, "gpt-4.5-omni", "General AI", "Đánh giá benchmark hiệu năng & chi phí năm trước", 460_000, 210_000, ver = "1.8.0")
        )

        // Ensure chronological processing so both Free Credits Remaining AND Outstanding Balance
        // from earlier tier exhaustion are clearly populated and visible right on launch.
        val entities = seeds.sortedBy { it.offsetMs }.reversed().map { raw ->
            val spec = MuseModelCatalog.findById(raw.modelId)
            val totalCost = MuseModelCatalog.calculateCostUsd(raw.modelId, raw.inTokens, raw.outTokens)
            val coveredFree = if (raw.forceOutstandingSplit) totalCost * 0.35 else totalCost
            val chargedOut = (totalCost - coveredFree).coerceAtLeast(0.0)

            UsageLogEntity(
                timestamp = now - raw.offsetMs,
                modelId = spec.modelId,
                modelName = spec.displayName,
                taskCategory = raw.category,
                promptSummary = raw.summary,
                inputTokens = raw.inTokens,
                outputTokens = raw.outTokens,
                totalCostUsd = totalCost,
                coveredByFreeCreditUsd = coveredFree,
                chargedToOutstandingUsd = chargedOut,
                latencyMs = (280L + (raw.outTokens / 450)).coerceIn(240L, 1850L),
                appVersionAtUse = raw.ver
            )
        }

        dao.insertUsageLogs(entities)
    }

    private suspend fun seedOtaReleases() {
        val now = System.currentTimeMillis()
        val releases = listOf(
            OtaReleaseEntity(
                versionName = "2.6.0-Quantum",
                versionCode = 26,
                channel = "Beta / Nightly",
                releaseDate = "Bản mới nhất • Sẵn sàng qua Wi-Fi",
                bundleSizeMb = 6.4,
                changelog = "• Nâng cấp thuật toán Auto-Routing tiết kiệm 28% Free Credits\n• Bổ sung biểu đồ dự báo dư nợ Outstanding Balance theo quý\n• Cơ chế Zero-Logout Snapshot v3: Cập nhật trực tiếp không cần tải APK",
                patchSignature = "SHA256:E3B0C44298FC1C149AFBF4C8",
                isInstalled = false,
                installedTimestamp = null
            ),
            OtaReleaseEntity(
                versionName = "2.5.0-SuperApp",
                versionCode = 25,
                channel = "Stable",
                releaseDate = "Khuyên dùng • Cập nhật Wi-Fi OTA",
                bundleSizeMb = 4.8,
                changelog = "• Tích hợp bảng tính tiền tự động theo Ngày, Tuần, Tháng, Năm & từng Model\n• Hiển thị tách biệt Free Credits In Use còn lại & Outstanding Balance\n• Giữ nguyên 100% Profile, Token đăng nhập & cấu hình sau khi Update",
                patchSignature = "SHA256:8F434346648F6B96DF89DDA9",
                isInstalled = false,
                installedTimestamp = null
            ),
            OtaReleaseEntity(
                versionName = "2.4.0",
                versionCode = 24,
                channel = "Stable",
                releaseDate = "Đang chạy trên thiết bị",
                bundleSizeMb = 4.2,
                changelog = "• Phiên bản nền tảng Muse Tool SuperApp với Session Vault\n• Hỗ trợ theo dõi Token thời gian thực cho 7 dòng Model AI",
                patchSignature = "SHA256:9F86D081884C7D659A2FEAA0",
                isInstalled = true,
                installedTimestamp = now - 86_400_000L * 3
            ),
            OtaReleaseEntity(
                versionName = "2.3.8",
                versionCode = 23,
                channel = "Stable",
                releaseDate = "12 ngày trước",
                bundleSizeMb = 3.9,
                changelog = "• Cải thiện tốc độ truy vấn lịch sử chi phí theo tháng\n• Khắc phục lỗi đăng xuất khi chuyển đổi mạng Wi-Fi",
                patchSignature = "SHA256:4B227777D4DD1FC61C6F884F",
                isInstalled = true,
                installedTimestamp = now - 86_400_000L * 12
            )
        )
        dao.insertOtaReleases(releases)
    }

    private suspend fun seedPromptTemplates() {
        val templates = listOf(
            PromptTemplateEntity(
                title = "Phân tích tối ưu chi phí Multi-Model",
                category = "FinOps & Billing",
                promptBody = "Hãy phân tích bảng chi phí Token giữa Gemini 3.8 Flash, Gemini 4 Argon và Claude 3.7 Sonnet để đề xuất chiến lược tối ưu Free Credits trong tháng này.",
                recommendedModelId = "gemini-3.8-flash",
                defaultInputTokens = 28_000,
                defaultOutputTokens = 14_000,
                isPinned = true
            ),
            PromptTemplateEntity(
                title = "Kiểm toán kiến trúc OTA Zero-Logout",
                category = "System Architecture",
                promptBody = "Thiết kế cơ chế Hot-Bundle Delta Update qua Wi-Fi cho phép cập nhật giao diện và logic ứng dụng mà không cần tải file APK, đồng thời giữ nguyên Session Token.",
                recommendedModelId = "gemini-4-argon",
                defaultInputTokens = 65_000,
                defaultOutputTokens = 32_000,
                isPinned = true
            ),
            PromptTemplateEntity(
                title = "Sinh bộ Banner UI SuperApp 2K",
                category = "Vision Studio",
                promptBody = "Tạo hình ảnh minh họa bảng điều khiển tài chính AI phong cách Cyber-FinTech với biểu đồ neon cyan và tím violet độ phân giải 2K.",
                recommendedModelId = "gemini-nano-banana-2.1",
                defaultInputTokens = 12_000,
                defaultOutputTokens = 8_000,
                isPinned = true
            ),
            PromptTemplateEntity(
                title = "Review Code & Refactor Coroutine Flow",
                category = "Code Architect",
                promptBody = "Kiểm tra toàn bộ luồng dữ liệu Room DAO sang ViewModel StateFlow để đảm bảo hiệu năng thời gian thực 60fps.",
                recommendedModelId = "claude-3.7-sonnet",
                defaultInputTokens = 54_000,
                defaultOutputTokens = 26_000,
                isPinned = false
            )
        )
        dao.insertPromptTemplates(templates)
    }
}
