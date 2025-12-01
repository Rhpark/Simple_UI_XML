/**
 * Structured SharedPreferences helpers that centralize read/write delegates and commit safety.<br><br>
 * 위임 프로퍼티와 안전한 커밋 로직으로 구성된 SharedPreferences 도우미 기반 클래스입니다.<br>
 */
package kr.open.library.simple_ui.core.local.base

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.open.library.simple_ui.core.logcat.Logx
import java.lang.Double.doubleToRawLongBits
import java.lang.Double.longBitsToDouble
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Base class that exposes delegate builders and thread-safe commit utilities for SharedPreferences.<br><br>
 * SharedPreferences 위임자 생성기와 스레드 안전한 커밋 도구를 제공하는 기반 클래스입니다.<br>
 *
 * Example:<br><br>
 * 예시:<br>
 * ```
 * class UserPreference(ctx: Context) : BaseSharedPreference(ctx, "user") {
 *     var userName by stringPref("user_name", "")
 *     var userAge by intPref("user_age", 0)
 * }
 * ```
 *
 * @param context Android context used to obtain application-level SharedPreferences.<br><br>
 *        애플리케이션 범위 SharedPreferences를 얻기 위한 콘텍스트입니다.
 * @param groupKey Preference file name.<br><br>
 *        SharedPreferences 파일 이름입니다.
 * @param sharedPrivateMode File mode, defaults to [Context.MODE_PRIVATE].<br><br>
 *        파일 모드이며 기본값은 [Context.MODE_PRIVATE]입니다.<br>
 */
