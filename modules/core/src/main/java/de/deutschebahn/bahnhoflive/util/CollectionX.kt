package de.deutschebahn.bahnhoflive.util

fun <T> Collection<T>.intersects(other: Collection<T>) = any {
    other.contains(it)
}