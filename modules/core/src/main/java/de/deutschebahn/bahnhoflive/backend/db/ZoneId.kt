package de.deutschebahn.bahnhoflive.backend.db

fun String.formatZoneId() = padStart(4, '0')