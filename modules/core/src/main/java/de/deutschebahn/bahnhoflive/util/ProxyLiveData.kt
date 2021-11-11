package de.deutschebahn.bahnhoflive.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class ProxyLiveData<T>(
    source: LiveData<T>? = null
) : MediatorLiveData<T>() {

    var source = source
        set(newSource) {
            if (field != newSource) {
                field?.also { oldSource ->
                    removeSource(oldSource)
                }
                field = newSource
                field?.also {
                    addSource(it) { newValue ->
                        value = newValue
                    }
                }
            }
        }

}