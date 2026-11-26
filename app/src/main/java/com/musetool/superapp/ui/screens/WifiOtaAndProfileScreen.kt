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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.musetool.superapp.data.local.OtaReleaseEntity
import com.musetool.superapp.data.local.UserProfileState
import com.musetool.superapp.data.ota.OtaInstallState
import com.musetool.superapp.data.ota.SilentOtaTelemetry
import com.musetool.superapp.data.ota.WifiConnectionStatus
import com.musetool.superapp.ui.components.MinimalThemeSwitcherBar
import com.musetool.superapp.ui.theme.AmberWarning
import com.musetool.superapp.ui.theme.CyberCyan
import com.musetool.superapp.ui.theme.ElectricViolet
import com.musetool.superapp.ui.theme.EmeraldCredit
import com.musetool.superapp.ui.theme.MuseThemePreset

@Composable
fun WifiOtaAndProfileScreen(
    profile: UserProfileState,
    wifiStatus: WifiConnectionStatus,
    otaReleases: List<OtaReleaseEntity>,
    otaInstallState: OtaInstallState,
    silentTelemetry: SilentOtaTelemetry,
    onRefreshWifi: () -> Unit,
    onStartWifiOtaUpdate: (OtaReleaseEntity) -> Unit,
    onCheckNewRemoteRelease: () -> Unit,
    onTriggerSilentHttpService: (String, Boolean) -> Unit,
    onUpdateSilentManifestConfig: (String, Boolean) -> Unit,
    onResetInstallState: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onUpdateOtaPrefs: (String, Boolean, Boolean) -> Unit,
    onSelectThemePreset: (MuseThemePreset) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("wifi_ota_profile_list"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Cập Nhật Wi-Fi OTA & Hồ Sơ Vĩnh Viễn",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Silent HTTP Manifest & Background Bundle Downloader (Non-Firebase) • Bảo toàn Profile 100%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Persistent Profile & Zero-Logout Session Vault Card
        item {
            ZeroLogoutProfileVaultCard(
                profile = profile,
                onEditProfile = { showEditProfileModal = true },
                onSelectThemePreset = onSelectThemePreset,
                onToggleDarkTheme = onToggleDarkTheme
            )
        }

        // 3. Silent OTA Service Infrastructure (HTTP Manifest & Background Downloader)
        item {
            SilentHttpOtaInfrastructureCard(
                telemetry = silentTelemetry,
                onUpdateConfig = onUpdateSilentManifestConfig,
                onTriggerSilentSync = onTriggerSilentHttpService
            )
        }

        // 4. Wi-Fi Direct OTA Engine Status & Preferences
        item {
            WifiDirectOtaStatusCard(
                profile = profile,
                wifiStatus = wifiStatus,
                onRefreshWifi = onRefreshWifi,
                onCheckNewRelease = onCheckNewRemoteRelease,
                onUpdateOtaPrefs = onUpdateOtaPrefs
            )
        }

        // 4. Active OTA Installation Pipeline Progress (When running or completed)
        if (otaInstallState !is OtaInstallState.Idle) {
            item {
                OtaLiveProgressCard(
                    state = otaInstallState,
                    onDismiss = onResetInstallState
                )
            }
        }

        // 5. Available & Historical OTA Versions
        item {
            Text(
                text = "DANH SÁCH PHIÊN BẢN OTA QUA WI-FI (${otaReleases.size} BẢN PHÁT HÀNH):",
                style = MaterialTheme.typography.labelMedium,
                color = CyberCyan
            )
        }

        items(otaReleases, key = { it.versionName }) { release ->
            OtaReleaseVersionCard(
                release = release,
                activeVersionName = profile.activeVersionName,
                activeVersionCode = profile.activeVersionCode,
                isInstalling = otaInstallState !is OtaInstallState.Idle && otaInstallState !is OtaInstallState.Completed,
                onInstallOverWifi = { onStartWifiOtaUpdate(release) }
            )
        }
    }

    if (showEditProfileModal) {
        EditPersistentProfileDialog(
            profile = profile,
            onDismiss = { showEditProfileModal = false },
            onSave = { name, email, role, workspace ->
                onUpdateProfile(name, email, role, workspace)
                showEditProfileModal = false
            }
        )
    }
}

