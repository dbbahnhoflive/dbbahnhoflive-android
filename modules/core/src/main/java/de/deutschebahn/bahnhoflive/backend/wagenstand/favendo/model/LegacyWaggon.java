package de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model;

import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.ColorInt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.wagenstand.models.FeatureStatus;
import de.deutschebahn.bahnhoflive.util.JSONHelper;

import static java.util.Collections.emptyList;

@Deprecated
public class LegacyWaggon {

    public static final int COLOR_SECOND_CLASS = Color.rgb(0, 178, 27);
    public static final int COLOR_FIRST_CLASS = Color.rgb(255, 230, 13);
    public static final int COLOR_RESTAURANT = Color.rgb(255, 0, 0);
    public static final int COLOR_LUGGAGE = Color.rgb(153, 153, 153);
    public static final int COLOR_SLEEPING = Color.rgb(0, 115, 255);
    public static final int COLOR_MISC = Color.rgb(255, 97, 3);
    public static final int COLOR_NONE = Color.argb(0, 1, 1, 1);

    public interface Type {
        String LOK = "s";
        String TRIEBKOPF = "t";
        String TRIEBKOPF_HINTEN = "v";
    }

    public static final Comparator<LegacyWaggon> POSITION_COMPARATOR = new Comparator<LegacyWaggon>() {
        @Override
        public int compare(LegacyWaggon o1, LegacyWaggon o2) {
            return o1.getPosition() - o2.getPosition();
        }
    };

    private boolean waggon;
    private String type;
    private List<String> symbols;
    private String waggonNumber;
    /**
     * Occupied area at station platform
     */
    private ArrayList<String> sections;
    private int position;
    private int length;
    private String differentDestination;

    private List<FeatureStatus> features = emptyList();

    private final static String WAGGON = "waggon";
    private final static String POSITION = "position";
    private final static String DIFFERENT_DESTINATION = "differentDestination";
    private final static String WAGGON_LENGTH = "length";
    private final static String TYPE = "type";
    private final static String SYMBOLS = "symbols";
    private final static String WAGGON_NUMBER = "number";
    private final static String SECTIONS = "sections";
    private static final String FEATURES = "features";


    public boolean isWaggon() {
        return waggon;
    }

