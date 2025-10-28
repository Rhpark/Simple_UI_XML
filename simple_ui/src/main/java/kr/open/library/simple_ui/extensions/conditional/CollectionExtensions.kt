package kr.open.library.simple_ui.extensions.conditional


/**
 * Collection extensions that provide additional utilities not covered in Kotlin stdlib
 * Focus on conditional operations and chainable functions
 */

/**
 * Conditionally filters the list based on a boolean condition
 *
 * @param condition Boolean condition to determine if filtering should be applied
 * @param predicate The filtering predicate to apply when condition is true
 * @return Original list if condition is false, filtered list if condition is true
 *
 * Example:
 * val results = products.filterIf(showOnSale) { it.isOnSale }
 */
public inline fun <T> List<T>.filterIf(condition: Boolean, predicate: (T) -> Boolean
): List<T> = if (condition) filter(predicate) else this


/**
 * Executes the given action if the list is not empty and returns the list for chaining
 * Different from stdlib's onEach - this only executes when list is not empty
 *
 * @param action The action to execute with the non-empty list
 * @return The original list for chaining
 *
 * Example:
 * notifications.ifNotEmpty { updateBadgeCount(it.size) }.ifEmpty { hideNotificationIcon() }
 */
public inline fun <T> List<T>.ifNotEmpty(action: (List<T>) -> Unit): List<T> {
    if (isNotEmpty()) action(this)
    return this
}

/**
 * Executes the given action if the list is empty and returns the list for chaining
 *
 * @param action The action to execute when list is empty
 * @return The original list for chaining
 *
 * Example:
 * searchResults.ifEmpty { showNoResultsMessage() }.ifNotEmpty { hideNoResultsMessage() }
 */
public inline fun <T> List<T>.ifEmpty(action: () -> Unit): List<T> {
    if (isEmpty()) action()
    return this
}

/**
 * Executes the given action if the map is not empty and returns the map for chaining
 *
 * @param action The action to execute with the non-empty map
 * @return The original map for chaining
 *
 * Example:
 * userPreferences.ifNotEmpty { saveToCache(it) }
 */
public inline fun <K, V> Map<K, V>.ifNotEmpty(action: (Map<K, V>) -> Unit): Map<K, V> {
    if (isNotEmpty()) action(this)
    return this
}

public inline fun <K, V> Map<K, V>.ifEmpty(action: (Map<K, V>) -> Unit): Map<K, V> {
    if (isEmpty()) action(this)
    return this
}
