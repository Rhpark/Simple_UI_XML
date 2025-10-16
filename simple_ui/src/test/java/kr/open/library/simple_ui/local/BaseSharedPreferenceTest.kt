package kr.open.library.simple_ui.local

import android.content.Context
import android.content.SharedPreferences
import kr.open.library.simple_ui.local.base.BaseSharedPreference
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.*

/**
 * BaseSharedPreference에 대한 단위 테스트
 *
 * 테스트 대상:
 * - 데이터 저장 및 불러오기 (String, Int, Boolean, Long, Float, Double)
 * - Property Delegation
 * - 기본값 처리
 * - 데이터 타입별 저장/로드
 *
 * 참고: 실제 Android Context가 필요한 테스트는 Instrumented Test에서 수행합니다.
 * 여기서는 로직 검증에 집중합니다.
 */
class BaseSharedPreferenceTest {

    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    // 테스트용 Preference 구현
    private class TestPreference(context: Context) : BaseSharedPreference(context, "test_pref") {
        // Property delegation 테스트용
        var userName by stringPref("user_name", "")
        var userAge by intPref("user_age", 0)
        var isPremium by booleanPref("is_premium", false)
        var userId by longPref("user_id", 0L)
        var rating by floatPref("rating", 0.0f)
        var balance by doublePref("balance", 0.0)

        // 테스트를 위한 public 함수
        fun testGetString(key: String, default: String?) = getString(key, default)
        fun testGetInt(key: String, default: Int) = getInt(key, default)
        fun testGetBoolean(key: String, default: Boolean) = getBoolean(key, default)
        fun testGetLong(key: String, default: Long) = getLong(key, default)
        fun testGetFloat(key: String, default: Float) = getFloat(key, default)
        fun testGetDouble(key: String, default: Double) = getDouble(key, default)

        fun testSaveApply(key: String, value: Any?) = saveApply(key, value)
        suspend fun testSaveCommit(key: String, value: Any?) = saveCommit(key, value)
    }

    @Before
    fun setup() {
        // Mock 객체 생성
        mockContext = mock(Context::class.java)
        mockSharedPreferences = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        // Context가 SharedPreferences를 반환하도록 설정
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.putFloat(anyString(), anyFloat())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
    }

    // ========== 1. String 타입 테스트 ==========

    @Test
    fun `문자열을_저장하고_불러올_수_있다`() {
        // Given
        val key = "test_key"
        val value = "test_value"
        `when`(mockSharedPreferences.getString(key, "")).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetString(key, "")

        // Then
        verify(mockEditor).putString(key, value)
        assertEquals("저장한 값과 불러온 값이 같아야 합니다", value, result)
    }

    @Test
    fun `문자열_기본값을_반환할_수_있다`() {
        // Given
        val key = "non_existent_key"
        val defaultValue = "default"
        `when`(mockSharedPreferences.getString(key, defaultValue)).thenReturn(defaultValue)

        val preference = TestPreference(mockContext)

        // When
        val result = preference.testGetString(key, defaultValue)

        // Then
        assertEquals("기본값이 반환되어야 합니다", defaultValue, result)
    }

    @Test
    fun `null_문자열을_저장할_수_있다`() {
        // Given
        val key = "nullable_key"
        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, null)

