package com.musetool.superapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musetool.superapp.data.local.OtaReleaseEntity
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.ui.components.formatPrimaryCurrency
import com.musetool.superapp.ui.components.formatSecondaryCurrency
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.CyberCyan
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.JetBrainsMonoFontFamily
import com.musetool.superapp.ui.theme.RoseDebt
import com.musetool.superapp.widget.MuseNote20UltraWidgetProvider
import java.util.Locale

enum class Note20WidgetSizeMode(val title: String, val gridSpec: String) {
    COMPACT_4X1("Tối giản 4×1", "Thanh Pill gọn"),
    STANDARD_4X2("Chuẩn 4×2", "One UI Cân bằng"),
    EXPANDED_4X3("Linh hoạt 4×3", "Đầy đủ Model & OTA")
}

enum class Note20MinimalFinish(
    val label: String,
    val bgHex: Long,
    val borderHex: Long,
    val primaryTextHex: Long,
    val mutedTextHex: Long,
    val accentHex: Long
) {
    GRAPHITE_MONO(
        label = "Graphite Tối Giản",
        bgHex = 0xFF121418,
        borderHex = 0xFF282C34,
        primaryTextHex = 0xFFF8FAFC,
        mutedTextHex = 0xFF8E96A4,
        accentHex = 0xFFE2E8F0
    ),
    MYSTIC_BRONZE_SUBTLE(
        label = "Mystic Bronze (Note20U)",
        bgHex = 0xFF161312,
        borderHex = 0xFF3A2E2A,
        primaryTextHex = 0xFFFAF6F3,
        mutedTextHex = 0xFFA89992,
        accentHex = 0xFFD4A373
    ),
    ALABASTER_LIGHT(
        label = "Trắng Tối Giản",
        bgHex = 0xFFF8F9FA,
        borderHex = 0xFFE2E8F0,
        primaryTextHex = 0xFF111317,
        mutedTextHex = 0xFF64748B,
        accentHex = 0xFF18181B
    )
}

