package de.deutschebahn.bahnhoflive.util

import androidx.lifecycle.LiveData

class EmptyLiveData<T> : LiveData<T?>(null) {
    companion object {
        val INSTANCE = EmptyLiveData<Nothing?>()
    }
}

inline fun <reified T> emptyLiveData(): LiveData<T?> = EmptyLiveData.INSTANCE as LiveData<T?>