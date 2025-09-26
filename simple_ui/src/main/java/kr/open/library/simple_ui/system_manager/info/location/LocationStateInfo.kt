package kr.open.library.simple_ui.system_manager.info.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.permissions.extentions.hasPermissions
import kr.open.library.simple_ui.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.system_manager.base.DataUpdate
import kr.open.library.simple_ui.system_manager.extensions.getLocationManager

public open class LocationStateInfo(
    context: Context,
) : BaseSystemService(context, listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {

    public val locationManager: LocationManager by lazy { context.getLocationManager() }

    private val msfUpdate: MutableStateFlow<LocationStateEvent> = MutableStateFlow(LocationStateEvent.OnGpsEnabled(isGpsEnabled()))
    public val sfUpdate: StateFlow<LocationStateEvent> = msfUpdate.asStateFlow()

    private val locationChanged     = DataUpdate<Location?>(getLocation())
    private val isGpsEnabled        = DataUpdate<Boolean>(isGpsEnabled())
    private val isNetworkEnabled    = DataUpdate<Boolean>(isNetworkEnabled())
    private val isPassiveEnabled    = DataUpdate<Boolean>(isPassiveEnabled())
    private val isFusedEnabled      = DataUpdate<Boolean>(checkSdkVersion(Build.VERSION_CODES.S, positiveWork = {isFusedEnabled()}, negativeWork = {false}))

    private var coroutineScope: CoroutineScope? = null


    /**
     * Sets up reactive flows for all location data updates
     */
    private fun setupDataFlows() {
        coroutineScope?.let { scope->
            scope.launch { locationChanged.state.collect { sendFlow(LocationStateEvent.OnLocationChanged(it)) } }
            scope.launch { isGpsEnabled.state.collect { sendFlow(LocationStateEvent.OnGpsEnabled(it)) }}
            scope.launch { isNetworkEnabled.state.collect { sendFlow(LocationStateEvent.OnNetworkEnabled(it)) } }
            scope.launch { isPassiveEnabled.state.collect { sendFlow(LocationStateEvent.OnPassiveEnabled(it)) } }
            scope.launch { isFusedEnabled.state.collect { sendFlow(LocationStateEvent.OnFusedEnabled(it)) } }
        }
    }


    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Logx.d("Location updated: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m")
            locationChanged.update(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Logx.d("Location status changed: provider=$provider, status=$status")
        }

        override fun onProviderEnabled(provider: String) {
            Logx.i("Location provider enabled: $provider")
            when (provider) {
                LocationManager.GPS_PROVIDER ->     isGpsEnabled.update(true)
                LocationManager.NETWORK_PROVIDER -> isNetworkEnabled.update(true)
                LocationManager.PASSIVE_PROVIDER -> isPassiveEnabled.update(true)
                LocationManager.FUSED_PROVIDER -> {
                    checkSdkVersion(Build.VERSION_CODES.S) { isFusedEnabled.update(true) }
                }
            }
        }

        override fun onProviderDisabled(provider: String) {
            Logx.i("Location provider disabled: $provider")
            when (provider) {
                LocationManager.GPS_PROVIDER ->     isGpsEnabled.update(false)
                LocationManager.NETWORK_PROVIDER -> isNetworkEnabled.update(false)
                LocationManager.PASSIVE_PROVIDER -> isPassiveEnabled.update(false)
                LocationManager.FUSED_PROVIDER -> {
                    checkSdkVersion(Build.VERSION_CODES.S) { isFusedEnabled.update(false) }
                }
            }
        }
    }

    private fun sendFlow(event: LocationStateEvent) = coroutineScope?.launch { msfUpdate.emit(event) }

    /**
     * This is needed because of TelephonyCallback.CellInfoListener(Telephony.registerCallBack)
     * or
     * PhoneStateListener.LISTEN_CELL_INFO(Telephony.registerListen).
     */
    private var gpsStateBroadcastReceiver : BroadcastReceiver?=null

    private val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    public fun registerStart(coroutineScope: CoroutineScope, locationProvider: String, minTimeMs: Long, minDistanceM: Float) {
        if(registerLocation()) {
            this.coroutineScope = coroutineScope
            registerLocationUpdateStart(locationProvider, minTimeMs, minDistanceM)
            setupDataFlows()
        } else {

        }
    }

    private fun registerLocation() :Boolean = tryCatchSystemManager(false) {
        unregisterGpsState()
        gpsStateBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                    isGpsEnabled.update(isGpsEnabled())
                    isNetworkEnabled.update(isNetworkEnabled())
                    isPassiveEnabled.update(isPassiveEnabled())
                    checkSdkVersion(Build.VERSION_CODES.S) { isFusedEnabled.update(isFusedEnabled()) }
                }
            }
        }
        context.registerReceiver(gpsStateBroadcastReceiver, intentFilter)
        true
    }

    /**
     *
     */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    private fun registerLocationUpdateStart(locationProvider: String, minTimeMs: Long, minDistanceM: Float) {
        locationManager.requestLocationUpdates(locationProvider, minTimeMs, minDistanceM, locationListener)
    }

    public override fun onDestroy() {
        unregister()
    }

    private fun unregisterLocationUpdateListener() {
        safeCatch { locationManager.removeUpdates(locationListener) }
    }

    private fun unregisterGpsState() {
        gpsStateBroadcastReceiver?.let { safeCatch { context.unregisterReceiver(it) } }
        gpsStateBroadcastReceiver = null
    }


    public fun isLocationEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    public fun isGpsEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    public fun isNetworkEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    public fun isPassiveEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)

    @RequiresApi(Build.VERSION_CODES.S)
    public fun isFusedEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)


    public fun isAnyEnabled(): Boolean {
        return checkSdkVersion(Build.VERSION_CODES.S,
            positiveWork = {
                (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled() || isFusedEnabled())
            },
            negativeWork = {
                (isLocationEnabled() || isGpsEnabled() || isNetworkEnabled() || isPassiveEnabled())
            }
        )
    }

    @SuppressLint("MissingPermission")
    public fun getLocation(): Location? {
        Logx.d("isAnyEnabled() ${isAnyEnabled()} ${context.hasPermissions(ACCESS_COARSE_LOCATION)}, ${context.hasPermissions(ACCESS_FINE_LOCATION)}")
        return if (!isAnyEnabled()) {
            checkSdkVersion(Build.VERSION_CODES.S,
                positiveWork = {
                    Logx.e("can not find location!, isLocationEnabled ${isLocationEnabled()}, isGpsEnabled ${isGpsEnabled()}, isNetworkEnabled ${isNetworkEnabled()}, isPassiveEnabled ${isPassiveEnabled()}, isFusedEnabled ${isFusedEnabled()}")
                },
                negativeWork = {
                    Logx.e("can not find location!, isLocationEnabled ${isLocationEnabled()}, isGpsEnabled ${isGpsEnabled()}, isNetworkEnabled ${isNetworkEnabled()}, isPassiveEnabled ${isPassiveEnabled()}")
                }
            )
            null
        } else if (context.hasPermissions(ACCESS_COARSE_LOCATION)
            || context.hasPermissions(ACCESS_FINE_LOCATION)) {
             locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else {
            Logx.e("can not find location!, ACCESS_COARSE_LOCATION ${context.hasPermissions(ACCESS_COARSE_LOCATION)}, ACCESS_FINE_LOCATION  ${context.hasPermissions(ACCESS_FINE_LOCATION)}")
            null
        }
    }

    public fun calculateDistance(fromLocation: Location, toLocation: Location): Float =
        fromLocation.distanceTo(toLocation)

    public fun calculateBearing(fromLocation: Location, toLocation: Location): Float =
        fromLocation.bearingTo(toLocation)

    public fun isLocationWithRadius(fromLocation: Location, toLocation: Location, radius: Float): Boolean =
        calculateDistance(fromLocation, toLocation) <= radius
    private val locationStorage by lazy { LocationSharedPreference(context) }

    public fun loadLocation(): Location? = locationStorage.loadLocation()
    public fun saveApplyLocation(location: Location) { locationStorage.saveApplyLocation(location) }
    public fun removeLocation() { locationStorage.removeApply() }

    public fun unregister() {
        unregisterGpsState()
        unregisterLocationUpdateListener()
        coroutineScope?.cancel()
        coroutineScope = null
    }
}