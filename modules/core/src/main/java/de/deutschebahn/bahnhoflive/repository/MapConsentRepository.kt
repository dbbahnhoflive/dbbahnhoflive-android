package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData

class MapConsentRepository(context: Context) {

    val consented = MutableLiveData(false)

}