    public void setWaggon(boolean waggon) {
        this.waggon = waggon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public String getWaggonNumber() {
        return waggonNumber;
    }

    public void setWaggonNumber(String waggonNumber) {
        this.waggonNumber = waggonNumber;
    }

    public ArrayList<String> getSections() {
        return sections;
    }

    public void setSections(ArrayList<String> sections) {
        this.sections = sections;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDifferentDestination() {
        return differentDestination;
    }

    public void setDifferentDestination(String differentDestination) {
        this.differentDestination = differentDestination;
    }

    public static ArrayList<LegacyWaggon> fromJSONArray(JSONArray waggonsJSONArray) throws JSONException {

        ArrayList<LegacyWaggon> waggonList = new ArrayList<>(waggonsJSONArray.length());

        for (int i = 0; i < waggonsJSONArray.length(); i++) {

            LegacyWaggon waggon = new LegacyWaggon();

            JSONObject waggonJSON = waggonsJSONArray.getJSONObject(i);

            waggon.setWaggon(waggonJSON.getBoolean(WAGGON));
            waggon.setPosition(waggonJSON.getInt(POSITION));
            waggon.setDifferentDestination(waggonJSON.optString(DIFFERENT_DESTINATION, ""));
            waggon.setLength(waggonJSON.getInt(WAGGON_LENGTH));
            waggon.setWaggonNumber(waggonJSON.optString(WAGGON_NUMBER, ""));

            // sanitize type
            String waggonType = waggonJSON.getString(TYPE);
            waggonType = waggonType.trim();
            waggonType = waggonType.replace(".Kl", "");


            waggon.setType(waggonType);

            String symbols = waggonJSON.optString(SYMBOLS, "");

            // convert string of symbols to ArrayList of single Chars
            ArrayList<String> chars = new ArrayList<>();
            for (char c : symbols.toCharArray()) {
                chars.add("" + c);
            }
            waggon.setSymbols(chars);
            waggon.setSections(JSONHelper.getArrayListFromJSON(waggonJSON.getJSONArray(SECTIONS)));

            final JSONArray featuresArray = waggonJSON.optJSONArray(FEATURES);
            if (featuresArray == null) {
                waggon.setFeatures(Collections.emptyList());
            } else {
                final ArrayList<FeatureStatus> features = new ArrayList<>(featuresArray.length());
                for (int feature = 0; feature < featuresArray.length(); feature++) {
                    features.add(new FeatureStatus(featuresArray.getJSONObject(feature)));
                }
                waggon.setFeatures(features);
            }

            waggonList.add(waggon);
        }

        return waggonList;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();

        try {
            result.put(POSITION, getPosition());
            result.put(SYMBOLS, TextUtils.join("", getSymbols()));
            result.put(DIFFERENT_DESTINATION, getDifferentDestination());
            result.put(TYPE, getType());
            result.put(WAGGON, isWaggon());
            result.put(WAGGON_LENGTH, getLength());
            result.put(WAGGON_NUMBER, getWaggonNumber());
            result.put(SECTIONS, new JSONArray(getSections()));
            final JSONArray featuresArray = new JSONArray();
            for (FeatureStatus feature : features) {
                featuresArray.put(feature.toJSON());
            }
            result.put(FEATURES, featuresArray);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ColorInt
    public int colorForType() {

        int color;

        switch (getType()) {
            case "1":
            case "3":
            case "4":
            case "i":
                color = COLOR_FIRST_CLASS; //first class
                break;
            case "2":
            case "5":
            case "6":
            case "7":
            case "h":
                color = COLOR_SECOND_CLASS; // second class
                break;
            case "8":
            case "9":
                color = COLOR_LUGGAGE; // luggage
                break;
            case "a":
            case "b":
            case "e":
                color = COLOR_RESTAURANT; // restaurant
                break;
            case "c":
            case "g":
                color = COLOR_SLEEPING; // sleeping
                break;
            default:
                color = COLOR_MISC; //"#ff0000"; // misc
        }

        return color;
    }

    @ColorInt
    public int getSecondaryColor() {
        int color;

        switch (getType()) {
            case "3":
            case "i":
                color = COLOR_SECOND_CLASS;
                break;
            case "4":
            case "7":
                color = COLOR_RESTAURANT;
                break;
            case "6":
                color = COLOR_LUGGAGE;
                break;
            default:
                color = COLOR_NONE;
        }

        return color;
    }

    public boolean getWaggonHasMultipleClasses() {
        switch (getType()) {
            case "3":
            case "4":
            case "i":
            case "6":
            case "7":
                return true;
            default:
                return false;
        }
    }

    public boolean isWaggonOfTypRestaurant() {
        return getSymbols().contains("p");
    }

    public String getClassOfWaggon() {
        switch (getType()) {
            case "1":
                return "1";
            case "2":
                return "2";
            case "3":
                return "1";
            case "4":
                return "1";
            case "5":
                return "2";
            case "6":
                return "2";
            case "7":
                return "2";
            case "8":
                return "";
            case "9":
                return "";
            case "a":
                return "1";
            case "b":
                return "1";
            case "c":
                return "";
            case "e":
                return "1";
            case "g":
                return "";
            case "h":
                return "2";
            case "i":
                return "1";
        }
        return "";
    }

    public boolean isWaggonHead() {
        return (getType().equals("q") || getType().equals(Type.TRIEBKOPF) || getType().equals("Ü"));
    }

    public boolean isWaggonTail() {
        return (getType().equals(Type.TRIEBKOPF_HINTEN) || getType().equals("r") || getType().equals("Ä"));
    }

    public boolean isTrainHeadBothWays() {
        return getType().equals(Type.LOK);
    }


    @Override
    public String toString() {
        return "Waggon{" +
                "waggon=" + waggon +
                ", type='" + type + '\'' +
                ", symbols=" + symbols +
                ", features=" + features +
                ", waggonNumber='" + waggonNumber + '\'' +
                ", sections=" + sections +
                ", position=" + position +
                ", length=" + length +
                ", differentDestination='" + differentDestination + '\'' +
                '}';
    }

    public List<FeatureStatus> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureStatus> features) {
        this.features = features;
    }
}
