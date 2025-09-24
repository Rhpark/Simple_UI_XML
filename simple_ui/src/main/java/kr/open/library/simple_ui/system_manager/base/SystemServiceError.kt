package kr.open.library.simple_ui.system_manager.base

/**
 * Base sealed class for all system service errors.
 * 모든 시스템 서비스 오류의 기본 sealed 클래스입니다.
 * 
 * This provides a hierarchical error system that allows for:
 * 이는 다음을 가능하게 하는 계층적 오류 시스템을 제공합니다:
 * - Type-safe error handling / 타입 안전 오류 처리
 * - Common error types across all services / 모든 서비스 간 공통 오류 타입
 * - Service-specific error types / 서비스별 특정 오류 타입
 * - Consistent error categorization / 일관된 오류 분류
 */
public sealed class SystemServiceError {
    
    /**
     * Permission-related errors that are common across all system services.
     * 모든 시스템 서비스에서 공통적인 권한 관련 오류입니다.
     */
    public sealed class Permission : SystemServiceError() {
        /** Required permission is not granted / 필요한 권한이 부여되지 않음 */
        public data class NotGranted(val permissions: List<String>) : Permission()
        
        /** Permission was previously granted but revoked / 이전에 부여된 권한이 철회됨 */
        public data class Revoked(val permission: String) : Permission()
        
        /** Special permission required (e.g., SYSTEM_ALERT_WINDOW) / 특별 권한 필요 */
        public data class SpecialPermissionRequired(val permission: String, val settingsAction: String? = null) : Permission()
        
        /** Permission restricted by device policy / 기기 정책에 의해 권한 제한 */
        public data class PolicyRestricted(val permission: String, val reason: String) : Permission()
    }
    
    /**
     * System service availability errors.
     * 시스템 서비스 가용성 오류입니다.
     */
    public sealed class SystemService : SystemServiceError() {
        /** System service is not available / 시스템 서비스 사용 불가 */
        public data class Unavailable(val serviceName: String, val cause: Throwable? = null) : SystemService()
        
        /** System service is temporarily busy / 시스템 서비스가 일시적으로 사용 중 */
        public data class Busy(val serviceName: String, val retryAfterMs: Long? = null) : SystemService()
        
        /** System service version/API not supported / 시스템 서비스 버전/API 미지원 */
        public data class UnsupportedVersion(val serviceName: String, val requiredApi: Int, val currentApi: Int) : SystemService()
    }
    
    /**
     * Hardware capability errors.
     * 하드웨어 기능 오류입니다.
     */
    public sealed class Hardware : SystemServiceError() {
        /** Required hardware feature not available / 필요한 하드웨어 기능 없음 */
        public data class FeatureNotSupported(val feature: String) : Hardware()
        
        /** Hardware is present but not functioning / 하드웨어는 있지만 작동하지 않음 */
        public data class Malfunction(val component: String, val details: String? = null) : Hardware()
        
        /** Hardware is disabled by user or policy / 사용자 또는 정책에 의해 하드웨어 비활성화 */
        public data class Disabled(val component: String, val reason: String) : Hardware()
    }
    
    /**
     * Configuration and validation errors.
     * 구성 및 유효성 검사 오류입니다.
     */
    public sealed class Configuration : SystemServiceError() {
        /** Invalid parameter or configuration / 잘못된 매개변수 또는 구성 */
        public data class InvalidParameter(val parameterName: String, val value: Any?, val reason: String) : Configuration()
        
        /** Required configuration is missing / 필요한 구성이 누락됨 */
        public data class MissingConfiguration(val configKey: String) : Configuration()
        
        /** Configuration conflict detected / 구성 충돌 감지 */
        public data class Conflict(val conflictingKeys: List<String>, val reason: String) : Configuration()
    }
    
    /**
     * Resource-related errors.
     * 리소스 관련 오류입니다.
     */
    public sealed class Resource : SystemServiceError() {
        /** System resource not found / 시스템 리소스를 찾을 수 없음 */
        public data class NotFound(val resourceType: String, val identifier: String) : Resource()
        
        /** Insufficient system resources / 시스템 리소스 부족 */
        public data class Insufficient(val resourceType: String, val required: String, val available: String) : Resource()
        
        /** Resource is locked or in use by another process / 리소스가 잠겨있거나 다른 프로세스에서 사용 중 */
        public data class Locked(val resourceType: String, val lockHolder: String? = null) : Resource()
    }
    
