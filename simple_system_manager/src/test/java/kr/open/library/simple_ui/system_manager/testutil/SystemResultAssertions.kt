package kr.open.library.simple_ui.system_manager.testutil

import kr.open.library.simple_ui.system_manager.core.base.SystemResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

internal fun assertSuccess(result: SystemResult<Unit>) {
    assertEquals(SystemResult.Success(Unit), result)
}

internal fun <T> assertSuccessValue(expected: T, result: SystemResult<T>) {
    assertEquals(SystemResult.Success(expected), result)
}

internal fun assertFailure(result: SystemResult<*>) {
    assertTrue("Expected SystemResult.Failure but was $result", result is SystemResult.Failure)
}

internal fun assertPermissionDenied(result: SystemResult<*>) {
    assertEquals(SystemResult.PermissionDenied, result)
}

internal fun assertPolicyRestricted(result: SystemResult<*>) {
    assertEquals(SystemResult.PolicyRestricted, result)
}
