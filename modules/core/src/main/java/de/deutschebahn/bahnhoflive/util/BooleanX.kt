package de.deutschebahn.bahnhoflive.util

infix fun <T> Boolean.then(action: () -> T) = if (this) action() else null