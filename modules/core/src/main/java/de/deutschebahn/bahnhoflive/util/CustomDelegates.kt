package de.deutschebahn.bahnhoflive.util

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object CustomDelegates {

    public inline fun <T> observable(initialValue: T, crossinline onChange: (newValue: T) -> Unit):
            ReadWriteProperty<Any?, T> =
        object : ObservableProperty<T>(initialValue) {
            override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean =
                oldValue != newValue

            override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) =
                onChange(newValue)
        }

}