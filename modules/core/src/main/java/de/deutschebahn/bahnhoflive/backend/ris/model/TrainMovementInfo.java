/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.ris.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.deutschebahn.bahnhoflive.ui.map.content.rimap.TrackKt;
import de.deutschebahn.bahnhoflive.util.DateUtil;

public class TrainMovementInfo implements Parcelable {

    static final String _arrival = "ar";
    static final String _departure = "dp";

    private static final String _hidden = "hi";
    private static final String _datetime = "pt";
    private static final String _platform = "pp";
    private static final String _correctedPlatform = "cp";
    private static final String _via = "ppth";
    private static final String _distantEndpoint = "pde";
    private static final String _correctedVia = "cpth";
    private static final String _wings = "wings";
    private static final String _correctedTime = "ct";
    private static final String _correctedStatus = "cs";
    private static final String _plannedStatus = "ps";
    private static final String _message = "m";
    private static final String TRAIN_INFO_ALTERNATIVE_NAME = TrainInfo._train_info_alternative_name;
    public static final Pattern TRACK_PATTERN = TrackKt.getTRACK_PATTERN();

    public static final String PLANNED_STATUS_ADDITIONAL = "a";

    private static Map<String, String> map = null;
    private static Map<String, String[]> codesMap = null;

    private String platform;
    private String via;
    private String wings;
    private String hidden;

    private String correctedPlatform;
    private String correctedVia;
    private String internalText;
    private String externalText;
    private String plannedStatus;
    private String correctedStatus;

    private String lineIdentifier;

    private String qosMessages;

    private String splitMessage;

    private String distantEndpoint;

    private long plannedDateTime = -1;
    private long correctedDateTime = -1;

    private boolean hasMessage;

    private static final String TIME = "time";
    private static final String HASMESSAGE = "hasmessage";
    private static final String PLATFORM = "platform";
    private static final String VIA = "via";
    private static final String CORRECTED_VIA = "cvia";
    private static final String DISTANT_ENDPOINT = "pde";
    private static final String WINGS = "wings";
    private static final String HIDDEN = "hi";
    private static final String CORRECTED_TIME = "ct";
    private static final String CORRECTED_PLATFORM = "cp";
    private static final String INTERNAL_TEXT = "int";
    private static final String EXTERNAL_TEXT = "ext";
    private static final String PLANNED_STATUS = "ps";
    private static final String CORRECTED_STATUS = "cs";
    private static final String QOS_MESSAGE = "qmessage";

    public TrainMovementInfo() {

    }

