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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.musetool.superapp.data.local.OtaReleaseEntity
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.ota.WifiConnectionStatus
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.data.repository.BillingPeriod
import com.musetool.superapp.shortcut.AppShortcutItem
import com.musetool.superapp.ui.components.AppIconLongPressShortcutsCard
import com.musetool.superapp.ui.components.CreditAndBalanceHeroCard
import com.musetool.superapp.ui.components.FinOpsForecastAndOptimizationCard
import com.musetool.superapp.ui.components.MinimalThemeSwitcherBar
import com.musetool.superapp.ui.components.ModelDistributionCanvasChart
import com.musetool.superapp.ui.components.TimeHorizonCostGrid
import com.musetool.superapp.ui.components.formatPreciseCurrency
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.JetBrainsMonoFontFamily
import com.musetool.superapp.ui.theme.MuseThemePreset

@Composable
fun DashboardScreen(
    profile: UserProfileState,
    summary: BillingDashboardSummary,
    selectedPeriod: BillingPeriod,
    showVndPrimary: Boolean,
    wifiStatus: WifiConnectionStatus,
    availableOtaUpdate: OtaReleaseEntity?,
    onSelectPeriod: (BillingPeriod) -> Unit,
    onToggleCurrency: () -> Unit,
    onTopUpFreeCredit: (Double) -> Unit,
    onSimulateExhaustFreeCredit: () -> Unit,
    onSettleOutstanding: (Double) -> Unit,
    onQuickAddUsage: (String, String, String, Int, Int, Int) -> Unit,
    onSelectThemePreset: (MuseThemePreset) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onExecuteAppShortcut: (AppShortcutItem) -> Unit,
    onPinAppShortcut: (AppShortcutItem) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickAddModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Clean Top Executive Header Bar
        item {
            SessionVaultHeaderStrip(
                profile = profile,
                wifiStatus = wifiStatus,
                showVndPrimary = showVndPrimary,
                onToggleCurrency = onToggleCurrency,
                onOpenProfileTab = { onNavigateToTab(3) }
            )
        }

        // 2. Wi-Fi OTA Update Alert Banner (only when newer version exists)
        if (availableOtaUpdate != null) {
            item {
                WifiOtaReadyBanner(
                    release = availableOtaUpdate,
                    currentVersion = profile.activeVersionName,
                    onOpenOtaScreen = { onNavigateToTab(3) }
                )
            }
        }

        // 3. Primary Hero: Free Credits In Use (Remaining) & Outstanding Balance
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

        // 4. Primary Action Row (Clean contrast: 1 Solid Primary + 1 Outlined Secondary)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showQuickAddModal = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_quick_add_usage"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tính tiền phiên mới",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ghi nhận Phiên phí",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = { onNavigateToTab(2) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_open_ai_studio"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Mở AI Studio →",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 5. Consolidated Time-Horizon Cost Card (Tổng, Hôm nay, 7 ngày, Tháng này, Năm nay)
        item {
            TimeHorizonCostGrid(
                summary = summary,
                showVndPrimary = showVndPrimary,
                vndRate = profile.vndExchangeRate,
                onSelectHorizon = { idx ->
                    val target = BillingPeriod.entries.getOrElse(idx) { BillingPeriod.ALL }
                    onSelectPeriod(target)
                    onNavigateToTab(1)
                }
            )
        }

        // 6. Per-Model Distribution & Period Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BillingPeriod.entries) { period ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPeriod(period) },
                            label = {
                                Text(
                                    text = period.labelVi,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("dashboard_period_chip_${period.name}")
                        )
                    }
                }

                ModelDistributionCanvasChart(
                    breakdowns = summary.modelBreakdowns,
                    showVndPrimary = showVndPrimary,
                    vndRate = profile.vndExchangeRate
                )
            }
        }

        // 7. FinOps Burn-Rate, Budget Runway & Smart Model Routing Advisor
        item {
            FinOpsForecastAndOptimizationCard(
                summary = summary,
                showVndPrimary = showVndPrimary,
                vndRate = profile.vndExchangeRate
            )
        }

        // 8. Compact Multi-Theme Switcher (8 Light & Dark Themes)
        item {
            MinimalThemeSwitcherBar(
                activeThemeId = profile.themePresetId,
                onSelectTheme = onSelectThemePreset,
                onToggleLightDark = onToggleDarkTheme
            )
        }

        // 9. App Icon Long-Press Quick Action Menu
        item {
            AppIconLongPressShortcutsCard(
                summary = summary,
                profile = profile,
                onExecuteShortcut = onExecuteAppShortcut,
                onPinShortcut = onPinAppShortcut
            )
        }
    }

    if (showQuickAddModal) {
        QuickAddModelUsageDialog(
            vndRate = profile.vndExchangeRate,
            onDismiss = { showQuickAddModal = false },
            onConfirm = { modelId, category, note, inTok, outTok, daysAgo ->
                onQuickAddUsage(modelId, category, note, inTok, outTok, daysAgo)
                showQuickAddModal = false
            }
        )
    }
}

