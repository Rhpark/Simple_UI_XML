package kr.open.library.simpleui_xml.system_service_manager.controller.notification

import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

sealed class NotificationControllerActivityVmEvent {
    object ShowNotification : NotificationControllerActivityVmEvent()

    object ShowBigTextNotification : NotificationControllerActivityVmEvent()

    object ShowProgress : NotificationControllerActivityVmEvent()

    object UpdateProgress : NotificationControllerActivityVmEvent()

    object CompleteProgress : NotificationControllerActivityVmEvent()

    object CancelNotification : NotificationControllerActivityVmEvent()

    object CancelAll : NotificationControllerActivityVmEvent()
}

class NotificationControllerActivityVm : BaseViewModelEvent<NotificationControllerActivityVmEvent>() {
    fun onClickShowNotification() = sendEventVm(NotificationControllerActivityVmEvent.ShowNotification)

    fun onClickShowBigTextNotification() = sendEventVm(NotificationControllerActivityVmEvent.ShowBigTextNotification)

    fun onClickShowProgress() = sendEventVm(NotificationControllerActivityVmEvent.ShowProgress)

    fun onClickUpdateProgress() = sendEventVm(NotificationControllerActivityVmEvent.UpdateProgress)

    fun onClickCompleteProgress() = sendEventVm(NotificationControllerActivityVmEvent.CompleteProgress)

    fun onClickCancelNotification() = sendEventVm(NotificationControllerActivityVmEvent.CancelNotification)

    fun onClickCancelAll() = sendEventVm(NotificationControllerActivityVmEvent.CancelAll)
}
