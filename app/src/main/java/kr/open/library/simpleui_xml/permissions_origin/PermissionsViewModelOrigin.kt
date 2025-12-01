package kr.open.library.simpleui_xml.permissions_origin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PermissionsViewModelOrigin : ViewModel() {
    // Event Flow (ViewModel -> View)
    private val _events = Channel<PermissionEvent>(Channel.BUFFERED)
    val events: Flow<PermissionEvent> = _events.receiveAsFlow()

    // Permission results state
    private val _permissionResults = MutableStateFlow<List<String>>(emptyList())
    val permissionResults: StateFlow<List<String>> = _permissionResults.asStateFlow()

    fun onClickCameraPermission() {
        viewModelScope.launch { _events.send(PermissionEvent.OnClickCameraPermission) }
    }

    fun onClickLocationPermission() {
        viewModelScope.launch { _events.send(PermissionEvent.OnClickLocationPermission) }
    }

    fun onClickMultiplePermissions() {
        viewModelScope.launch { _events.send(PermissionEvent.OnClickMultiplePermissions) }
    }

    fun addPermissionResult(result: String) {
        val currentResults = _permissionResults.value.toMutableList()
        currentResults.add(result)
        _permissionResults.value = currentResults
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}
