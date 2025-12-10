package kr.open.library.simple_ui.core.system_manager.info.location

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.local.base.BaseSharedPreference

/**
 * SharedPreferences wrapper for storing and retrieving Location objects.<br><br>
 * 위치 정보를 저장·조회하기 위한 SharedPreferences 래퍼입니다.<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트입니다.<br>
 */
public class LocationSharedPreference(
    private val context: Context,
) : BaseSharedPreference(context, "location_preferences") {
    /**
     * Preference delegate for latitude.<br><br>
     * 위도 저장을 위한 Preference 위임자입니다.<br>
     */
    private var prefLat by doublePref(LATITUDE_SUFFIX, 0.0)

    /**
     * Preference delegate for longitude.<br><br>
     * 경도 저장을 위한 Preference 위임자입니다.<br>
     */
    private var prefLon by doublePref(LONGITUDE_SUFFIX, 0.0)

    /**
     * Preference delegate for accuracy.<br><br>
     * 정확도 저장을 위한 Preference 위임자입니다.<br>
     */
    private var prefAccuracy by floatPref(ACCURACY_SUFFIX, 0.0f)

    /**
     * Preference delegate for time.<br><br>
     * 시간 저장을 위한 Preference 위임자입니다.<br>
     */
    private var prefTime by longPref(TIME_SUFFIX, 0L)

    /**
     * Preference delegate for provider.<br><br>
     * 제공자 저장을 위한 Preference 위임자입니다.<br>
     */
    private var prefProvider by stringPref(PROVIDER_SUFFIX, null)

    private companion object {
        /**
         * Name of the SharedPreferences file.<br><br>
         * SharedPreferences 파일 이름입니다.<br>
         */
        private const val PREF_NAME = "location_preferences"

        /**
         * Key suffix for latitude.<br><br>
         * 위도 키 접미사입니다.<br>
         */
        private const val LATITUDE_SUFFIX = "_latitude"

        /**
         * Key suffix for longitude.<br><br>
         * 경도 키 접미사입니다.<br>
         */
        private const val LONGITUDE_SUFFIX = "_longitude"

        /**
         * Key suffix for accuracy.<br><br>
         * 정확도 키 접미사입니다.<br>
         */
        private const val ACCURACY_SUFFIX = "_accuracy"

        /**
         * Key suffix for time.<br><br>
         * 시간 키 접미사입니다.<br>
         */
        private const val TIME_SUFFIX = "_time"

        /**
         * Key suffix for provider.<br><br>
         * 제공자 키 접미사입니다.<br>
         */
        private const val PROVIDER_SUFFIX = "_provider"
    }

    /**
     * Lazy-initialized SharedPreferences instance.<br><br>
     * 지연 초기화된 SharedPreferences 인스턴스입니다.<br>
     */
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves the location to SharedPreferences.<br><br>
     * 위치를 SharedPreferences에 저장합니다.<br>
     *
     * @param location The location to save.<br><br>
     *                 저장할 위치 객체입니다.
     */
    public fun saveApplyLocation(location: Location) {
        prefLat = location.latitude
        prefLon = location.longitude
        prefAccuracy = location.accuracy
        prefTime = location.time
        prefProvider = location.provider
    }

    /**
     * Loads the saved location from SharedPreferences.<br><br>
     * SharedPreferences에 저장된 위치를 불러옵니다.<br>
     *
     * @return The saved Location object, or `null` if no location is saved.<br><br>
     *         저장된 위치가 있으면 Location 객체를, 없으면 `null`을 반환합니다.<br>
     */
    public fun loadLocation(): Location? = safeCatch(defaultValue = null) {
        if (prefTime == 0L) {
            null
        } else {
            prefProvider?.let {
                Location(it).apply {
                    latitude = prefLat
                    longitude = prefLon
                    accuracy = prefAccuracy
                    time = prefTime
                }
            }
        }
    }

    /**
     * Removes all saved location data from SharedPreferences.<br><br>
     * SharedPreferences에 저장된 모든 위치 데이터를 삭제합니다.<br>
     */
    public fun removeApply() {
        removeAllApply()
    }
}
