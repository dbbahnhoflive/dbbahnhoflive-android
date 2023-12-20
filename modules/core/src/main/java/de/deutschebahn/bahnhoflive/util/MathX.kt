package de.deutschebahn.bahnhoflive.util

fun Double.isZero() : Boolean = this>-0.0000001 && this<0.0000001
fun Double.isNotZero() : Boolean = !isZero()
