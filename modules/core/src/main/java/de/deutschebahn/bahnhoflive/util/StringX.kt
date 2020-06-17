package de.deutschebahn.bahnhoflive.util

fun String?.nonBlankOrNull() = takeUnless { it.isNullOrBlank() }