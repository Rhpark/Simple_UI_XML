package kr.open.library.simple_ui.core.extensions.date

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

/**
 * Converts a timestamp in milliseconds to a formatted string using the specified pattern and locale.<br><br>
 * 밀리초 단위의 타임스탬프를 지정된 패턴과 로케일을 사용하여 포맷된 문자열로 변환합니다.<br>
 *
 * @param format The date-time pattern (e.g., "HH:mm:ss dd/MM/yyyy").<br><br>
 *               날짜-시간 패턴 (예: "HH:mm:ss dd/MM/yyyy").<br>
 *
 * @param locale The locale to use for formatting. Defaults to the system default locale.<br><br>
 *               포맷팅에 사용할 로케일. 기본값은 시스템 기본 로케일입니다.<br>
 *
 * @return A formatted date-time string.<br><br>
 *         포맷된 날짜-시간 문자열.<br>
 */
public fun Long.toDateString(
    format: String,
    locale: Locale = Locale.getDefault(),
): String = SimpleDateFormat(format, locale).format(Date(this))

/**
 * Parses a date-time string using the specified pattern and locale, and returns the timestamp in milliseconds.<br><br>
 * 지정된 패턴과 로케일을 사용하여 날짜-시간 문자열을 파싱하고, 밀리초 단위의 타임스탬프를 반환합니다.<br>
 *
 * @param format The date-time pattern to use for parsing (e.g., "HH:mm:ss dd/MM/yyyy").<br><br>
 *               파싱에 사용할 날짜-시간 패턴 (예: "HH:mm:ss dd/MM/yyyy").<br>
 *
 * @param locale The locale to use for parsing. Defaults to the system default locale.<br><br>
 *               파싱에 사용할 로케일. 기본값은 시스템 기본 로케일입니다.<br>
 *
 * @return The timestamp in milliseconds since epoch.<br><br>
 *         에포크 이후의 밀리초 단위 타임스탬프.<br>
 *
 * @throws IllegalArgumentException if the string cannot be parsed or doesn't match the format completely.<br><br>
 *                                  문자열을 파싱할 수 없거나 형식과 완전히 일치하지 않을 경우.<br>
 */
public fun String.toDateLong(
    format: String,
    locale: Locale = Locale.getDefault(),
): Long {
    val formatter = SimpleDateFormat(format, locale)
    val parsePosition = ParsePosition(0)

    val parsedDate =
        formatter.parse(this, parsePosition)
            ?: throw IllegalArgumentException("Invalid time string")

    if (parsePosition.index != length) {
        throw IllegalArgumentException("Invalid time string")
    }

    return parsedDate.time
}

/**
 * Parses a date-time string using the specified pattern and locale, and returns a Date object.<br><br>
 * 지정된 패턴과 로케일을 사용하여 날짜-시간 문자열을 파싱하고, Date 객체를 반환합니다.<br>
 *
 * @param format The date-time pattern to use for parsing (e.g., "HH:mm:ss dd/MM/yyyy").<br><br>
 *               파싱에 사용할 날짜-시간 패턴 (예: "HH:mm:ss dd/MM/yyyy").<br>
 *
 * @param locale The locale to use for parsing. Defaults to the system default locale.<br><br>
 *               파싱에 사용할 로케일. 기본값은 시스템 기본 로케일입니다.<br>
 *
 * @return A Date object if parsing succeeds, or null if parsing fails.<br><br>
 *         파싱이 성공하면 Date 객체, 실패하면 null.<br>
 */
public fun String.toDate(
    format: String,
    locale: Locale = Locale.getDefault(),
): Date? =
    try {
        SimpleDateFormat(format, locale).parse(this)
    } catch (e: Exception) {
        null
    }

