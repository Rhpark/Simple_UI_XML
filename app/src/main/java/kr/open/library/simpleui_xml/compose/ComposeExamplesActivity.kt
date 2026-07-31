package kr.open.library.simpleui_xml.compose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.open.library.simple_ui.compose.permissions.rememberPermissionRequestState
import kr.open.library.simple_ui.compose.scroll.ScrollEdge
import kr.open.library.simple_ui.compose.scroll.rememberEdgeReachedState
import kr.open.library.simple_ui.compose.scroll.rememberScrollDirectionState
import kr.open.library.simple_ui.compose.state.CollectVmEvent
import kr.open.library.simple_ui.compose.systembars.SystemBarsStyle

/**
 * `simple_compose`의 주요 API를 한 화면에서 실행하는 예제 Activity입니다.<br>
 * Example Activity that demonstrates the primary `simple_compose` APIs on one screen.<br>
 */
class ComposeExamplesActivity : ComponentActivity() {
    private val viewModel: ComposeExamplesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeExamplesScreen(viewModel)
        }
    }
}

/**
 * Contains stable semantics tags used by physical-device Compose tests.<br><br>
 * Compose 실기기 테스트에서 사용하는 안정적인 semantics 태그를 제공합니다.<br>
 */
internal object ComposeExampleSemantics {
    const val SCREEN = "compose_example_screen"
    const val PERMISSION_STATE = "compose_permission_state"
    const val PERMISSION_REQUEST = "compose_permission_request"
    const val EVENT_STATE = "compose_event_state"
    const val EVENT_SEND = "compose_event_send"
    const val SYSTEM_BAR_STATE = "compose_system_bar_state"
    const val SYSTEM_BAR_TOGGLE = "compose_system_bar_toggle"
    const val SCROLL_STATE = "compose_scroll_state"
    const val SCROLL_LIST = "compose_scroll_list"
}

@Composable
private fun ComposeExamplesScreen(viewModel: ComposeExamplesViewModel) {
    var useDarkSystemBarIcons by remember { mutableStateOf(true) }
    SystemBarsStyle(statusBarDarkIcons = useDarkSystemBarIcons)

    Column(
        modifier = Modifier
            .testTag(ComposeExampleSemantics.SCREEN)
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ExampleTitle("Simple Compose Examples")
        PermissionExample()
        ViewModelEventExample(viewModel)
        SystemBarsExample(
            useDarkIcons = useDarkSystemBarIcons,
            onToggle = { useDarkSystemBarIcons = !useDarkSystemBarIcons },
        )
        ScrollExample(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PermissionExample() {
    val permissionState = rememberPermissionRequestState(
        permissions = listOf(Manifest.permission.CAMERA),
    )
    var lastResult by remember { mutableStateOf("아직 요청하지 않음") }

    ExampleSection(title = "1. 권한 요청") {
        ExampleText(
            text = "상태: ${permissionState.phase}, 보유: ${permissionState.allGranted}",
            modifier = Modifier.testTag(ComposeExampleSemantics.PERMISSION_STATE),
        )
        ExampleText("최근 결과: $lastResult")
        ExampleButton(
            text = "카메라 권한 요청",
            modifier = Modifier.testTag(ComposeExampleSemantics.PERMISSION_REQUEST),
        ) {
            permissionState.request { deniedItems ->
                lastResult = if (deniedItems.isEmpty()) {
                    "모두 허용됨"
                } else {
                    deniedItems.joinToString { "${it.permission}: ${it.result}" }
                }
            }
        }

        if (permissionState.rationaleRequired.isNotEmpty()) {
            ExampleText("권한 사용 이유를 확인한 뒤 계속하거나 취소해 주세요.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleButton("계속") { permissionState.continueRequest() }
                ExampleButton("취소") { permissionState.cancelRequest() }
            }
        }
    }
}

@Composable
private fun ViewModelEventExample(viewModel: ComposeExamplesViewModel) {
    var lastReceivedSequence by rememberSaveable { mutableStateOf<Int?>(null) }

    viewModel.CollectVmEvent { sequence ->
        lastReceivedSequence = sequence
    }

    ExampleSection(title = "2. ViewModel 일회성 이벤트") {
        val result = lastReceivedSequence?.let { "ViewModel 이벤트 #$it" } ?: "없음"
        ExampleText(
            text = "최근 수신 이벤트: $result",
            modifier = Modifier.testTag(ComposeExampleSemantics.EVENT_STATE),
        )
        ExampleText("CollectVmEvent가 화면 생명주기에 맞춰 단발 이벤트를 수집합니다.")
        ExampleButton(
            text = "ViewModel 이벤트 보내기",
            modifier = Modifier.testTag(ComposeExampleSemantics.EVENT_SEND),
            onClick = viewModel::sendNextEvent,
        )
    }
}

@Composable
private fun SystemBarsExample(
    useDarkIcons: Boolean,
    onToggle: () -> Unit,
) {
    ExampleSection(title = "3. 시스템 바 아이콘") {
        ExampleText(
            text = if (useDarkIcons) "현재: 어두운 아이콘" else "현재: 밝은 아이콘",
            modifier = Modifier.testTag(ComposeExampleSemantics.SYSTEM_BAR_STATE),
        )
        ExampleButton(
            text = "아이콘 명암 전환",
            modifier = Modifier.testTag(ComposeExampleSemantics.SYSTEM_BAR_TOGGLE),
            onClick = onToggle,
        )
    }
}

@Composable
private fun ScrollExample(modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val direction by rememberScrollDirectionState(listState)
    val isTopReached by rememberEdgeReachedState(listState, ScrollEdge.TOP)
    val isBottomReached by rememberEdgeReachedState(listState, ScrollEdge.BOTTOM)

    ExampleSection(
        title = "4. LazyList 스크롤 상태",
        modifier = modifier,
    ) {
        ExampleText(
            text = "방향: $direction / 위: $isTopReached / 아래: $isBottomReached",
            modifier = Modifier.testTag(ComposeExampleSemantics.SCROLL_STATE),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .testTag(ComposeExampleSemantics.SCROLL_LIST)
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White),
        ) {
            items((1..30).toList()) { index ->
                ExampleText(
                    text = "스크롤 항목 $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun ExampleSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFEAEAEA))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun ExampleTitle(text: String) {
    BasicText(
        text = text,
        style = TextStyle(
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}

@Composable
private fun ExampleText(
    text: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(color = Color.Black, fontSize = 14.sp),
    )
}

@Composable
private fun ExampleButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color(0xFF315A8A))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = TextStyle(color = Color.White, fontSize = 14.sp),
        )
    }
}
