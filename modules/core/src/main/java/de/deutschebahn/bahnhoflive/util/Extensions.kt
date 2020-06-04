package de.deutschebahn.bahnhoflive.util

/**
 * Calls the specified function [block] with `this` value as its receiver only if it is not `null` and finally returns `null`.
 */
inline fun <T> T?.destroy(block: T.() -> Unit): T? {
    this?.block()
    return null
}
