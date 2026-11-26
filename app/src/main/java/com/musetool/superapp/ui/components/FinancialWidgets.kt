package com.musetool.superapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.data.repository.ModelCostBreakdown
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.CyberCyan
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.JetBrainsMonoFontFamily
import com.musetool.superapp.ui.theme.MuseThemePreset
import com.musetool.superapp.ui.theme.RoseDebt
import java.text.NumberFormat
import java.util.Locale

fun formatPrimaryCurrency(
    usdAmount: Double,
    showVndPrimary: Boolean,
    vndRate: Double
): String {
    return if (showVndPrimary) {
        val vnd = (usdAmount * vndRate).toLong()
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(vnd)
        "${formatted}₫"
    } else {
        "$%.2f".format(Locale.US, usdAmount)
    }
}

fun formatPreciseCurrency(
    usdAmount: Double,
    showVndPrimary: Boolean,
    vndRate: Double
): String {
    return if (showVndPrimary) {
        val vnd = (usdAmount * vndRate).toLong()
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(vnd)
        "${formatted}₫"
    } else {
        if (usdAmount < 0.01 && usdAmount > 0.0) {
            "$%.5f".format(Locale.US, usdAmount)
        } else {
            "$%.3f".format(Locale.US, usdAmount)
        }
    }
}

fun formatSecondaryCurrency(
    usdAmount: Double,
    showVndPrimary: Boolean,
    vndRate: Double
): String {
    return if (showVndPrimary) {
        "≈ $%.2f USD".format(Locale.US, usdAmount)
    } else {
        val vnd = (usdAmount * vndRate).toLong()
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(vnd)
        "≈ ${formatted}₫"
    }
}

/**
 * Editorial Financial Hero Card:
 * Clean, spacious, high-contrast layout displaying Free Credits In Use (Remaining)
 * and Outstanding Balance side-by-side without cluttered boxes.
 */
