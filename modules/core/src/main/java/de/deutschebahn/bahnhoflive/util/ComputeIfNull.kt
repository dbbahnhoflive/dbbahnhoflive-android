package de.deutschebahn.bahnhoflive.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ComputeIfNull<T>(
    val computation: () -> T?
) : ReadWriteProperty<Any, T?> {

    private var cache: T? = null

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        cache = value
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T? =
        cache ?: computation().also {
            cache = it
        }
}