    static TrainMovementInfo readMovement(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (XmlPullParser.START_TAG!= parser.getEventType()
                || ( !_arrival.equals( parser.getName() ) &&
                (!_departure.equals(parser.getName())) ) )
            throw new XmlPullParserException( "expected train movement info (<ar> or <dp>)");

        TrainMovementInfo result = new TrainMovementInfo();
        result.setPlatform(parser.getAttributeValue(null, _platform));
        if(result.getPlatform()==null || result.getPlatform().isEmpty())
            result.setPlatform("k.A.");
        result.setVia(parser.getAttributeValue(null, _via));
        result.setDistantEndpoint(parser.getAttributeValue(null, _distantEndpoint));
        result.setWings(parser.getAttributeValue(null, _wings));
        result.setHidden(parser.getAttributeValue(null, _hidden));
        result.setCorrectedPlatform(parser.getAttributeValue(null, _correctedPlatform));
        result.setPlannedStatus(parser.getAttributeValue(null, _plannedStatus));
        result.setCorrectedStatus(parser.getAttributeValue(null, _correctedStatus));
        result.setCorrectedVia(parser.getAttributeValue(null, _correctedVia));

        String correctedDateTimeString = parser.getAttributeValue(null, _correctedTime);
        String plannedDateTimeString = parser.getAttributeValue(null, _datetime);

        if (parser.getAttributeValue(null, TRAIN_INFO_ALTERNATIVE_NAME) != null) {
            result.setLineIdentifier(parser.getAttributeValue(null, TRAIN_INFO_ALTERNATIVE_NAME));
        }

        if (correctedDateTimeString != null) {
            Date correctedDateTime = DateUtil.parseIRISDateTime(correctedDateTimeString);
            result.setCorrectedDateTime(correctedDateTime.getTime());
        }

        if (plannedDateTimeString != null) {
            Date plannedDateTime = DateUtil.parseIRISDateTime(plannedDateTimeString);
            result.setPlannedDateTime(plannedDateTime.getTime());
        }

        ArrayList<Message> messages = new ArrayList<>();

        int depth = 0;
        while (parser.next() != XmlPullParser.END_TAG || depth!=0) {
            if (parser.getEventType()==XmlPullParser.END_TAG) {
                depth--;
            }
            if (parser.getEventType()!=XmlPullParser.START_TAG) {
                continue;
            }
            depth++;
            if (_message.equals(parser.getName())) {
                Message message = readMessage(parser);
                if (message != null) {
                    messages.add(message);
                }
            }
        }

        // check if messages have been revoked

        ArrayList<Message> reversedMessages = new ArrayList<>(messages);
        Collections.reverse(reversedMessages);
        for (Message message : reversedMessages) {
            String code = message.getCode();
            String[] revocationCodes = revocationCodesForKey(code);

            for (String revocationCode : revocationCodes) {
                for (Message revocableMessage : messages) {
                    if (revocableMessage.getCode().equals(revocationCode)
                            && !message.getId().equals(revocableMessage.getId())) {
                        revocableMessage.setRevoked(true);
                    }
                }
            }
        }

        if (messages != null && messages.size()> 0) {
            String finalMessage = buildQosMessageForTrain(result, messages);
            if (finalMessage != null) {
                result.setQosMessages(finalMessage);
            }
        }

        return result;
    }

    public static String buildQosMessageForTrain(TrainMovementInfo train, ArrayList<Message> messages) {
        if (train.isTrainMovementCancelled()) {
            return null;
        }

        ArrayList<String> displayMessages = new ArrayList<>();
        for (Message message : messages) {
            if (!message.isRevoked()) {
                final String displayMessage = message.getDisplayMessage();
                if (displayMessage != null && !displayMessages.contains(displayMessage)) {
                    displayMessages.add(displayMessage);
                }
            }
        }

        if (displayMessages.size() == 0) {
            return null;
        }

        String _message = TextUtils.join(" +++ ", displayMessages);
        return _message;
    }

