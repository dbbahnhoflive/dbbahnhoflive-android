package de.deutschebahn.bahnhoflive.util

import android.os.Build
import android.os.Parcel

fun <T> Parcel.readParcelableCompatible(classLoader: ClassLoader?, clazz: Class<T>): T? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.readParcelable(classLoader, clazz)
        } else {
            @Suppress("DEPRECATION")
            this.readParcelable(classLoader)
        }
    } catch (_: Exception) {
        null
    }
}


fun <T> Parcel.readArrayListCompatible(
    classLoader: ClassLoader?,
    clazz: Class<out T>
): ArrayList<T>? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.readArrayList(classLoader, clazz)
        } else {
            @Suppress("DEPRECATION")
            this.readArrayList(classLoader) as? ArrayList<T>?
        }
    } catch (_: Exception) {
        null
    }
}