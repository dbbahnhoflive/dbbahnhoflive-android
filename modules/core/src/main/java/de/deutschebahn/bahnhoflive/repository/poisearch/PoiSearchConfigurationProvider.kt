package de.deutschebahn.bahnhoflive.repository.poisearch

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.concurrent.Executors

class PoiSearchConfigurationProvider(val context: Context) {

    val configuration = object : LiveData<PoiSearchConfiguration>() {

        var kickOff = true

        override fun onActive() {
            super.onActive()

            if (kickOff) {
                kickOff = false
                Executors.newSingleThreadExecutor().submit {
                    postValue(PoiSearchConfiguration(context))
                }
            }
        }
    }

}