    private static Message readMessage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, _message);

        String type = parser.getAttributeValue(null, "t");
        String code = parser.getAttributeValue(null, "c");

        if (type == null || code == null) {
            return null;
        }

        if (type.equals("q") && relevevantMessage(code)) {
            // qos message

            Message message = new Message();

            String messageId = parser.getAttributeValue(null, "id");
            String validFrom = parser.getAttributeValue(null, "from");
            String validTo = parser.getAttributeValue(null, "to");
            String _deleted = parser.getAttributeValue(null, "del");
            boolean deleted = false;
            if (_deleted != null) {
                deleted = Integer.parseInt(_deleted) == 1;
                if (deleted) {
                    return null;
                }
            }

            message.setId(messageId);
            message.setValidFrom(validFrom);
            message.setValidTo(validTo);
            message.setCode(code);
            message.setType(type);
            message.setDeleted(deleted);
            message.setDisplayMessage(messageForKey(code));

            return message;
        }

        return null;
    }

    private static boolean relevevantMessage(String code) {
        return RimapMessageCodes.INSTANCE.isCodeRelevant(code);
    }

    @Nullable
    private static String messageForKey(String key) {
        return RimapMessageCodes.INSTANCE.getMessages().get(key);
    }

    @NonNull
    private static String[] revocationCodesForKey(String key) {
        return RimapMessageCodes.INSTANCE.getRevocationsOf(key);
    }

    public long getPlannedDateTime() { return plannedDateTime; }

    private void setPlannedDateTime(long dateTime) { this.plannedDateTime = dateTime; }

    public long getCorrectedDateTime() { return correctedDateTime; }

    private void setCorrectedDateTime(long delay) { this.correctedDateTime = delay; }

    public String getPlatform() {
        return platform != null ? platform : "";
    }

    private void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVia() {
        return via;
    }

    private void setVia(String via) {
        this.via = via;
    }

    public String getWings() {
        return wings;
    }

    private void setWings(String swig) {
        this.wings = swig;
    }

    public String getHidden(){
        return (hidden==null)?"0":hidden;
    }

    private void setHidden(String hidden) { this.hidden = hidden; }

    public boolean hasMessage() { return hasMessage; }

    private void setHasMessage(boolean hasMessage) { this.hasMessage = hasMessage; }

    public String getLineIdentifier() {
        return lineIdentifier;
    }

    private void setLineIdentifier(String lineIdentifier) {
        this.lineIdentifier = lineIdentifier;
    }

    public String getCorrectedPlatform() {
        return correctedPlatform;
    }

    private void setCorrectedPlatform(String correctedPlatform) { this.correctedPlatform = correctedPlatform; }

    public String getCorrectedVia() { return correctedVia; }

    private void setCorrectedVia(String correctedVia) { this.correctedVia = correctedVia; }

    public String getInternalText() { return internalText; }

    private void setInternalText(String internalText) { this.internalText = internalText; }

    public String getExternalText() { return externalText; }

    private void setExternalText(String externalText) { this.externalText = externalText; }

    public String getPlannedStatus() { return plannedStatus; }

    private void setPlannedStatus(String plannedStatus) { this.plannedStatus = plannedStatus; }

    public String getCorrectedStatus() { return correctedStatus; }

    private void setCorrectedStatus(String correctedStatus) { this.correctedStatus = correctedStatus; }

    public String getQosMessages() {
        return qosMessages;
    }

    private void setQosMessages(String qosMessages) {
        this.qosMessages = qosMessages;
    }

    public String getSplitMessage() {
        return splitMessage;
    }

    public void setSplitMessage(String splitMessage) {
        this.splitMessage = splitMessage;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put(HASMESSAGE, hasMessage());
            result.put(PLATFORM, getPlatform());
            result.put(VIA, getVia());
            result.put(DISTANT_ENDPOINT, getDistantEndpoint());
            result.put(WINGS, getWings());
            result.put(HIDDEN, getHidden());
            result.put(CORRECTED_PLATFORM, getCorrectedPlatform());
            result.put(INTERNAL_TEXT, getInternalText());
            result.put(EXTERNAL_TEXT, getExternalText());
            result.put(PLANNED_STATUS, getPlannedStatus());
            result.put(CORRECTED_STATUS, getCorrectedStatus());
            result.put(CORRECTED_VIA, getCorrectedVia());
            result.put(TIME, getPlannedDateTime());
            result.put(QOS_MESSAGE, getQosMessages());
            result.put(CORRECTED_TIME, getCorrectedDateTime());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    protected TrainMovementInfo(Parcel in) {
        hasMessage = in.readInt() == 1;
        platform = in.readString();
        via = in.readString();
        distantEndpoint = in.readString();
        wings = in.readString();
        hidden = in.readString();
        correctedPlatform = in.readString();
        internalText = in.readString();
        externalText = in.readString();
        plannedStatus = in.readString();
        correctedStatus = in.readString();
        correctedVia = in.readString();
        plannedDateTime = in.readLong();
        qosMessages = in.readString();
        correctedDateTime = in.readLong();
        splitMessage = in.readString();
        lineIdentifier = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(hasMessage ? 0 : 1);
        dest.writeString(platform);
        dest.writeString(via);
        dest.writeString(distantEndpoint);
        dest.writeString(wings);
        dest.writeString(hidden);
        dest.writeString(correctedPlatform);
        dest.writeString(internalText);
        dest.writeString(externalText);
        dest.writeString(plannedStatus);
        dest.writeString(correctedStatus);
        dest.writeString(correctedVia);
        dest.writeLong(plannedDateTime);
        dest.writeString(qosMessages);
        dest.writeLong(correctedDateTime);
        dest.writeString(splitMessage);
        dest.writeString(lineIdentifier);
    }

    public static final Creator<TrainMovementInfo> CREATOR = new Creator<TrainMovementInfo>() {
        @Override
        public TrainMovementInfo createFromParcel(Parcel in) {
            return new TrainMovementInfo(in);
        }

        @Override
        public TrainMovementInfo[] newArray(int size) {
            return new TrainMovementInfo[size];
        }
    };

    public static TrainMovementInfo fromJSON(JSONObject jsonObject) {
        if (jsonObject==null) {
            return null;
        }
        TrainMovementInfo tmi = new TrainMovementInfo();
        try {

            tmi.setPlannedDateTime(jsonObject.getLong(TIME));

            if (!jsonObject.isNull(HASMESSAGE)) {
                tmi.setHasMessage(jsonObject.optBoolean(HASMESSAGE));
            } else {
                tmi.setHasMessage(false);
            }
            if (!jsonObject.isNull(PLATFORM)) {
                tmi.setPlatform(jsonObject.getString(PLATFORM));
            }
            if (!jsonObject.isNull(VIA)) {
                tmi.setVia(jsonObject.getString(VIA));
            }
            if (!jsonObject.isNull(DISTANT_ENDPOINT)) {
                tmi.setDistantEndpoint(jsonObject.getString(DISTANT_ENDPOINT));
            }
            if (!jsonObject.isNull(HIDDEN)) {
                tmi.setHidden(jsonObject.getString(HIDDEN));
            }
            if (!jsonObject.isNull(WINGS)) {
                tmi.setWings(jsonObject.getString(WINGS));
            }
            if (!jsonObject.isNull(CORRECTED_STATUS)) {
                tmi.setCorrectedStatus(jsonObject.getString(CORRECTED_STATUS));
            }
            if (!jsonObject.isNull(CORRECTED_PLATFORM)) {
                tmi.setCorrectedPlatform(jsonObject.getString(CORRECTED_PLATFORM));
            }
            if (!jsonObject.isNull(CORRECTED_VIA)) {
                tmi.setCorrectedVia(jsonObject.getString(CORRECTED_VIA));
            }
            if (!jsonObject.isNull(INTERNAL_TEXT)) {
                tmi.setInternalText(jsonObject.getString(INTERNAL_TEXT));
            }
            if (!jsonObject.isNull(EXTERNAL_TEXT)) {
                tmi.setInternalText(jsonObject.getString(EXTERNAL_TEXT));
            }
            if (!jsonObject.isNull(CORRECTED_TIME)) {
                Date correctedTime = new Date(tmi.getCorrectedDateTime());
                tmi.setCorrectedDateTime(correctedTime.getTime());
            }
            if (!jsonObject.isNull(QOS_MESSAGE)) {
                tmi.setQosMessages(jsonObject.getString(QOS_MESSAGE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return tmi;
    }

    public long delayInMinutes() {
        long correctedTime = getCorrectedDateTime();
        if (correctedTime <= 0) {
            return 0;
        }

        long delay = correctedTime - getPlannedDateTime();

        return (long) (delay / 1000f / 60f);
    }

    public String formattedViaStations(boolean departure) {
        //via
        ArrayList<String> sanitizedStops = (ArrayList<String>)getCorrectedViaAsArray().clone();
        // if type is arrival...
        if (!departure) {
            // Remove the first Stop because it is already visible as Station Label.
            sanitizedStops.remove(0);
        } else {
            // otherwise remove the last stop
            sanitizedStops.remove(sanitizedStops.size()-1);
        }

        if (sanitizedStops.size() > 0) {
            // Convert the sanitized via list to a comma separated String.
            return TextUtils.join(", ", sanitizedStops);
        } else {
            return "-";
        }
    }

    public String getDestinationStop(boolean departure) {
        if (departure && !TextUtils.isEmpty(distantEndpoint)) {
            return distantEndpoint;
        }

        return getViaDestinationStop(departure);
    }

    private String getViaDestinationStop(boolean departure) {
        ArrayList<String> stops = getFormattedVia();

        final int noOfStops = stops.size();
        if (noOfStops <= 0) {
            return "";
        }
        if (departure) {
            return stops.get(noOfStops - 1);
        } else {
            return stops.get(0);
        }
    }

    public boolean isAdditional() {
        return PLANNED_STATUS_ADDITIONAL.equals(getPlannedStatus());
    }

    public String getAdditionalTrainMessage() {

        String correctedStatus = getCorrectedStatus();
        String plannedStatus = getPlannedStatus();
        if (correctedStatus != null && correctedStatus.length() > 0) {
            plannedStatus = correctedStatus;
        }
        if (isAdditional()) {
            return "Zusätzlicher Halt";
        }

        return null;
    }

    public String actualVia() {
        return (getCorrectedVia() != null && getCorrectedVia().length() > 0) ? getCorrectedVia() : getVia();
    }

    @Nullable
    public String getPurePlatform() { // 1a, 5d-f
        final String bestPlatform = getBestPlatform();
        if (bestPlatform == null) {
            return null;
        }

        try {
        final Matcher matcher = TRACK_PATTERN.matcher(bestPlatform);
        return matcher.find() ? matcher.group() : null;
        }
        catch(Exception exception) {
            return null;
        }
    }

    @Nullable
    private String getBestPlatform() {
        if (getCorrectedPlatform() != null && getCorrectedPlatform().length() > 0) {
            return getCorrectedPlatform();
        } else if (getPlatform()!=null && getPlatform().length() > 0) {
            return getPlatform();
        }

        return null;
    }


    @NonNull
    public String getDisplayPlatform() {
        final String bestPlatform = getBestPlatform();
        return bestPlatform == null ? "k.A." : bestPlatform;
    }

    @Nullable
    public String getPlatformWithoutExtensions() { // 1a->1, 5d-f->5
        if(platform==null) return null;
        String platformString = platform.trim();

        String retString = "";

        for (int i = 0; i < platformString.length(); i++) {
            if( Character.isDigit(platformString.charAt(i)) )
                retString+=platformString.charAt(i);
        }


        if (retString.isEmpty()) return null;
        return retString;
    }

    public long getActualTime() {
        return getCorrectedDateTime() > -1 ? getCorrectedDateTime() : getPlannedDateTime();
    }

    public String getDelayMessage() {
        if (delayInMinutes() >= 5) {
            return String.format(Locale.GERMAN, "ca. %d Minuten später", delayInMinutes());
        }
        return null;
    }

    public String getPlatformMessage() {
        if (getCorrectedPlatform() != null && getCorrectedPlatform().length() > 0) {
            return String.format(Locale.GERMAN, "Heute Gleis %s", getCorrectedPlatform());
        }
        return null;
    }

    public String getMissingStationsMessage() {
        ArrayList<String> missingStations = new ArrayList<>();

        ArrayList<String> viaAsArray = getViaAsArray();
        ArrayList<String> correctedViaAsArray = getCorrectedViaAsArray();

        if (correctedViaAsArray.size() == 0) {
            return null;
        }

        for (String station : viaAsArray) {
            if (station.length() > 0 && !correctedViaAsArray.contains(station)) {
                missingStations.add(station);
            }
        }

        if (missingStations.size() > 0) {
            String stationsString = TextUtils.join(", ", missingStations);
            return String.format("Hält nicht in %s", stationsString);
        }

        return null;
    }

    public String getAddedStationsMessage() {
        ArrayList<String> addedStations = new ArrayList<>();
        ArrayList<String> viaAsArray = getViaAsArray();
        ArrayList<String> correctedViaAsArray = getCorrectedViaAsArray();

        if (correctedViaAsArray.size() == 0) {
            return null;
        }

        for (String station : correctedViaAsArray) {
            if (station.length() > 0 && !viaAsArray.contains(station)) {
                addedStations.add(station);
            }
        }

        if (addedStations.size() > 0) {
            String stationsString = TextUtils.join(", ", addedStations);
            return String.format("Hält auch in %s", stationsString);
        }
        return null;
    }

    public boolean isTrainMovementCancelled() {
        return isStatusCanceled(getCorrectedStatus());
    }

    private boolean isStatusCanceled(String correctedStatus) {
        return correctedStatus != null && correctedStatus.equals("c");
    }

    public boolean isCompletelyCanceled() {
        return isTrainMovementCancelled() || isStatusCanceled(getPlannedStatus());
    }

    public String getFormattedTime() {
        return formatTime(getPlannedDateTime());
    }

    private String formatTime(long time) {
        Date dateTime = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(dateTime);
    }

    public String getFormattedDate() {
        return formatDate(getPlannedDateTime());
    }

    private String formatDate(long time) {
        Date dateTime = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        return sdf.format(dateTime);
    }

    public String getFormattedActualTime() {
        final long correctedDateTime = getCorrectedDateTime();
        return correctedDateTime < 0 ? getFormattedTime() : formatTime(correctedDateTime);
    }

    public ArrayList<String> getViaAsArray() {
        ArrayList<String> result = new ArrayList<>();
        if (getVia()!=null) {
            String [] via = getVia().split("\\|");
            result.addAll(Arrays.asList(via));
        }
        return result;
    }

    public ArrayList<String> getCorrectedViaAsArray() {
        ArrayList<String> result = new ArrayList<>();
        if (getCorrectedVia()!=null) {
            String [] via = getCorrectedVia().split("\\|");
            result.addAll(Arrays.asList(via));
        } else {
            return getViaAsArray();
        }
        return result;
    }

    public ArrayList<String> getFormattedVia() {
        ArrayList<String> result = new ArrayList<>();
        if (actualVia()!=null) {
            String [] via = actualVia().split("\\|");
            result.addAll(Arrays.asList(via));
        }
        return result;
    }

    public TrainMovementInfo merge(TrainMovementInfo other) {
        // update qos messages
        if (other.getQosMessages() != null) {
            setQosMessages(other.getQosMessages());
        }

        //update platform
        if (other.getCorrectedPlatform()!=null) {
            setCorrectedPlatform(other.getCorrectedPlatform());
            //setPlatform(other.getCorrectedPlatform());
        }

        // update path
        if (other.getCorrectedVia() != null) {
            setCorrectedVia(other.getCorrectedVia());
        }

        // update status
        if (other.getCorrectedStatus() != null) {
            setCorrectedStatus(other.getCorrectedStatus());
        }

        //set corrected planned time (uncertain whether case exists)
        if (other.getCorrectedDateTime() > -1) {
            setCorrectedDateTime(other.getCorrectedDateTime());
        }

        if (other.getPlannedDateTime() > -1) {
            setPlannedDateTime(other.getPlannedDateTime());
        }

        return this;
    }

    public void setDistantEndpoint(String distantEndpoint) {
        this.distantEndpoint = distantEndpoint;
    }

    public String getDistantEndpoint() {
        return distantEndpoint;
    }

    @Nullable
    public String getDistantEndpointMessage() {
        if (TextUtils.isEmpty(distantEndpoint)) {
            return null;
        }

        final String viaDestinationStop = getViaDestinationStop(true);

        if (distantEndpoint.equals(viaDestinationStop)) {
            return null;
        }

        return "Ab " + viaDestinationStop + " weiter in Richtung " + distantEndpoint;
    }
}
