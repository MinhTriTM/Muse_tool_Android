package com.musetool.superapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musetool.superapp.data.local.MuseModelCatalog
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.repository.BillingDashboardSummary
import com.musetool.superapp.shortcut.AppShortcutItem
import com.musetool.superapp.shortcut.MuseAppShortcutManager
import com.musetool.superapp.ui.MuseSuperAppViewModel
import com.musetool.superapp.ui.components.formatPrimaryCurrency
import com.musetool.superapp.ui.screens.AiStudioCalculatorScreen
import com.musetool.superapp.ui.screens.BillingAnalyticsScreen
import com.musetool.superapp.ui.screens.DashboardScreen
import com.musetool.superapp.ui.screens.Note20UltraWidgetStudioScreen
import com.musetool.superapp.ui.screens.QuickAddModelUsageDialog
import com.musetool.superapp.ui.screens.WifiOtaAndProfileScreen
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.CyberCyan
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.JetBrainsMonoFontFamily
import com.musetool.superapp.ui.theme.MuseThemePreset
import com.musetool.superapp.ui.theme.MuseToolTheme
import com.musetool.superapp.ui.theme.RoseDebt
import com.musetool.superapp.widget.MuseNote20UltraWidgetProvider
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var latestShortcutActionState = mutableStateOf<String?>(null)
    private var latestShortcutModelState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractShortcutAction(intent)
        setContent {
            MuseSuperAppRoot(
                incomingShortcutAction = latestShortcutActionState.value,
                incomingShortcutModelId = latestShortcutModelState.value,
                onShortcutHandled = {
                    latestShortcutActionState.value = null
                    latestShortcutModelState.value = null
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractShortcutAction(intent)
    }

    private fun extractShortcutAction(intent: Intent?) {
        val action = intent?.action ?: return
        if (action.startsWith("com.musetool.superapp.SHORTCUT_")) {
            latestShortcutActionState.value = action
            latestShortcutModelState.value =
                intent.getStringExtra(MuseAppShortcutManager.EXTRA_SHORTCUT_MODEL_ID)
        }
    }
}

private data class NavDestination(
    val index: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun MuseSuperAppRoot(
    incomingShortcutAction: String? = null,
    incomingShortcutModelId: String? = null,
    onShortcutHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: MuseSuperAppViewModel = viewModel(
        factory = MuseSuperAppViewModel.provideFactory(context)
    )

    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val billingSummary by viewModel.billingSummary.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val selectedModelFilter by viewModel.selectedModelIdFilter.collectAsStateWithLifecycle()
    val showVndPrimary by viewModel.showVndPrimary.collectAsStateWithLifecycle()
    val otaReleases by viewModel.otaReleases.collectAsStateWithLifecycle()
    val promptTemplates by viewModel.promptTemplates.collectAsStateWithLifecycle()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsStateWithLifecycle()
    val lastAiResult by viewModel.lastAiResult.collectAsStateWithLifecycle()
    val wifiStatus by viewModel.wifiStatus.collectAsStateWithLifecycle()
    val otaInstallState by viewModel.otaInstallState.collectAsStateWithLifecycle()
    val silentOtaTelemetry by viewModel.silentOtaTelemetry.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var edgePanelEnabled by rememberSaveable { mutableStateOf(true) }
    var isEdgeDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var showSPenQuickLogModal by remember { mutableStateOf(false) }
    var customNotice by remember { mutableStateOf<String?>(null) }
    var shortcutSelectedModelId by rememberSaveable { mutableStateOf<String?>(null) }

    val handleShortcutAction: (String, String?) -> Unit = { action, targetModel ->
        when (action) {
            MuseAppShortcutManager.ACTION_CHECK_BALANCE -> {
                currentTab = 1
                customNotice = "App Shortcut · Free Credits còn lại: $%.2f USD | Outstanding Balance: $%.2f USD".format(
                    Locale.US,
                    billingSummary.freeCreditsRemainingUsd,
                    billingSummary.netOutstandingBalanceUsd
                )
            }

            MuseAppShortcutManager.ACTION_OPEN_LAST_MODEL -> {
                val resolvedModelId = targetModel
                    ?: billingSummary.filteredLogs.firstOrNull()?.modelId
                    ?: billingSummary.modelBreakdowns.firstOrNull { it.totalCostUsd > 0 }?.modelSpec?.modelId
                    ?: MuseModelCatalog.models.first().modelId
                val spec = MuseModelCatalog.findById(resolvedModelId)
                shortcutSelectedModelId = spec.modelId
                currentTab = 2
                customNotice = "App Shortcut · Đã mở AI Studio với Model dùng gần nhất: ${spec.displayName}"
            }

            MuseAppShortcutManager.ACTION_QUICK_LOG_USAGE -> {
                showSPenQuickLogModal = true
                customNotice = "App Shortcut · Mở bảng ghi nhận nhanh chi phí phiên Model AI"
            }

            MuseAppShortcutManager.ACTION_WIFI_OTA_SYNC -> {
                currentTab = 3
                customNotice = "App Shortcut · Mở kênh Wi-Fi OTA & Hồ sơ Zero-Logout (v${profile.activeVersionName})"
            }
        }
    }

    // Handle incoming Android Launcher App Icon Long-Press Shortcut Intent
    LaunchedEffect(incomingShortcutAction, incomingShortcutModelId) {
        if (!incomingShortcutAction.isNullOrBlank()) {
            handleShortcutAction(incomingShortcutAction, incomingShortcutModelId)
            onShortcutHandled()
        }
    }

    // Automatically push real-time updates to the Samsung Note 20 Ultra 5G VN Home Screen Widget
    // AND sync live Android App Icon Long-Press Dynamic Shortcuts
    LaunchedEffect(billingSummary, profile) {
        val topModel = billingSummary.modelBreakdowns.firstOrNull()?.modelSpec?.displayName ?: "Gemini 3.8 Flash"
        MuseNote20UltraWidgetProvider.syncDataToNote20Widget(
            context = context,
            freeRemainingUsd = billingSummary.freeCreditsRemainingUsd,
            freeUsedUsd = billingSummary.freeCreditsUsedUsd,
            outstandingUsd = billingSummary.netOutstandingBalanceUsd,
            todayUsd = billingSummary.todayCostUsd,
            weekUsd = billingSummary.thisWeekCostUsd,
            monthUsd = billingSummary.thisMonthCostUsd,
            yearUsd = billingSummary.thisYearCostUsd,
            totalUsd = billingSummary.totalAllTimeCostUsd,
            vndRate = profile.vndExchangeRate,
            profileName = profile.displayName,
            versionName = profile.activeVersionName,
            topModelName = topModel
        )
        MuseAppShortcutManager.syncDynamicShortcuts(
            context = context,
            summary = billingSummary,
            profile = profile
        )
    }

    if (currentTab != 0) {
        BackHandler {
            currentTab = 0
        }
    }

    val newerOtaRelease = otaReleases.firstOrNull { it.versionCode > profile.activeVersionCode }

    val navItems = listOf(
        NavDestination(0, "Tổng quan", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_tab_dashboard"),
        NavDestination(1, "Tính tiền", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_tab_billing"),
        NavDestination(2, "AI Studio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_tab_studio"),
        NavDestination(3, "OTA & Hồ sơ", Icons.Filled.SystemUpdateAlt, Icons.Outlined.SystemUpdateAlt, "nav_tab_ota_profile"),
        NavDestination(4, "Note20U", Icons.Filled.Widgets, Icons.Outlined.Widgets, "nav_tab_note20_widget")
    )

    val activeThemePreset = MuseThemePreset.fromId(profile.themePresetId)

    MuseToolTheme(themePreset = activeThemePreset) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { dest ->
                        val isSelected = currentTab == dest.index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = dest.index },
                            icon = {
                                if (dest.index == 3 && newerOtaRelease != null) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = AmberWarning,
                                                contentColor = Color.Black
                                            ) {
                                                Text("•")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag(dest.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val activeMsg = customNotice ?: bannerMessage
                    AnimatedVisibility(
                        visible = !activeMsg.isNullOrBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("live_status_banner"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeMsg.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        customNotice = null
                                        viewModel.dismissBanner()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Đóng thông báo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            0 -> DashboardScreen(
                                profile = profile,
                                summary = billingSummary,
                                selectedPeriod = selectedPeriod,
                                showVndPrimary = showVndPrimary,
                                wifiStatus = wifiStatus,
                                availableOtaUpdate = newerOtaRelease,
                                onSelectPeriod = viewModel::selectBillingPeriod,
                                onToggleCurrency = viewModel::toggleCurrencyDisplay,
                                onTopUpFreeCredit = viewModel::topUpFreeCredits,
                                onSimulateExhaustFreeCredit = viewModel::simulateExhaustFreeCredits,
                                onSettleOutstanding = viewModel::settleOutstandingBalance,
                                onQuickAddUsage = viewModel::logManualModelUsage,
                                onSelectThemePreset = viewModel::selectThemePreset,
                                onToggleDarkTheme = viewModel::toggleDarkTheme,
                                onExecuteAppShortcut = { item ->
                                    handleShortcutAction(item.action, item.targetModelId)
                                },
                                onPinAppShortcut = { item ->
                                    val pinned = MuseAppShortcutManager.requestPinAppShortcut(context, item)
                                    customNotice = if (pinned) {
                                        "Đã gửi yêu cầu ghim lối tắt '${item.shortLabel}' ra màn hình chính!"
                                    } else {
                                        "Lối tắt '${item.shortLabel}' đã sẵn sàng khi nhấn giữ biểu tượng ứng dụng!"
                                    }
                                },
                                onNavigateToTab = { currentTab = it }
                            )

                            1 -> BillingAnalyticsScreen(
                                profile = profile,
                                summary = billingSummary,
                                selectedPeriod = selectedPeriod,
                                selectedModelIdFilter = selectedModelFilter,
                                showVndPrimary = showVndPrimary,
                                onSelectPeriod = viewModel::selectBillingPeriod,
                                onSelectModelFilter = viewModel::selectModelFilter,
                                onTopUpFreeCredit = viewModel::topUpFreeCredits,
                                onSimulateExhaustFreeCredit = viewModel::simulateExhaustFreeCredits,
                                onSettleOutstanding = viewModel::settleOutstandingBalance,
                                onUpdateBillingConfig = viewModel::updateBillingConfig,
                                onQuickAddUsage = viewModel::logManualModelUsage,
                                onDeleteLog = viewModel::deleteUsageRecord
                            )

                            2 -> AiStudioCalculatorScreen(
                                profile = profile,
                                summary = billingSummary,
                                templates = promptTemplates,
                                isGenerating = isGeneratingAi,
                                lastResult = lastAiResult,
                                showVndPrimary = showVndPrimary,
                                preselectedModelId = shortcutSelectedModelId,
                                onRunPrompt = viewModel::runAiStudioPrompt,
                                onSaveTemplate = viewModel::saveCustomPromptTemplate
                            )

                            3 -> WifiOtaAndProfileScreen(
                                profile = profile,
                                wifiStatus = wifiStatus,
                                otaReleases = otaReleases,
                                otaInstallState = otaInstallState,
                                silentTelemetry = silentOtaTelemetry,
                                onRefreshWifi = viewModel::refreshWifiStatus,
                                onStartWifiOtaUpdate = viewModel::startWifiOtaUpdate,
                                onCheckNewRemoteRelease = viewModel::checkAndSimulateNewCloudRelease,
                                onTriggerSilentHttpService = { url, autoApply ->
                                    viewModel.triggerSilentHttpOtaService(context, url, autoApply, true)
                                },
                                onUpdateSilentManifestConfig = viewModel::updateSilentManifestConfig,
                                onResetInstallState = viewModel::resetOtaInstallBanner,
                                onUpdateProfile = viewModel::updateProfileDetails,
                                onUpdateOtaPrefs = viewModel::updateOtaSettings,
                                onSelectThemePreset = viewModel::selectThemePreset,
                                onToggleDarkTheme = viewModel::toggleDarkTheme
                            )

                            4 -> Note20UltraWidgetStudioScreen(
                                profile = profile,
                                summary = billingSummary,
                                showVndPrimary = showVndPrimary,
                                edgePanelEnabled = edgePanelEnabled,
                                newerOtaRelease = newerOtaRelease,
                                onToggleEdgePanel = { edgePanelEnabled = it },
                                onToggleCurrency = viewModel::toggleCurrencyDisplay,
                                onTopUpFreeCredit = viewModel::topUpFreeCredits,
                                onQuickLogFromSPen = { showSPenQuickLogModal = true },
                                onOpenOtaTab = { currentTab = 3 },
                                onNotify = { customNotice = it }
                            )
                        }
                    }
                }

                // Samsung Galaxy Note 20 Ultra 5G VN Curved Edge Handle & Slide-Out Minimalist Widget Drawer
                if (edgePanelEnabled) {
                    Note20UltraEdgePanelOverlay(
                        isOpen = isEdgeDrawerOpen,
                        profile = profile,
                        summary = billingSummary,
                        showVndPrimary = showVndPrimary,
                        activeThemePreset = activeThemePreset,
                        onToggleOpen = { isEdgeDrawerOpen = !isEdgeDrawerOpen },
                        onToggleCurrency = viewModel::toggleCurrencyDisplay,
                        onCycleThemePreset = viewModel::cycleNextThemePreset,
                        onOpenWidgetStudio = {
                            isEdgeDrawerOpen = false
                            currentTab = 4
                        },
                        onQuickSPenLog = {
                            isEdgeDrawerOpen = false
                            showSPenQuickLogModal = true
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        if (showSPenQuickLogModal) {
            QuickAddModelUsageDialog(
                vndRate = profile.vndExchangeRate,
                onDismiss = { showSPenQuickLogModal = false },
                onConfirm = { modelId, category, note, inTok, outTok, daysAgo ->
                    viewModel.logManualModelUsage(modelId, category, note, inTok, outTok, daysAgo)
                    showSPenQuickLogModal = false
                }
            )
        }
    }
}

@Composable
private fun Note20UltraEdgePanelOverlay(
    isOpen: Boolean,
    profile: UserProfileState,
    summary: BillingDashboardSummary,
    showVndPrimary: Boolean,
    activeThemePreset: MuseThemePreset,
    onToggleOpen: () -> Unit,
    onToggleCurrency: () -> Unit,
    onCycleThemePreset: () -> Unit,
    onOpenWidgetStudio: () -> Unit,
    onQuickSPenLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minimalist Curved Edge Handle (Note 20 Ultra 5G style)
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(92.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                )
                .clickable(onClick = onToggleOpen)
                .testTag("note20_edge_handle"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Mở Edge Panel Note 20 Ultra",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Column(
                modifier = Modifier
                    .width(235.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .padding(16.dp)
                    .testTag("note20_edge_drawer"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NOTE20U 5G EDGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "v${profile.activeVersionName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Column {
                    Text(
                        text = "Free Credits Còn Lại",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrimaryCurrency(summary.freeCreditsRemainingUsd, showVndPrimary, profile.vndExchangeRate),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldCredit
                    )
                }

                Column {
                    Text(
                        text = "Outstanding Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPrimaryCurrency(summary.netOutstandingBalanceUsd, showVndPrimary, profile.vndExchangeRate),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = RoseDebt
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Text(
                    text = "Ngày: ${formatPrimaryCurrency(summary.todayCostUsd, showVndPrimary, profile.vndExchangeRate)} · Tháng: ${formatPrimaryCurrency(summary.thisMonthCostUsd, showVndPrimary, profile.vndExchangeRate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tổng: ${formatPrimaryCurrency(summary.totalAllTimeCostUsd, showVndPrimary, profile.vndExchangeRate)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onToggleCurrency),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (showVndPrimary) "Đổi USD" else "Đổi VNĐ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onQuickSPenLog),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "S-Pen +",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onCycleThemePreset)
                        .testTag("edge_btn_cycle_theme"),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Theme: ${activeThemePreset.titleVi} ↻",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenWidgetStudio),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Tùy chỉnh & Ghim Widget →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                    )
                }
            }
        }
    }
}
