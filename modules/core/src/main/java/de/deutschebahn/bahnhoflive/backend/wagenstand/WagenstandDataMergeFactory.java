package de.deutschebahn.bahnhoflive.backend.wagenstand;

import android.util.Log;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.LegacyTrain;
import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.LegacyWaggon;
import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.Wagenstand;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugausstattungData;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllSectorData;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandFahrzeugData;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstInformationData;
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.FeatureStatus;
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.Status;
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.WaggonFeature;

/**
 * Use this class to set or merge the  Wagenstand.waggons depending on WagenstandIstInformationData
 *
 */

public class WagenstandDataMergeFactory {

    public static final Collator COLLATOR = Collator.getInstance(Locale.GERMAN);
    private static String TAG = WagenstandDataMergeFactory.class.getSimpleName();

    /**
     * Replaces the Wagenstand.waggons with WagenstandIstInformationData
     *
     */
    public static Wagenstand toWagenstand(WagenstandIstInformationData wagenstandIstInformationData) {

        final Wagenstand wagenstand = new Wagenstand();
        wagenstand.setAdditionalText("");

        ArrayList<LegacyWaggon> waggons = new ArrayList<LegacyWaggon>();

        int positionCount = 0;

        // AllFahrzeuggruppe is single trains
        Iterator<WagenstandFahrzeugData> wagenstandFahrzeugDataIterator = wagenstandIstInformationData.allFahrzeuggruppe.iterator();

        ArrayList<LegacyTrain> subTrains = new ArrayList<>();
        ArrayList<String> trainTypes = new ArrayList<>();
        ArrayList<String> trainNumbers = new ArrayList<>();

        ArrayList<String> sections = new ArrayList<>();
        for (WagenstandAllSectorData sectorData : wagenstandIstInformationData.halt.allSektor) {
            sections.add(sectorData.sektorbezeichnung);
            Log.d("TK", "Sektor: " + sectorData.sektorbezeichnung);
        }
        Collections.sort(sections);

        while(wagenstandFahrzeugDataIterator.hasNext()) {
            WagenstandFahrzeugData wagenstandFahrzeugData = wagenstandFahrzeugDataIterator.next();

            final LegacyTrain subTrain = new LegacyTrain();
            subTrain.setNumber(wagenstandFahrzeugData.verkehrlichezugnummer);
            subTrain.setDestinationStation(wagenstandFahrzeugData.zielbetriebsstellename);

            subTrain.setType(TrainInfo.Category.ICE);
            subTrain.setSections(sections);

            subTrain.setSectionSpan(extractSectionSpan(wagenstandFahrzeugData));

            subTrains.add(subTrain);
            trainNumbers.add(wagenstandFahrzeugData.verkehrlichezugnummer);
            trainTypes.add(TrainInfo.Category.ICE);

            int len = wagenstandFahrzeugData.allFahrzeug.size();

            for(int i = 0 ; i < len ; i++) {
                WagenstandAllFahrzeugData wagenstandAllFahrzeugData = wagenstandFahrzeugData.allFahrzeug.get(i);

                LegacyWaggon waggon = new LegacyWaggon();
                // add waggon at this time to avoid sorting problems with ICE loks
                waggons.add(waggon);

                List<String> equipment = new ArrayList<>();

                final List<WagenstandAllFahrzeugausstattungData> allFahrzeugausstattung = wagenstandAllFahrzeugData.allFahrzeugausstattung;
                final ArrayList<FeatureStatus> features = new ArrayList<>(allFahrzeugausstattung.size());

                for (WagenstandAllFahrzeugausstattungData wagenstandAllFahrzeugausstattungData : allFahrzeugausstattung) {
                    try {
                        final WaggonFeature waggonFeature = WaggonFeature.valueOf(wagenstandAllFahrzeugausstattungData.ausstattungsart);
                        final Status status = Status.valueOf(wagenstandAllFahrzeugausstattungData.status);

                        features.add(new FeatureStatus(waggonFeature, status));
                    } catch (IllegalArgumentException e) {
                        Log.i(TAG, "waggon feature unusable", e);
                    }
                }

                waggon.setFeatures(features);

                // differentDestination=''
                waggon.setDifferentDestination("");

                // Set teh waggon flag, false if wagenstandFahrzeugDataIterator is an locomotive
                waggon.setWaggon( isWaggon(wagenstandAllFahrzeugData.kategorie) );

                // sections=[G]
                ArrayList<String> sectors = new ArrayList();
                sectors.add(wagenstandAllFahrzeugData.fahrzeugsektor);
                waggon.setSections(sectors);

                //type='t'
                // if we find a steuerwagen of an ICE, add a waggon manually
                if(wagenstandAllFahrzeugData.kategorie.contains("STEUERWAGEN")) {
                    final String typ = (i == 0) ? LegacyWaggon.Type.TRIEBKOPF_HINTEN : LegacyWaggon.Type.TRIEBKOPF;
                    LegacyWaggon lokWaggon = getLokWaggon(typ, positionCount, i);
                    lokWaggon.setSections(sectors);
                    //waggon.setDifferentDestination(wagenstandFahrzeugData.zielbetriebsstellename);

                    Log.d(TAG, String.format("%s",wagenstandFahrzeugData.zielbetriebsstellename));

                    if(i == 0) {
                        if( waggons.size() > 1 ) {
                            waggons.add( (positionCount), lokWaggon);
                        } else {
                            waggons.add(0, lokWaggon);
                        }
                    }  else {
                        waggons.add(lokWaggon);
                    }

                    String t = (wagenstandAllFahrzeugData.kategorie.equals(WagenstandAllFahrzeugData.Category.STEUERWAGENERSTEKLASSE))? "1": "2";
                    waggon.setType(t);

                    positionCount++;
                }else {
                    waggon.setType( getWaggonType(wagenstandAllFahrzeugData.kategorie, i));
                }

                if("GESCHLOSSEN".equals(wagenstandAllFahrzeugData.status) && !WagenstandAllFahrzeugData.Category.TRIEBKOPF.equals(wagenstandAllFahrzeugData.kategorie)){
                    waggon.setType("8");
                    waggon.setDifferentDestination("verschlossen");
                }

                /**
                 * For now we don't receive these informations
                 * (remove if we get the ausstattungs data from the response)
                 */
                if(waggon.getType().equals("b")
                        || waggon.getType().equals("4")
                        || waggon.getType().equals("7")) {
                    equipment.add("p");
                }
                // set the equipment
                waggon.setSymbols( new ArrayList<String>(equipment) );

                // position=0
                waggon.setPosition(positionCount);

                // waggonNumber=''
                waggon.setWaggonNumber(wagenstandAllFahrzeugData.wagenordnungsnummer);

                positionCount++;
            }
        }

        //201704131427

        String dateTimeString = "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(wagenstandIstInformationData.halt.abfahrtszeit);
            dateTimeString = new SimpleDateFormat("HH:mm").format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        wagenstand.setDays(new ArrayList<String>());
        wagenstand.setName("");
        wagenstand.setAdditionalText("");
        wagenstand.setPlatform(wagenstandIstInformationData.halt.gleisbezeichnung);
        wagenstand.setTime(dateTimeString);
        wagenstand.setSubtrains(subTrains);
        wagenstand.setTrainNumbers(trainNumbers);
        wagenstand.setTrainTypes(trainTypes);

        //wagenstandIstInformationData.allFahrzeuggruppe
        // set the waggons only if we have more then 1 items
        if(waggons.size() > 1) {
            Collections.reverse(waggons);
            wagenstand.setWaggons(waggons);
        }

        return wagenstand;
    }

