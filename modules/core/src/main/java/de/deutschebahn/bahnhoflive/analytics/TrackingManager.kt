/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.analytics

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IntDef
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.ui.consent.ConsentActivity

open class TrackingManager(activity: ComponentActivity? = null) {
    private val trackingDelegate: TrackingDelegate = get().trackingDelegate
    fun collectLifecycleData(activity: Activity) {
        trackingDelegate.collectLifecycleData(activity)
    }

    fun pauseCollectingLifecycleData() {
        trackingDelegate.pauseCollectingLifecycleData()
    }

    interface Screen {
        companion object {
            const val H0 = "h0"
            const val H1 = "h1"
            const val H2 = "h2"
            const val H3 = "h3"
            const val F1 = "f1"
            const val F3 = "f3"
            const val D1 = "d1"
            const val D2 = "d2"
            const val D3 = "d3"
        }
    }

    interface Source {
        companion object {
            const val TAB_NAVI = "tab_navi"
            const val HAFAS_REQUEST = "hafas_request"
            const val INFO = Entity.INFO
            const val SHOPS = Entity.SHOPS
        }
    }

    interface Action {
        companion object {
            const val TRACKING_ACTIVATE = "tracking_activate"
            const val TRACKING_DEACTIVATE = "tracking_deactivate"
            const val SCROLL = "scroll"
            const val TAP = "tap"
        }
    }

    interface UiElement {
        companion object {
            const val ABFAHRTSTAFEL = Entity.ABFAHRTSTAFEL
            const val ABFAHRT_DB = Entity.ABFAHRT_DB
            const val ABFAHRT_NAEHE_OPNV = Entity.ABFAHRT_NAEHE_OPNV
            const val ABFAHRT_NAEHE_BHF = Entity.ABFAHRT_NAEHE_BHF
            const val ABFAHRT_OEPNV = Entity.ABFAHRT_OEPNV
            const val AUSSTATTUNGS_MERKMALE = Entity.AUSSTATTUNGS_MERKMALE
            const val DEPARTURE = Entity.DEPARTURE
            const val EINSTELLUNGEN = Entity.EINSTELLUNGEN
            const val FAVORITEN = Entity.FAVORITEN
            const val FEEDBACK = Entity.FEEDBACK
            const val FILTER = Entity.FILTER
            const val FILTER_BUTTON = "filter_button"
            const val INFO = Entity.INFO
            const val LIST = "list"
            const val MAP = Entity.MAP
            const val MAP_BUTTON = Entity.MAP_BUTTON
            const val PARKPLAETZE = Category.PARKPLAETZE
            const val PIN = Entity.PIN
            const val POIS = Entity.POIS
            const val SHOPS = Entity.SHOPS
            const val SUCHE = Entity.SUCHE
            const val TOGGLE_ABFAHRT = Entity.TOGGLE_ABFAHRT
            const val TOGGLE_ANKUNFT = Entity.TOGGLE_ANKUNFT
            const val TOGGLE_DB = "toggle_db"
            const val TOGGLE_OEPNV = "toggle_oepnv"
            const val UEBERSICHT = Entity.UEBERSICHT
            const val VERBINDUNG_AUSWAHL = Entity.VERBINDUNG_AUSWAHL
            const val WAGENREIHUNG = Entity.WAGENREIHUNG
            const val NEARBY = "naehe"
            const val ABFAHRT_FAVORITEN_BHF = "abfahrt_favoriten_bhf"
            const val ABFAHRT_FAVORITEN_OPNV = "abfahrt_favoriten_opnv"
            const val ABFAHRT_SUCHE_BHF = "abfahrt_suche_bhf"
            const val ABFAHRT_SUCHE_OPNV = "abfahrt_suche_opnv"
            const val POI_SEARCH = "poi-suche"
            const val POI_SEARCH_RESULT = "tap-result"
            const val POI_SEARCH_QUERY = "such-aktion"
            const val ECO_TEASER = "oekostrom-teaser"
            const val CHATBOT = "chatbot"
            const val MEK_TEASER = "mek-teaser"
        }
    }

    interface Entity {
        companion object {
            const val ABFAHRTSTAFEL = "abfahrtstafel"
            const val ABFAHRT_DB = "abfahrt_db"
            const val ABFAHRT_NAEHE_OPNV = "abfahrt_naehe_opnv"
            const val ABFAHRT_NAEHE_BHF = "abfahrt_naehe_bhf"
            const val ABFAHRT_OEPNV = "abfahrt_oepnv"
            const val AUSSTATTUNGS_MERKMALE = "ausstattungs_merkmale"
            const val DATENSCHUTZ = "datenschutz"
            const val DEPARTURE = "departure"
            const val EINSTELLUNGEN = "einstellungen"
            const val FAVORITEN = "favoriten"
            const val FEEDBACK = "feedback"
            const val COMPLAINT = "verschmutzung"
            const val RATE = "bewerten"
            const val REPORT_BUG = "kontakt"
            const val FILTER = "filter"
            const val IMPRESSUM = "impressum"
            const val INFO = "info"
            const val MAP = "map"
            const val MAP_BUTTON = "map_button"
            const val PIN = "pin"
            const val POIS = "pois"
            const val SHOPS = "shops"
            const val SUCHE = "suche"
            const val TOGGLE_ABFAHRT = "toggle_abfahrt"
            const val TOGGLE_ANKUNFT = "toggle_ankunft"
            const val UEBERSICHT = "uebersicht"
            const val VERBINDUNG_AUSWAHL = "verbindung_auswahl"
            const val WAGENREIHUNG = "wagenreihung"
            const val NEWS_BOX = "newsbox"
            const val COUPON = "coupon"
            const val LINK = "link"
            const val WEBSITE = "website"
            const val APP = "app"
            const val NEWS_TYPE = "newstype"
        }
    }

