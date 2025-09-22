package kr.open.library.simple_ui.presenter.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class with event handling capability.
 * This class provides a communication channel from ViewModel to View
 * using Kotlin Flows.
 *
 * @param EVENT_TYPE The type of events this ViewModel can emit
 */
public abstract class BaseViewModelEvent<EVENT_TYPE> : BaseViewModel() {

    /****************************************************************
     *                                                              *
     * Using for ViewModel -> View event                            *
     * (Activity.class, Fragment.class, CustomView.class)           *
     *                                                              *
     ****************************************************************/
    private val eventVm = Channel<EVENT_TYPE>(Channel.BUFFERED)
    public val mEventVm: Flow<EVENT_TYPE> = eventVm.receiveAsFlow()

    protected fun sendEventVm(event: EVENT_TYPE) { viewModelScope.launch { eventVm.send(event) } }


    override fun onCleared() {
        super.onCleared()
        // Close the event channel to prevent memory leaks
        eventVm.close()
    }
}