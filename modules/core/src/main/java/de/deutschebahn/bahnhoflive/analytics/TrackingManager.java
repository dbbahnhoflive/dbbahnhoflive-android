package de.deutschebahn.bahnhoflive.analytics;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

import de.deutschebahn.bahnhoflive.BaseApplication;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class TrackingManager {

    public static final String TAG = TrackingManager.class.getSimpleName();

    public static final String TRACK_KEY_STATION_MAP = "station_map";
    public static final String TRACK_KEY_SHOPPEN_SCHLEMMEN = "shoppen_schlemmen";
    public static final String TRACK_KEY_NEWS_EVENTS = "news_events";
    public static final String TRACK_KEY_CONNECTION = "connection";
    public static final String TRACK_KEY_FEEDBACK = "feedback";
    public static final String TRACK_KEY_MAP_FULL = "map_full";
    public static final String TRACK_KEY_TIMETABLE = "timetable";

    public static final String EXTRA_TRACKING_TAG = "trackingTag";
    private final TrackingDelegate trackingDelegate;

    public static void putTrackingTag(Bundle bundle, String trackingTag) {
        bundle.putString(EXTRA_TRACKING_TAG, trackingTag);
    }

    public static String tagFromArguments(Bundle bundle) {
        if (bundle == null) {
            return null;
        }

        return bundle.getString(EXTRA_TRACKING_TAG);
    }

    public void collectLifecycleData(Activity activity) {
        trackingDelegate.collectLifecycleData(activity);
    }

    public void pauseCollectingLifecycleData() {
        trackingDelegate.pauseCollectingLifecycleData();
    }

    public interface Screen {
        String H0 = "h0";
        String H1 = "h1";
        String H2 = "h2";
        String H3 = "h3";
        String F1 = "f1";
        String F3 = "f3";
        String D1 = "d1";
        String D2 = "d2";
        String D3 = "d3";
    }

    public interface Source {
        String TAB_NAVI = "tab_navi";
        String HAFAS_REQUEST = "hafas_request";
        String INFO = Entity.INFO;
        String SHOPS = Entity.SHOPS;
    }

    public interface Action {
        String TRACKING_ACTIVATE = "tracking_activate";
        String TRACKING_DEACTIVATE = "tracking_deactivate";
        String SCROLL = "scroll";
        String TAP = "tap";
    }

    public interface UiElement {
        String ABFAHRTSTAFEL = Entity.ABFAHRTSTAFEL;
        String ABFAHRT_DB = Entity.ABFAHRT_DB;
        String ABFAHRT_NAEHE_OPNV = Entity.ABFAHRT_NAEHE_OPNV;
        String ABFAHRT_NAEHE_BHF = Entity.ABFAHRT_NAEHE_BHF;
        String ABFAHRT_OEPNV = Entity.ABFAHRT_OEPNV;
        String AUSSTATTUNGS_MERKMALE = Entity.AUSSTATTUNGS_MERKMALE;
        String DEPARTURE = Entity.DEPARTURE;
        String EINSTELLUNGEN = Entity.EINSTELLUNGEN;
        String FAVORITEN = Entity.FAVORITEN;
        String FEEDBACK = Entity.FEEDBACK;
        String FILTER = Entity.FILTER;
        String FILTER_BUTTON = "filter_button";
        String INFO = Entity.INFO;
        String LIST = "list";
        String MAP = Entity.MAP;
        String MAP_BUTTON = Entity.MAP_BUTTON;
        String PARKPLAETZE = Category.PARKPLAETZE;
        String PIN = Entity.PIN;
        String POIS = Entity.POIS;
        String SHOPS = Entity.SHOPS;
        String SUCHE = Entity.SUCHE;
        String TOGGLE_ABFAHRT = Entity.TOGGLE_ABFAHRT;
        String TOGGLE_ANKUNFT = Entity.TOGGLE_ANKUNFT;
        String TOGGLE_DB = "toggle_db";
        String TOGGLE_OEPNV = "toggle_oepnv";
        String UEBERSICHT = Entity.UEBERSICHT;
        String VERBINDUNG_AUSWAHL = Entity.VERBINDUNG_AUSWAHL;
        String WAGENREIHUNG = Entity.WAGENREIHUNG;
        String NEARBY = "naehe";
        String ABFAHRT_FAVORITEN_BHF = "abfahrt_favoriten_bhf";
        String ABFAHRT_FAVORITEN_OPNV = "abfahrt_favoriten_opnv";
        String ABFAHRT_SUCHE_BHF = "abfahrt_suche_bhf";
        String ABFAHRT_SUCHE_OPNV = "abfahrt_suche_opnv";
        String POI_SEARCH = "poi-suche";
        String POI_SEARCH_RESULT = "tap-result";
        String POI_SEARCH_QUERY = "such-aktion";
        String ECO_TEASER = "oekostrom-teaser";
        String CHATBOT = "chatbot";
        String MEK_TEASER = "mek-teaser";
    }

    public interface Entity {
        String ABFAHRTSTAFEL = "abfahrtstafel";
        String ABFAHRT_DB = "abfahrt_db";
        String ABFAHRT_NAEHE_OPNV = "abfahrt_naehe_opnv";
        String ABFAHRT_NAEHE_BHF = "abfahrt_naehe_bhf";
        String ABFAHRT_OEPNV = "abfahrt_oepnv";
        String AUSSTATTUNGS_MERKMALE = "ausstattungs_merkmale";
        String DATENSCHUTZ = "datenschutz";
        String DEPARTURE = "departure";
        String EINSTELLUNGEN = "einstellungen";
        String FAVORITEN = "favoriten";
        String FEEDBACK = "feedback";
        String FILTER = "filter";
        String IMPRESSUM = "impressum";
        String INFO = "info";
        String MAP = "map";
        String MAP_BUTTON = "map_button";
        String PIN = "pin";
        String POIS = "pois";
        String SHOPS = "shops";
        String SUCHE = "suche";
        String TOGGLE_ABFAHRT = "toggle_abfahrt";
        String TOGGLE_ANKUNFT = "toggle_ankunft";
        String UEBERSICHT = "uebersicht";
        String VERBINDUNG_AUSWAHL = "verbindung_auswahl";
        String WAGENREIHUNG = "wagenreihung";
        String NEWS_BOX = "newsbox";
        String COUPON = "coupon";
        String LINK = "link";
        String WEBSITE = "website";
        String APP = "app";
        String NEWS_TYPE = "newstype";
    }

    public interface Category {
        String AUFZUEGE = "aufzuege";
        String AUFZUEGE_GEMERKT = "aufzuege_gemerkt";
        @Deprecated
        String BAHNHOF_AUSSTATTUNG = "bahnhof_ausstattung";
        String INFOS_UND_SERVICES = "infos_und_services";
        String LAGEPLAN = "lageplan";
        String PARKPLAETZE = "parkplaetze";
        String SERVICE_UND_RUFNUMMERN = "service_und_rufnummern";
        String WLAN = "wlan";
        String ZUGANG_WEGE = "zugang_wege";

        String BAECKEREIEN = "baeckereien";
        String DIENSTLEISTUNGEN = "dienstleistungen";
        String GASTRONOMIE = "gastronomie";
        String GESUNDHEIT_UND_PFLEGE = "gesundheit_und_pflege";
        String LEBENSMITTEL = "lebensmittel";
        String PRESSE_UND_BUCH = "presse_und_buch";
        String SHOPS = Entity.SHOPS;

        String COUPONS = "rabatt_coupons";
    }

    public interface AdditionalVariable {
        String SEARCH = "Search";
        String FOLLOWED_POI = "FollowedPOI";
        String RESULT = "Result";
    }

    public TrackingManager() {
        trackingDelegate = BaseApplication.get().trackingDelegate;
    }

    public void setOptOut(boolean optOut) {
        trackingDelegate.setOptOut(optOut);
    }

    @NonNull
    public static TrackingManager fromActivity(android.app.Activity activity) {
        if (activity instanceof Provider) {
            final TrackingManager stationTrackingManager = ((Provider) activity).getStationTrackingManager();
            if (stationTrackingManager != null) {
                return stationTrackingManager;
            }
        }

        return new TrackingManager() {
            @Override
            public void track(int type, String... pages) {
                Log.w(TAG, "Host activity does not provide tracking manager");
                super.track(type, pages);
            }
        };
    }

    @Retention(SOURCE)
    @IntDef({TYPE_STATE, TYPE_ACTION})
    public @interface Type {
    }

    public static final int TYPE_STATE = 1;
    public static final int TYPE_ACTION = 2;

    protected String composePageName(String... states) {
        return TextUtils.join(":", states);
    }

    private static void putPageVariables(String[] states, Map<String, Object> contextVariables) {
        int pageCount = 1;
        for (String s : states) {
            contextVariables.put("Page" + pageCount, s);
            pageCount++;
        }
    }


    @NonNull
    protected Map<String, Object> composeContextVariables(@Nullable Map<String, Object> additionalVariables, String... pages) {
        Map<String, Object> contextVariables = additionalVariables == null ? new HashMap<>() : new HashMap<>(additionalVariables);

        putPageVariables(pages, contextVariables);

        return contextVariables;
    }

    public void track(@Type int type, String... pages) {
        track(type, null, pages);
    }

    public void track(@Type int type, Map<String, Object> additionalParameters, String... pages) {
        if (pages == null || pages.length == 0) {
            return;
        }

        final String pageName = composePageName(pages);
        Map<String, Object> contextVariables = composeContextVariables(additionalParameters, pages);

        track(type, pageName, contextVariables);
    }

    void track(@Type int type, String tag, Map<String, Object> contextVariables) {
        switch (type) {
            case TYPE_ACTION:
                Log.i(TAG, "ADBMobile.trackAction: " + tag + ", " + contextVariables);
                trackingDelegate.trackAction(tag, contextVariables);
                break;
            case TYPE_STATE:
                Log.i(TAG, "ADBMobile.trackState: " + tag + ", " + contextVariables);
                trackingDelegate.trackState(tag, contextVariables);
                break;
        }

    }

    public interface Provider {

        @NonNull
        TrackingManager getStationTrackingManager();
    }
}
