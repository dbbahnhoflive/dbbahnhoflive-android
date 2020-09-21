/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.ris.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Xml;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RISTimetable implements Parcelable {

    //these are set by the RIS Request
    private final String evaId;
    private final Long hour;

    //these are set by the parser
    private String stationsName;
    private final Map<String, TrainInfo> trainInfos = new HashMap<>();

    protected RISTimetable(Parcel in) {
        evaId = in.readString();
        final long hour = in.readLong();
        this.hour = hour < 0 ? null : hour;
        stationsName = in.readString();
        ArrayList<TrainInfo> trainList = in.createTypedArrayList(TrainInfo.CREATOR);
        for (TrainInfo train : trainList) {
            trainInfos.put(train.getId(), train);
        }
    }

    public RISTimetable(String evaId, Long hour) {
        this.evaId = evaId;
        this.hour = hour;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(evaId);
        dest.writeLong(hour == null ? -1 : hour);
        dest.writeString(stationsName);
        dest.writeTypedList(getTrains());
    }

    public static final Creator<RISTimetable> CREATOR = new Creator<RISTimetable>() {
        @Override
        public RISTimetable createFromParcel(Parcel in) {
            return new RISTimetable(in);
        }

        @Override
        public RISTimetable[] newArray(int size) {
            return new RISTimetable[size];
        }
    };

    public String getEvaId() {
        return evaId;
    }

    private void setStationName(String stationsName) {
        this.stationsName = stationsName;
    }

    public List<TrainInfo> getTrains() {
        return new ArrayList<>(trainInfos.values());
    }

    //Parser section
    private static final String _timetable = "timetable";
    private static final String _station = "station";

    @NonNull
    public static RISTimetable parseRISTimetable(String evaId, final Long hour, byte[] bytes) throws XmlPullParserException, IOException {
        return withParser(bytes, parser -> readFeed(evaId, hour, parser));
    }

    interface ParserConsumer<T> {
        T consume(XmlPullParser parser) throws IOException, XmlPullParserException;
    }

    private static <T> T withParser(byte[] bytes, ParserConsumer<T> consumer) throws XmlPullParserException, IOException {
        final InputStream in = new ByteArrayInputStream(bytes);

        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return consumer.consume(parser);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static RISTimetable readFeed(String evaId, final Long hour, XmlPullParser parser) throws XmlPullParserException, IOException {
        RISTimetable result = new RISTimetable(evaId, hour);
        parser.require(XmlPullParser.START_TAG, null, _timetable);
        result.setStationName(parser.getAttributeValue(null, _station));

        TrainInfo.readTrains(result.trainInfos, parser);
        return result;
    }

    public static List<TrainInfo> readTrainInfos(byte[] bytes) throws IOException, XmlPullParserException {
        final HashMap<String, TrainInfo> target = new HashMap<>();
        withParser(bytes, parser -> TrainInfo.readTrains(target, parser));
        return new ArrayList<>(target.values());
    }

    @NonNull
    public static List<TrainInfo> filter(Collection<TrainInfo> trainInfos, TrainEvent trainEvent) {
        ArrayList<TrainInfo> filteredTrains = new ArrayList<>();
        for (TrainInfo i : trainInfos) {
            TrainMovementInfo tmi = trainEvent.movementRetriever.getTrainMovementInfo(i);

            //dont show train if max(tmi.plannedDateTime, correctedDateTime) is longer ago than MAX_PAST
            if (tmi != null && !isLongPast(tmi) && !tmi.getHidden().equals("1")) {
                filteredTrains.add(i);
            }
        }
        Collections.sort(filteredTrains, trainEvent.comparator);
        return filteredTrains;
    }

    @NonNull
    public static List<String> getTracksForFilter(List<TrainInfo> trainInfos) {
        final LinkedList<String> platformList = getTracks(trainInfos);

        platformList.add(0, "Alle");

        return platformList;
    }

    @NonNull
    public static LinkedList<String> getTracks(List<TrainInfo> trainInfos) {
        final Set<String> platformSet = new HashSet<>();

        for (TrainInfo trainInfo : trainInfos) {
            putTrack(platformSet, trainInfo, TrainEvent.DEPARTURE);
            putTrack(platformSet, trainInfo, TrainEvent.ARRIVAL);
        }

        final LinkedList<String> platformList = new LinkedList<>(platformSet);

        Collections.sort(platformList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                //comparing tracks which might contain single numbers, but also combination of numbers and chars or chars only, e.g. "k.a.", "7 A-D"

                try {
                    //try to convert strings into numbers and compare them
                    String d1 = extractDigits(o1);
                    String d2 = extractDigits(o2);

                    if (d1.length() > 0 && d2.length() > 0) {
                        int res = Integer.valueOf(d1).compareTo(Integer.valueOf(d2));
                        if (res == 0) {
                            //extracted digits are equal, compare original strings
                            return o1.compareTo(o2);
                        } else {
                            return res;
                        }
                    }
                } catch (Exception e) {
                    //ignore and try string compare
                }

                return o1.compareTo(o2);
            }
        });
        return platformList;
    }

    private static void putTrack(Set<String> platforms, TrainInfo trainInfo, TrainEvent trainEvent) {
        final TrainMovementInfo trainMovementInfo = trainEvent.movementRetriever.getTrainMovementInfo(trainInfo);
        if (trainMovementInfo != null) {
            final String platform = trainMovementInfo.getPurePlatform();
            if (platform != null) {
                platforms.add(platform);
            }
        }
    }

    private static String extractDigits(String src) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    @NonNull
    public static List<String> getTrainCategories(Collection<TrainInfo> trainInfos) {

        ArrayList<String> traintypes = new ArrayList<>();

        for (TrainInfo trainInfo : trainInfos) {
            String traintype = trainInfo.getTrainCategory();
            if (traintype != null && !traintypes.contains(traintype)) {
                traintypes.add(traintype);
            }
        }

        Collections.sort(traintypes);
        traintypes.add(0, "Alle");

        return traintypes;
    }

    public static <T extends Collection<TrainInfo>> T determineSplitMessages(final T trainInfos, TrainEvent trainEvent) {
        for (TrainInfo trainInfo : trainInfos) {

            TrainMovementInfo referenceItem = trainEvent.movementRetriever.getTrainMovementInfo(trainInfo);
            String wingsReference = referenceItem.getWings();

            if (wingsReference == null || wingsReference.length() == 0) {
                // skip if there is now wings information
                continue;
            }
            // search for referenceId

            for (TrainInfo referenceTrainInfo : trainInfos) {
                if (referenceTrainInfo.getId().contains(wingsReference)) {
                    // we found a matching reference item

                    applySplitMessages(referenceItem,
                            trainEvent.movementRetriever.getTrainMovementInfo(referenceTrainInfo));
                }
            }
        }
        return trainInfos;
    }

    private static void applySplitMessages(TrainMovementInfo trainMovementInfo, TrainMovementInfo referencedTrainMovementInfo) {

        final List<String> matchingTrainStations = referencedTrainMovementInfo.getFormattedVia();

        String matchingFirstStation = matchingTrainStations.get(0);
        String firstReferenceStation = trainMovementInfo.getFormattedVia().get(0);

        if (matchingFirstStation.equals(firstReferenceStation)) {
            // first two stations are the same so we assume the train will split up

            String previousStation = "";
            for (String station : trainMovementInfo.getFormattedVia()) {

                boolean doesntContainStation = !matchingTrainStations.contains(station);
                if (doesntContainStation) {
                    referencedTrainMovementInfo.setSplitMessage(String.format("Zugteilung in %s", previousStation));
                    trainMovementInfo.setSplitMessage(String.format("Zugteilung in %s", previousStation));

                    break;
                }
                previousStation = station;
            }

        } else {
            //first two stations defer, so we assume the trains will join

            for (String station : trainMovementInfo.getFormattedVia()) {
                boolean containsStation = matchingTrainStations.contains(station);
                if (containsStation) {
                    referencedTrainMovementInfo.setSplitMessage(String.format("Vereinigung in %s", station));
                    trainMovementInfo.setSplitMessage(String.format("Vereinigung in %s", station));

                    break;
                }
            }
        }
    }

    private static boolean isLongPast(TrainMovementInfo tmi) {
        Date plannedTime = new Date(tmi.getPlannedDateTime());
        long eventTime = plannedTime.getTime();

        long correctedTime = (tmi.getCorrectedDateTime() <= 0) ? 0 : tmi.getCorrectedDateTime() - eventTime;
        //long delayMinutes = (int)(correctedTime/1000f/60f);

        long now = System.currentTimeMillis();
        long finalTime = eventTime + correctedTime;

        return finalTime < now;
    }

    @NonNull
    public Map<String, TrainInfo> getTrainInfos() {
        return trainInfos;
    }

}
