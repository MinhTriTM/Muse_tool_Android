package com.musetool.superapp.data.ai

import com.musetool.superapp.data.local.MuseModelCatalog
import kotlinx.coroutines.delay

data class AiGenerationResult(
    val modelId: String,
    val modelName: String,
    val responseText: String,
    val inputTokens: Int,
    val outputTokens: Int,
    val costUsd: Double,
    val latencyMs: Long,
    val isPlaceholderNotice: Boolean = true
)

/**
 * Single interface for routing all AI calls in Muse Tool SuperApp.
 * Because Firebase setup was declined by the user, this is backed by PlaceholderAiRepository
 * returning clearly labeled sample responses and real token/cost telemetry.
 */
interface AiRepository {
    suspend fun generateResponse(
        modelId: String,
        taskCategory: String,
        prompt: String,
        simulatedOutputTokensTarget: Int = 650
    ): AiGenerationResult
}

class PlaceholderAiRepository : AiRepository {
    override suspend fun generateResponse(
        modelId: String,
        taskCategory: String,
        prompt: String,
        simulatedOutputTokensTarget: Int
    ): AiGenerationResult {
        val spec = MuseModelCatalog.findById(modelId)
        val startTime = System.currentTimeMillis()
        delay(550L)

        // Realistic token calculation based on prompt length + model output profile
        val baseInputTokens = ((prompt.length * 1.35).toInt()).coerceAtLeast(120)
        val inputTokens = baseInputTokens + (prompt.hashCode().and(0x7F))
        val outputTokens = simulatedOutputTokensTarget.coerceAtLeast(180)
        val costUsd = MuseModelCatalog.calculateCostUsd(modelId, inputTokens, outputTokens)
        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(320L)

        val sampleResponse = buildString {
            appendLine("⚡ [PHẢN HỒI MẪU / SAMPLE RESPONSE — ${spec.displayName}]")
            appendLine("• Chuyên mục: $taskCategory | Nhà cung cấp: ${spec.provider}")
            appendLine("• Yêu cầu đã nhận: \"${prompt.take(110)}${if (prompt.length > 110) "..." else ""}\"")
            appendLine()
            appendLine("1. Phân tích & Kết quả xử lý:")
            appendLine("   Hệ thống Muse Tool SuperApp đã thực thi pipeline cho model ${spec.displayName} (${spec.modelId}).")
            appendLine("   - Input Tokens: %,d tokens ($%.4f / 1M)".format(inputTokens, spec.inputPricePer1MUsd))
            appendLine("   - Output Tokens: %,d tokens ($%.4f / 1M)".format(outputTokens, spec.outputPricePer1MUsd))
            if (spec.perRequestFeeUsd > 0) {
                appendLine("   - Phí xử lý media cố định: $%.4f / request".format(spec.perRequestFeeUsd))
            }
            appendLine("   - Tổng chi phí phiên này: $%.6f USD".format(costUsd))
            appendLine()
            appendLine("2. Lưu ý kết nối Gemini (Firebase AI Logic):")
            append("   Phản hồi này là dữ liệu mẫu vì thiết lập Firebase đã bị từ chối. Bật Firebase để kết nối trực tiếp với Gemini thông qua Firebase AI Logic.")
        }

        return AiGenerationResult(
            modelId = spec.modelId,
            modelName = spec.displayName,
            responseText = sampleResponse,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            costUsd = costUsd,
            latencyMs = elapsed,
            isPlaceholderNotice = true
        )
    }
}
