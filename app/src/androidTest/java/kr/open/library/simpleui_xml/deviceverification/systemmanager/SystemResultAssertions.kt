package kr.open.library.simpleui_xml.deviceverification.systemmanager

import kr.open.library.simple_ui.system_manager.core.base.SystemResult

internal fun <T> SystemResult<T>.requireSuccess(): T =
    when (this) {
        is SystemResult.Success -> value
        is SystemResult.Failure -> throw AssertionError("SystemResult.Failure", cause)
        SystemResult.PermissionDenied -> throw AssertionError("SystemResult.PermissionDenied")
        SystemResult.PolicyRestricted -> throw AssertionError("SystemResult.PolicyRestricted")
    }