/**
 * Converts a timestamp in milliseconds to a LocalDateTime using the system default time zone.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 시스템 기본 시간대를 사용하여 밀리초 단위의 타임스탬프를 LocalDateTime으로 변환합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @return A LocalDateTime object representing the timestamp in the system default time zone.<br><br>
 *         시스템 기본 시간대에서 타임스탬프를 나타내는 LocalDateTime 객체.<br>
 *
 * Example usage:
 * ```
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 *     val nowMillis = System.currentTimeMillis()
 *     val localDateTime = nowMillis.toLocalDateTime()
 *     val formatted = localDateTime.format("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
 *     Logx.d("Formatted LocalDateTime: $formatted")
 * }
 * ```
 */
public fun Long.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

/**
 * Formats a LocalDateTime to a string using the specified pattern and locale.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 지정된 패턴과 로케일을 사용하여 LocalDateTime을 문자열로 포맷합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param pattern The date-time pattern to use for formatting (e.g., "yyyy-MM-dd HH:mm:ss").<br><br>
 *               포맷팅에 사용할 날짜-시간 패턴 (예: "yyyy-MM-dd HH:mm:ss").<br>
 *
 * @param locale The locale to use for formatting. Defaults to the system default locale.<br><br>
 *               포맷팅에 사용할 로케일. 기본값은 시스템 기본 로케일입니다.<br>
 *
 * @return A formatted date-time string.<br><br>
 *         포맷된 날짜-시간 문자열.<br>
 */
public fun LocalDateTime.format(
    pattern: String,
    locale: Locale = Locale.getDefault(),
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, locale)
    return this.format(formatter)
}

/**
 * Converts a Date object to a LocalDateTime using the system default time zone.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 시스템 기본 시간대를 사용하여 Date 객체를 LocalDateTime으로 변환합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @return A LocalDateTime object representing the Date in the system default time zone.<br><br>
 *         시스템 기본 시간대에서 Date를 나타내는 LocalDateTime 객체.<br>
 *
 * Example usage:
 * ```
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 *     val date = Date()
 *     val formatted = date.formatToString("yyyy-MM-dd HH:mm:ss")
 *     Logx.d("Formatted Date: $formatted")
 * }
 * ```
 */
public fun Date.toLocalDateTime(): LocalDateTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

/**
 * Formats a Date object to a string using the specified pattern and locale via LocalDateTime.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * LocalDateTime을 통해 지정된 패턴과 로케일을 사용하여 Date 객체를 문자열로 포맷합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param pattern The date-time pattern to use for formatting (e.g., "yyyy-MM-dd HH:mm:ss").<br><br>
 *               포맷팅에 사용할 날짜-시간 패턴 (예: "yyyy-MM-dd HH:mm:ss").<br>
 *
 * @param locale The locale to use for formatting. Defaults to the system default locale.<br><br>
 *               포맷팅에 사용할 로케일. 기본값은 시스템 기본 로케일입니다.<br>
 *
 * @return A formatted date-time string.<br><br>
 *         포맷된 날짜-시간 문자열.<br>
 */
public fun Date.toStringFormat(
    pattern: String,
    locale: Locale = Locale.getDefault(),
): String = this.toLocalDateTime().format(pattern, locale)

/**
 * Formats a Date object to a string using SimpleDateFormat with the specified pattern and locale.<br>
 * Works on all API levels.<br><br>
 * SimpleDateFormat을 사용하여 지정된 패턴과 로케일로 Date 객체를 문자열로 포맷합니다.<br>
 * 모든 API 레벨에서 작동합니다.<br>
 *
 * @param format The date-time pattern to use for formatting (e.g., "HH:mm:ss dd/MM/yyyy").<br><br>
 *               포맷팅에 사용할 날짜-시간 패턴 (예: "HH:mm:ss dd/MM/yyyy").<br>
 *
 * @param locale The locale to use for formatting. Defaults to the system default locale.<br><br>
 *               포맷팅에 사용할 로케일. 기본값은 시스템 기본 로케일입니다.<br>
 *
 * @return A formatted date-time string.<br><br>
 *         포맷된 날짜-시간 문자열.<br>
 */
public fun Date.toStringSimpleFormat(
    format: String,
    locale: Locale = Locale.getDefault(),
): String = SimpleDateFormat(format, locale).format(this)

