package kr.open.library.simple_ui.core.permissions.internal

import android.content.Context
import kr.open.library.simple_ui.core.permissions.extensions.readDeclaredManifestPermissions as readDeclaredManifestPermissionsPublic

/**
 * Forwards manifest permission reads to the public extensions package for binary compatibility.<br><br>
 * 바이너리 호환성을 위해 매니페스트 권한 조회를 공개 extensions 패키지로 전달합니다.<br>
 *
 * @return Manifest-declared permissions returned by the public replacement API.<br><br>
 *         공개 대체 API가 반환한 매니페스트 선언 권한 집합입니다.<br>
 */
@Deprecated(
    message = "Use kr.open.library.simple_ui.core.permissions.extensions.readDeclaredManifestPermissions instead.",
    replaceWith = ReplaceWith(
        expression = "readDeclaredManifestPermissions()",
        imports = ["kr.open.library.simple_ui.core.permissions.extensions.readDeclaredManifestPermissions"],
    ),
)
public fun Context.readDeclaredManifestPermissions(): Set<String> = readDeclaredManifestPermissionsPublic()