    public static String extractSectionSpan(WagenstandFahrzeugData wagenstandFahrzeugData) {
        if (wagenstandFahrzeugData == null) {
            return "";
        }

        final List<WagenstandAllFahrzeugData> allFahrzeug = wagenstandFahrzeugData.allFahrzeug;
        if (allFahrzeug.isEmpty()) {
            return "";
        }

        final String firstSector = allFahrzeug.get(0).fahrzeugsektor;
        final String lastSector = allFahrzeug.get(allFahrzeug.size() - 1).fahrzeugsektor;

        if (firstSector.equals(lastSector)) {
            return firstSector;
        }

        return COLLATOR.compare(firstSector, lastSector) < 0
                ? String.format("%s-%s", firstSector, lastSector)
                : String.format("%s-%s", lastSector, firstSector);
    }

    /**
     * Get a lok waggon if we have an ICE, we get only 1 item.
     */
    protected static LegacyWaggon getLokWaggon(String type, int positionCount, int index) {
        LegacyWaggon waggon = new LegacyWaggon();
        waggon.setDifferentDestination("");
        waggon.setSymbols( new ArrayList<String>() );
        waggon.setPosition( positionCount );
        waggon.setType( getWaggonType(type, index) );

        return waggon;
    }

    /**
     * Get the waggon type from the response key
     */
    protected static String getWaggonType(String category, int index) {
        if( category.equals(WagenstandAllFahrzeugData.Category.REISEZUGWAGENERSTEKLASSE) ) return "1";
        if( category.equals(WagenstandAllFahrzeugData.Category.REISEZUGWAGENZWEITEKLASSE) ) return "2";
        if( category.equals(WagenstandAllFahrzeugData.Category.REISEZUGWAGENERSTEZWEITEKLASSE) ) return "3";
        if( category.equals(WagenstandAllFahrzeugData.Category.SPEISEWAGEN) ) return "b";
        if( category.equals(WagenstandAllFahrzeugData.Category.HALBSPEISEWAGENERSTEKLASSE) ) return "4";
        if( category.equals(WagenstandAllFahrzeugData.Category.HALBSPEISEWAGENZWEITEKLASSE) ) return "7";
        if (category.equals(WagenstandAllFahrzeugData.Category.LOK)) return LegacyWaggon.Type.LOK;
        if (category.equals(WagenstandAllFahrzeugData.Category.TRIEBKOPF))
            return (index == 0) ? LegacyWaggon.Type.TRIEBKOPF_HINTEN : LegacyWaggon.Type.TRIEBKOPF;
        return category;
    }

    /**
     * Check if the cat is an locomotive
     */
    protected static boolean isWaggon(String cat) {
        if( cat.equals(WagenstandAllFahrzeugData.Category.LOK) ) return false;
        if( cat.equals(WagenstandAllFahrzeugData.Category.TRIEBKOPF) ) return false;
        if(cat.contains("STEUERWAGEN")) return false;

        return false;
    }

}