public abstract class BaseSharedPreference(
    context: Context,
    groupKey: String,
    sharedPrivateMode: Int = Context.MODE_PRIVATE,
) {
    companion object {
        private const val DOUBLE_TYPE = "_DOUBLE_"
    }

    protected val sp: SharedPreferences by lazy { context.applicationContext.getSharedPreferences(groupKey, sharedPrivateMode) }

    protected val commitMutex: Mutex by lazy { Mutex() }

    /**
     * Builds a nullable `String` delegate bound to [key] with an optional default.<br><br>
     * [key]와 기본값을 바인딩한 Nullable `String` 위임자를 만듭니다.<br>
     *
     * @param key SharedPreferences entry key.<br><br>
     *        SharedPreferences에 사용할 키입니다.
     * @param defaultValue Fallback string when the key has no value.<br><br>
     *        값이 없을 때 사용할 기본 문자열입니다.
     * @return Read/write property delegate backed by SharedPreferences.<br><br>
     *         SharedPreferences를 기반으로 하는 읽기·쓰기 위임자를 반환합니다.<br>
     */
    protected fun stringPref(
        key: String,
        defaultValue: String? = null,
    ) = object : ReadWriteProperty<Any?, String?> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): String? = getString(key, defaultValue)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: String?,
        ) = saveApply(key, value)
    }

    /**
     * Builds an `Int` delegate bound to [key] with [defaultValue].<br><br>
     * [key]와 [defaultValue]를 바인딩한 `Int` 위임자를 만듭니다.<br>
     */
    protected fun intPref(
        key: String,
        defaultValue: Int,
    ) = object : ReadWriteProperty<Any?, Int> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Int = getInt(key, defaultValue)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: Int,
        ) = saveApply(key, value)
    }

    /**
     * Builds a `Boolean` delegate bound to [key] with [defaultValue].<br><br>
     * [key]와 [defaultValue]를 바인딩한 `Boolean` 위임자를 만듭니다.<br>
     */
    protected fun booleanPref(
        key: String,
        defaultValue: Boolean,
    ) = object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Boolean = getBoolean(key, defaultValue)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: Boolean,
        ) = saveApply(key, value)
    }

    /**
     * Builds a `Long` delegate bound to [key] with [defaultValue].<br><br>
     * [key]와 [defaultValue]를 바인딩한 `Long` 위임자를 만듭니다.<br>
     */
    protected fun longPref(
        key: String,
        defaultValue: Long,
    ) = object : ReadWriteProperty<Any?, Long> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Long = getLong(key, defaultValue)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: Long,
        ) = saveApply(key, value)
    }

    /**
     * Builds a `Float` delegate bound to [key] with [defaultValue].<br><br>
     * [key]와 [defaultValue]를 바인딩한 `Float` 위임자를 만듭니다.<br>
     */
    protected fun floatPref(
        key: String,
        defaultValue: Float,
    ) = object : ReadWriteProperty<Any?, Float> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Float = getFloat(key, defaultValue)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: Float,
        ) = saveApply(key, value)
    }

    /**
     * Builds a `Double` delegate bound to [key] with [defaultValue], storing raw bits in `Long`.<br><br>
     * [key]와 [defaultValue]를 바인딩한 `Double` 위임자를 만들고 내부적으로 `Long` 비트로 저장합니다.<br>
     */
    protected fun doublePref(
        key: String,
        defaultValue: Double,
    ) = object : ReadWriteProperty<Any?, Double> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Double = getDouble(key, defaultValue)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: Double,
        ) = saveApply(key, value)
    }

    /**
     * Reads a nullable `String` from preferences.<br><br>
     * SharedPreferences에서 Nullable `String` 값을 읽어옵니다.<br>
     */
    protected fun getString(
        key: String,
        defaultValue: String?,
    ): String? = sp.getString(key, defaultValue)

    /**
     * Reads an `Int` from preferences.<br><br>
     * SharedPreferences에서 `Int` 값을 읽어옵니다.<br>
     */
    protected fun getInt(
        key: String,
        defaultValue: Int,
    ): Int = sp.getInt(key, defaultValue)

    /**
     * Reads a `Float` from preferences.<br><br>
     * SharedPreferences에서 `Float` 값을 읽어옵니다.<br>
     */
    protected fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float = sp.getFloat(key, defaultValue)

    /**
     * Reads a `Boolean` from preferences.<br><br>
     * SharedPreferences에서 `Boolean` 값을 읽어옵니다.<br>
     */
    protected fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = sp.getBoolean(key, defaultValue)

    /**
     * Reads a `Long` from preferences.<br><br>
     * SharedPreferences에서 `Long` 값을 읽어옵니다.<br>
     */
    protected fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = sp.getLong(key, defaultValue)

    /**
     * Reads a `Set<String>` from preferences.<br><br>
     * SharedPreferences에서 `Set<String>` 값을 읽어옵니다.<br>
     */
    protected fun getSet(
        key: String,
        defaultValue: Set<String>?,
    ): Set<String>? = sp.getStringSet(key, defaultValue)

    /**
     * Reads a `Double` by mapping to raw long bits stored with a suffix.<br><br>
     * 접미사를 덧붙여 저장된 `Long` 비트를 다시 `Double`로 변환해 읽어옵니다.<br>
     */
    protected fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double = longBitsToDouble(sp.getLong(key + DOUBLE_TYPE, doubleToRawLongBits(defaultValue)))

    /**
     * Writes the provided [value] into the editor, handling primitive and Set<String> types.<br><br>
     * 프리미티브 및 `Set<String>` 타입을 처리하면서 [value]를 에디터에 기록합니다.<br>
     *
     * @param key Preference entry key.<br><br>
     *        SharedPreferences 키입니다.
     * @param value Value to store; unsupported types remove the key.<br><br>
     *        저장할 값이며 지원되지 않는 타입이면 키를 삭제합니다.
     * @return Same [SharedPreferences.Editor] for chaining.<br><br>
     *         체이닝을 위한 동일한 [SharedPreferences.Editor]를 반환합니다.<br>
     */
    protected fun SharedPreferences.Editor.putValue(
        key: String,
        value: Any?,
    ): SharedPreferences.Editor =
        when (value) {
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Double -> putLong(key + DOUBLE_TYPE, doubleToRawLongBits(value))
            is Long -> putLong(key, value)
            is Set<*> -> {
                val stringSet = value.filterIsInstance<String>().toSet()
                if (stringSet.size != value.size) {
                    Logx.e("[ERROR] Set<*> is not Set<String>. Key: $key, Value: $value")
                    throw ClassCastException("[ERROR] Set<*> is not Set<String>. Key: $key, Value: $value")
                }
                putStringSet(key, stringSet)
            }

            else -> {
                if (value != null) {
                    Logx.e("Unsupported value type: $key, ${value.javaClass}")
                }
                remove(key)
            }
        }

    /**
     * Returns a fresh [SharedPreferences.Editor] instance.<br><br>
     * 새로운 [SharedPreferences.Editor] 인스턴스를 반환합니다.<br>
     */
    protected fun getEditor(): SharedPreferences.Editor = sp.edit()

    /**
     * Applies pending edits without another mutation.<br><br>
     * 추가 수정 없이 보류 중인 변경 사항을 적용합니다.<br>
     */
    protected fun saveApply() {
        sp.edit().apply()
    }

    /**
     * Applies a single [key]/[value] mutation asynchronously.<br><br>
     * [key]/[value] 쌍을 비동기적으로 적용합니다.<br>
     */
    protected fun saveApply(
        key: String,
        value: Any?,
    ) {
        sp.edit().putValue(key, value).apply()
    }

    /**
     * Commits a [key]/[value] mutation using a coroutine-friendly mutex.<br><br>
     * 코루틴에 안전한 뮤텍스를 사용해 [key]/[value] 변경을 커밋합니다.<br>
     *
     * @return true when `commit()` succeeded, otherwise false.<br><br>
     *         `commit()`이 성공하면 true, 아니면 false를 반환합니다.<br>
     */
    protected suspend fun saveCommit(
        key: String,
        value: Any?,
    ): Boolean = commitDoWork { putValue(key, value) }

    /**
     * Runs [doWork] inside `Dispatchers.IO` and mutual exclusion, then commits synchronously.<br><br>
     * `Dispatchers.IO`와 상호 배제를 사용해 [doWork]를 실행한 뒤 동기 커밋합니다.<br>
     *
     * @return Result of `commit()` execution.<br><br>
     *         `commit()` 실행 결과를 반환합니다.<br>
     */
    protected suspend inline fun commitDoWork(crossinline doWork: SharedPreferences.Editor.() -> Unit): Boolean =
        withContext(Dispatchers.IO) { commitMutex.withLock { sp.edit().apply { doWork() }.commit() } }

    /**
     * Clears all entries using `apply()`. Use when asynchronous removal is acceptable.<br><br>
     * `apply()`를 사용해 모든 항목을 삭제하며, 비동기 삭제가 허용될 때 이용합니다.<br>
     */
    protected fun removeAllApply() {
        sp.edit().clear().apply()
    }

    /**
     * Removes a specific key using `apply()`.<br><br>
     * 특정 키를 `apply()` 방식으로 삭제합니다.<br>
     */
    private fun removeAt(key: String) {
        sp.edit().remove(key).apply()
    }

    /**
     * Removes an `Int` entry using [removeAt].<br><br>
     * [removeAt]을 통해 `Int` 항목을 삭제합니다.<br>
     */
    protected fun removeAtInt(key: String) {
        removeAt(key)
    }

    /**
     * Removes a `String` entry using [removeAt].<br><br>
     * [removeAt]을 통해 `String` 항목을 삭제합니다.<br>
     */
    protected fun removeAtString(key: String) {
        removeAt(key)
    }

    /**
     * Removes a `Float` entry using [removeAt].<br><br>
     * [removeAt]을 통해 `Float` 항목을 삭제합니다.<br>
     */
    protected fun removeAtFloat(key: String) {
        removeAt(key)
    }

    /**
     * Removes a `Long` entry using [removeAt].<br><br>
     * [removeAt]을 통해 `Long` 항목을 삭제합니다.<br>
     */
    protected fun removeAtLong(key: String) {
        removeAt(key)
    }

    /**
     * Removes a `Boolean` entry using [removeAt].<br><br>
     * [removeAt]을 통해 `Boolean` 항목을 삭제합니다.<br>
     */
    protected fun removeAtBoolean(key: String) {
        removeAt(key)
    }

    /**
     * Removes a `Double` entry by deleting the suffixed key.<br><br>
     * 접미사가 붙은 키를 삭제해 `Double` 항목을 제거합니다.<br>
     */
    protected fun removeAtDouble(key: String) {
        removeAt(key + DOUBLE_TYPE)
    }

    /**
     * Clears all entries with a synchronous commit.<br><br>
     * 모든 항목을 동기 커밋 방식으로 삭제합니다.<br>
     */
    protected suspend fun removeAllCommit(): Boolean = commitDoWork { clear() }

    /**
     * Removes a specific key with synchronous commit.<br><br>
     * 특정 키를 동기 커밋 방식으로 삭제합니다.<br>
     */
    private suspend fun removeAtCommit(key: String): Boolean = commitDoWork { remove(key) }

    /**
     * Removes an `Int` entry with synchronous commit.<br><br>
     * `Int` 항목을 동기 커밋 방식으로 삭제합니다.<br>
     */
    protected suspend fun removeAtIntCommit(key: String): Boolean = removeAtCommit(key)

    /**
     * Removes a `Float` entry with synchronous commit.<br><br>
     * `Float` 항목을 동기 커밋 방식으로 삭제합니다.<br>
     */
    protected suspend fun removeAtFloatCommit(key: String): Boolean = removeAtCommit(key)

    /**
     * Removes a `Long` entry with synchronous commit.<br><br>
     * `Long` 항목을 동기 커밋 방식으로 삭제합니다.<br>
     */
    protected suspend fun removeAtLongCommit(key: String): Boolean = removeAtCommit(key)

    /**
     * Removes a `String` entry with synchronous commit.<br><br>
     * `String` 항목을 동기 커밋 방식으로 삭제합니다.<br>
     */
    protected suspend fun removeAtStringCommit(key: String): Boolean = removeAtCommit(key)

    /**
     * Removes a `Double` entry (suffixed key) with synchronous commit.<br><br>
     * 접미사 키를 사용하는 `Double` 항목을 동기 커밋 방식으로 삭제합니다.<br>
     */
    protected suspend fun removeAtDoubleCommit(key: String): Boolean = removeAtCommit(key + DOUBLE_TYPE)
}
