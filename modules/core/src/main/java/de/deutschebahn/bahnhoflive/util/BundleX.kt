package de.deutschebahn.bahnhoflive.util

import android.os.Build
import android.os.Bundle
import java.io.Serializable


fun <T> Bundle.getParcelableCompatible(key: String?, clazz: Class<T>): T? {
    // The reason for not using <T extends Parcelable> is because the caller could provide a
    // super class to restrict the children that doesn't implement Parcelable itself while the
    // children do, more details at b/210800751 (same reasoning applies here).
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getParcelable(
                key,
                clazz
            )
        else {
            @Suppress("DEPRECATION")
            this.getParcelable(key)
        }
    } catch (_: Exception) {
        null
    }
}

fun <T : Serializable?> Bundle.getSerializableCompatible(key: String?,clazz: Class<T>): T? {

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.getSerializable(key, clazz)
        else {
            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            this.getSerializable(key) as T
        }
    } catch (_: Exception) {
        null
    }
}