@Composable
fun CreditAndBalanceHeroCard(
    summary: BillingDashboardSummary,
    showVndPrimary: Boolean,
    vndRate: Double,
    onTopUpFreeCredit: (Double) -> Unit,
    onSimulateExhaustFreeCredit: () -> Unit,
    onSettleOutstanding: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedFreeRatio by animateFloatAsState(
        targetValue = (1f - summary.freeCreditUsageRatio).coerceIn(0f, 1f),
        animationSpec = tween(550),
        label = "freeCreditRemainingRatio"
    )
    val isFreeExhausted = summary.freeCreditsRemainingUsd <= 0.001

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credit_and_balance_hero_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            // Subtle Top Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ví Tín Dụng & Dư Nợ AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isFreeExhausted) RoseDebt.copy(alpha = 0.12f) else EmeraldCredit.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isFreeExhausted) RoseDebt else EmeraldCredit)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFreeExhausted) "Đang tính Dư nợ" else "Free Credit khả dụng",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFreeExhausted) RoseDebt else EmeraldCredit,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dual Metric Columns (Clean Bento Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. FREE CREDITS IN USE (CÒN LẠI)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .padding(16.dp)
                        .testTag("free_credits_box")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Free Credits còn lại",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Free Credit",
                            tint = EmeraldCredit,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPrimaryCurrency(summary.freeCreditsRemainingUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 23.sp
                        ),
                        color = if (isFreeExhausted) AmberWarning else EmeraldCredit,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatSecondaryCurrency(summary.freeCreditsRemainingUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { animatedFreeRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isFreeExhausted) AmberWarning else EmeraldCredit,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Đã dùng $%.2f / $%.2f".format(
                            Locale.US,
                            summary.freeCreditsUsedUsd,
                            summary.freeCreditsGrantedUsd
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 2. OUTSTANDING BALANCE (KHI HẾT FREE CREDIT)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .padding(16.dp)
                        .testTag("outstanding_balance_box")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Outstanding Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (summary.netOutstandingBalanceUsd > 0) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                            contentDescription = "Outstanding Balance",
                            tint = if (summary.netOutstandingBalanceUsd > 0) RoseDebt else EmeraldCredit,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPrimaryCurrency(summary.netOutstandingBalanceUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 23.sp
                        ),
                        color = if (summary.netOutstandingBalanceUsd > 0) RoseDebt else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatSecondaryCurrency(summary.netOutstandingBalanceUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    val overageProgress = (summary.netOutstandingBalanceUsd / 25.0).toFloat().coerceIn(0.04f, 1f)
                    LinearProgressIndicator(
                        progress = { if (summary.netOutstandingBalanceUsd > 0) overageProgress else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoseDebt,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vượt mức $%.2f · Đã trả $%.2f".format(
                            Locale.US,
                            summary.grossOutstandingAccruedUsd,
                            summary.settledOutstandingUsd
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clean Action Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onTopUpFreeCredit(10.0) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_topup_free_credit"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "Cộng thêm Free Credit",
                        tint = EmeraldCredit,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+$10 Free Credit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isFreeExhausted) {
                    OutlinedButton(
                        onClick = onSimulateExhaustFreeCredit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_exhaust_free_credit"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Mô phỏng Hết Free",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (summary.netOutstandingBalanceUsd > 0.0) {
                    Button(
                        onClick = { onSettleOutstanding(summary.netOutstandingBalanceUsd) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_settle_outstanding"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Thanh toán dư nợ",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tất toán Dư nợ",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean Time-Horizon Cost Summary Card:
 * Consolidates All-Time Total + Today / Week / Month / Year into a single cohesive card
 * with crisp typography and zero visual clutter.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeHorizonCostGrid(
    summary: BillingDashboardSummary,
    showVndPrimary: Boolean,
    vndRate: Double,
    onSelectHorizon: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            // Top Row: All-Time Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectHorizon(4) }
                    .testTag("horizon_total_card"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tổng chi phí lũy kế",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrimaryCurrency(summary.totalAllTimeCostUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 30.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatSecondaryCurrency(summary.totalAllTimeCostUsd, showVndPrimary, vndRate)} · ${summary.totalRequestsCount} phiên · %,d tokens".format(
                            Locale.US,
                            summary.totalTokensProcessed
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Free: $%.2f".format(Locale.US, summary.freeCreditsUsedUsd),
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldCredit,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dư nợ: $%.2f".format(Locale.US, summary.netOutstandingBalanceUsd),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (summary.netOutstandingBalanceUsd > 0) RoseDebt else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            // 2x2 Minimalist Horizon Grid (Hôm nay, Tuần này, Tháng này, Năm nay)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizonMiniCard(
                    title = "Hôm nay",
                    usdAmount = summary.todayCostUsd,
                    showVndPrimary = showVndPrimary,
                    vndRate = vndRate,
                    onClick = { onSelectHorizon(0) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("horizon_day_card")
                )
                HorizonMiniCard(
                    title = "7 ngày qua",
                    usdAmount = summary.thisWeekCostUsd,
                    showVndPrimary = showVndPrimary,
                    vndRate = vndRate,
                    onClick = { onSelectHorizon(1) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("horizon_week_card")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizonMiniCard(
                    title = "Tháng này",
                    usdAmount = summary.thisMonthCostUsd,
                    showVndPrimary = showVndPrimary,
                    vndRate = vndRate,
                    onClick = { onSelectHorizon(2) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("horizon_month_card")
                )
                HorizonMiniCard(
                    title = "Năm nay",
                    usdAmount = summary.thisYearCostUsd,
                    showVndPrimary = showVndPrimary,
                    vndRate = vndRate,
                    onClick = { onSelectHorizon(3) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("horizon_year_card")
                )
            }
        }
    }
}

@Composable
private fun HorizonMiniCard(
    title: String,
    usdAmount: Double,
    showVndPrimary: Boolean,
    vndRate: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatPrimaryCurrency(usdAmount, showVndPrimary, vndRate),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JetBrainsMonoFontFamily
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatSecondaryCurrency(usdAmount, showVndPrimary, vndRate),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModelDistributionCanvasChart(
    breakdowns: List<ModelCostBreakdown>,
    showVndPrimary: Boolean,
    vndRate: Double,
    modifier: Modifier = Modifier
) {
    val activeModels = breakdowns.filter { it.totalCostUsd > 0.0 }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chi phí theo từng loại Model",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${activeModels.size}/${breakdowns.size} model hoạt động",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                val totalWidth = size.width
                val totalHeight = size.height
                if (activeModels.isEmpty()) {
                    drawRoundRect(
                        color = emptyBarColor,
                        size = Size(totalWidth, totalHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                } else {
                    var currentX = 0f
                    activeModels.forEach { item ->
                        val sweepWidth = (item.percentageOfPeriod / 100f) * totalWidth
                        if (sweepWidth > 1f) {
                            drawRect(
                                color = Color(item.modelSpec.colorHex),
                                topLeft = Offset(currentX, 0f),
                                size = Size(sweepWidth.coerceAtMost(totalWidth - currentX), totalHeight)
                            )
                            currentX += sweepWidth
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            breakdowns.forEachIndexed { index, item ->
                val modelColor = Color(item.modelSpec.colorHex)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(modelColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.modelSpec.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${item.requestCount} phiên · %,d In / %,d Out".format(
                                    Locale.US,
                                    item.inputTokens,
                                    item.outputTokens
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatPreciseCurrency(item.totalCostUsd, showVndPrimary, vndRate),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%.1f%% · Free $%.2f / Nợ $%.2f".format(
                                Locale.US,
                                item.percentageOfPeriod,
                                item.coveredByFreeUsd,
                                item.chargedToOutstandingUsd
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.chargedToOutstandingUsd > 0) RoseDebt else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < breakdowns.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Compact, sleek single-row Theme Bar with color dots and Sáng/Tối toggle.
 */
@Composable
fun MinimalThemeSwitcherBar(
    activeThemeId: String,
    onSelectTheme: (MuseThemePreset) -> Unit,
    onToggleLightDark: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePreset = MuseThemePreset.fromId(activeThemeId)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("minimal_theme_switcher_bar"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Giao diện:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activePreset.titleVi} (${activePreset.modeLabel})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Segmented Sáng / Tối Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val isDark = activePreset.isDark
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isDark) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { onToggleLightDark(false) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("btn_quick_light_mode")
                    ) {
                        Text(
                            text = "Sáng",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (!isDark) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { onToggleLightDark(true) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("btn_quick_dark_mode")
                    ) {
                        Text(
                            text = "Tối",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isDark) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    items = MuseThemePreset.entries,
                    key = { it.id }
                ) { preset ->
                    val isSelected = preset.id == activePreset.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectTheme(preset) }
                            .testTag("theme_preset_chip_${preset.id}"),
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.previewBgHex))
                                    .border(1.5.dp, Color(preset.previewAccentHex), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.titleVi,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinOpsForecastAndOptimizationCard(
    summary: BillingDashboardSummary,
    showVndPrimary: Boolean,
    vndRate: Double,
    modifier: Modifier = Modifier
) {
    val budgetRatio = summary.monthlyBudgetUtilizationRatio.coerceIn(0f, 1f)
    val projectedRatio = (summary.projectedMonthEndCostUsd / summary.monthlyBudgetLimitUsd.coerceAtLeast(1.0))
        .toFloat()
        .coerceIn(0f, 1.35f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("finops_forecast_optimization_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dự báo dòng tiền & Tối ưu Model",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Phân tích tốc độ tiêu thụ & hạn mức ngân sách tháng",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (summary.isProjectedToExceedBudget) AmberWarning.copy(alpha = 0.14f) else EmeraldCredit.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = if (summary.isProjectedToExceedBudget) "Vượt hạn mức" else "An toàn",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (summary.isProjectedToExceedBudget) AmberWarning else EmeraldCredit,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Trung bình/ngày",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrimaryCurrency(summary.dailyBurnRateUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Dự kiến cuối tháng",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrimaryCurrency(summary.projectedMonthEndCostUsd, showVndPrimary, vndRate),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isProjectedToExceedBudget) AmberWarning else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Thời hạn Free",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (summary.estimatedDaysUntilFreeExhausted == 0) "0 ngày"
                        else "~${summary.estimatedDaysUntilFreeExhausted} ngày",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = if (summary.estimatedDaysUntilFreeExhausted <= 3) RoseDebt else EmeraldCredit
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ngân sách tháng (${formatPrimaryCurrency(summary.monthlyBudgetLimitUsd, showVndPrimary, vndRate)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(budgetRatio * 100).toInt()}% đã dùng · Dự báo ${(projectedRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LinearProgressIndicator(
                    progress = { budgetRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (summary.isProjectedToExceedBudget) AmberWarning else EmeraldCredit,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            }

            val rec = summary.finOpsRecommendation
            if (rec != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gợi ý tối ưu: ${rec.currentTopModelName} → ${rec.suggestedModelName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldCredit,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tiết kiệm ~${formatPrimaryCurrency(rec.potentialMonthlySavingsUsd, showVndPrimary, vndRate)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldCredit,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = rec.rationaleVi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AppIconLongPressShortcutsCard(
    summary: BillingDashboardSummary,
    profile: com.musetool.superapp.data.local.UserProfileState,
    onExecuteShortcut: (com.musetool.superapp.shortcut.AppShortcutItem) -> Unit,
    onPinShortcut: (com.musetool.superapp.shortcut.AppShortcutItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val liveShortcuts = com.musetool.superapp.shortcut.MuseAppShortcutManager.buildLiveShortcutsList(
        summary = summary,
        profile = profile
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_icon_long_press_shortcuts_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lối tắt nhấn giữ biểu tượng (App Shortcuts)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Nhấn giữ icon ngoài màn hình chính hoặc chạm nhanh bên dưới",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            liveShortcuts.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .clickable { onExecuteShortcut(item) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("shortcut_menu_item_${item.shortcutId}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.longLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.subtitleDetail,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPinShortcut(item) }
                                .testTag("btn_pin_shortcut_${item.shortcutId}"),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Ghim",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onExecuteShortcut(item) }
                                .testTag("btn_exec_shortcut_${item.shortcutId}"),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Mở",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
