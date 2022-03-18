package de.deutschebahn.bahnhoflive.util

fun <K, V> Map<K, V>?.asMutable() =
    this?.toMutableMap() ?: mutableMapOf()