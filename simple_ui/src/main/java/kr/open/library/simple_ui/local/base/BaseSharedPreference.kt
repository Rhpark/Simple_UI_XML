package kr.open.library.simple_ui.local.base

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.open.library.simple_ui.logcat.Logx
import java.lang.Double.doubleToRawLongBits
import java.lang.Double.longBitsToDouble
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * ex)
 *   class UserPreference(context: Context) : BaseSharedPreference(context, "user") {
 *       var userName by stringPref("user_name", "")
 *       var userAge by intPref("user_age", 0)
 *   }
 *
 */
public abstract class BaseSharedPreference(context: Context, groupKey: String, sharedPrivateMode: Int = Context.MODE_PRIVATE) {

    companion object {
        private const val DOUBLE_TYPE = "_DOUBLE_"
    }

    protected val sp: SharedPreferences by lazy { context.applicationContext.getSharedPreferences(groupKey, sharedPrivateMode) }

    protected val commitMutex : Mutex by lazy { Mutex() } // use for coroutine commit, synchronized

    /** Delegate Value **/
    protected fun stringPref(key: String, defaultValue: String? = null) = object : ReadWriteProperty<Any?, String?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? = getString(key, defaultValue)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) = saveApply(key, value)
    }

    protected fun intPref(key: String, defaultValue: Int) = object : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int = getInt(key, defaultValue)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) = saveApply(key, value)
    }

    protected fun booleanPref(key: String, defaultValue: Boolean) = object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = getBoolean(key, defaultValue)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) = saveApply(key, value)
    }

    protected fun longPref(key: String, defaultValue: Long) = object : ReadWriteProperty<Any?, Long> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Long = getLong(key, defaultValue)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) = saveApply(key, value)
    }

    protected fun floatPref(key: String, defaultValue: Float) = object : ReadWriteProperty<Any?, Float> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Float = getFloat(key, defaultValue)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) = saveApply(key, value)
    }

    protected fun doublePref(key: String, defaultValue: Double) = object : ReadWriteProperty<Any?, Double> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Double = getDouble(key, defaultValue)
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) = saveApply(key, value)
    }



    /******** Load data ********/
    protected fun getString(key: String, defaultValue: String?): String? = sp.getString(key, defaultValue)
    protected fun getInt(key: String, defaultValue: Int): Int = sp.getInt(key, defaultValue)
    protected fun getFloat(key: String, defaultValue: Float): Float = sp.getFloat(key, defaultValue)
    protected fun getBoolean(key: String, defaultValue: Boolean): Boolean = sp.getBoolean(key, defaultValue)
    protected fun getLong(key: String, defaultValue: Long): Long = sp.getLong(key, defaultValue)
    protected fun getSet(key: String, defaultValue: Set<String>?): Set<String>? = sp.getStringSet(key, defaultValue)
    protected fun getDouble(key: String, defaultValue:Double): Double  = longBitsToDouble(sp.getLong(key + DOUBLE_TYPE, doubleToRawLongBits(defaultValue)))

    /**
     * Save Data
     * must be called after apply() or commit()
     */
    protected fun SharedPreferences.Editor.putValue(key: String, value: Any?): SharedPreferences.Editor = when (value) {
        is String -> putString(key, value)
        is Boolean -> putBoolean(key, value)
        is Float-> putFloat(key, value)
        is Int -> putInt(key, value)
        is Double ->  putLong(key + DOUBLE_TYPE, doubleToRawLongBits(value))
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
    /** editor **/
    protected fun getEditor() : SharedPreferences.Editor = sp.edit()

    /** Save **/
    protected fun saveApply() { sp.edit().apply() }
    protected fun saveApply(key: String, value: Any?) { sp.edit().putValue(key, value).apply() }
    protected suspend fun saveCommit(key: String, value: Any?):Boolean = commitDoWork{  putValue(key, value) }

    protected suspend inline fun commitDoWork(crossinline doWork: SharedPreferences.Editor.() -> Unit): Boolean =
        withContext(Dispatchers.IO) { commitMutex.withLock { sp.edit().apply { doWork() }.commit() } }

    /** Remove **/
    protected fun removeAllApply() { sp.edit().clear().apply() }
    private fun removeAt(key: String) { sp.edit().remove(key).apply() }
    protected fun removeAtInt(key: String)      { removeAt(key) }
    protected fun removeAtString(key: String)   { removeAt(key) }
    protected fun removeAtFloat(key: String)    { removeAt(key) }
    protected fun removeAtLong(key: String)     { removeAt(key) }
    protected fun removeAtBoolean(key: String)  { removeAt(key) }
    protected fun removeAtDouble(key: String)   { removeAt(key + DOUBLE_TYPE) }

    protected suspend fun removeAllCommit():Boolean = commitDoWork{ clear() }
    private suspend fun removeAtCommit(key: String):Boolean         = commitDoWork{ remove(key) }
    protected suspend fun removeAtIntCommit(key: String):Boolean      = removeAtCommit(key)
    protected suspend fun removeAtFloatCommit(key: String):Boolean    = removeAtCommit(key)
    protected suspend fun removeAtLongCommit(key: String):Boolean     = removeAtCommit(key)
    protected suspend fun removeAtStringCommit(key: String):Boolean   = removeAtCommit(key)
    protected suspend fun removeAtDoubleCommit(key: String):Boolean   = removeAtCommit(key + DOUBLE_TYPE)
}