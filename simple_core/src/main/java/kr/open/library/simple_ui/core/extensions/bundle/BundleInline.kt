/**
 * 번들에서 타입 안전하게 값을 읽어오는 확장 함수를 모아 둔 패키지입니다.
 * Package containing Bundle extensions for safe, type-aware value access.
 */
package kr.open.library.simple_ui.core.extensions.bundle

import android.os.Bundle
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Returns the value for [key] with type-safety and falls back to [defaultValue] when the lookup
 * fails or the value cannot be cast to the requested type.<br><br>
 * 번들에서 특정 키의 값을 타입에 맞춰 반환하고, 없거나 변환에 실패하면 기본값을 돌려줍니다.<br>
 *
 * @param key Key that identifies the stored value.<br><br>
 *            가져올 값의 키입니다.
 *
 * @param defaultValue Fallback value returned when the key is missing or casting fails.<br><br>
 *        값이 없거나 타입 변환에 실패했을 때 사용할 기본값입니다.
 *
 * @return Either the stored value cast to `T` or [defaultValue] when unavailable.<br>
 *         Logs an error via [Logx] when the key is missing or the requested type is unsupported.<br><br>
 *         저장된 값이 있다면 해당 타입의 값, 그렇지 않다면 [defaultValue].<br>
 *         지원하지 않는 타입이거나 키를 찾지 못하면 [Logx]로 오류를 남깁니다.
 */
public inline fun <reified T> Bundle.getValue(
    key: String,
    defaultValue: T,
): T =
    if (containsKey(key)) {
        when (T::class) {
            Int::class -> getInt(key, defaultValue as Int) as T
            Boolean::class -> getBoolean(key, defaultValue as Boolean) as T
            Float::class -> getFloat(key, defaultValue as Float) as T
            Long::class -> getLong(key, defaultValue as Long) as T
            Double::class -> getDouble(key, defaultValue as Double) as T
            String::class -> getString(key, defaultValue as String) as T
            Char::class -> getChar(key, defaultValue as Char) as T
            Short::class -> getShort(key, defaultValue as Short) as T
            Byte::class -> getByte(key, defaultValue as Byte) as T
            ByteArray::class -> (getByteArray(key) ?: defaultValue as ByteArray) as T
            Bundle::class -> (getBundle(key) ?: defaultValue as Bundle) as T
            else -> {
                Logx.e("Can not cast Type ${T::class} key $key, defaultVaule $defaultValue")
                defaultValue
            }
        }
    } else {
        Logx.e("can not find key $key , defaultValue $defaultValue")
        defaultValue
    }