        // Then
        verify(mockEditor).remove(key)  // null 저장 시 remove 호출
    }

    // ========== 2. Int 타입 테스트 ==========

    @Test
    fun `정수를_저장하고_불러올_수_있다`() {
        // Given
        val key = "age_key"
        val value = 25
        `when`(mockSharedPreferences.getInt(key, 0)).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetInt(key, 0)

        // Then
        verify(mockEditor).putInt(key, value)
        assertEquals("저장한 정수값과 불러온 값이 같아야 합니다", value, result)
    }

    @Test
    fun `정수_기본값을_반환할_수_있다`() {
        // Given
        val key = "missing_int"
        val defaultValue = 100
        `when`(mockSharedPreferences.getInt(key, defaultValue)).thenReturn(defaultValue)

        val preference = TestPreference(mockContext)

        // When
        val result = preference.testGetInt(key, defaultValue)

        // Then
        assertEquals("기본값이 반환되어야 합니다", defaultValue, result)
    }

    @Test
    fun `음수_정수를_저장할_수_있다`() {
        // Given
        val key = "negative_key"
        val value = -42
        `when`(mockSharedPreferences.getInt(key, 0)).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetInt(key, 0)

        // Then
        assertEquals("음수 값이 저장되어야 합니다", value, result)
    }

    // ========== 3. Boolean 타입 테스트 ==========

    @Test
    fun `true_값을_저장하고_불러올_수_있다`() {
        // Given
        val key = "premium_key"
        val value = true
        `when`(mockSharedPreferences.getBoolean(key, false)).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetBoolean(key, false)

        // Then
        verify(mockEditor).putBoolean(key, value)
        assertTrue("true 값이 저장되어야 합니다", result)
    }

    @Test
    fun `false_값을_저장하고_불러올_수_있다`() {
        // Given
        val key = "enabled_key"
        val value = false
        `when`(mockSharedPreferences.getBoolean(key, true)).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetBoolean(key, true)

        // Then
        assertFalse("false 값이 저장되어야 합니다", result)
    }

    // ========== 4. Long 타입 테스트 ==========

    @Test
    fun `Long_값을_저장하고_불러올_수_있다`() {
        // Given
        val key = "timestamp_key"
        val value = 1234567890123L
        `when`(mockSharedPreferences.getLong(key, 0L)).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetLong(key, 0L)

        // Then
        verify(mockEditor).putLong(key, value)
        assertEquals("Long 값이 저장되어야 합니다", value, result)
    }

    // ========== 5. Float 타입 테스트 ==========

    @Test
    fun `Float_값을_저장하고_불러올_수_있다`() {
        // Given
        val key = "rating_key"
        val value = 4.5f
        `when`(mockSharedPreferences.getFloat(key, 0.0f)).thenReturn(value)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetFloat(key, 0.0f)

        // Then
        verify(mockEditor).putFloat(key, value)
        assertEquals("Float 값이 저장되어야 합니다", value, result, 0.001f)
    }

    // ========== 6. Double 타입 테스트 ==========

    @Test
    fun `Double_값을_저장하고_불러올_수_있다`() {
        // Given
        val key = "balance_key"
        val value = 12345.67890
        val valueAsLong = java.lang.Double.doubleToRawLongBits(value)
        `when`(mockSharedPreferences.getLong(key + "_DOUBLE_", 0L)).thenReturn(valueAsLong)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply(key, value)
        val result = preference.testGetDouble(key, 0.0)

        // Then
        verify(mockEditor).putLong(key + "_DOUBLE_", valueAsLong)
        assertEquals("Double 값이 저장되어야 합니다", value, result, 0.00001)
    }

    // ========== 7. Property Delegation 테스트 ==========

    @Test
    fun `Property_delegation으로_문자열을_사용할_수_있다`() {
        // Given
        val userName = "홍길동"
        `when`(mockSharedPreferences.getString("user_name", "")).thenReturn(userName)

        val preference = TestPreference(mockContext)

        // When
        preference.userName = userName
        val result = preference.userName

        // Then
        assertEquals("Property delegation으로 값을 읽을 수 있어야 합니다", userName, result)
    }

    @Test
    fun `Property_delegation으로_정수를_사용할_수_있다`() {
        // Given
        val age = 30
        `when`(mockSharedPreferences.getInt("user_age", 0)).thenReturn(age)

        val preference = TestPreference(mockContext)

        // When
        preference.userAge = age
        val result = preference.userAge

        // Then
        assertEquals("Property delegation으로 정수를 읽을 수 있어야 합니다", age, result)
    }

    @Test
    fun `Property_delegation으로_Boolean을_사용할_수_있다`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("is_premium", false)).thenReturn(true)

        val preference = TestPreference(mockContext)

        // When
        preference.isPremium = true
        val result = preference.isPremium

        // Then
        assertTrue("Property delegation으로 Boolean을 읽을 수 있어야 합니다", result)
    }

    // ========== 8. Commit (Coroutine) 테스트 ==========

    @Test
    fun `Commit으로_저장하면_true를_반환한다`() = runBlocking {
        // Given
        `when`(mockEditor.commit()).thenReturn(true)

        val preference = TestPreference(mockContext)

        // When
        val result = preference.testSaveCommit("test_key", "test_value")

        // Then
        assertTrue("Commit이 성공하면 true를 반환해야 합니다", result)
        verify(mockEditor).commit()
    }

    @Test
    fun `Commit이_실패하면_false를_반환한다`() = runBlocking {
        // Given
        `when`(mockEditor.commit()).thenReturn(false)

        val preference = TestPreference(mockContext)

        // When
        val result = preference.testSaveCommit("test_key", "test_value")

        // Then
        assertFalse("Commit이 실패하면 false를 반환해야 합니다", result)
    }

    // ========== 9. 실제 사용 시나리오 테스트 ==========

    @Test
    fun `로그인_정보를_저장하는_시나리오`() {
        // Given - 사용자가 로그인할 때
        val userId = 12345L
        val userName = "user@example.com"
        val isPremium = true

        `when`(mockSharedPreferences.getLong("user_id", 0L)).thenReturn(userId)
        `when`(mockSharedPreferences.getString("user_name", "")).thenReturn(userName)
        `when`(mockSharedPreferences.getBoolean("is_premium", false)).thenReturn(isPremium)

        val preference = TestPreference(mockContext)

        // When - 로그인 정보 저장
        preference.userId = userId
        preference.userName = userName
        preference.isPremium = isPremium

        // Then - 저장된 정보 확인
        assertEquals("사용자 ID가 저장되어야 합니다", userId, preference.userId)
        assertEquals("사용자 이름이 저장되어야 합니다", userName, preference.userName)
        assertTrue("프리미엄 상태가 저장되어야 합니다", preference.isPremium)
    }

    @Test
    fun `앱_설정을_저장하는_시나리오`() {
        // Given - 앱 설정 변경
        val fontSize = 16
        val isDarkMode = true
        val notificationEnabled = false

        `when`(mockSharedPreferences.getInt("font_size", 14)).thenReturn(fontSize)
        `when`(mockSharedPreferences.getBoolean("dark_mode", false)).thenReturn(isDarkMode)
        `when`(mockSharedPreferences.getBoolean("notification", true)).thenReturn(notificationEnabled)

        val preference = TestPreference(mockContext)

        // When - 설정 저장
        preference.testSaveApply("font_size", fontSize)
        preference.testSaveApply("dark_mode", isDarkMode)
        preference.testSaveApply("notification", notificationEnabled)

        // Then - 설정 확인
        assertEquals("폰트 크기가 저장되어야 합니다", fontSize,
            preference.testGetInt("font_size", 14))
        assertTrue("다크 모드가 저장되어야 합니다",
            preference.testGetBoolean("dark_mode", false))
        assertFalse("알림 설정이 저장되어야 합니다",
            preference.testGetBoolean("notification", true))
    }

    @Test
    fun `게임_점수를_저장하는_시나리오`() {
        // Given - 게임에서 최고 점수 저장
        val highScore = 9999
        val lastPlayTime = System.currentTimeMillis()
        val averageRating = 4.8f

        `when`(mockSharedPreferences.getInt("high_score", 0)).thenReturn(highScore)
        `when`(mockSharedPreferences.getLong("last_play", 0L)).thenReturn(lastPlayTime)
        `when`(mockSharedPreferences.getFloat("rating", 0.0f)).thenReturn(averageRating)

        val preference = TestPreference(mockContext)

        // When
        preference.testSaveApply("high_score", highScore)
        preference.testSaveApply("last_play", lastPlayTime)
        preference.testSaveApply("rating", averageRating)

        // Then
        assertEquals("최고 점수가 저장되어야 합니다", highScore,
            preference.testGetInt("high_score", 0))
        assertEquals("마지막 플레이 시간이 저장되어야 합니다", lastPlayTime,
            preference.testGetLong("last_play", 0L))
        assertEquals("평균 평점이 저장되어야 합니다", averageRating,
            preference.testGetFloat("rating", 0.0f), 0.01f)
    }

    // ========== 10. 기본값 처리 테스트 ==========

    @Test
    fun `모든_타입의_기본값이_올바르게_동작한다`() {
        // Given
        `when`(mockSharedPreferences.getString("str", "default")).thenReturn("default")
        `when`(mockSharedPreferences.getInt("int", -1)).thenReturn(-1)
        `when`(mockSharedPreferences.getBoolean("bool", true)).thenReturn(true)
        `when`(mockSharedPreferences.getLong("long", -1L)).thenReturn(-1L)
        `when`(mockSharedPreferences.getFloat("float", -1.0f)).thenReturn(-1.0f)

        val preference = TestPreference(mockContext)

        // When & Then
        assertEquals("String 기본값", "default", preference.testGetString("str", "default"))
        assertEquals("Int 기본값", -1, preference.testGetInt("int", -1))
        assertTrue("Boolean 기본값", preference.testGetBoolean("bool", true))
        assertEquals("Long 기본값", -1L, preference.testGetLong("long", -1L))
        assertEquals("Float 기본값", -1.0f, preference.testGetFloat("float", -1.0f), 0.001f)
    }
}
