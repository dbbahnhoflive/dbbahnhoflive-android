/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.ris.model;

import static de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo.Category.EC;
import static de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo.Category.IC;
import static de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo.Category.ICE;
import static de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo.Category.S;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ParcelCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.deutschebahn.bahnhoflive.util.DebugX;

public class TrainInfo implements Parcelable {

    private boolean replacement = false;
    private boolean special = false;

    private boolean showWagonOrder = false;
    @NonNull
    private String genuineName;

    public void setReplacement(boolean replacement) {
        this.replacement = replacement;
    }

    public boolean isReplacement() {
        return replacement || getReferenceTrainInfo() != null;
    }

    public void setGenuineName(String genuineName) {
        this.genuineName = genuineName;
    }

    /**
     * Must not be null but possibly is
     */
    @Nullable
    public String getGenuineName() {
        return genuineName;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setShowWagonOrder(boolean show) {
        this.showWagonOrder = show;
    }
    public boolean getShowWagonOrder() {
        return this.showWagonOrder;
    }

    public interface Category {
        String ICE = "ICE";
        String IC = "IC";
        String EC = "EC";
        String S = "S";
    }

    static final String _train_info_alternative_name = "l";

    private static final String _id = "id";
    private static final String _train = "s";
    private static final String _train_info = "tl";
    private static final String _train_info_category = "c";
    private static final String _train_info_type = "t";
    private static final String _train_info_name = "n";
    private static final String _ref = "ref";

    @NonNull
    static TrainInfo readTrain(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, _train);
        final TrainInfo info = new TrainInfo();
        TrainMovementInfo arrival = null;
        TrainMovementInfo departure = null;
        TrainInfo referenceTrainInfo = null;

        String id = parser.getAttributeValue(null, _id);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            switch (name) {
                case _train_info:
                    readInfo(info, parser);
                    break;
                case TrainMovementInfo._arrival:
                    arrival = TrainMovementInfo.readMovement(parser);
                    break;
                case TrainMovementInfo._departure:
                    departure = TrainMovementInfo.readMovement(parser);
                    break;
                case _ref:
                    referenceTrainInfo = new TrainInfo();
                    readRef(referenceTrainInfo, parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        info.setArrival(arrival);
        info.setDeparture(departure);

        if (referenceTrainInfo != null) {
            info.setReferenceTrainInfo(referenceTrainInfo);
        }

        info.setId(id);
        return info;

    }

    private static void readInfo(TrainInfo info, XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, _train_info);

        //KK 2 we assume there are no <m> tags below <tl> tags

        String category = parser.getAttributeValue(null, _train_info_category);
        if (category != null) {
            info.setTrainCategory(category);
        }

        String genuineName = parser.getAttributeValue(null, _train_info_name);
        String lineName = parser.getAttributeValue(null, _train_info_alternative_name);


        //name can have two sources: n (name) oder l (line) attribute. line is only used when category is "S"
        String name = null;
        if (S.equals(category)) {
            name = lineName;
        } else {
            name = genuineName;
        }

        final @Nullable String type = parser.getAttributeValue(null, _train_info_type);
        if ("e".equals(type)) {
            info.setReplacement(true);
        }
        if ("s".equals(type)) {
            info.setSpecial(true);
        }


        if (name != null) {
            info.setTrainName(name);
        }
        if (lineName != null) {
            info.setTrainLineName(lineName);
        }
        if (genuineName != null) {
            info.setGenuineName(genuineName);
        }

        parser.nextTag();
    }

    private static void readRef(TrainInfo info, XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, _ref);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(_train_info)) {
                readInfo(info, parser);
            } else {
                skip(parser);
            }
        }
    }

    static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    static Map<String, TrainInfo> readTrains(Map<String, TrainInfo> target, XmlPullParser parser) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(_train)) {
                final TrainInfo train = readTrain(parser);
                target.put(train.getId(), train);
            } else {
                skip(parser);
            }
        }
        return target;
    }

    static boolean isAdditional(@Nullable final TrainMovementInfo trainMovementInfo) {
        return trainMovementInfo != null && trainMovementInfo.isAdditional();
    }

    public static Map<String, TrainInfo> merge(@NonNull Map<String, TrainInfo> target, @NonNull Map<String, TrainInfo> source) {
        for (TrainInfo sourceTrainInfo : source.values()) {
            final TrainInfo targetTrainInfo = target.get(sourceTrainInfo.getId());
            if (targetTrainInfo == null) {
                target.put(sourceTrainInfo.getId(), sourceTrainInfo);
            } else {
                targetTrainInfo.merge(sourceTrainInfo);
            }
        }

        return target;
    }

    // target = plandaten = initials
    // flatChanges = changes
    @NonNull
    public static Map<String, TrainInfo> mergeChanges(@NonNull Map<String, TrainInfo> target,
                                                      @NonNull Collection<TrainInfo> flatChanges,
                                                      long actualTime,
                                                      long endOfPlanTime,
                                                      @NonNull Map<String,TrainInfo> missingTrainInfos) {

        for (TrainInfo sourceTrainInfo : flatChanges) {
            final TrainInfo targetTrainInfo = target.get(sourceTrainInfo.getId());

            if (targetTrainInfo == null) {
                if (sourceTrainInfo.isReplacement() || sourceTrainInfo.isSpecial() || sourceTrainInfo.isAdditional()) {
                    target.put(sourceTrainInfo.getId(), sourceTrainInfo);
                } else {

                    // check departure
                    if (sourceTrainInfo.departure != null &&
                            !sourceTrainInfo.departure.isTrainMovementCancelled() &&
                            sourceTrainInfo.departure.getCorrectedDateTime() > actualTime &&
                            sourceTrainInfo.departure.getCorrectedDateTime() < endOfPlanTime
                    ) {
                        // missing train found
                        missingTrainInfos.put(sourceTrainInfo.getId(), sourceTrainInfo);
                    }

                    // check arrival
                    if (sourceTrainInfo.arrival != null &&
                            !sourceTrainInfo.arrival.isTrainMovementCancelled() &&
                            sourceTrainInfo.arrival.getCorrectedDateTime() > actualTime &&
                            sourceTrainInfo.arrival.getCorrectedDateTime() < endOfPlanTime
                    ) {
                        if(sourceTrainInfo.departure != null &&  sourceTrainInfo.departure.getCorrectedDateTime() >= endOfPlanTime) {
                            // special case: a train outside of our plan data in the future arrives early: ignore it!
                        }
                        else {
                            // missing train found, possibly overwrite...
                            missingTrainInfos.put(sourceTrainInfo.getId(), sourceTrainInfo);
                        }
                    }

                }
            } else {
                targetTrainInfo.merge(sourceTrainInfo);
            }
        }

        return target;
    }

    private boolean isAdditional() {
        return isAdditional(departure) || isAdditional(arrival);
    }

    public interface ChangeListener {
        void onTrainInfoChanged(TrainInfo trainInfo);
    }

    private String id;
    private TrainMovementInfo arrival;
    private TrainMovementInfo departure;
    private String trainCategory;
    private String trainName;
    private String trainGenericName;
    private boolean hasMessage;
    private TrainInfo referenceTrainInfo; // If TrainInfo has a reference train
    private boolean hasWagenstand;

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    protected TrainInfo(Parcel in) {
        id = in.readString();
        final ClassLoader classLoader = TrainMovementInfo.class.getClassLoader();
        arrival = in.readParcelable(classLoader);
        departure = in.readParcelable(classLoader);
        trainCategory = in.readString();
        trainName = in.readString();
        trainGenericName = in.readString();
        hasMessage = in.readInt() == 1;
        referenceTrainInfo = in.readParcelable(TrainInfo.class.getClassLoader());
        hasWagenstand = in.readInt() == 1;
        genuineName = in.readString();
        replacement = ParcelCompat.readBoolean(in);
        special = ParcelCompat.readBoolean(in);
        showWagonOrder=ParcelCompat.readBoolean(in);
    }

    public TrainInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(arrival, 0);
        dest.writeParcelable(departure, 0);
        dest.writeString(trainCategory);
        dest.writeString(trainName);
        dest.writeString(trainGenericName);
        dest.writeInt(hasMessage ? 1 : 0);
        dest.writeParcelable(referenceTrainInfo, 0);
        dest.writeInt(hasWagenstand ? 1 : 0);
        dest.writeString(genuineName);
        ParcelCompat.writeBoolean(dest, replacement);
        ParcelCompat.writeBoolean(dest, special);
        ParcelCompat.writeBoolean(dest, showWagonOrder);
    }

    public static final Creator<TrainInfo> CREATOR = new Creator<TrainInfo>() {
        @Override
        public TrainInfo createFromParcel(Parcel in) {
            return new TrainInfo(in);
        }

        @Override
        public TrainInfo[] newArray(int size) {
            return new TrainInfo[size];
        }
    };

    private boolean hasMessage() {
        return hasMessage;
    }

    private void setHasMessage(boolean hasMessage) {
        this.hasMessage = hasMessage;
    }

    /**
     * Must not be null but possibly is
     */
    @Nullable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TrainMovementInfo getArrival() {
        return arrival;
    }

    private void setArrival(TrainMovementInfo arrival) {
        this.arrival = arrival;
    }

    public TrainMovementInfo getDeparture() {
        return departure;
    }

    private void setDeparture(TrainMovementInfo departure) {
        this.departure = departure;
    }

    public String getTrainName() {
        return trainName;
    }

    private void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    /**
     * Must not be null but possibly is
     */
    @Nullable
    public String getTrainCategory() {
        return trainCategory;
    }

    public String determineTrainType() {
        return trainCategory;
    }

    private void setTrainCategory(String trainCategory) {
        this.trainCategory = trainCategory;
    }

    public String getTrainGenericName() {
        return trainGenericName;
    }

    private void setTrainLineName(String trainGenericName) {
        this.trainGenericName = trainGenericName;
    }

    TrainInfo getReferenceTrainInfo() {
        return referenceTrainInfo;
    }

    private void setReferenceTrainInfo(TrainInfo referenceTrainInfo) {
        this.referenceTrainInfo = referenceTrainInfo;
    }

    public boolean getHasWagenstand() {
        return hasWagenstand;
    }

    public void setHasWagenstand(boolean hasWagenstand) {
        if (hasWagenstand != this.hasWagenstand) {
            this.hasWagenstand = hasWagenstand;

            notifyChangeListeners();
        }
    }

    private void notifyChangeListeners() {
        for (ChangeListener changeListener : changeListeners) {
            changeListener.onTrainInfoChanged(this);
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    public String replacementTrainMessage(String lineIdentifier) {
        if (isReplacement()) {
            if (getReferenceTrainInfo() != null) {

                if (!TextUtils.isEmpty(lineIdentifier)) {
                    return String.format("Ersatzzug für %s %s",
                            getReferenceTrainInfo().getTrainCategory(),
                            lineIdentifier);
                }

                return String.format("Ersatzzug für %s %s",
                        getReferenceTrainInfo().getTrainCategory(),
                        getReferenceTrainInfo().getTrainName());
            }

            return "Ersatzzug";
        }

        if (isSpecial()) {
            return "Sonderzug";
        }

        return null;
    }


    //Two TrainInfo object are equal if they share an equal Id.
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TrainInfo) || getId() == null) {
            return false;
        }

        return getId().equals(((TrainInfo) o).getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static JSONArray toJSONArray(List<TrainInfo> trains) {
        JSONArray result = new JSONArray();
        if (trains != null) {
            for (TrainInfo ti : trains) {
                result.put(ti.toJSON());
            }
        }
        return result;
    }

    private static final String NAME = "name";
    private static final String GENERIC_NAME = "generic_name";
    private static final String ID = "id";
    private static final String ARRIVAL = "arrival";
    private static final String DEPARTURE = "departure";
    private static final String HASMESSAGE = "hasmessage";
    private static final String CATEGORY = "cat";


    private JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put(NAME, getTrainName());
            result.put(GENERIC_NAME, getTrainGenericName());
            result.put(ID, getId());
            if (getArrival() != null) {
                result.put(ARRIVAL, getArrival().toJSON());
            }
            if (getDeparture() != null) {
                result.put(DEPARTURE, getDeparture().toJSON());
            }
            result.put(HASMESSAGE, hasMessage);
            result.put(CATEGORY, getTrainCategory());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public static List<TrainInfo> fromJSONArray(JSONArray jsonArray) {
        List<TrainInfo> result = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    TrainInfo ti = TrainInfo.fromJSON(jsonArray.getJSONObject(i));
                    if (ti != null) {
                        result.add(ti);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static TrainInfo fromJSON(JSONObject jsonObject) {
        TrainInfo result = new TrainInfo();
        try {
            result.setId(jsonObject.getString(ID));
            result.setTrainName(jsonObject.getString(NAME));
            result.setTrainLineName(jsonObject.getString(GENERIC_NAME));
            result.setTrainCategory(jsonObject.getString(CATEGORY));

            if (!jsonObject.isNull(HASMESSAGE)) {
                result.setHasMessage(jsonObject.getBoolean(HASMESSAGE));
            }
            if (jsonObject.has(ARRIVAL)) {
                result.setArrival(TrainMovementInfo.fromJSON(jsonObject.getJSONObject(ARRIVAL)));
            }
            if (jsonObject.has(DEPARTURE)) {
                result.setDeparture(TrainMovementInfo.fromJSON(jsonObject.getJSONObject(DEPARTURE)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public boolean supportsWagonOrder() {
        return shouldOfferWagenOrder(); // todo #cr
    }

    public static class Comparator implements java.util.Comparator<TrainInfo> {

        // times are the same so compare the track
        private final Collator collator = Collator.getInstance();
        private final TrainEvent trainEvent;

        public Comparator(TrainEvent trainEvent) {
            this.trainEvent = trainEvent;
        }

        @Override
        public int compare(TrainInfo lhs, TrainInfo rhs) {
            if (lhs == null || rhs == null) {
                return 0;
            }

            final TrainMovementInfo lhsTrainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(lhs);
            final TrainMovementInfo rhsTrainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(rhs);

            if (lhsTrainMovementInfo == null ||
                    rhsTrainMovementInfo == null ||
                    lhsTrainMovementInfo.getPlannedDateTime() == -1) {
                return 0;
            }

            int a = (int) lhsTrainMovementInfo.getPlannedDateTime();
            int b = (int) rhsTrainMovementInfo.getPlannedDateTime();

            int cmp = Integer.compare(a, b);

            if (cmp == 0) {
                cmp = collator.compare(
                        lhsTrainMovementInfo.getDisplayPlatform(),
                        rhsTrainMovementInfo.getDisplayPlatform());
            }

            return cmp;
        }
    }

    public TrainInfo merge(TrainInfo other) {
        if (other.hasMessage()) {
            this.setHasMessage(true);
        }

        TrainMovementInfo newArrivalData = other.getArrival();
        TrainMovementInfo oldArrivalData = this.getArrival();

        if (newArrivalData != null) {
            //update arrival

            if (oldArrivalData != null) {
                oldArrivalData.merge(other.getArrival());
            } else {
                setArrival(newArrivalData);
            }
        }

        TrainMovementInfo newDepartureData = other.getDeparture();
        TrainMovementInfo oldDepartureData = this.getDeparture();

        if (newDepartureData != null) {
            //update departure

            if (oldDepartureData != null) {
                oldDepartureData.merge(newDepartureData);
            } else {
                setDeparture(newDepartureData);
            }
        }

        // Update general Stop Information

        // TODO This should be the changed train
        if (other.getTrainCategory() != null) {
            this.setTrainCategory(other.getTrainCategory());
        }
        if (other.getTrainName() != null) {
            this.setTrainName(other.getTrainName());
        }

        return this;
    }

    private final static List<String> CATEGORIES_WITH_WAGON_ORDER = Arrays.asList(
            ICE,
            IC,
            EC);


    public boolean shouldOfferWagenOrder() {
        final String trainCategory = getTrainCategory();
        return trainCategory != null && CATEGORIES_WITH_WAGON_ORDER.contains(trainCategory.toUpperCase());
    }

    public void logDeparture(Boolean withId) {

        if (departure == null) return;

        long plannedTime = departure.getPlannedDateTime();
        long correctedTime = departure.getCorrectedDateTime();
        String lineIdentifier = departure.getLineIdentifier();
        if (lineIdentifier == null) {
            lineIdentifier = this.trainCategory;
            if (lineIdentifier == null)
                lineIdentifier = "";
            else
                lineIdentifier += " " + this.trainName;
        }

        if (withId)
            Log.d(
                    "dbg",
                    String.format("%-36s", getId()) +
                            String.format("%-8s", lineIdentifier) +
                            DebugX.Companion.getFormattedDateTimeFromMillis(
                                    plannedTime,
                                    " pl: ",
                                    "HH:mm"
                            ) +
                            DebugX.Companion.getFormattedDateTimeFromMillis(
                                    correctedTime,
                                    " co: ",
                                    "HH:mm"
                            )

            );
        else
            Log.d(
                    "dbg",
                    String.format("%-8s", lineIdentifier) +
                            DebugX.Companion.getFormattedDateTimeFromMillis(
                                    plannedTime,
                                    " pl: ",
                                    "HH:mm"
                            ) +
                            DebugX.Companion.getFormattedDateTimeFromMillis(
                                    correctedTime,
                                    " co: ",
                                    "HH:mm"
                            ));

    }

}
