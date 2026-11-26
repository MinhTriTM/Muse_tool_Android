package com.musetool.superapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class AiModelSpec(
    val modelId: String,
    val displayName: String,
    val provider: String,
    val tierBadge: String,
    val category: String,
    val inputPricePer1MUsd: Double,
    val outputPricePer1MUsd: Double,
    val perRequestFeeUsd: Double = 0.0,
    val colorHex: Long
)

object MuseModelCatalog {
    val models = listOf(
        AiModelSpec(
            modelId = "gemini-3.8-flash",
            displayName = "Gemini 3.8 Flash",
            provider = "Google DeepMind",
            tierBadge = "High-Speed",
            category = "Multimodal & Code",
            inputPricePer1MUsd = 0.15,
            outputPricePer1MUsd = 0.60,
            colorHex = 0xFFF1F5F9
        ),
        AiModelSpec(
            modelId = "gemini-4-argon",
            displayName = "Gemini 4 Argon",
            provider = "Google DeepMind",
            tierBadge = "Frontier Pro",
            category = "Deep Reasoning & STEM",
            inputPricePer1MUsd = 2.50,
            outputPricePer1MUsd = 10.00,
            colorHex = 0xFFD4A373
        ),
        AiModelSpec(
            modelId = "gemini-3.5-flash-lite",
            displayName = "Gemini 3.5 Flash-Lite",
            provider = "Google DeepMind",
            tierBadge = "Ultra-Lite",
            category = "Fast Batch & Chat",
            inputPricePer1MUsd = 0.075,
            outputPricePer1MUsd = 0.30,
            colorHex = 0xFF5EEAD4
        ),
        AiModelSpec(
            modelId = "gemini-nano-banana-2.1",
            displayName = "Gemini Nano Banana 2.1",
            provider = "Google DeepMind",
            tierBadge = "Vision Studio",
            category = "Image Generation 2K",
            inputPricePer1MUsd = 0.35,
            outputPricePer1MUsd = 1.40,
            perRequestFeeUsd = 0.04,
            colorHex = 0xFFCBD5E1
        ),
        AiModelSpec(
            modelId = "claude-3.7-sonnet",
            displayName = "Claude 3.7 Sonnet",
            provider = "Anthropic",
            tierBadge = "Code Architect",
            category = "Agentic Coding",
            inputPricePer1MUsd = 3.00,
            outputPricePer1MUsd = 15.00,
            colorHex = 0xFF94A3B8
        ),
        AiModelSpec(
            modelId = "gpt-4.5-omni",
            displayName = "GPT-4.5 Omni",
            provider = "OpenAI",
            tierBadge = "Omni Enterprise",
            category = "General Intelligence",
            inputPricePer1MUsd = 2.00,
            outputPricePer1MUsd = 8.00,
            colorHex = 0xFF7DD3FC
        ),
        AiModelSpec(
            modelId = "deepseek-r1-reasoner",
            displayName = "DeepSeek R1 Reasoner",
            provider = "DeepSeek",
            tierBadge = "Chain-of-Thought",
            category = "Math & Logic",
            inputPricePer1MUsd = 0.55,
            outputPricePer1MUsd = 2.19,
            colorHex = 0xFF64748B
        )
    )

    fun findById(modelId: String): AiModelSpec =
        models.firstOrNull { it.modelId == modelId } ?: models.first()

    fun calculateCostUsd(modelId: String, inputTokens: Int, outputTokens: Int): Double {
        val spec = findById(modelId)
        val inputCost = (inputTokens.toDouble() / 1_000_000.0) * spec.inputPricePer1MUsd
        val outputCost = (outputTokens.toDouble() / 1_000_000.0) * spec.outputPricePer1MUsd
        return inputCost + outputCost + spec.perRequestFeeUsd
    }
}

@Entity(tableName = "usage_logs")
data class UsageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val modelId: String,
    val modelName: String,
    val taskCategory: String,
    val promptSummary: String,
    val inputTokens: Int,
    val outputTokens: Int,
    val totalCostUsd: Double,
    val coveredByFreeCreditUsd: Double,
    val chargedToOutstandingUsd: Double,
    val latencyMs: Long,
    val appVersionAtUse: String
)

@Entity(tableName = "ota_releases")
data class OtaReleaseEntity(
    @PrimaryKey val versionName: String,
    val versionCode: Int,
    val channel: String,
    val releaseDate: String,
    val bundleSizeMb: Double,
    val changelog: String,
    val patchSignature: String,
    val isInstalled: Boolean,
    val installedTimestamp: Long? = null
)

@Entity(tableName = "prompt_templates")
data class PromptTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val promptBody: String,
    val recommendedModelId: String,
    val defaultInputTokens: Int,
    val defaultOutputTokens: Int,
    val isPinned: Boolean = false
)

@Serializable
data class UserProfileState(
    val userId: String = "MUSE-PRO-8842",
    val displayName: String = "Kazuto Yui",
    val email: String = "kazutoyui20125@gmail.com",
    val roleTitle: String = "AI Architect & Lead Developer",
    val workspaceName: String = "Muse Tool Cloud Workspace",
    val sessionToken: String = "jwt_muse_persistent_994f8a21c7e4",
    val isLoggedIn: Boolean = true,
    val sessionCreatedAt: Long = 1732000000000L,
    val freeCreditAllocationUsd: Double = 25.00,
    val paidAmountUsd: Double = 0.0,
    val monthlyBudgetLimitUsd: Double = 50.00,
    val vndExchangeRate: Double = 25450.0,
    val activeVersionName: String = "2.4.0",
    val activeVersionCode: Int = 24,
    val otaChannel: String = "Stable",
    val autoWifiOtaEnabled: Boolean = true,
    val keepProfileOnUpdate: Boolean = true,
    val lastProfileSnapshotHash: String = "SHA256:9F86D081884C7D659A2FEAA0",
    val snapshotRestoreCount: Int = 3,
    val darkThemeEnabled: Boolean = true,
    val themePresetId: String = "OBSIDIAN_MONO"
)
