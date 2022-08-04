/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.tutorial;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TutorialManager {

    public static TutorialManager mInstance;
    private static Context mContext;

    private TutorialManager() {
    }

    public static TutorialManager getInstance(Context context) {
        if (mInstance == null) {
            mContext = context;
            mInstance = new TutorialManager();
        }
        return mInstance;
    }

    public interface Id {
        String MAP = "f3_map";
        String MAP_TRACK_DEPARTURES = "track_departures";
        String POI_SEARCH = "poi_search";
        String COUPONS = "coupons";
    }

    private List<Tutorial> seedingList() {
        //FIXME: add string resources instead

        ArrayList<Tutorial> tutorials = new ArrayList<>(10);

        tutorials.add(
                new Tutorial("hub_intial", "DB Bahnhof live", "Neues Design und neue Funktionen. Viel Spaß beim Entdecken.", 0));
        // FIXME: Add skip-on-interaction, once view is there
        tutorials.add(
                new Tutorial("hub_departure", "In der Nähe", "Finden Sie schnell und komfortable Bahnhöfe und Haltestellen.", 4));
/*
        tutorials.add(
                new Tutorial("h1_live", "Live Infos", "Aktuelle Informationen zu Shops, Parkplätzen und mehr (an ausgewählten Bahnhöfen).", 1));
*/
        tutorials.add(
                new Tutorial("h1_tips", "Tipps & Hinweise", "Unter Einstellungen können Sie Tipps & Hinweise deaktivieren.", 4));
        tutorials.add(
                new Tutorial("h2_departure", "Verbindungsdetails", "Erhalten Sie alle Details zu Ihrer Verbindung, inkl. aktuellem Wagenreihungsplan.", 7));

        // FIXME: still need to display it and handle skip-on-interaction
        tutorials.add(
                new Tutorial("servicestore_detail", "DB Services", "Sie haben Fragen? Wir helfen Ihnen weiter.", 7));
        tutorials.add(
                new Tutorial("d1_aufzuege", "Merkliste erstellen", "Verwalten Sie Ihre relevante Aufzüge. Ganz einfach und übersichtlich.", 4));
        tutorials.add(
                new Tutorial("d1_parking", "Parken am Bahnhof", "Informieren Sie sich mit einem Klick über Anfahrtswege, Öffnungszeiten und Preise.", 4));
        tutorials.add(
                new Tutorial(Id.MAP, "Filter", "Nutzen Sie den Filter, um sich für Sie relevante Inhalte anzeigen zu lassen.", 9));
        tutorials.add(
                new Tutorial(Id.MAP_TRACK_DEPARTURES, "Abfahrtstafel am Gleis", "Wählen Sie Ihr Gleis auf der Karte und Sie erhalten die Abfahrtsinfos der nächsten Züge.", 0));
        tutorials.add(
                new Tutorial(Id.POI_SEARCH, "Neue Suchfunktion", "Finden Sie gezielt Angebote, Services und Informationen an Ihrem Bahnhof.", 4));
        tutorials.add(
                new Tutorial(Id.COUPONS, "Rabatt Coupons", "Alle Angebote finden Sie im Bereich Shoppen & Schlemmen unter Rabatt Coupons", 0));

        //FIXME: Add once View ist there; User used filter without opening 'All' item
        //tutorials.add(
        //        new Tutorial("f1_map", "Profi Tip für Filtereinstellungen", "Schieben Sie einzelne Kategorie-Einträge nach links, um schnell alle dazugehörigen Inhalte auf der Karte anzuzeigen.", 2));

        return tutorials;
    }

    public void seedTutorials() {
        // TutorialPreferenceStore.getInstance(mContext).cleanStore();

        List<Tutorial> tutorials = TutorialPreferenceStore.getInstance(mContext).getAll();
        List<Tutorial> seedableTutorials = seedingList();

        // initialize configuration
        if (tutorials.isEmpty()
                ||  seedableTutorials.size() > tutorials.size()) {

            // Seed tutorial configuration
            for (Tutorial tutorial : seedableTutorials) {
                TutorialPreferenceStore.getInstance(mContext).update(tutorial);
            }
        }
    }

    /**
     * Shows tutorial for view
     *
     * @param tutorialView The reference View which holds the layout
     * @param identifier   The identifier of the tutorial which should be shown
     */
    public boolean showTutorialIfNecessary(final TutorialView tutorialView, final String identifier) {
        if (tutorialView != null) {
            Tutorial tutorial = getTutorialForView(identifier);
            if (tutorial != null) {
                tutorialView.show(new TutorialView.TutorialViewDelegate() {
                    @Override
                    public void didCloseTutorialView(TutorialView view, Tutorial tutorial) {
                        // Update tutorial's state
                        markTutorialAsSeen(tutorial);
                    }
                }, tutorial);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks all tutorials and returns the one that needs to be shown.
     * It then decreases the view count for all Tutorials on this view.
     * @param viewIdentifier An identifier for a view. A view can have multiple tutorials
     * @return Tutorial or null
     */
    public Tutorial getTutorialForView(String viewIdentifier) {

        final TutorialPreferenceStore store = TutorialPreferenceStore.getInstance(mContext);

        // Check if user has enabled tutorials or already has seen them all.
        // If so/not, we can spare the rest
        if (!doesUserWantToSeeTutorials() || hasSeenAllTutorials()) {
            return null;
        }

        List<Tutorial> tutorialsForView = store.getTutorials(viewIdentifier);

        Tutorial tutorialToShow = null;
        // Checks all tutorials relevant for this view
        for (Tutorial tutorial : tutorialsForView) {
            if (tutorial.currentCount <= 0 && !tutorial.closedByUser) {
                // only one tutorial should be displayed at the time
                tutorialToShow = tutorial;
            }
            // decrease the view count for the next round
            tutorial.currentCount--;
        }
        // Update all tutorials according to their view count
        store.update(tutorialsForView);

        if (tutorialToShow != null) {
            Log.d("Tutorial", "Found tutorial " + tutorialToShow);
        } else {
            Log.d("Tutorial", "No tutorial to show");
        }

        return tutorialToShow;
    }

    public boolean doesUserWantToSeeTutorials() {
        final TutorialPreferenceStore store = TutorialPreferenceStore.getInstance(mContext);
        return store.doesUserWantToSeeTutorials();
    }

    public boolean hasSeenAllTutorials() {
        return TutorialPreferenceStore.getInstance(mContext).hasSeenAllTutorials();
    }

    /**
     * Marks the tutorial as seen
     * @param tutorial
     */
    public void markTutorialAsSeen(Tutorial tutorial) {
        tutorial.closedByUser = true;
        TutorialPreferenceStore.getInstance(mContext).update(tutorial);
    }

    /**
     * Marks the tutorial as seen
     * @param identifier
     */
    public void markTutorialAsSeen(String identifier) {

        final TutorialPreferenceStore store = TutorialPreferenceStore.getInstance(mContext);
        Tutorial tutorial = store.getTutorial(identifier);

        if (tutorial != null) {
            tutorial.closedByUser = true;
            TutorialPreferenceStore.getInstance(mContext).update(tutorial);
        }
    }

    /**
     * Resets the view count back to default if the user has seen the Tutorial but not closed it.
     * @param tutorialView
     */
    public void markTutorialAsIgnored(@Nullable TutorialView tutorialView) {
        if (tutorialView == null) {
            return;
        }

        Tutorial tutorial = tutorialView.getCurrentlyVisibleTutorial();

        tutorialView.hide();

        if (tutorial != null) {
            tutorial.currentCount = tutorial.countdown;
            TutorialPreferenceStore.getInstance(mContext).update(tutorial);
        }
    }

}