    /**
     * State-related errors.
     * 상태 관련 오류입니다.
     */
    public sealed class State : SystemServiceError() {
        /** Operation not allowed in current state / 현재 상태에서 작업 허용되지 않음 */
        public data class InvalidState(val currentState: String, val requiredState: String, val operation: String) : State()
        
        /** Resource already exists / 리소스가 이미 존재함 */
        public data class AlreadyExists(val resourceType: String, val identifier: String) : State()
        
        /** Operation timed out / 작업 시간 초과 */
        public data class Timeout(val operation: String, val timeoutMs: Long) : State()
    }
    
    /**
     * Network and connectivity errors.
     * 네트워크 및 연결 오류입니다.
     */
    public sealed class Network : SystemServiceError() {
        /** No network connectivity / 네트워크 연결 없음 */
        public object NotConnected : Network()
        
        /** Limited connectivity (e.g., captive portal) / 제한된 연결 (예: 캡티브 포털) */
        public data class LimitedConnectivity(val reason: String) : Network()
        
        /** Network operation failed / 네트워크 작업 실패 */
        public data class OperationFailed(val operation: String, val cause: Throwable) : Network()
    }
    
    /**
     * Security-related errors.
     * 보안 관련 오류입니다.
     */
    public sealed class Security : SystemServiceError() {
        /** Operation blocked by security policy / 보안 정책에 의해 작업 차단 */
        public data class PolicyViolation(val policy: String, val operation: String) : Security()
        
        /** Authentication required / 인증 필요 */
        public data class AuthenticationRequired(val authenticationType: String) : Security()
        
        /** Access denied / 접근 거부 */
        public data class AccessDenied(val resource: String, val reason: String) : Security()
    }
    
    /**
     * Unknown or unexpected errors.
     * 알 수 없거나 예상치 못한 오류입니다.
     */
    public sealed class Unknown : SystemServiceError() {
        /** Unexpected exception occurred / 예상치 못한 예외 발생 */
        public data class Exception(val cause: Throwable, val context: String? = null) : Unknown()
        
        /** Unexpected system behavior / 예상치 못한 시스템 동작 */
        public data class UnexpectedBehavior(val description: String, val context: String? = null) : Unknown()
    }
    
    /**
     * Location service specific errors.
     * 위치 서비스별 특정 오류입니다.
     */
    public sealed class Location : SystemServiceError() {
        /** Location provider is not available / 위치 제공자를 사용할 수 없음 */
        public data class ProviderNotAvailable(val provider: String) : Location()
        
        /** Location provider is disabled / 위치 제공자가 비활성화됨 */
        public data class ProviderDisabled(val provider: String) : Location()
        
        /** Location updates failed to start / 위치 업데이트 시작 실패 */
        public data class UpdateStartFailed(val provider: String, val reason: String) : Location()
        
        /** Location calculation failed / 위치 계산 실패 */
        public data class CalculationFailed(val operation: String, val reason: String) : Location()
        
        /** GPS timeout / GPS 시간 초과 */
        public data class GpsTimeout(val timeoutMs: Long) : Location()
        
        /** Location accuracy insufficient / 위치 정확도 부충분 */
        public data class InsufficientAccuracy(val requiredAccuracy: Float, val actualAccuracy: Float) : Location()
    }
    
    /**
     * Bluetooth/BLE service specific errors.
     * Bluetooth/BLE 서비스별 특정 오류입니다.
     */
    public sealed class Bluetooth : SystemServiceError() {
        /** BLE is not supported on this device / 이 기기에서 BLE가 지원되지 않음 */
        public object BleNotSupported : Bluetooth()
        
        /** Bluetooth adapter is not available / Bluetooth 어댑터를 사용할 수 없음 */
        public object AdapterNotAvailable : Bluetooth()
        
        /** Bluetooth is turned off / Bluetooth가 꺼져 있음 */
        public object BluetoothOff : Bluetooth()
        
        /** BLE scanning failed / BLE 스캐닝 실패 */
        public data class ScanFailed(val errorCode: Int, val reason: String) : Bluetooth()
        
        /** BLE advertising failed / BLE 광고 실패 */
        public data class AdvertiseFailed(val errorCode: Int, val reason: String) : Bluetooth()
        
        /** BLE connection failed / BLE 연결 실패 */
        public data class ConnectionFailed(val deviceAddress: String, val status: Int) : Bluetooth()
        
        /** GATT operation failed / GATT 작업 실패 */
        public data class GattOperationFailed(val operation: String, val status: Int, val deviceAddress: String) : Bluetooth()
    }
}

/**
 * Extension functions for common error operations.
 * 공통 오류 작업을 위한 확장 함수들입니다.
 */