@Composable
fun Note20UltraWidgetStudioScreen(
    profile: UserProfileState,
    summary: BillingDashboardSummary,
    showVndPrimary: Boolean,
    edgePanelEnabled: Boolean,
    newerOtaRelease: OtaReleaseEntity?,
    onToggleEdgePanel: (Boolean) -> Unit,
    onToggleCurrency: () -> Unit,
    onTopUpFreeCredit: (Double) -> Unit,
    onQuickLogFromSPen: () -> Unit,
    onOpenOtaTab: () -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sizeMode by remember { mutableStateOf(Note20WidgetSizeMode.STANDARD_4X2) }
    var finishStyle by remember { mutableStateOf(Note20MinimalFinish.GRAPHITE_MONO) }
    var cornerRadiusDp by remember { mutableFloatStateOf(26f) }
    var surfaceAlpha by remember { mutableFloatStateOf(0.96f) }
    var selectedPeriodIndex by remember { mutableIntStateOf(0) }

    val topModelName = summary.modelBreakdowns.firstOrNull()?.modelSpec?.displayName ?: "Gemini 3.8 Flash"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("note20_widget_studio_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Minimalist Title & Samsung Note 20 Ultra 5G VN Hardware Tag
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Widget Note 20 Ultra 5G VN",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SM-N986B/DS (XXV) • 6.9\" WQHD+ 120Hz • Thiết kế tối giản, ít màu, linh hoạt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Note 20 Ultra 5G VN",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "One UI · 5G VN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. Live Interactive Samsung Note 20 Ultra 5G VN Home Screen Preview Frame
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF07080A))
                    .border(1.dp, Color(0xFF22262E), RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                // Simulated Note 20 Ultra 5G VN Status Bar (Punch-hole camera + 5G VN indicator)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VN-5G • 120Hz",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    // Center Infinity-O punch hole camera dot of Note 20 Ultra
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A1D24))
                            .border(1.dp, Color(0xFF334155), CircleShape)
                    )
                    Text(
                        text = "${sizeMode.title} • Live",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }

                // The Live Minimalist Flexible Widget Card itself
                InteractiveNote20UltraWidgetCard(
                    profile = profile,
                    summary = summary,
                    sizeMode = sizeMode,
                    finish = finishStyle,
                    cornerRadiusDp = cornerRadiusDp,
                    surfaceAlpha = surfaceAlpha,
                    showVndPrimary = showVndPrimary,
                    selectedPeriodIndex = selectedPeriodIndex,
                    newerOtaRelease = newerOtaRelease,
                    onCyclePeriod = { selectedPeriodIndex = (selectedPeriodIndex + 1) % 5 },
                    onToggleCurrency = onToggleCurrency,
                    onQuickSPenAction = onQuickLogFromSPen,
                    onOpenOta = onOpenOtaTab
                )
            }
        }

        // 3. Action Buttons: Pin to Samsung One UI Home Screen & Sync Data
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        MuseNote20UltraWidgetProvider.syncDataToNote20Widget(
                            context = context,
                            freeRemainingUsd = summary.freeCreditsRemainingUsd,
                            freeUsedUsd = summary.freeCreditsUsedUsd,
                            outstandingUsd = summary.netOutstandingBalanceUsd,
                            todayUsd = summary.todayCostUsd,
                            weekUsd = summary.thisWeekCostUsd,
                            monthUsd = summary.thisMonthCostUsd,
                            yearUsd = summary.thisYearCostUsd,
                            totalUsd = summary.totalAllTimeCostUsd,
                            vndRate = profile.vndExchangeRate,
                            profileName = profile.displayName,
                            versionName = profile.activeVersionName,
                            topModelName = topModelName
                        )
                        val pinned = MuseNote20UltraWidgetProvider.requestPinNote20Widget(context)
                        if (pinned) {
                            onNotify("Đã gửi yêu cầu ghim Widget lên màn hình chính Samsung Note 20 Ultra 5G VN!")
                        } else {
                            onNotify("Đã đồng bộ Widget Note 20 Ultra 5G VN! Nhấn giữ màn hình chính One UI -> Widget -> Muse Note20 Ultra 5G VN.")
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_pin_note20_widget"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddToHomeScreen,
                        contentDescription = "Ghim Widget One UI",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ghim Widget Màn Hình Chính",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        MuseNote20UltraWidgetProvider.syncDataToNote20Widget(
                            context = context,
                            freeRemainingUsd = summary.freeCreditsRemainingUsd,
                            freeUsedUsd = summary.freeCreditsUsedUsd,
                            outstandingUsd = summary.netOutstandingBalanceUsd,
                            todayUsd = summary.todayCostUsd,
                            weekUsd = summary.thisWeekCostUsd,
                            monthUsd = summary.thisMonthCostUsd,
                            yearUsd = summary.thisYearCostUsd,
                            totalUsd = summary.totalAllTimeCostUsd,
                            vndRate = profile.vndExchangeRate,
                            profileName = profile.displayName,
                            versionName = profile.activeVersionName,
                            topModelName = topModelName
                        )
                        onNotify("Đã đồng bộ số liệu Free Credits ($%.2f) & Outstanding ($%.2f) ra Widget!".format(
                            Locale.US,
                            summary.freeCreditsRemainingUsd,
                            summary.netOutstandingBalanceUsd
                        ))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_sync_note20_widget"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Đồng bộ Widget",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đồng bộ Ngay")
                }
            }
        }

        // 4. Flexible & Fluid Layout Controls (Kích thước, Tông màu ít màu, Độ bo góc, Edge Panel)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ViewQuilt,
                            contentDescription = "Tùy biến linh hoạt",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TÙY BIẾN LINH HOẠT & TỐI GIẢN (ONE UI GRID)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Size selector
                    Column {
                        Text(
                            text = "Bố cục linh động (Chạm để chuyển đổi tức thì):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Note20WidgetSizeMode.entries) { mode ->
                                FilterChip(
                                    selected = sizeMode == mode,
                                    onClick = { sizeMode = mode },
                                    label = { Text("${mode.title} (${mode.gridSpec})") },
                                    modifier = Modifier.testTag("widget_size_chip_${mode.name}")
                                )
                            }
                        }
                    }

                    // Minimalist low-color finish selector
                    Column {
                        Text(
                            text = "Tông màu tối giản (Ít màu • Chống mỏi mắt AMOLED 2X):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Note20MinimalFinish.entries) { finish ->
                                FilterChip(
                                    selected = finishStyle == finish,
                                    onClick = { finishStyle = finish },
                                    label = { Text(finish.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("widget_finish_chip_${finish.name}")
                                )
                            }
                        }
                    }

                    // Corner radius & transparency sliders
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bo góc One UI & Độ trong suốt nền",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${cornerRadiusDp.toInt()}dp • ${(surfaceAlpha * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Slider(
                            value = cornerRadiusDp,
                            onValueChange = { cornerRadiusDp = it },
                            valueRange = 8f..34f
                        )
                        Slider(
                            value = surfaceAlpha,
                            onValueChange = { surfaceAlpha = it },
                            valueRange = 0.65f..1.0f
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Curved Edge Panel Toggle for Note 20 Ultra 5G
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Thanh cạnh Edge Panel & S-Pen (Note 20 Ultra 5G)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hiển thị thanh trượt tối giản bên mép phải màn hình cong để xem nhanh chi phí từ mọi tab",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = edgePanelEnabled,
                            onCheckedChange = onToggleEdgePanel,
                            modifier = Modifier.testTag("switch_note20_edge_panel")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveNote20UltraWidgetCard(
    profile: UserProfileState,
    summary: BillingDashboardSummary,
    sizeMode: Note20WidgetSizeMode,
    finish: Note20MinimalFinish,
    cornerRadiusDp: Float,
    surfaceAlpha: Float,
    showVndPrimary: Boolean,
    selectedPeriodIndex: Int,
    newerOtaRelease: OtaReleaseEntity?,
    onCyclePeriod: () -> Unit,
    onToggleCurrency: () -> Unit,
    onQuickSPenAction: () -> Unit,
    onOpenOta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(finish.bgHex).copy(alpha = surfaceAlpha)
    val borderColor = Color(finish.borderHex)
    val primaryText = Color(finish.primaryTextHex)
    val mutedText = Color(finish.mutedTextHex)
    val accentColor = Color(finish.accentHex)

    val periodLabels = listOf("Hôm nay", "Tuần này", "Tháng này", "Năm nay", "Tổng cộng")
    val periodValues = listOf(
        summary.todayCostUsd,
        summary.thisWeekCostUsd,
        summary.thisMonthCostUsd,
        summary.thisYearCostUsd,
        summary.totalAllTimeCostUsd
    )
    val activePeriodLabel = periodLabels.getOrElse(selectedPeriodIndex) { "Tổng cộng" }
    val activePeriodCost = periodValues.getOrElse(selectedPeriodIndex) { summary.totalAllTimeCostUsd }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(cornerRadiusDp.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadiusDp.dp))
            .padding(18.dp)
            .testTag("interactive_note20_widget_preview")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Top bar: Minimalist Header + Tap-to-switch currency or OTA status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldCredit)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MUSE · NOTE20 ULTRA 5G VN",
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedText
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleCurrency),
                        shape = RoundedCornerShape(8.dp),
                        color = borderColor.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = if (showVndPrimary) "VNĐ ₫" else "USD $",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenOta),
                        shape = RoundedCornerShape(8.dp),
                        color = borderColor.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = if (newerOtaRelease != null) "OTA v${newerOtaRelease.versionName} ↑" else "v${profile.activeVersionName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (newerOtaRelease != null) AmberWarning else mutedText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Core 3-Column Minimalist Metrics: Free Credits Remaining | Outstanding Balance | Active Period (Tap to cycle!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Free Credits In Use Remaining
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free Credits",
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedText
                    )
                    Text(
                        text = formatPrimaryCurrency(summary.freeCreditsRemainingUsd, showVndPrimary, profile.vndExchangeRate),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "Còn lại (${((1f - summary.freeCreditUsageRatio) * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldCredit
                    )
                }

                // 2. Outstanding Balance
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Outstanding",
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedText
                    )
                    Text(
                        text = formatPrimaryCurrency(summary.netOutstandingBalanceUsd, showVndPrimary, profile.vndExchangeRate),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "Khi hết Free",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoseDebt
                    )
                }

                // 3. Fluid Tappable Period Metric (Cycles Day -> Week -> Month -> Year -> Total)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(borderColor.copy(alpha = 0.35f))
                        .clickable(onClick = onCyclePeriod)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("widget_cycle_period_box")
                ) {
                    Text(
                        text = "$activePeriodLabel ↻",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                    Text(
                        text = formatPrimaryCurrency(activePeriodCost, showVndPrimary, profile.vndExchangeRate),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "Chạm đổi kỳ",
                        style = MaterialTheme.typography.labelSmall,
                        color = mutedText
                    )
                }
            }

            // Additional sections for 4x2 Standard and 4x3 Expanded modes
            AnimatedContent(targetState = sizeMode, label = "widgetSizeTransition") { mode ->
                when (mode) {
                    Note20WidgetSizeMode.COMPACT_4X1 -> {
                        // Ultra-minimal progress line only
                        LinearProgressIndicator(
                            progress = { (1f - summary.freeCreditUsageRatio).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = primaryText,
                            trackColor = borderColor
                        )
                    }

                    Note20WidgetSizeMode.STANDARD_4X2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = borderColor)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ngày: ${formatPrimaryCurrency(summary.todayCostUsd, showVndPrimary, profile.vndExchangeRate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedText
                                )
                                Text(
                                    text = "Tuần: ${formatPrimaryCurrency(summary.thisWeekCostUsd, showVndPrimary, profile.vndExchangeRate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedText
                                )
                                Text(
                                    text = "Tháng: ${formatPrimaryCurrency(summary.thisMonthCostUsd, showVndPrimary, profile.vndExchangeRate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedText
                                )
                                Text(
                                    text = "Năm: ${formatPrimaryCurrency(summary.thisYearCostUsd, showVndPrimary, profile.vndExchangeRate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedText
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${profile.displayName} • Giữ Profile khi Update",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedText
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClick = onQuickSPenAction)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "S-Pen Quick Log",
                                        tint = primaryText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "S-Pen Tính Phí +",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = primaryText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Note20WidgetSizeMode.EXPANDED_4X3 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizontalDivider(color = borderColor)
                            // Top 3 models in minimalist monochrome bars
                            Text(
                                text = "CHI PHÍ THEO LOẠI MODEL DÙNG:",
                                style = MaterialTheme.typography.labelSmall,
                                color = mutedText
                            )
                            summary.modelBreakdowns.take(4).forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.modelSpec.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = primaryText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${item.requestCount} calls · ${formatPrimaryCurrency(item.totalCostUsd, showVndPrimary, profile.vndExchangeRate)} (%.0f%%)".format(
                                            Locale.US,
                                            item.percentageOfPeriod
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = mutedText
                                    )
                                }
                            }
                            HorizontalDivider(color = borderColor)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Zero-Logout: ${profile.lastProfileSnapshotHash.take(16)}...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = mutedText
                                )
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClick = onQuickSPenAction),
                                    shape = RoundedCornerShape(8.dp),
                                    color = primaryText
                                ) {
                                    Text(
                                        text = "S-Pen Ghi Nhận Nhanh",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = bgColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
