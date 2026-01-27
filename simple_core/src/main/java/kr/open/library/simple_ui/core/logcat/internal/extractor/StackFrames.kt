package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * 현재 프레임과 부모 프레임을 묶는 컨테이너입니다.
 *
 * Container for current and parent stack frames.
 * <br><br>
 * 현재 프레임과 부모 프레임을 함께 전달하기 위한 모델입니다.
 *
 * @property current 현재 프레임.
 * @property parent 부모 프레임(없을 수 있음).
 */
internal data class StackFrames(
    val current: StackFrame,
    val parent: StackFrame?,
)
