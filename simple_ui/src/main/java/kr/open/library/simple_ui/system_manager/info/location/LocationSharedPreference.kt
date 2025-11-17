package kr.open.library.simple_ui.system_manager.info.location

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.local.base.BaseSharedPreference

/**
 * SharedPreferences wrapper for storing and retrieving Location objects.
 * Location 객체를 저장하고 검색하기 위한 SharedPreferences 래퍼입니다.
 */
public class LocationSharedPreference(private val context: Context) :
    BaseSharedPreference(context, "location_preferences") {

    private var prefLat by doublePref(LATITUDE_SUFFIX, 0.0)
    private var prefLon by doublePref(LONGITUDE_SUFFIX, 0.0)
    private var prefAccuracy by floatPref(ACCURACY_SUFFIX, 0.0f)
    private var prefTime by longPref(TIME_SUFFIX, 0L)
    private var prefProvider by stringPref(PROVIDER_SUFFIX, null)

    private companion object {
        private const val PREF_NAME = "location_preferences"
        private const val LATITUDE_SUFFIX = "_latitude"
        private const val LONGITUDE_SUFFIX = "_longitude"
        private const val ACCURACY_SUFFIX = "_accuracy"
        private const val TIME_SUFFIX = "_time"
        private const val PROVIDER_SUFFIX = "_provider"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    public fun saveApplyLocation(location: Location) {
        prefLat = location.latitude
        prefLon = location.longitude
        prefAccuracy = location.accuracy
        prefTime = location.time
        prefProvider = location.provider
    }


    public fun loadLocation(): Location? = safeCatch(defaultValue = null) {

        if(prefTime == 0L) { null }
        else {
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

    public fun removeApply() { removeAllApply() }
}