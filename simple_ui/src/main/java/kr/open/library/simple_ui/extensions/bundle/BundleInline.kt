package kr.open.library.simple_ui.extensions.bundle

import android.os.Bundle
import kr.open.library.simple_ui.logcat.Logx

public inline fun <reified T> Bundle.getValue(
    key: String,
    defaultValue: T,
): T = if (containsKey(key)) {
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

