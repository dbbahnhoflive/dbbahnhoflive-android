package de.deutschebahn.bahnhoflive.util

fun <T> Sequence<T>.append(nextSequence: Sequence<T>?) = nextSequence?.let { plus(it) } ?: this

fun <T> Sequence<T>.append(element: T?) = element?.let { plus(it) } ?: this