@Composable
private fun SessionVaultHeaderStrip(
    profile: UserProfileState,
    wifiStatus: WifiConnectionStatus,
    showVndPrimary: Boolean,
    onToggleCurrency: () -> Unit,
    onOpenProfileTab: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenProfileTab)
            .padding(vertical = 4.dp)
            .testTag("session_vault_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.displayName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Đã bảo toàn phiên đăng nhập",
                        tint = EmeraldCredit,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "v${profile.activeVersionName} · Zero-Logout",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi OTA",
                        tint = EmeraldCredit,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${wifiStatus.linkSpeedMbps} Mbps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Clean Currency Pill (USD <-> VND)
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onToggleCurrency)
                .testTag("btn_toggle_currency"),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = "Đổi tiền tệ",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (showVndPrimary) "VNĐ ₫" else "USD $",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WifiOtaReadyBanner(
    release: OtaReleaseEntity,
    currentVersion: String,
    onOpenOtaScreen: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenOtaScreen)
            .testTag("wifi_ota_ready_banner"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberWarning.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdateAlt,
                    contentDescription = "Bản cập nhật OTA mới",
                    tint = AmberWarning,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Bản cập nhật Wi-Fi OTA v${release.versionName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Từ v$currentVersion (${release.bundleSizeMb} MB) · Không tải APK · Giữ nguyên Profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Cập nhật",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuickAddModelUsageDialog(
    vndRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Int, Int) -> Unit
) {
    var selectedModel by remember { mutableStateOf(MuseModelCatalog.models.first()) }
    var inputTokensText by remember { mutableStateOf("85000") }
    var outputTokensText by remember { mutableStateOf("42000") }
    var noteText by remember { mutableStateOf("Tự động tính phí xử lý dữ liệu đa tầng") }
    var selectedDayOffset by remember { mutableIntStateOf(0) }

    val inTok = inputTokensText.toIntOrNull()?.coerceAtLeast(100) ?: 50_000
    val outTok = outputTokensText.toIntOrNull()?.coerceAtLeast(100) ?: 25_000
    val previewCost = MuseModelCatalog.calculateCostUsd(selectedModel.modelId, inTok, outTok)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ghi nhận & Tính tiền theo Model",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chọn loại Model dùng:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MuseModelCatalog.models) { spec ->
                        FilterChip(
                            selected = selectedModel.modelId == spec.modelId,
                            onClick = { selectedModel = spec },
                            label = { Text(spec.displayName) }
                        )
                    }
                }

                Text(
                    text = "Thời điểm ghi nhận:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val timePresets = listOf(
                    0 to "Hôm nay",
                    2 to "Tuần này",
                    14 to "Tháng này",
                    90 to "Năm nay",
                    370 to "Năm trước"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(timePresets) { (days, label) ->
                        FilterChip(
                            selected = selectedDayOffset == days,
                            onClick = { selectedDayOffset = days },
                            label = { Text(label) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputTokensText,
                        onValueChange = { inputTokensText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Input Tokens") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = outputTokensText,
                        onValueChange = { outputTokensText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Output Tokens") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Mô tả tác vụ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${selectedModel.displayName}: In $${selectedModel.inputPricePer1MUsd}/1M · Out $${selectedModel.outputPricePer1MUsd}/1M",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Thành tiền: ${formatPreciseCurrency(previewCost, false, vndRate)} (${formatPreciseCurrency(previewCost, true, vndRate)})",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedModel.modelId,
                        selectedModel.category,
                        noteText,
                        inTok,
                        outTok,
                        selectedDayOffset
                    )
                }
            ) {
                Text("Ghi nhận & Khấu trừ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