/**
 * Calculates the absolute time difference in seconds between this Date and another Date.<br><br>
 * 이 Date와 다른 Date 사이의 절대 시간 차이를 초 단위로 계산합니다.<br>
 *
 * @param other The Date to compare with.<br><br>
 *              비교할 Date 객체.
 *
 * @return The absolute time difference in seconds.<br><br>
 *         절대 시간 차이 (초 단위).<br>
 */
public fun Date.toCompareInSeconds(other: Date): Long = Math.abs(this.time - other.time) / 1000

/**
 * Calculates the absolute time difference in minutes between this Date and another Date.<br><br>
 * 이 Date와 다른 Date 사이의 절대 시간 차이를 분 단위로 계산합니다.<br>
 *
 * @param other The Date to compare with.<br><br>
 *              비교할 Date 객체.
 *
 * @return The absolute time difference in minutes.<br><br>
 *         절대 시간 차이 (분 단위).<br>
 */
public fun Date.toCompareInMinutes(other: Date): Long = Math.abs(this.time - other.time) / (1000 * 60)

/**
 * Calculates the absolute time difference in hours between this Date and another Date.<br><br>
 * 이 Date와 다른 Date 사이의 절대 시간 차이를 시간 단위로 계산합니다.<br>
 *
 * @param other The Date to compare with.<br><br>
 *              비교할 Date 객체.
 *
 * @return The absolute time difference in hours.<br><br>
 *         절대 시간 차이 (시간 단위).<br>
 */
public fun Date.toCompareInHours(other: Date): Long = Math.abs(this.time - other.time) / (1000 * 3600)

/**
 * Calculates the number of hours between this LocalDateTime and another LocalDateTime.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 이 LocalDateTime과 다른 LocalDateTime 사이의 시간 차를 계산합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param other The LocalDateTime to compare with.<br><br>
 *              비교할 LocalDateTime 객체.
 *
 * @return The number of hours between the two LocalDateTimes (can be negative if other is before this).<br><br>
 *         두 LocalDateTime 사이의 시간 차 (other가 this보다 이전이면 음수일 수 있음).<br>
 */
public fun LocalDateTime.toCompareInHours(other: LocalDateTime): Long = ChronoUnit.HOURS.between(this, other)

/**
 * Calculates the number of minutes between this LocalDateTime and another LocalDateTime.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 이 LocalDateTime과 다른 LocalDateTime 사이의 분 차이를 계산합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param other The LocalDateTime to compare with.<br><br>
 *              비교할 LocalDateTime 객체.
 *
 * @return The number of minutes between the two LocalDateTimes (can be negative if other is before this).<br><br>
 *         두 LocalDateTime 사이의 분 차이 (other가 this보다 이전이면 음수일 수 있음).<br>
 */
public fun LocalDateTime.toCompareInMinutes(other: LocalDateTime): Long = ChronoUnit.MINUTES.between(this, other)

/**
 * Calculates the number of days between this LocalDate and another LocalDate.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 이 LocalDate와 다른 LocalDate 사이의 일 수를 계산합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param other The LocalDate to compare with.<br><br>
 *              비교할 LocalDate 객체.
 *
 * @return The number of days between the two LocalDates (can be negative if other is before this).<br><br>
 *         두 LocalDate 사이의 일 수 (other가 this보다 이전이면 음수일 수 있음).<br>
 */
public fun LocalDate.toCompareInDays(other: LocalDate): Long = ChronoUnit.DAYS.between(this, other)

/**
 * Calculates the number of months between this LocalDate and another LocalDate.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 이 LocalDate와 다른 LocalDate 사이의 월 수를 계산합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param other The LocalDate to compare with.<br><br>
 *              비교할 LocalDate 객체.
 *
 * @return The number of months between the two LocalDates (can be negative if other is before this).<br><br>
 *         두 LocalDate 사이의 월 수 (other가 this보다 이전이면 음수일 수 있음).<br>
 */
public fun LocalDate.toCompareInMonths(other: LocalDate): Long = ChronoUnit.MONTHS.between(this, other)

