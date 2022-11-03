package de.deutschebahn.bahnhoflive.util

import android.content.Intent
import androidx.fragment.app.Fragment
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.ui.map.MapConsentDialogFragment
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.OnMapConsentDialogListener

fun <K, V> Map<K, V>?.asMutable() =
    this?.toMutableMap() ?: mutableMapOf()

fun startMapActivityIfConsent(fragment: Fragment, createMapIntentFunction: () -> Intent?)
{
    if (BaseApplication.get().applicationServices.mapConsentRepository.consented.value==false) {
        val mp = MapConsentDialogFragment()
        mp.setOnMapConsentDialogListener(object : OnMapConsentDialogListener {
            override fun onConsentAccepted() {
                createMapIntentFunction()?.let{
                    if (fragment is MapPresetProvider) {
                        (fragment as MapPresetProvider).prepareMapIntent(it)
                    }
                    fragment.startActivity(it)
                }
            }
        })
        mp.show(fragment.parentFragmentManager, null)
    } else {
        createMapIntentFunction()?.let{fragment.startActivity(it)}
    }

}
