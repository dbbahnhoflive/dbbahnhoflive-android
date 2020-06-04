package de.deutschebahn.bahnhoflive.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

fun <T> T.asLiveData() = MutableLiveData<T>().also { it.value = this }

fun <T> LiveData<T?>.nonNull(): LiveData<T> = MediatorLiveData<T>().also { mediatorLiveData ->
    mediatorLiveData.addSource(this) { newValue ->
        if (newValue != null) {
            mediatorLiveData.value = newValue
        }
    }
}