package com.musetool.superapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musetool.superapp.R
import com.musetool.superapp.data.ai.AiGenerationResult
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.PromptTemplateEntity
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.ui.components.formatPreciseCurrency
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.CyberCyan
import com.musetool.superapp.ui.theme.ElectricViolet
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.RoseDebt
import java.util.Locale

@Composable
fun AiStudioCalculatorScreen(
    profile: UserProfileState,
    summary: BillingDashboardSummary,
    templates: List<PromptTemplateEntity>,
    isGenerating: Boolean,
    lastResult: AiGenerationResult?,
    showVndPrimary: Boolean,
    preselectedModelId: String? = null,
    onRunPrompt: (String, String, String, Int) -> Unit,
    onSaveTemplate: (String, String, String, String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSpec by remember {
        mutableStateOf(
            if (!preselectedModelId.isNullOrBlank()) {
                MuseModelCatalog.findById(preselectedModelId)
            } else {
                MuseModelCatalog.models.first()
            }
        )
    }

    LaunchedEffect(preselectedModelId) {
        if (!preselectedModelId.isNullOrBlank()) {
            selectedSpec = MuseModelCatalog.findById(preselectedModelId)
        }
    }
    var promptText by remember {
        mutableStateOf(
            "Phân tích chiến lược tối ưu chi phí Multi-Model giữa Gemini 3.8 Flash, Gemini 4 Argon và Claude 3.7 Sonnet khi chạy tác vụ lớn."
        )
    }
    var targetOutputTokens by remember { mutableFloatStateOf(24000f) }

    val estimatedInputTokens = ((promptText.length * 380).coerceAtLeast(4500))
    val estimatedOutputTokens = targetOutputTokens.toInt().coerceAtLeast(1000)
    val estimatedCostUsd = MuseModelCatalog.calculateCostUsd(
        selectedSpec.modelId,
        estimatedInputTokens,
        estimatedOutputTokens
    )
    val willCoverByFree = estimatedCostUsd.coerceAtMost(summary.freeCreditsRemainingUsd)
    val willSpillToOutstanding = (estimatedCostUsd - willCoverByFree).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("ai_studio_lazy_column"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "AI Studio & Bộ Tính Tiền Thời Gian Thực",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Chạy thử nghiệm 7 dòng Model AI • Tự động bóc tách Free Credits & Outstanding Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Mandatory Firebase AI Notice Banner (since user declined Firebase setup)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_ai_placeholder_notice"),
                shape = RoundedCornerShape(16.dp),
                color = AmberWarning.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberWarning.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Thông báo Firebase AI",
                        tint = AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.firebase_ai_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. Model Selector Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. Chọn dòng Model sử dụng (7 Models)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MuseModelCatalog.models) { spec ->
                        val isSelected = selectedSpec.modelId == spec.modelId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSpec = spec },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = spec.displayName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "In $${spec.inputPricePer1MUsd} · Out $${spec.outputPricePer1MUsd}/1M",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("studio_model_chip_${spec.modelId}")
                        )
                    }
                }
            }
        }

        // 4. Pinned Prompt Templates
        if (templates.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "2. Mẫu Prompt tác vụ nhanh",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(templates) { tpl ->
                            Surface(
                                modifier = Modifier
                                    .width(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        promptText = tpl.promptBody
                                        selectedSpec = MuseModelCatalog.findById(tpl.recommendedModelId)
                                        targetOutputTokens = tpl.defaultOutputTokens.toFloat()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = tpl.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${tpl.category} · ${MuseModelCatalog.findById(tpl.recommendedModelId).displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Prompt Input & Live Pre-Execution Cost Calculator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text("Nhập Prompt hoặc Tác vụ AI cần xử lý & tính phí") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_prompt_input"),
                        minLines = 3,
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quy mô Output Tokens dự kiến:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%,d tokens".format(Locale.US, estimatedOutputTokens),
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = targetOutputTokens,
                        onValueChange = { targetOutputTokens = it },
                        valueRange = 2000f..150000f,
                        modifier = Modifier.testTag("studio_tokens_slider")
                    )

                    // Live Cost Preview Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = "Dự tính chi phí",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DỰ TÍNH TỰ ĐỘNG TRƯỚC KHI CHẠY:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = formatPreciseCurrency(
                                        estimatedCostUsd,
                                        showVndPrimary,
                                        profile.vndExchangeRate
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• Khấu trừ Free Credit: $%.4f".format(Locale.US, willCoverByFree),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldCredit
                                )
                                Text(
                                    text = "• Cộng vào Outstanding: $%.4f".format(Locale.US, willSpillToOutstanding),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (willSpillToOutstanding > 0) RoseDebt else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onRunPrompt(
                                    selectedSpec.modelId,
                                    selectedSpec.category,
                                    promptText,
                                    estimatedOutputTokens
                                )
                            },
                            enabled = !isGenerating && promptText.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_run_ai_prompt"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang xử lý & Tính tiền...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Thực thi",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Thực thi & Tự động Tính Phí",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onSaveTemplate(
                                    promptText.take(32),
                                    selectedSpec.category,
                                    promptText,
                                    selectedSpec.modelId,
                                    estimatedInputTokens,
                                    estimatedOutputTokens
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_save_prompt_template")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Lưu mẫu",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Output Result Card
        if (lastResult != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_studio_output_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Kết quả",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "KẾT QUẢ & HÓA ĐƠN PHIÊN (${lastResult.modelName})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CyberCyan
                                )
                            }
                            Text(
                                text = "${lastResult.latencyMs} ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldCredit
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lastResult.responseText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 7. Live 7-Model Price & Waterfall Comparison Matrix for Current Token Volume
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cross_model_cost_matrix_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MA TRẬN SO SÁNH CHI PHÍ 7 MODEL (CÙNG QUY MÔ %,d IN / %,d OUT TOKENS)".format(
                            Locale.US,
                            estimatedInputTokens,
                            estimatedOutputTokens
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val maxModelCost = MuseModelCatalog.models.maxOfOrNull {
                        MuseModelCatalog.calculateCostUsd(it.modelId, estimatedInputTokens, estimatedOutputTokens)
                    }?.coerceAtLeast(0.0001) ?: 1.0

                    MuseModelCatalog.models.forEach { spec ->
                        val cost = MuseModelCatalog.calculateCostUsd(
                            spec.modelId,
                            estimatedInputTokens,
                            estimatedOutputTokens
                        )
                        val isCurrent = spec.modelId == selectedSpec.modelId
                        val diffRatio = (cost / maxModelCost).toFloat().coerceIn(0.04f, 1f)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedSpec = spec },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(Color(spec.colorHex))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = spec.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = formatPreciseCurrency(cost, showVndPrimary, profile.vndExchangeRate),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "In $${spec.inputPricePer1MUsd}/1M • Out $${spec.outputPricePer1MUsd}/1M",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(diffRatio * 100).toInt()}% so với mức cao nhất",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
