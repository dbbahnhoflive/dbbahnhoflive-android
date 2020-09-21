/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.LegacyWaggon.Type;
import de.deutschebahn.bahnhoflive.util.Collections;
import de.deutschebahn.bahnhoflive.util.JSONHelper;

@Deprecated
public class Wagenstand {

    public ArrayList<String> days;
    public String name;
    public String time;
    public String platform;
    public String additionalText;
    public ArrayList<String> trainTypes;
    public ArrayList<String> trainNumbers;
    public ArrayList<LegacyTrain> subtrains;
    public ArrayList<LegacyWaggon> waggons;

    public final static String TRACK_RECORDS = "trackRecords";
    public final static String TRAIN_RECORDS = "trainRecords";
    public final static String NAME = "name";
    public final static String TIME = "time";
    public final static String ADDITIONAL_TEXT = "additionalText";
    public final static String TRAIN_NUMBERS = "trainNumbers";
    public final static String DAYS = "days";
    public final static String PLATFORM = "platform";
    public final static String SUBTRAINS = "subtrains";
    public final static String WAGGONS = "waggons";
    public final static String TRAIN_TYPES = "traintypes";

    private boolean reversed = false;

    public ArrayList<String> getDays() {
        return days;
    }

    public void setDays(ArrayList<String> days) {
        this.days = days;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAdditionalText() {
        if (!TextUtils.isEmpty(additionalText)) {
            additionalText = additionalText.replaceAll("\n", " ");
            return additionalText;
        }
        return "";
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }

    public ArrayList<String> getTrainTypes() {
        return trainTypes;
    }

    public void setTrainTypes(ArrayList<String> trainTypes) {
        this.trainTypes = trainTypes;
    }

    public ArrayList<String> getTrainNumbers() {
        return trainNumbers;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setTrainNumbers(ArrayList<String> trainNumbers) {
        this.trainNumbers = trainNumbers;
    }

    public ArrayList<LegacyTrain> getSubtrains() {
        if(subtrains == null){
            return new ArrayList<>();
        }
        return subtrains;
    }

    public void setSubtrains(ArrayList<LegacyTrain> subtrains) {
        this.subtrains = subtrains;
    }

    public ArrayList<LegacyWaggon> getWaggons() {
        if(waggons == null){
            return new ArrayList<>();
        }
        return waggons;
    }

    public void setWaggons(ArrayList<LegacyWaggon> waggons) {
        this.waggons = waggons;
    }

    public static List<Wagenstand> fromSollRequestJSONArray(JSONArray jsonArray) {

        List<Wagenstand> wagenstandList = new ArrayList<>();
        //FIXME: we expect only a single JSONObject in the jsonArray, iOS takes the first entry, Android will take the last one. Should be cleaned up...
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                JSONArray stations = jsonObject.getJSONArray("stations");
                for (int j = 0; j < stations.length(); j++) {
                    JSONObject stationObject = (JSONObject) stations.get(j);
                    JSONArray trackRecords = stationObject.getJSONArray(TRACK_RECORDS);
                    for (int k = 0; k < trackRecords.length(); k++) {
                        JSONObject trackRecord = trackRecords.getJSONObject(k);
                        JSONArray trainRecords = trackRecord.getJSONArray(TRAIN_RECORDS);
                        String platform = trackRecord.getString("platform");
                        wagenstandList = wagenstandFromSimplifiedJSON(trainRecords, platform);
                    }
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return wagenstandList;
    }

    public static JSONArray toJSONArray(List<Wagenstand>wagenstandList) {
        // convert an array of Wagentand objects back to JSON
        JSONArray wagenstandListArray = new JSONArray();
        for (Wagenstand wagenstand : wagenstandList) {
            wagenstandListArray.put(wagenstand.toJSON());
        }
        return wagenstandListArray;
    }

    public static List<Wagenstand> wagenstandFromSimplifiedJSON(JSONArray simplifiedArray, String platform) throws JSONException {

        List<Wagenstand> wagenstandList = new ArrayList<>();
        for (int k = 0; k < simplifiedArray.length(); k++) {
            Wagenstand wagenstand = new Wagenstand();
            JSONObject wagenstandJSON = simplifiedArray.getJSONObject(k);

            if (platform.length() > 0) {
                wagenstand.setPlatform(platform);
            } else {
                wagenstand.setPlatform(wagenstandJSON.optString(PLATFORM, ""));
            }
            wagenstand.setName(wagenstandJSON.getString(NAME));
            wagenstand.setTime(wagenstandJSON.getString(TIME));
            wagenstand.setAdditionalText(wagenstandJSON.getString(ADDITIONAL_TEXT));

            JSONArray trainNumbers = wagenstandJSON.getJSONArray(TRAIN_NUMBERS);
            JSONArray days = wagenstandJSON.getJSONArray(DAYS);
            JSONArray subtrains = wagenstandJSON.getJSONArray(SUBTRAINS);
            JSONArray waggons = wagenstandJSON.getJSONArray(WAGGONS);
            JSONArray trainTypes = wagenstandJSON.getJSONArray(TRAIN_TYPES);

            wagenstand.setWaggons(LegacyWaggon.fromJSONArray(waggons));

            wagenstand.setSubtrains(LegacyTrain.fromJSONArray(subtrains, trainNumbers, trainTypes));

            wagenstand.setTrainNumbers(JSONHelper.getArrayListFromJSON(trainNumbers));
            wagenstand.setDays(JSONHelper.getArrayListFromJSON(days));
            wagenstand.setTrainTypes(JSONHelper.getArrayListFromJSON(trainTypes));

            wagenstandList.add(wagenstand);
        }
        return wagenstandList;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();

        try {

            JSONArray waggons = new JSONArray();
            for (LegacyWaggon waggon : getWaggons()) {
                waggons.put(waggon.toJSON());
            }

            JSONArray subTrains = new JSONArray();
            for (LegacyTrain train : getSubtrains()) {
                subTrains.put(train.toJSON());
            }

            result.put(NAME, getName());
            result.put(SUBTRAINS, subTrains);
            result.put(WAGGONS, waggons);
            result.put(TIME, getTime());
            if (getPlatform().length() > 0) {
                result.put(PLATFORM, getPlatform());
            }
            result.put(ADDITIONAL_TEXT, getAdditionalText());
            result.put(TRAIN_NUMBERS, new JSONArray(getTrainNumbers()));
            result.put(DAYS, new JSONArray(getDays()));
            result.put(TRAIN_TYPES, new JSONArray(getTrainTypes()));

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int indexForWaggonByNumber(@NonNull String waggonNumber) {
        for (LegacyWaggon waggon : getWaggons()) {
            if (waggon.getWaggonNumber().equals(waggonNumber)) {
                return getWaggons().indexOf(waggon);
            }
        }
        return 0;
    }

    public ArrayList<String> getJoinedSectionList() {
        ArrayList<String> sections = new ArrayList<>();
        for (LegacyTrain train : getSubtrains()) {
            for (String section : train.getSections()) {
                if (!sections.contains(section)) {
                    sections.add(section);
                }
            }
        }
        return sections;
    }

    public LegacyTrain getDestinatinationForWaggon(LegacyWaggon waggon) {
        LegacyTrain destinationTrain = null;
        for (LegacyTrain train : this.subtrains) {

            destinationTrain = train;

            for (String waggonSection : waggon.getSections()) {
                // check if train contains all waggon sections
                // if so, then this must be the destination of our waggon
                if (!train.getSections().contains(waggonSection)) {
                    destinationTrain = null;
                }
            }

            if (destinationTrain != null) {
                return destinationTrain;
            }
        }
        return null;
    }

    public static String getFormattedStringFromParameters(final Map<String, Object> parameters) {

        String time = null;
        String platform = null;
        String waggon = null;

        if (parameters.containsKey("time")) {
            time = parameters.get("time").toString();
        }

        if (parameters.containsKey("platform")) {
            platform = parameters.get("platform").toString();
        }

        if (parameters.containsKey("waggon")) {
            waggon = parameters.get("waggon").toString();
        }

        return getFormattedStringFromParameters(time, platform, waggon);
    }

    public static String getFormattedStringFromParameters(String time, String platform, String waggon) {

        String headline = "";

        if (time != null) {
            headline += time;
        }

        if (platform != null) {
            headline += String.format(" Gl. %s", platform);
        }

        if (waggon != null) {
            headline += String.format(" Wagen ", waggon);
        }

        return headline;
    }

    public void sortBySection() {
        if (Collections.hasContent(waggons)) {
            final LegacyWaggon firstWaggon = waggons.get(0);
            final LegacyWaggon lastWaggon = waggons.get(waggons.size() - 1);
            final String firstWaggonSection = firstWaggon.getSections().get(0);
            final String lastWaggonSection = lastWaggon.getSections().get(0);

            if (Collator.getInstance().compare(firstWaggonSection, lastWaggonSection) > 0) {
                reverse();
            }
        }
    }

    private void reverse() {
        java.util.Collections.reverse(waggons);
        for (LegacyWaggon waggon : waggons) {
            switch (waggon.getType()) {
                case Type.TRIEBKOPF:
                    waggon.setType(Type.TRIEBKOPF_HINTEN);
                    break;
                case Type.TRIEBKOPF_HINTEN:
                    waggon.setType(Type.TRIEBKOPF);
                    break;
            }
        }

        reversed = !reversed;
    }

    public boolean isReversed() {
        return reversed;
    }
}