@Composable
private fun ZeroLogoutProfileVaultCard(
    profile: UserProfileState,
    onEditProfile: () -> Unit,
    onSelectThemePreset: (MuseThemePreset) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("zero_logout_profile_vault_card"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.displayName.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Đã xác thực",
                                tint = EmeraldCredit,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = profile.roleTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberCyan
                        )
                        Text(
                            text = "${profile.email} • ${profile.workspaceName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_edit_profile")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa hồ sơ",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sửa")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Persistent Token & Snapshot Hash Metadata Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Cơ chế giữ Profile",
                                tint = EmeraldCredit,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CƠ CHẾ GIỮ PROFILE & PHIÊN ĐĂNG NHẬP KHI UPDATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldCredit
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldCredit.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "ZERO-LOGOUT ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldCredit,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Session Token",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Persistent Session Token: ${profile.sessionToken}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Snapshot Hash: ${profile.lastProfileSnapshotHash} • Đã bảo toàn qua ${profile.snapshotRestoreCount} lần Update",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Phiên bản đang chạy: v${profile.activeVersionName} (Build #${profile.activeVersionCode}) — Dùng tiếp ngay sau Update không cần Login lại.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-Theme Switcher (8 Light & Dark Themes)
            MinimalThemeSwitcherBar(
                activeThemeId = profile.themePresetId,
                onSelectTheme = onSelectThemePreset,
                onToggleLightDark = onToggleDarkTheme
            )
        }
    }
}