    interface Category {
        companion object {
            const val AUFZUEGE = "aufzuege"
            const val AUFZUEGE_GEMERKT = "aufzuege_gemerkt"

            @Deprecated("")
            val BAHNHOF_AUSSTATTUNG = "bahnhof_ausstattung"
            const val INFOS_UND_SERVICES = "infos_und_services"
            const val LAGEPLAN = "lageplan"
            const val PARKPLAETZE = "parkplaetze"
            const val SERVICE_UND_RUFNUMMERN = "service_und_rufnummern"
            const val WLAN = "wlan"
            const val ZUGANG_WEGE = "zugang_wege"
            const val BARRIEREFREIHEIT = "barrierefreiheit"
            const val SCHIENENERSATZVERKEHR = "schienenersatzverkehr"
            const val BAECKEREIEN = "baeckereien"
            const val DIENSTLEISTUNGEN = "dienstleistungen"
            const val GASTRONOMIE = "gastronomie"
            const val GESUNDHEIT_UND_PFLEGE = "gesundheit_und_pflege"
            const val LEBENSMITTEL = "lebensmittel"
            const val PRESSE_UND_BUCH = "presse_und_buch"
            const val SHOPS = Entity.SHOPS
            const val COUPONS = "rabatt_coupons"
            const val SCHLIESSFAECHER = "schliessfaecher"

        }
    }

    interface AdditionalVariable {
        companion object {
            const val SEARCH = "Search"
            const val FOLLOWED_POI = "FollowedPOI"
            const val RESULT = "Result"
        }
    }

    fun setConsented(consent: Boolean) {
        trackingDelegate.consentState =
            if (consent) ConsentState.CONSENTED else ConsentState.DISSENTED
        if (consent) {
            track(TYPE_ACTION, Action.TRACKING_ACTIVATE)
        }
    }

    val consentState: ConsentState
        get() = trackingDelegate.consentState

    init {
        activity?.apply {

            val registerForActivityResult =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_CANCELED) {
                        finish()
                    }
                }

            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    if (consentState == ConsentState.PENDING && !isFinishing) {
                        registerForActivityResult.launch(ConsentActivity.createIntent(this@apply))
                    }

                    super.onResume(owner)
                }
            })
        }
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_STATE, TYPE_ACTION)
    annotation class Type

    protected fun composePageName(vararg states: String?): String {
        return TextUtils.join(":", states)
    }

    protected open fun composeContextVariables(
        additionalVariables: Map<String, Any?>?,
        vararg pages: Array<out String?>
    ): Map<String, Any?> {
        val contextVariables: MutableMap<String, Any?> =
            if (additionalVariables == null) HashMap() else HashMap(additionalVariables)
        putPageVariables(pages, contextVariables)
        return contextVariables
    }

    open fun track(@Type type: Int, vararg pages: String?) {
        track(type, null as Map<String, Any>?, *pages)
    }

    fun track(@Type type: Int, additionalParameters: Map<String, Any?>?, vararg pages: String?) {
        if (pages.isEmpty()) {
            return
        }
        val pageName = composePageName(*pages)
        val contextVariables = composeContextVariables(additionalParameters, pages)
        track(type, pageName, contextVariables)
    }

    fun track(@Type type: Int, tag: String, contextVariables: Map<String, Any?>) {
        when (type) {
            TYPE_ACTION -> {
                Log.i(TAG, "ADBMobile.trackAction: $tag, $contextVariables")
                trackingDelegate.trackAction(tag, contextVariables)
            }
            TYPE_STATE -> {
                Log.i(TAG, "ADBMobile.trackState: $tag, $contextVariables")
                trackingDelegate.trackState(tag, contextVariables)
            }
        }
    }

    interface Provider {
        val stationTrackingManager: TrackingManager
    }

    companion object {
        val TAG = TrackingManager::class.java.simpleName
        const val TRACK_KEY_STATION_MAP = "station_map"
        const val TRACK_KEY_SHOPPEN_SCHLEMMEN = "shoppen_schlemmen"
        const val TRACK_KEY_NEWS_EVENTS = "news_events"
        const val TRACK_KEY_CONNECTION = "connection"
        const val TRACK_KEY_FEEDBACK = "feedback"
        const val TRACK_KEY_MAP_FULL = "map_full"
        const val TRACK_KEY_TIMETABLE = "timetable"
        const val EXTRA_TRACKING_TAG = "trackingTag"

        @JvmStatic
        fun putTrackingTag(bundle: Bundle, trackingTag: String?) {
            bundle.putString(EXTRA_TRACKING_TAG, trackingTag)
        }

        @JvmStatic
        fun tagFromArguments(bundle: Bundle?): String? {
            return bundle?.getString(EXTRA_TRACKING_TAG)
        }

        @JvmStatic
        fun fromActivity(activity: ComponentActivity?): TrackingManager {
            if (activity is Provider) {
                return (activity as Provider).stationTrackingManager
            }
            return object : TrackingManager(activity) {
                override fun track(type: Int, vararg pages: String?) {
                    Log.w(TAG, "Host activity does not provide tracking manager")
                    super.track(type, *pages)
                }
            }
        }

        const val TYPE_STATE = 1
        const val TYPE_ACTION = 2
        private fun putPageVariables(
            states: Array<out Array<out String?>>,
            contextVariables: MutableMap<String, Any?>
        ) {
            var pageCount = 1
            for (s in states) {
                contextVariables["Page$pageCount"] = s
                pageCount++
            }
        }
    }

}