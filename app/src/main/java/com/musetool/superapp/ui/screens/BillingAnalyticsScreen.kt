package com.musetool.superapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.UsageLogEntity
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.data.repository.BillingPeriod
import com.musetool.superapp.data.repository.ModelCostBreakdown
import com.musetool.superapp.data.repository.PeriodBucketSummary
import android.content.Intent
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.ui.platform.LocalContext
import com.musetool.superapp.ui.components.CreditAndBalanceHeroCard
import com.musetool.superapp.ui.components.FinOpsForecastAndOptimizationCard
import com.musetool.superapp.ui.components.formatPreciseCurrency
import com.musetool.superapp.ui.components.formatPrimaryCurrency
import com.musetool.superapp.ui.components.formatSecondaryCurrency
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.CyberCyan
import com.musetool.superapp.ui.theme.ElectricViolet
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.RoseDebt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillingAnalyticsScreen(
    profile: UserProfileState,
    summary: BillingDashboardSummary,
    selectedPeriod: BillingPeriod,
    selectedModelIdFilter: String,
    showVndPrimary: Boolean,
    onSelectPeriod: (BillingPeriod) -> Unit,
    onSelectModelFilter: (String) -> Unit,
    onTopUpFreeCredit: (Double) -> Unit,
    onSimulateExhaustFreeCredit: () -> Unit,
    onSettleOutstanding: (Double) -> Unit,
    onUpdateBillingConfig: (Double, Double, Double) -> Unit,
    onQuickAddUsage: (String, String, String, Int, Int, Int) -> Unit,
    onDeleteLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showConfigModal by remember { mutableStateOf(false) }
    var showAddUsageModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("billing_analytics_list"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title & Config Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trung Tâm Tính Tiền Tự Động",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Phân tích theo Ngày • Tuần • Tháng • Năm & Từng Loại Model",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val reportText = buildString {
                                appendLine("=== BÁO CÁO ĐỐI SOÁT CHI PHÍ MUSE TOOL SUPERAPP ===")
                                appendLine("Tài khoản: ${profile.displayName} (${profile.email}) • v${profile.activeVersionName}")
                                appendLine("Free Credits Còn Lại: $%.2f / $%.2f USD".format(Locale.US, summary.freeCreditsRemainingUsd, summary.freeCreditsGrantedUsd))
                                appendLine("Outstanding Balance (Dư nợ): $%.2f USD".format(Locale.US, summary.netOutstandingBalanceUsd))
                                appendLine("--- TỔNG HỢP THEO THỜI GIAN ---")
                                appendLine("• Hôm nay: $%.3f USD".format(Locale.US, summary.todayCostUsd))
                                appendLine("• Tuần này: $%.3f USD".format(Locale.US, summary.thisWeekCostUsd))
                                appendLine("• Tháng này: $%.3f USD (Dự báo cuối tháng: $%.2f USD)".format(Locale.US, summary.thisMonthCostUsd, summary.projectedMonthEndCostUsd))
                                appendLine("• Năm nay: $%.3f USD".format(Locale.US, summary.thisYearCostUsd))
                                appendLine("• Tổng toàn thời gian: $%.3f USD".format(Locale.US, summary.totalAllTimeCostUsd))
                                appendLine("--- CHI TIẾT THEO MODEL ---")
                                summary.modelBreakdowns.filter { it.totalCostUsd > 0 }.forEach { m ->
                                    appendLine("• ${m.modelSpec.displayName}: $%.4f USD (${m.requestCount} calls, %.1f%%)".format(Locale.US, m.totalCostUsd, m.percentageOfPeriod))
                                }
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Báo cáo chi phí Muse Tool SuperApp")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Xuất báo cáo đối soát"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_export_billing_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "Xuất báo cáo đối soát",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { showConfigModal = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_billing_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Cấu hình định mức",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Định mức")
                    }
                    Button(
                        onClick = { showAddUsageModal = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_add_billing_record")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm bản ghi",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. Free Credits In Use & Outstanding Balance Hero Card
        item {
            CreditAndBalanceHeroCard(
                summary = summary,
                showVndPrimary = showVndPrimary,
                vndRate = profile.vndExchangeRate,
                onTopUpFreeCredit = onTopUpFreeCredit,
                onSimulateExhaustFreeCredit = onSimulateExhaustFreeCredit,
                onSettleOutstanding = onSettleOutstanding
            )
        }

        // 2b. FinOps Forecast & Smart Model Routing Card
        item {
            FinOpsForecastAndOptimizationCard(
                summary = summary,
                showVndPrimary = showVndPrimary,
                vndRate = profile.vndExchangeRate
            )
        }

        // 3. Period Selector (Từng Ngày, Từng Tuần, Từng Tháng, Từng Năm, Tổng Cộng)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Chu kỳ thống kê (Ngày · Tuần · Tháng · Năm · Tổng)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BillingPeriod.entries) { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { onSelectPeriod(period) },
                            label = { Text(period.labelVi) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("period_filter_${period.name}")
                        )
                    }
                }
            }
        }

        // 4. Period Breakdown Buckets (Exact totals per Day / Week / Month / Year)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Chi tiết theo ${selectedPeriod.labelVi}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val maxBucketCost = summary.periodBuckets.maxOfOrNull { it.totalCostUsd }?.coerceAtLeast(0.01) ?: 1.0
                    summary.periodBuckets.forEach { bucket ->
                        PeriodBucketRow(
                            bucket = bucket,
                            maxCost = maxBucketCost,
                            showVndPrimary = showVndPrimary,
                            vndRate = profile.vndExchangeRate
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // 5. Per-Model Breakdown Section (Từng loại Model dùng)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Chi phí theo từng loại Model (${selectedPeriod.labelVi})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedModelIdFilter == "ALL",
                            onClick = { onSelectModelFilter("ALL") },
                            label = { Text("Tất cả (${MuseModelCatalog.models.size})") }
                        )
                    }
                    items(MuseModelCatalog.models) { spec ->
                        FilterChip(
                            selected = selectedModelIdFilter == spec.modelId,
                            onClick = { onSelectModelFilter(spec.modelId) },
                            label = { Text(spec.displayName) }
                        )
                    }
                }
            }
        }

        items(summary.modelBreakdowns, key = { it.modelSpec.modelId }) { breakdown ->
            PerModelDetailedBillingCard(
                breakdown = breakdown,
                showVndPrimary = showVndPrimary,
                vndRate = profile.vndExchangeRate
            )
        }

        // 6. Individual Usage Transaction Ledger
        item {
            Text(
                text = "Nhật ký phiên gọi & Khấu trừ (${summary.filteredLogs.size} bản ghi)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(summary.filteredLogs, key = { it.id }) { log ->
            UsageTransactionRowCard(
                log = log,
                showVndPrimary = showVndPrimary,
                vndRate = profile.vndExchangeRate,
                onDelete = { onDeleteLog(log.id) }
            )
        }
    }

    if (showConfigModal) {
        BillingLimitConfigDialog(
            currentFreeCredit = profile.freeCreditAllocationUsd,
            currentBudget = profile.monthlyBudgetLimitUsd,
            currentVndRate = profile.vndExchangeRate,
            onDismiss = { showConfigModal = false },
            onSave = { freeUsd, budgetUsd, rate ->
                onUpdateBillingConfig(freeUsd, budgetUsd, rate)
                showConfigModal = false
            }
        )
    }

    if (showAddUsageModal) {
        QuickAddModelUsageDialog(
            vndRate = profile.vndExchangeRate,
            onDismiss = { showAddUsageModal = false },
            onConfirm = { modelId, category, note, inTok, outTok, daysAgo ->
                onQuickAddUsage(modelId, category, note, inTok, outTok, daysAgo)
                showAddUsageModal = false
            }
        )
    }
}