@Composable
private fun WifiDirectOtaStatusCard(
    profile: UserProfileState,
    wifiStatus: WifiConnectionStatus,
    onRefreshWifi: () -> Unit,
    onCheckNewRelease: () -> Unit,
    onUpdateOtaPrefs: (String, Boolean, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Trạng thái Wi-Fi OTA",
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "KÊNH CẬP NHẬT WI-FI OTA (KHÔNG CẦN FILE APK)",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                        Text(
                            text = wifiStatus.networkLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldCredit
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRefreshWifi,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Làm mới Wi-Fi",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel selection & switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kênh OTA:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf("Stable", "Beta / Nightly").forEach { ch ->
                    FilterChip(
                        selected = profile.otaChannel == ch,
                        onClick = {
                            onUpdateOtaPrefs(ch, profile.autoWifiOtaEnabled, profile.keepProfileOnUpdate)
                        },
                        label = { Text(ch) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Giữ nguyên Profile, Token & Free Credits khi Update",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = profile.keepProfileOnUpdate,
                    onCheckedChange = {
                        onUpdateOtaPrefs(profile.otaChannel, profile.autoWifiOtaEnabled, it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCheckNewRelease,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_check_wifi_ota_update"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Quét bản cập nhật mới qua Wi-Fi",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quét Bản Cập Nhật Mới Qua Wi-Fi Cloud",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OtaLiveProgressCard(
    state: OtaInstallState,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ota_live_progress_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberCyan, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            when (state) {
                is OtaInstallState.BackingUpProfile -> {
                    Text(
                        text = "BƯỚC 1/4: ĐANG SAO LƯU SNAPSHOT PROFILE & SESSION TOKEN",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberWarning
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = state.detail, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = AmberWarning
                    )
                }

                is OtaInstallState.StreamingWifiDelta -> {
                    Text(
                        text = "BƯỚC 2/4: TẢI GÓI DELTA BUNDLE QUA WI-FI (KHÔNG TẢI APK)",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Đã đồng bộ %.2f MB / %.2f MB (%d%%)".format(
                            state.downloadedMb,
                            state.totalMb,
                            (state.progress * 100).toInt()
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = CyberCyan
                    )
                }

                is OtaInstallState.VerifyingSignature -> {
                    Text(
                        text = "BƯỚC 3/4: XÁC THỰC CHỮ KÝ SỐ & TOÀN VẸN DỮ LIỆU",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricViolet
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Chữ ký gói OTA: ${state.hash}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ElectricViolet)
                }

                is OtaInstallState.HotSwappingRuntime -> {
                    Text(
                        text = "BƯỚC 4/4: HOT-SWAP LÊN PHIÊN BẢN v${state.versionName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldCredit
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Đang khôi phục phiên đăng nhập từ Session Vault (Không cần Login lại)...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = EmeraldCredit)
                }

                is OtaInstallState.Completed -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Cập nhật thành công",
                            tint = EmeraldCredit,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CẬP NHẬT WI-FI OTA THÀNH CÔNG LÊN v${state.newVersionName}!",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldCredit,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Hồ sơ '${state.preservedProfileName}' & Token (${state.preservedSessionToken.take(18)}...) được giữ nguyên 100%.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "✓ Snapshot Hash mới: ${state.snapshotHash} • Dùng tiếp ngay lập tức không cần đăng nhập lại!",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldCredit,
                            contentColor = Color(0xFF002419)
                        )
                    ) {
                        Text("Tiếp tục sử dụng v${state.newVersionName}", fontWeight = FontWeight.Bold)
                    }
                }

                is OtaInstallState.Error -> {
                    Text(text = "Lỗi OTA: ${state.reason}", color = MaterialTheme.colorScheme.error)
                }

                OtaInstallState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun OtaReleaseVersionCard(
    release: OtaReleaseEntity,
    activeVersionName: String,
    activeVersionCode: Int,
    isInstalling: Boolean,
    onInstallOverWifi: () -> Unit
) {
    val isCurrentActive = release.versionName == activeVersionName
    val isNewer = release.versionCode > activeVersionCode

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ota_release_card_${release.versionCode}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = when {
                        isCurrentActive -> EmeraldCredit.copy(alpha = 0.6f)
                        isNewer -> AmberWarning.copy(alpha = 0.6f)
                        else -> Color(0xFF28354E)
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Phiên bản v${release.versionName}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrentActive) EmeraldCredit.copy(alpha = 0.2f)
                            else if (isNewer) AmberWarning.copy(alpha = 0.2f)
                            else CyberCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = when {
                                    isCurrentActive -> "ĐANG CHẠY"
                                    isNewer -> "BẢN MỚI OTA"
                                    else -> "ĐÃ CÀI ĐẶT"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isCurrentActive -> EmeraldCredit
                                    isNewer -> AmberWarning
                                    else -> CyberCyan
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "Kênh: ${release.channel} • Gói Delta: ${release.bundleSizeMb} MB • ${release.releaseDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = release.changelog,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Chữ ký: ${release.patchSignature}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isCurrentActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onInstallOverWifi,
                    enabled = !isInstalling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_install_ota_${release.versionCode}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNewer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isNewer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdateAlt,
                        contentDescription = "Cập nhật qua Wi-Fi",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNewer) {
                            "Update qua Wi-Fi lên v${release.versionName} (Giữ Profile)"
                        } else {
                            "Chuyển sang bản v${release.versionName} (Giữ Profile)"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EditPersistentProfileDialog(
    profile: UserProfileState,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var email by remember { mutableStateOf(profile.email) }
    var role by remember { mutableStateOf(profile.roleTitle) }
    var workspace by remember { mutableStateOf(profile.workspaceName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cập nhật Profile trong Session Vault") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên hiển thị") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email tài khoản") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Chức danh / Vai trò") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = workspace,
                    onValueChange = { workspace = it },
                    label = { Text("Tên Workspace") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, email, role, workspace) }) {
                Text("Lưu & Khóa Snapshot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun SilentHttpOtaInfrastructureCard(
    telemetry: SilentOtaTelemetry,
    onUpdateConfig: (String, Boolean) -> Unit,
    onTriggerSilentSync: (String, Boolean) -> Unit
) {
    var manifestUrlText by remember(telemetry.manifestEndpointUrl) {
        mutableStateOf(telemetry.manifestEndpointUrl)
    }
    var autoApplySilent by remember(telemetry.silentAutoDownloadEnabled) {
        mutableStateOf(telemetry.silentAutoDownloadEnabled)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("silent_http_ota_service_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SILENT OTA SERVICE (HTTP MANIFEST • NON-FIREBASE)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SilentOtaSyncService (dataSync) • Tải ngầm gói cấu hình JSON & xác thực SHA-256",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = if (telemetry.isBackgroundSyncRunning) "SYNCING..."
                        else if (telemetry.lastHttpStatusCode != null) "HTTP ${telemetry.lastHttpStatusCode}"
                        else "READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (telemetry.isBackgroundSyncRunning) AmberWarning else EmeraldCredit,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = manifestUrlText,
                onValueChange = {
                    manifestUrlText = it
                    onUpdateConfig(it, autoApplySilent)
                },
                label = { Text("HTTP Version Manifest Endpoint URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_http_manifest_url"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tự động tải ngầm & áp dụng Silent Update",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tải vào filesDir/ota_bundles/ & giữ nguyên Session Token",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoApplySilent,
                    onCheckedChange = {
                        autoApplySilent = it
                        onUpdateConfig(manifestUrlText, it)
                    },
                    modifier = Modifier.testTag("switch_silent_auto_apply")
                )
            }

            // Live Background Download & Manifest Telemetry Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = telemetry.statusLog,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (telemetry.isBackgroundSyncRunning || telemetry.backgroundDownloadProgress > 0f) {
                        LinearProgressIndicator(
                            progress = { telemetry.backgroundDownloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (telemetry.discoveredManifestVersion != null) {
                        Text(
                            text = "Manifest Version: ${telemetry.discoveredManifestVersion} • Latency: ${telemetry.lastCheckLatencyMs ?: 0}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldCredit
                        )
                    }

                    if (telemetry.localCachedBundlePath != null) {
                        Text(
                            text = "Bundle Cache: ${telemetry.localCachedBundlePath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (telemetry.verifiedSha256 != null) {
                        Text(
                            text = "Verified Checksum: ${telemetry.verifiedSha256}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                }
            }

            Button(
                onClick = { onTriggerSilentSync(manifestUrlText, autoApplySilent) },
                enabled = !telemetry.isBackgroundSyncRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_trigger_silent_http_ota"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Chạy Silent HTTP OTA Service",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (telemetry.isBackgroundSyncRunning) {
                        "Đang đồng bộ ngầm qua SilentOtaSyncService..."
                    } else {
                        "Chạy Silent HTTP Manifest Check & Tải Ngầm"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
