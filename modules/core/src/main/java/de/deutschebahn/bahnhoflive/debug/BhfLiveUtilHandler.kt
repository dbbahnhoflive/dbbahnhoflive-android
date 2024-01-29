package de.deutschebahn.bahnhoflive.debug

import android.annotation.SuppressLint
import android.content.Context
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.analytics.ConsentState
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.tutorial.TutorialPreferenceStore


class BhfLiveUtilHandler(val context: Context) {

    val dataChangeObserver = InMemoryDbProviderInterface.registerValueChangeObserver(context) {key, value ->
        try {
            handleChangeEvent(key, value)
        } catch (e: Exception) {
            // todo: log
        }

    }






    private fun handleChangeEvent(key: String, value: String) {

        when(key) {
            "#getversionname" -> InMemoryDbProviderInterface.setValue(context, "versionname", get().versionName)
            "#getversioncode" -> InMemoryDbProviderInterface.setValue(context, "versioncode", get().versionCode.toString())

            "#gettrackingstate" -> InMemoryDbProviderInterface.setValue(context, "trackingstate", get().trackingDelegate.consentState.toString())
            "#tracking" -> {
                if(value.compareTo("true", true)==0)
                    get().trackingDelegate.consentState= ConsentState.CONSENTED
                else
                    get().trackingDelegate.consentState= ConsentState.DISSENTED
            }

            "#gettippsstate" -> InMemoryDbProviderInterface.setValue(context, "tippsstate",
                TutorialManager.getInstance().doesUserWantToSeeTutorials().toString())
            "#tipps" -> {
                TutorialPreferenceStore.getInstance(get())
                    ?.setUserWantsTutorials(value.compareTo("true", true)==0)
            }

            else -> {}
        }

    }


    companion object {

        @SuppressLint("StaticFieldLeak")
        private var instance : BhfLiveUtilHandler? = null

        fun init(context:Context) {
          // todo: NUR bei debug ??
            instance = BhfLiveUtilHandler(context)
        }

//        fun exit() {
//            instance?.also {
//                it.onStop()
//            }
//            instance = null
//        }

    }


}