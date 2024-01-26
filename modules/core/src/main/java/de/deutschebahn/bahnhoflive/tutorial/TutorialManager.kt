/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.tutorial

import android.content.Context
import android.util.Log
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.tutorial.TutorialView.TutorialViewDelegate

class TutorialManager private constructor(context : Context) {
    // idea is to use different Tutorials for same View, not sure its working
    // todo: check and clear Id's, manual id's in seedingList()
    interface Id {
        companion object {
            const val MAP = "f3_map"
            const val MAP_TRACK_DEPARTURES = "track_departures"
            const val POI_SEARCH = "poi_search"
            const val COUPONS = "coupons"
            const val PUSH_GENERAL = "push_general"
            const val PUSH_ELEVATORS = "push_elevators"
            const val JOURNEY = "journey"
            const val TIMETABLE = "timetable"
        }
    }

    private val tutorialPreferenceStore = TutorialPreferenceStore(context)

//    private var mContext : Context? = null
//    fun setContext(context:Context) {
//      mContext = context
//    }

    private fun seedingList(): List<Tutorial> {
        //FIXME: add string resources instead
        val tutorials = ArrayList<Tutorial>(10)
        tutorials.add(
            Tutorial(
                "hub_intial",
                "DB Bahnhof live",
                "Neues Design und neue Funktionen. Viel Spaß beim Entdecken.",
                0
            )
        )
        // FIXME: Add skip-on-interaction, once view is there
        tutorials.add(
            Tutorial(
                "hub_departure",
                "In der Nähe",
                "Finden Sie schnell und komfortable Bahnhöfe und Haltestellen.",
                4
            )
        )
        /*
        tutorials.add(
                new Tutorial("h1_live", "Live Infos", "Aktuelle Informationen zu Shops, Parkplätzen und mehr (an ausgewählten Bahnhöfen).", 1));
*/tutorials.add(
            Tutorial(
                "h1_tips",
                "Tipps & Hinweise",
                "Unter Einstellungen können Sie Tipps & Hinweise deaktivieren.",
                4
            )
        )
        tutorials.add(
            Tutorial(
                "h2_departure",
                "Verbindungsdetails",
                "Erhalten Sie alle Details zu Ihrer Verbindung, inkl. aktuellem Wagenreihungsplan.",
                7
            )
        )

        // FIXME: still need to display it and handle skip-on-interaction
        tutorials.add(
            Tutorial(
                "servicestore_detail",
                "DB Services",
                "Sie haben Fragen? Wir helfen Ihnen weiter.",
                7
            )
        )
        tutorials.add(
            Tutorial(
                "d1_aufzuege",
                "Merkliste erstellen",
                "Verwalten Sie Ihre relevante Aufzüge. Ganz einfach und übersichtlich.",
                4
            )
        )
        tutorials.add(
            Tutorial(
                "d1_parking",
                "Parken am Bahnhof",
                "Informieren Sie sich mit einem Klick über Anfahrtswege, Öffnungszeiten und Preise.",
                4
            )
        )
        tutorials.add(
            Tutorial(
                Id.MAP,
                "Filter",
                "Nutzen Sie den Filter, um sich für Sie relevante Inhalte anzeigen zu lassen.",
                9
            )
        )
        tutorials.add(
            Tutorial(
                Id.MAP_TRACK_DEPARTURES,
                "Abfahrtstafel am Gleis",
                "Wählen Sie Ihr Gleis auf der Karte und Sie erhalten die Abfahrtsinfos der nächsten Züge.",
                0
            )
        )
        tutorials.add(
            Tutorial(
                Id.POI_SEARCH,
                "Neue Suchfunktion",
                "Finden Sie gezielt Angebote, Services und Informationen an Ihrem Bahnhof.",
                4
            )
        )
        tutorials.add(
            Tutorial(
                Id.COUPONS,
                "Rabatt Coupons",
                "Alle Angebote finden Sie im Bereich Shoppen & Schlemmen unter Rabatt Coupons",
                0
            )
        )

        //FIXME: Add once View ist there; User used filter without opening 'All' item
        //tutorials.add(
        //        new Tutorial("f1_map", "Profi Tip für Filtereinstellungen", "Schieben Sie einzelne Kategorie-Einträge nach links, um schnell alle dazugehörigen Inhalte auf der Karte anzuzeigen.", 2));
        tutorials.add(
            Tutorial(
                Id.PUSH_GENERAL,
                "Neu: Mitteilungen erhalten",
                "Aktivieren Sie die Push-Mitteilungen zur Verfügbarkeit gemerkter Aufzüge.",
                0
            )
        )
        tutorials.add(
            Tutorial(
                Id.PUSH_ELEVATORS,
                "Neu: Mitteilungen Aufzüge",
                "Erhalten Sie eine Nachricht, wenn Ihr Aufzug defekt oder wieder in Betrieb ist.",
                0
            )
        )
        tutorials.add(
            Tutorial(
                Id.JOURNEY,
                "So wechseln Sie den Bahnhof",
                "Wählen Sie einen Halt im Fahrtverlauf, um den Bahnhof zu wechseln.",
                0
            )
        )
        tutorials.add(
            Tutorial(
                Id.TIMETABLE,
                "Gegenüberliegende Gleise",
                "Wählen Sie eine Verbindung aus, um weitere Gleisinfomationen zu erhalten.",
                0
            )
        )
        return tutorials
    }

