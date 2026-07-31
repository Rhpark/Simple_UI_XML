package kr.open.library.simpleui.consumer

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kr.open.library.simple_ui.compose.scroll.ScrollDirection
import kr.open.library.simple_ui.compose.scroll.rememberScrollDirectionState

@Composable
public fun rememberConsumerScrollDirection(state: LazyListState): State<ScrollDirection> =
    rememberScrollDirectionState(state)
