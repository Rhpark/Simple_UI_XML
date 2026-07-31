package kr.open.library.simpleui_xml.deviceverification

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.open.library.simple_ui.compose.permissions.rememberPermissionRequestState

/**
 * Hosts the manual Compose special-permission checks in the verification build.<br><br>
 * verification 빌드에서 Compose 특수 권한의 수동 검증 화면을 제공합니다.<br>
 */
internal class ComposePermissionManualVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposePermissionManualVerificationScreen()
        }
    }
}

@Composable
private fun ComposePermissionManualVerificationScreen() {
    val permissionState = rememberPermissionRequestState(
        permissions = listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
        gateSettingsNavigation = true,
    )
    var requestInvocationCount by rememberSaveable { mutableStateOf(0) }
    var resultCallbackCount by rememberSaveable { mutableStateOf(0) }
    var lastCallbackResult by rememberSaveable { mutableStateOf("없음") }

    val requestPermission = {
        requestInvocationCount += 1
        permissionState.request { deniedItems ->
            resultCallbackCount += 1
            lastCallbackResult = deniedItems
                .takeIf { it.isNotEmpty() }
                ?.joinToString { "${it.permission}: ${it.result}" }
                ?: "모두 허용됨"
        }
    }
    val deniedItems = permissionState.deniedItems
        .takeIf { it.isNotEmpty() }
        ?.joinToString { "${it.permission}: ${it.result}" }
        ?: "없음"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ManualText(
            text = "COMPOSE-P0-002 특수 권한 설정 복귀",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        ManualText(
            "오버레이 권한을 끈 상태에서 요청을 시작하고, 진행 중 중복 요청을 시도한 뒤 " +
                "설정 화면에서 허용 또는 거부하고 돌아와 상태와 콜백 횟수를 확인해 주세요.",
        )
        ManualStateSection(
            phase = permissionState.phase.name,
            isRequesting = permissionState.isRequesting,
            allGranted = permissionState.allGranted,
            settingsNavigationRequired = permissionState.settingsNavigationRequired ?: "없음",
            requestInvocationCount = requestInvocationCount,
            resultCallbackCount = resultCallbackCount,
            deniedItems = deniedItems,
            lastCallbackResult = lastCallbackResult,
        )
        ManualActionButton(
            text = "권한 요청 시작",
            onClick = requestPermission,
        )
        ManualActionButton(
            text = "진행 중 중복 요청 시도",
            onClick = requestPermission,
        )
        if (permissionState.settingsNavigationRequired != null) {
            ManualActionButton(
                text = "설정 화면으로 이동",
                onClick = permissionState::continueSettingsNavigation,
            )
            ManualActionButton(
                text = "설정 이동 취소",
                onClick = permissionState::cancelSettingsNavigation,
            )
        }
    }
}

@Composable
private fun ManualStateSection(
    phase: String,
    isRequesting: Boolean,
    allGranted: Boolean,
    settingsNavigationRequired: String,
    requestInvocationCount: Int,
    resultCallbackCount: Int,
    deniedItems: String,
    lastCallbackResult: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F4F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ManualText("phase: $phase", fontWeight = FontWeight.Bold)
        ManualText("isRequesting: $isRequesting")
        ManualText("allGranted: $allGranted")
        ManualText("설정 이동 대기: $settingsNavigationRequired")
        ManualText("request() 호출 수: $requestInvocationCount")
        ManualText("결과 콜백 수: $resultCallbackCount")
        ManualText("State 거부 결과: $deniedItems")
        ManualText("최근 콜백 결과: $lastCallbackResult")
    }
}

@Composable
private fun ManualActionButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F5AA6))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        ManualText(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ManualText(
    text: String,
    color: Color = Color(0xFF1A1A1A),
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    BasicText(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
        ),
    )
}