    fun seedTutorials() {
        val tutorials = tutorialPreferenceStore.all
        val seedableTutorials = seedingList()

        // initialize configuration

        // eliminate non-existing tutorials from tutorials
        var removeTutorials = false
        for (tutorial in tutorials) {
            for (seedingTutorial in seedableTutorials) {
                if (seedingTutorial.id.compareTo(tutorial.id, ignoreCase = true) == 0) {
                    tutorial.descriptionText = ""
                    removeTutorials = true
                    break
                }
            }
        }
        if (tutorials.isEmpty() || seedableTutorials.size > tutorials.size || removeTutorials) {

            // Seed tutorial configuration
            for (tutorial in seedableTutorials) {
                tutorialPreferenceStore.update(tutorial)
            }
        }
    }

    /**
     * Shows tutorial for view
     *
     * @param tutorialView The reference View which holds the layout
     * @param identifier   The identifier of the tutorial which should be shown
     */
    fun showTutorialIfNecessary(tutorialView: TutorialView?, identifier: String?): Boolean {
        if (tutorialView != null) {
            val tutorial = getTutorialForView(identifier)
            if (tutorial != null) {
                tutorialView.show(object : TutorialViewDelegate {
                    override fun didCloseTutorialView(view: TutorialView?, tutorial: Tutorial?) {
                        // Update tutorial's state
                        markTutorialAsSeen(tutorial)
                    }
                }, tutorial)
                return true
            }
        }
        return false
    }

    /**
     * Checks all tutorials and returns the one that needs to be shown.
     * It then decreases the view count for all Tutorials on this view.
     * @param viewIdentifier An identifier for a view. A view can have multiple tutorials
     * @return Tutorial or null
     */
    fun getTutorialForView(viewIdentifier: String?): Tutorial? {
        val store = tutorialPreferenceStore

        // Check if user has enabled tutorials or already has seen them all.
        // If so/not, we can spare the rest
        if (!doesUserWantToSeeTutorials() || hasSeenAllTutorials()) {
            return null
        }
        val tutorialsForView = store.getTutorials(viewIdentifier)
        var tutorialToShow: Tutorial? = null
        // Checks all tutorials relevant for this view
        for (tutorial in tutorialsForView) {
            if (tutorial.currentCount <= 0 && !tutorial.closedByUser) {
                // only one tutorial should be displayed at the time
                tutorialToShow = tutorial
            }
            // decrease the view count for the next round
            tutorial.currentCount--
        }
        // Update all tutorials according to their view count
        store.update(tutorialsForView)
        if (tutorialToShow != null) {
            Log.d("Tutorial", "Found tutorial $tutorialToShow")
        } else {
            Log.d("Tutorial", "No tutorial to show")
        }
        return tutorialToShow
    }

    fun doesUserWantToSeeTutorials(): Boolean {
        return tutorialPreferenceStore.doesUserWantToSeeTutorials()
    }

    private fun hasSeenAllTutorials(): Boolean {
        return tutorialPreferenceStore.hasSeenAllTutorials()
    }

    /**
     * Marks the tutorial as seen
     * @param tutorial
     */
    fun markTutorialAsSeen(tutorial: Tutorial?) {
        tutorial?.let {
            it.closedByUser = true
            tutorialPreferenceStore.update(it)
        }
    }

    /**
     * Marks the tutorial as seen
     * @param identifier
     */
    fun markTutorialAsSeen(identifier: String?) {
        identifier?.let {itIdentifier->
            val tutorial = tutorialPreferenceStore.getTutorial(itIdentifier)
            tutorial?.let {
                it.closedByUser = true
                tutorialPreferenceStore.update(it)
            }
        }
    }

    /**
     * Resets the view count back to default if the user has seen the Tutorial but not closed it.
     * @param tutorialView
     */
    fun markTutorialAsIgnored(tutorialView: TutorialView?) {
        if (tutorialView == null) {
            return
        }
        tutorialView.hide()

        val tutorial = tutorialView.currentlyVisibleTutorial
        tutorial?.let {
            it.currentCount = tutorial.countdown
            tutorialPreferenceStore.update(it)
        }
    }

    companion object {

        @Volatile private var instance : TutorialManager? = null

        @JvmStatic
        fun  getInstance() : TutorialManager {

            if (instance == null)
                instance = TutorialManager(BaseApplication.get())

            return instance!!
        }

    }


}