@Composable
private fun PeriodBucketRow(
    bucket: PeriodBucketSummary,
    maxCost: Double,
    showVndPrimary: Boolean,
    vndRate: Double
) {
    val ratio = (bucket.totalCostUsd / maxCost).toFloat().coerceIn(0.04f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bucket.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = bucket.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatPrimaryCurrency(bucket.totalCostUsd, showVndPrimary, vndRate),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatSecondaryCurrency(bucket.totalCostUsd, showVndPrimary, vndRate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Free Credit: $%.2f".format(Locale.US, bucket.freeCreditCoveredUsd),
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldCredit
            )
            Text(
                text = "Outstanding Balance: $%.2f".format(Locale.US, bucket.outstandingChargedUsd),
                style = MaterialTheme.typography.labelSmall,
                color = if (bucket.outstandingChargedUsd > 0) RoseDebt else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PerModelDetailedBillingCard(
    breakdown: ModelCostBreakdown,
    showVndPrimary: Boolean,
    vndRate: Double
) {
    val spec = breakdown.modelSpec
    val modelColor = Color(spec.colorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_breakdown_card_${spec.modelId}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(modelColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = spec.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = spec.tierBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${spec.provider} · In $${spec.inputPricePer1MUsd}/1M | Out $${spec.outputPricePer1MUsd}/1M",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatPreciseCurrency(breakdown.totalCostUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%.1f%% kỳ này".format(Locale.US, breakdown.percentageOfPeriod),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChipBox(
                    label = "Số phiên",
                    value = "${breakdown.requestCount} calls",
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricChipBox(
                    label = "Trừ Free Credit",
                    value = "$%.3f".format(Locale.US, breakdown.coveredByFreeUsd),
                    color = EmeraldCredit,
                    modifier = Modifier.weight(1f)
                )
                MetricChipBox(
                    label = "Tính vào Dư nợ",
                    value = "$%.3f".format(Locale.US, breakdown.chargedToOutstandingUsd),
                    color = RoseDebt,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tổng lưu lượng: %,d Input Tokens • %,d Output Tokens".format(
                    Locale.US,
                    breakdown.inputTokens,
                    breakdown.outputTokens
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricChipBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UsageTransactionRowCard(
    log: UsageLogEntity,
    showVndPrimary: Boolean,
    vndRate: Double,
    onDelete: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val spec = MuseModelCatalog.findById(log.modelId)
    val modelColor = Color(spec.colorHex)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(modelColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.modelName,
                        style = MaterialTheme.typography.titleMedium,
                        color = modelColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${dateFmt.format(Date(log.timestamp))} (v${log.appVersionAtUse})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.promptSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "In: %,d • Out: %,d tokens | Free: $%.3f • Outstanding: $%.3f".format(
                        Locale.US,
                        log.inputTokens,
                        log.outputTokens,
                        log.coveredByFreeCreditUsd,
                        log.chargedToOutstandingUsd
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.chargedToOutstandingUsd > 0) RoseDebt else EmeraldCredit
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatPreciseCurrency(log.totalCostUsd, showVndPrimary, vndRate),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa bản ghi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BillingLimitConfigDialog(
    currentFreeCredit: Double,
    currentBudget: Double,
    currentVndRate: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    var freeText by remember { mutableStateOf("%.2f".format(Locale.US, currentFreeCredit)) }
    var budgetText by remember { mutableStateOf("%.2f".format(Locale.US, currentBudget)) }
    var rateText by remember { mutableStateOf("%.0f".format(Locale.US, currentVndRate)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cấu hình Free Credits & Tỷ giá") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = freeText,
                    onValueChange = { freeText = it },
                    label = { Text("Tổng Free Credits được cấp (USD)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Ngưỡng cảnh báo ngân sách tháng (USD)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Tỷ giá quy đổi 1 USD -> VNĐ") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val f = freeText.toDoubleOrNull() ?: currentFreeCredit
                    val b = budgetText.toDoubleOrNull() ?: currentBudget
                    val r = rateText.toDoubleOrNull() ?: currentVndRate
                    onSave(f, b, r)
                }
            ) {
                Text("Lưu Cấu Hình")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
