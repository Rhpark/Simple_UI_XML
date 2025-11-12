package kr.open.library.simple_ui.robolectric.local

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.local.base.BaseSharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class BaseSharedPreferenceRobolectricTest {

    private lateinit var application: Application
    private lateinit var preference: TestPreference

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        preference = TestPreference(application)
        preference.clear()
    }

    @After
    fun tearDown() {
        runBlocking {
            preference.clearCommit()
        }
        application.deleteSharedPreferences(TestPreference.PREF_NAME)
    }

    @Test
    fun propertyDelegates_persistPrimitiveValues() {
        preference.userName = "RhPark"
        preference.userAge = 34
        preference.isPremium = true
        preference.userId = 999_999L
        preference.rating = 4.5f
        preference.balance = 12.345

        assertEquals("RhPark", preference.userName)
        assertEquals(34, preference.userAge)
        assertTrue(preference.isPremium)
        assertEquals(999_999L, preference.userId)
        assertEquals(4.5f, preference.rating, 0.0f)
        assertEquals(12.345, preference.balance, 0.0)
        // double 저장 시 내부적으로 suffix 키를 사용함
        assertTrue(preference.rawContains("balance_DOUBLE_"))
    }

    @Test
    fun stringPref_withNull_removesKey() {
        preference.userName = "initial_value"
        assertTrue(preference.rawContains("user_name"))

        preference.userName = null

        assertFalse(preference.rawContains("user_name"))
        assertEquals("", preference.userName)
    }

    @Test
    fun saveCommit_persistsValueSynchronously() {
        val saved = preference.saveValueWithCommit("session_count", 7)

        assertTrue(saved)
        assertEquals(7, preference.readInt("session_count", 0))
    }

    @Test
    fun removeAllApply_clearsAllEntries() {
        preference.userName = "temp"
        preference.userAge = 1
        preference.balance = 2.0

        preference.clear()

        assertTrue(preference.rawEntries().isEmpty())
    }

    @Test
    fun removeAtDoubleCommit_removesBothKeys() {
        preference.balance = 99.0
        assertTrue(preference.rawContains("balance_DOUBLE_"))

        val removed = preference.removeBalanceCommit()

        assertTrue(removed)
        assertFalse(preference.rawContains("balance_DOUBLE_"))
        assertEquals(0.0, preference.balance, 0.0)
    }

    @Test
    fun doublePref_edgeCases_preservesPrecision() {
        val edgeCases = listOf(
            Double.MAX_VALUE,
            Double.MIN_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Math.PI,
            Math.E,
            0.0,
            -0.0
        )

        edgeCases.forEach { testValue ->
            preference.balance = testValue
            assertEquals(testValue, preference.balance, 0.0)
            // Verify exact bit-level equality for special values
            assertEquals(testValue.toBits(), preference.balance.toBits())
        }
    }

    @Test
    fun stringSetPref_savesAndLoadsCorrectly() {
        val tags = setOf("kotlin", "android", "test", "robolectric")

        preference.saveStringSet("user_tags", tags)

        val loaded = preference.loadStringSet("user_tags", emptySet())
        assertEquals(tags, loaded)
    }

    @Test
    fun putValue_withNonStringSet_throwsException() {
        val mixedSet = setOf("valid", 1, true)

        assertThrows(ClassCastException::class.java) {
            preference.putArbitraryValue("mixed_set", mixedSet)
        }
    }

    @Test
    fun saveCommit_concurrentCalls_areSerialized() = runBlocking {
        val jobs = (1..10).map { i ->
            launch(Dispatchers.Default) {
                preference.saveValueWithCommit("counter_$i", i * 10)
            }
        }
        jobs.forEach { it.join() }

        // Verify all values were saved correctly
        (1..10).forEach { i ->
            assertEquals(i * 10, preference.readInt("counter_$i", -1))
        }
    }

    @Test
    fun removeAtString_removesKeyImmediately() {
        preference.userName = "test_value"
        assertTrue(preference.rawContains("user_name"))

        preference.removeUserName()

        assertFalse(preference.rawContains("user_name"))
        assertEquals("", preference.userName) // Returns default value
    }

    @Test
    fun removeAtInt_removesKeyImmediately() {
        preference.userAge = 42
        assertTrue(preference.rawContains("user_age"))

        preference.removeUserAge()

        assertFalse(preference.rawContains("user_age"))
        assertEquals(0, preference.userAge) // Returns default value
    }

    @Test
    fun getEditor_allowsBatchOperations() {
        val editor = preference.getEditorPublic()
        editor.putString("batch_key1", "value1")
        editor.putInt("batch_key2", 999)
        editor.putBoolean("batch_key3", true)
        editor.apply()

        // Wait for apply to complete (it's async)
        Thread.sleep(100)

        assertEquals("value1", preference.readString("batch_key1", null))
        assertEquals(999, preference.readInt("batch_key2", 0))
        assertEquals(true, preference.readBoolean("batch_key3", false))
    }

    @Test
    fun propertyDelegates_returnDefaultValues_whenKeysDoNotExist() {
        // Clear all to ensure keys don't exist
        preference.clear()

        assertEquals("", preference.userName)
        assertEquals(0, preference.userAge)
        assertEquals(false, preference.isPremium)
        assertEquals(0L, preference.userId)
        assertEquals(0.0f, preference.rating, 0.0f)
        assertEquals(0.0, preference.balance, 0.0)
    }

    @Test
    fun putValue_withUnsupportedType_logsErrorAndRemovesKey() {
        // First set a valid value
        preference.userAge = 42
        assertTrue(preference.rawContains("user_age"))

        // Try to put an unsupported type (not null)
        preference.putArbitraryValue("user_age", listOf("invalid"))

        // Key should be removed
        assertFalse(preference.rawContains("user_age"))
    }

    @Test
    fun saveApply_withoutParameters_appliesChanges() {
        // Manually edit and call saveApply()
        preference.saveApplyManual()
        // This just tests that the method doesn't crash
    }

    @Test
    fun removeAtFloat_removesKeyImmediately() {
        preference.rating = 4.5f
        assertTrue(preference.rawContains("rating"))

        preference.removeRating()

        assertFalse(preference.rawContains("rating"))
        assertEquals(0.0f, preference.rating, 0.0f)
    }

    @Test
    fun removeAtLong_removesKeyImmediately() {
        preference.userId = 12345L
        assertTrue(preference.rawContains("user_id"))

        preference.removeUserId()

        assertFalse(preference.rawContains("user_id"))
        assertEquals(0L, preference.userId)
    }

    @Test
    fun removeAtBoolean_removesKeyImmediately() {
        preference.isPremium = true
        assertTrue(preference.rawContains("is_premium"))

        preference.removeIsPremium()

        assertFalse(preference.rawContains("is_premium"))
        assertFalse(preference.isPremium)
    }

    @Test
    fun removeAtDouble_removesKeyImmediately() {
        preference.balance = 99.99
        assertTrue(preference.rawContains("balance_DOUBLE_"))

        preference.removeBalance()

        assertFalse(preference.rawContains("balance_DOUBLE_"))
        assertEquals(0.0, preference.balance, 0.0)
    }

    @Test
    fun removeAtIntCommit_removesKeyWithCommit() = runBlocking {
        preference.userAge = 42
        assertTrue(preference.rawContains("user_age"))

        val removed = preference.removeUserAgeCommit()

        assertTrue(removed)
        assertFalse(preference.rawContains("user_age"))
        assertEquals(0, preference.userAge)
    }

    @Test
    fun removeAtFloatCommit_removesKeyWithCommit() = runBlocking {
        preference.rating = 4.5f
        assertTrue(preference.rawContains("rating"))

        val removed = preference.removeRatingCommit()

        assertTrue(removed)
        assertFalse(preference.rawContains("rating"))
        assertEquals(0.0f, preference.rating, 0.0f)
    }

    @Test
    fun removeAtLongCommit_removesKeyWithCommit() = runBlocking {
        preference.userId = 12345L
        assertTrue(preference.rawContains("user_id"))

        val removed = preference.removeUserIdCommit()

        assertTrue(removed)
        assertFalse(preference.rawContains("user_id"))
        assertEquals(0L, preference.userId)
    }

    @Test
    fun removeAtStringCommit_removesKeyWithCommit() = runBlocking {
        preference.userName = "test"
        assertTrue(preference.rawContains("user_name"))

        val removed = preference.removeUserNameCommit()

        assertTrue(removed)
        assertFalse(preference.rawContains("user_name"))
        assertEquals("", preference.userName)
    }

    private class TestPreference(context: Context) :
        BaseSharedPreference(context, PREF_NAME) {

        var userName by stringPref("user_name", "")
        var userAge by intPref("user_age", 0)
        var isPremium by booleanPref("is_premium", false)
        var userId by longPref("user_id", 0L)
        var rating by floatPref("rating", 0.0f)
        var balance by doublePref("balance", 0.0)

        fun clear() = removeAllApply()

        suspend fun clearCommit(): Boolean = removeAllCommit()

        fun rawEntries(): Map<String, *> = sp.all

        fun rawContains(key: String): Boolean = sp.contains(key)

        fun readInt(key: String, defaultValue: Int): Int = getInt(key, defaultValue)

        fun readString(key: String, defaultValue: String?): String? = getString(key, defaultValue)

        fun readBoolean(key: String, defaultValue: Boolean): Boolean = getBoolean(key, defaultValue)

        fun saveStringSet(key: String, value: Set<String>) = saveApply(key, value)

        fun loadStringSet(key: String, defaultValue: Set<String>): Set<String>? = getSet(key, defaultValue)

        fun saveValueWithCommit(key: String, value: Any?): Boolean = runBlocking {
            super.saveCommit(key, value)
        }

        fun putArbitraryValue(key: String, value: Any?) = saveApply(key, value)

        fun removeBalanceCommit(): Boolean = runBlocking {
            removeAtDoubleCommit("balance")
        }

        fun removeUserName() = removeAtString("user_name")

        fun removeUserAge() = removeAtInt("user_age")

        fun removeRating() = removeAtFloat("rating")

        fun removeUserId() = removeAtLong("user_id")

        fun removeIsPremium() = removeAtBoolean("is_premium")

        fun removeBalance() = removeAtDouble("balance")

        fun removeUserAgeCommit(): Boolean = runBlocking {
            removeAtIntCommit("user_age")
        }

        fun removeRatingCommit(): Boolean = runBlocking {
            removeAtFloatCommit("rating")
        }

        fun removeUserIdCommit(): Boolean = runBlocking {
            removeAtLongCommit("user_id")
        }

        fun removeUserNameCommit(): Boolean = runBlocking {
            removeAtStringCommit("user_name")
        }

        fun saveApplyManual() = saveApply()

        fun getEditorPublic() = getEditor()

        companion object {
            const val PREF_NAME = "test_pref"
        }
    }
}