/**
 * Calculates the number of years between this LocalDate and another LocalDate.<br>
 * Requires API level 26 (Android O) or higher.<br><br>
 * 이 LocalDate와 다른 LocalDate 사이의 연 수를 계산합니다.<br>
 * API 레벨 26 (Android O) 이상이 필요합니다.<br>
 *
 * @param other The LocalDate to compare with.<br><br>
 *              비교할 LocalDate 객체.
 *
 * @return The number of years between the two LocalDates (can be negative if other is before this).<br><br>
 *         두 LocalDate 사이의 연 수 (other가 this보다 이전이면 음수일 수 있음).<br>
 */
public fun LocalDate.toCompareInYears(other: LocalDate): Long = ChronoUnit.YEARS.between(this, other)

/**
 * Converts a duration in seconds to minutes.<br><br>
 * 초 단위의 지속 시간을 분 단위로 변환합니다.<br>
 *
 * @return The duration in minutes.<br><br>
 *         분 단위의 지속 시간.<br>
 */
public fun Long.secondsToMinutes(): Long = this / 60

/**
 * Converts a duration in seconds to hours.<br><br>
 * 초 단위의 지속 시간을 시간 단위로 변환합니다.<br>
 *
 * @return The duration in hours.<br><br>
 *         시간 단위의 지속 시간.<br>
 */
public fun Long.secondsToHours(): Long = this / 3600

/**
 * Converts a duration in seconds to days.<br><br>
 * 초 단위의 지속 시간을 일 단위로 변환합니다.<br>
 *
 * @return The duration in days.<br><br>
 *         일 단위의 지속 시간.<br>
 */
public fun Long.secondsToDays(): Long = this / 86400

/**
 * Converts a duration in milliseconds to seconds.<br><br>
 * 밀리초 단위의 지속 시간을 초 단위로 변환합니다.<br>
 *
 * @return The duration in seconds.<br><br>
 *         초 단위의 지속 시간.<br>
 */
public fun Long.millisecondsToSeconds(): Long = this / 1000

/**
 * Converts a duration in milliseconds to minutes.<br><br>
 * 밀리초 단위의 지속 시간을 분 단위로 변환합니다.<br>
 *
 * @return The duration in minutes.<br><br>
 *         분 단위의 지속 시간.<br>
 */
public fun Long.millisecondsToMinutes(): Long = this / (1000 * 60)

/**
 * Converts a duration in milliseconds to hours.<br><br>
 * 밀리초 단위의 지속 시간을 시간 단위로 변환합니다.<br>
 *
 * @return The duration in hours.<br><br>
 *         시간 단위의 지속 시간.<br>
 */
public fun Long.millisecondsToHours(): Long = this / (1000 * 3600)

/**
 * Converts a duration in milliseconds to days.<br><br>
 * 밀리초 단위의 지속 시간을 일 단위로 변환합니다.<br>
 *
 * @return The duration in days.<br><br>
 *         일 단위의 지속 시간.<br>
 */
public fun Long.millisecondsToDays(): Long = this / (1000 * 86400)

/**
 * Converts a duration in seconds to minutes.<br><br>
 * 초 단위의 지속 시간을 분 단위로 변환합니다.<br>
 *
 * @return The duration in minutes.<br><br>
 *         분 단위의 지속 시간.<br>
 */
public fun Int.secondsToMinutes(): Int = this / 60

/**
 * Converts a duration in seconds to hours.<br><br>
 * 초 단위의 지속 시간을 시간 단위로 변환합니다.<br>
 *
 * @return The duration in hours.<br><br>
 *         시간 단위의 지속 시간.<br>
 */
public fun Int.secondsToHours(): Int = this / 3600

/**
 * Converts a duration in seconds to days.<br><br>
 * 초 단위의 지속 시간을 일 단위로 변환합니다.<br>
 *
 * @return The duration in days.<br><br>
 *         일 단위의 지속 시간.<br>
 */
public fun Int.secondsToDays(): Int = this / 86400
