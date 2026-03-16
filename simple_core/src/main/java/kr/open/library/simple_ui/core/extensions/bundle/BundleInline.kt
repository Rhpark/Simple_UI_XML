/**
 * Bundle 값을 타입에 맞게 안전하게 읽기 위한 확장 함수를 모아 둔 패키지입니다.<br><br>
 * Package containing Bundle extensions for safe, type-aware value access.<br>
 */
package kr.open.library.simple_ui.core.extensions.bundle

import android.os.Bundle
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * [key]에 저장된 값을 요청한 타입으로 읽고, 키가 없거나 변환에 실패하면 [defaultValue]를 반환합니다.<br><br>
 * Returns the value for [key] with type-safety and falls back to [defaultValue] when the lookup
 * fails or the value cannot be cast to the requested type.<br>
 *
 * @param T 반환받을 타입입니다.<br><br>
 *          The type to cast the value to.<br>
 * @param key 조회할 Bundle 키입니다.<br><br>
 *            Key that identifies the stored value.<br>
 * @param defaultValue 키가 없거나 타입 변환이 실패했을 때 반환할 기본값입니다.<br><br>
 *                     Fallback value returned when the key is missing or casting fails.<br>
 * @return 저장된 값을 `T`로 반환하거나, 값을 사용할 수 없으면 [defaultValue]를 반환합니다.
 *         키가 없을 때는 조용히 기본값을 반환하고, 지원하지 않는 타입인 경우 [Logx]로 오류를 남깁니다.<br><br>
 *         Either the stored value cast to `T` or [defaultValue] when unavailable.
 *         Missing keys return the fallback quietly, while unsupported requested types log an error via [Logx].<br>
 */
public inline fun <reified T> Bundle.getValue(key: String, defaultValue: T): T = safeCatch(defaultValue) {
    if (!containsKey(key)) {
        return@safeCatch defaultValue
    }

    when (T::class) {
        Int::class -> getInt(key, defaultValue as Int) as T
        Boolean::class -> getBoolean(key, defaultValue as Boolean) as T
        Float::class -> getFloat(key, defaultValue as Float) as T
        Long::class -> getLong(key, defaultValue as Long) as T
        Double::class -> getDouble(key, defaultValue as Double) as T
        Char::class -> getChar(key, defaultValue as Char) as T
        Short::class -> getShort(key, defaultValue as Short) as T
        Byte::class -> getByte(key, defaultValue as Byte) as T

        String::class -> (getString(key) ?: defaultValue) as T
        ByteArray::class -> (getByteArray(key) ?: defaultValue) as T
        Bundle::class -> (getBundle(key) ?: defaultValue) as T

        else -> {
            Logx.e("Can not cast Type ${T::class} key $key, defaultValue $defaultValue")
            defaultValue
        }
    }
}