/**
 * Checks if this error is related to permissions.
 * 이 오류가 권한과 관련된 것인지 확인합니다.
 */
public fun SystemServiceError.isPermissionError(): Boolean = this is SystemServiceError.Permission

/**
 * Checks if this error is recoverable (user can take action).
 * 이 오류가 복구 가능한지(사용자가 조치를 취할 수 있는지) 확인합니다.
 */
public fun SystemServiceError.isRecoverable(): Boolean = when (this) {
    is SystemServiceError.Permission -> true
    is SystemServiceError.Hardware.Disabled -> true
    is SystemServiceError.Configuration -> true
    is SystemServiceError.Network -> true
    is SystemServiceError.Security.AuthenticationRequired -> true
    else -> false
}

/**
 * Gets a user-friendly error message.
 * 사용자 친화적 오류 메시지를 가져옵니다.
 */
public fun SystemServiceError.getUserMessage(): String = when (this) {
    is SystemServiceError.Permission.NotGranted -> 
        "권한이 필요합니다: ${permissions.joinToString(", ")}"
    is SystemServiceError.SystemService.Unavailable -> 
        "시스템 서비스($serviceName)를 사용할 수 없습니다"
    is SystemServiceError.Hardware.FeatureNotSupported -> 
        "이 기기는 $feature 기능을 지원하지 않습니다"
    is SystemServiceError.Network.NotConnected -> 
        "네트워크에 연결되어 있지 않습니다"
    is SystemServiceError.Location.ProviderNotAvailable -> 
        "위치 제공자(${provider})를 사용할 수 없습니다"
    is SystemServiceError.Location.ProviderDisabled -> 
        "위치 서비스(${provider})가 비활성화되어 있습니다"
    is SystemServiceError.Location.GpsTimeout -> 
        "GPS 신호를 찾을 수 없습니다 (${timeoutMs}ms 초과)"
    else -> "시스템 오류가 발생했습니다"
}

/**
 * Gets a developer-friendly error message with details.
 * 상세한 개발자 친화적 오류 메시지를 가져옵니다.
 */
public fun SystemServiceError.getDeveloperMessage(): String = when (this) {
    is SystemServiceError.Permission.NotGranted -> 
        "Permissions not granted: ${permissions.joinToString(", ")}"
    is SystemServiceError.SystemService.Unavailable -> 
        "System service '$serviceName' unavailable${cause?.let { ": ${it.message}" } ?: ""}"
    is SystemServiceError.Hardware.FeatureNotSupported -> 
        "Hardware feature '$feature' not supported on this device"
    is SystemServiceError.Configuration.InvalidParameter -> 
        "Invalid parameter '$parameterName' = $value: $reason"
    is SystemServiceError.Location.ProviderNotAvailable -> 
        "Location provider '${provider}' not available on this device"
    is SystemServiceError.Location.UpdateStartFailed -> 
        "Failed to start location updates for provider '${provider}': ${reason}"
    is SystemServiceError.Location.CalculationFailed -> 
        "Location calculation failed for operation '${operation}': ${reason}"
    is SystemServiceError.Location.InsufficientAccuracy -> 
        "Location accuracy insufficient: required ${requiredAccuracy}m, actual ${actualAccuracy}m"
    is SystemServiceError.Unknown.Exception -> 
        "Unexpected exception${context?.let { " in $it" } ?: ""}: ${cause.message}"
    else -> toString()
}

/**
 * Exception wrapper for SystemServiceError to be used with Result.failure().
 * SystemServiceError를 Result.failure()와 함께 사용하기 위한 Exception 래퍼입니다.
 */
public class SystemServiceException(
    public val error: SystemServiceError,
    cause: Throwable? = null
) : Exception(error.getUserMessage(), cause) {
    
    /**
     * Gets the developer message from the wrapped error.
     * 래핑된 오류에서 개발자 메시지를 가져옵니다.
     */
    public fun getDeveloperMessage(): String = error.getDeveloperMessage()
    
    /**
     * Gets the user message from the wrapped error.
     * 래핑된 오류에서 사용자 메시지를 가져옵니다.
     */
    public fun getUserMessage(): String = error.getUserMessage()
    
    /**
     * Checks if this exception wraps a permission error.
     * 이 예외가 권한 오류를 래핑하는지 확인합니다.
     */
    public fun isPermissionError(): Boolean = error.isPermissionError()
    
    /**
     * Checks if this exception wraps a recoverable error.
     * 이 예외가 복구 가능한 오류를 래핑하는지 확인합니다.
     */
    public fun isRecoverable(): Boolean = error.isRecoverable()
}


