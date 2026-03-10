package kr.open.library.simple_ui.core.permissions.internal

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch

/**
 * Safely reads the permissions declared in AndroidManifest.xml.<br><br>
 * AndroidManifest.xml에 선언된 권한 목록을 안전하게 조회합니다.<br>
 *
 * @return Returns the set of manifest-declared permissions, or an empty set if retrieval fails.<br><br>
 *         조회에 실패하면 빈 집합을 반환하고, 성공하면 매니페스트 선언 권한 집합을 반환합니다.<br>
 */
fun Context.readDeclaredManifestPermissions(): Set<String> = safeCatch(defaultValue = emptySet()) {
    val packageInfo = checkSdkVersion(
        Build.VERSION_CODES.TIRAMISU,
        positiveWork = {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
            )
        },
        negativeWork = {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        },
    )
    packageInfo.requestedPermissions?.toSet() ?: emptySet()
}
