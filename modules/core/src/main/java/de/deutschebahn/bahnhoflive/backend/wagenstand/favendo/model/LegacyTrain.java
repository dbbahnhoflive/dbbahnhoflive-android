package de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.deutschebahn.bahnhoflive.util.JSONHelper;

@Deprecated
public class LegacyTrain {

    public String destinationStation;
    public String type;
    public String number;
    public ArrayList<String> destinationVia;
    public ArrayList<String> sections;
    private String sectionSpan;

    private final static String DESTINATION = "destination";
    private final static String DESTINATION_NAME = "destinationName";
    private final static String DESTINATION_VIA = "destinationVia";
    private final static String SECTIONS = "sections";
    private final static String SECTION_SPAN = "sectionSpan";

    public ArrayList<String> getDestinationVia() {
        return destinationVia;
    }

    public void setDestinationVia(ArrayList<String> destinationVia) {
        this.destinationVia = destinationVia;
    }

    public String getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }

    public ArrayList<String> getSections() {
        return sections;
    }

    public void setSections(ArrayList<String> sections) {
        this.sections = sections;
    }

    public String getType() {
        if (type == null) {
            return "";
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        if (number == null) {
            return "";
        }
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    private static LegacyTrain fromJSON(JSONObject trainJSON) throws JSONException {
        LegacyTrain train = new LegacyTrain();
        JSONObject destinationJSON = trainJSON.getJSONObject(DESTINATION);

        train.setDestinationStation(destinationJSON.optString(DESTINATION_NAME, ""));
        train.setDestinationVia(JSONHelper.getArrayListFromJSON(destinationJSON.getJSONArray(DESTINATION_VIA)));

        train.setSections(JSONHelper.getArrayListFromJSON(trainJSON.getJSONArray(SECTIONS)));
        train.setSectionSpan(trainJSON.optString(SECTION_SPAN, ""));
        return train;
    }

    public static LegacyTrain fromJSON(String trainDefinition) {
        try {
            JSONObject station = new JSONObject(trainDefinition);
            return fromJSON(station);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<LegacyTrain> fromJSONArray(JSONArray trainsJSON,
                                                       JSONArray trainNumbers,
                                                       JSONArray trainTypes) throws JSONException {
        ArrayList<LegacyTrain> trains = new ArrayList<>(trainsJSON.length());

        for (int i = 0; i < trainsJSON.length(); i++) {

            Object trainObject = trainsJSON.get(i);

            String trainType = "";
            String trainNumber = "";

            LegacyTrain train;
            if (trainObject instanceof String) {
                train = fromJSON((String)trainObject);
            } else {
                train = fromJSON((JSONObject)trainObject);
            }

            if (i < trainTypes.length()) {
                trainType = (String)trainTypes.get(i);
                trainNumber = (String)trainNumbers.get(i);
            } else {
                trainType = (String)trainTypes.get(0);
                trainNumber = (String)trainNumbers.get(0);
            }

            train.number = trainNumber;
            train.type = trainType;

            trains.add(train);
        }

        return trains;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();

        try {
            JSONObject destination = new JSONObject();
            destination.put(DESTINATION_NAME, getDestinationStation());
            ArrayList<String> dest = getDestinationVia();
            if(dest == null){
                dest = new ArrayList<>();
            }
            destination.put(DESTINATION_VIA, new JSONArray(dest));
            result.put(DESTINATION, destination);

            result.put(SECTIONS, new JSONArray(getSections()));
            result.put(SECTION_SPAN, sectionSpan);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Train{" +
                "destinationStation='" + destinationStation + '\'' +
                ", type='" + type + '\'' +
                ", number='" + number + '\'' +
                ", destinationVia=" + destinationVia +
                ", sections=" + sections +
                ", sectionSpan=" + sectionSpan +
                '}';
    }

    public void setSectionSpan(String sectionSpan) {
        this.sectionSpan = sectionSpan;
    }

    public String getSectionSpan() {
        return sectionSpan;
    }
}
