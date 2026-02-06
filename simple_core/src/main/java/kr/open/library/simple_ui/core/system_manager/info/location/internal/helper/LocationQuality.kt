package kr.open.library.simple_ui.core.system_manager.info.location.internal.helper

import android.location.Location
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.SIGNIFICANT_ACCURACY_DELTA_METERS
import kr.open.library.simple_ui.core.system_manager.info.location.LocationStateConstants.SIGNIFICANT_TIME_DELTA_MS

/**
 * Utility object for deciding whether a candidate location is better than current best.<br><br>
 * 후보 위치가 현재 최적 위치보다 더 나은지 판별하는 유틸 객체입니다.<br>
 */
internal object LocationQuality {
    /**
     * Determines whether [newLoc] is better than [currentBest] using recency/accuracy heuristics.<br><br>
     * 최신성/정확도 휴리스틱을 사용해 [newLoc]가 [currentBest]보다 나은지 판단합니다.<br>
     *
     * @param newLoc Candidate location to compare.<br><br>
     *               비교할 후보 위치입니다.<br>
     *
     * @param currentBest Current best location; may be `null`.<br><br>
     *                    현재 최적 위치이며 `null`일 수 있습니다.<br>
     *
     * @return `true` if [newLoc] is considered better; otherwise `false`.<br><br>
     *         [newLoc]가 더 낫다고 판단되면 `true`, 아니면 `false`입니다.<br>
     */
    internal fun isBetter(newLoc: Location, currentBest: Location?): Boolean {
        if (currentBest == null) return true

        val timeDelta = newLoc.time - currentBest.time
        val isSignificantlyNewer = timeDelta > SIGNIFICANT_TIME_DELTA_MS
        val isSignificantlyOlder = timeDelta < -SIGNIFICANT_TIME_DELTA_MS
        val isNewer = timeDelta > 0

        if (isSignificantlyNewer) return true
        if (isSignificantlyOlder) return false

        val accuracyDelta = (newLoc.accuracy - currentBest.accuracy).toInt()
        val isMoreAccurate = accuracyDelta < 0
        val isLessAccurate = accuracyDelta > 0
        val isSignificantlyLessAccurate = accuracyDelta > SIGNIFICANT_ACCURACY_DELTA_METERS

        val isFromSameProvider = newLoc.provider == currentBest.provider

        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }
}
