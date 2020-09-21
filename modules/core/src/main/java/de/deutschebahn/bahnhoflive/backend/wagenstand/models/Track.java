/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Track implements Comparable<Track>, Parcelable {

    private String number;
    private String verboseName;

    public Track(String number, String verboseName) {
        this.number = number;
        this.verboseName = verboseName;
    }

    protected Track(Parcel in) {
        this(in.readString(), in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(number);
        dest.writeString(verboseName);
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public static ArrayList<Track> fromJSON(JSONArray tracksJSONArray) {

        ArrayList<Track> parsedTracks = new ArrayList<>(tracksJSONArray.length());

        for (int i = 0; i < tracksJSONArray.length(); i++) {

            try {

                JSONObject trackJSONObject = (JSONObject) tracksJSONArray.get(i);

                String number = trackJSONObject.getString("number");
                String verboseName = trackJSONObject.getString("name");

                if (!number.matches("\\d+")) {
                    // if track number doesnt contain digits, filter it!
                    continue;
                }

                Track track = new Track(number, verboseName);
                parsedTracks.add(track);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        Collections.sort(parsedTracks);

        return parsedTracks;
    }

    public static String[] numbersFromList(List<Track>tracks) {

        if (tracks == null) {
            return new String[]{};
        }

        ArrayList<String> trackNumbers = new ArrayList<>();
        Collections.sort(tracks);

        for (Track track : tracks) {
            trackNumbers.add(track.number);
        }

        return trackNumbers.toArray(new String[]{});
    }


    public int compareTo(Track track) {
        try {
            int lhTrackNumber = Integer.parseInt(removeNonDigits(this.number));
            int rhTrackNumber = Integer.parseInt(removeNonDigits(track.number));

            int ret = lhTrackNumber - rhTrackNumber;

            if (ret == 0) {
                ret = lhTrackNumber - rhTrackNumber;
            }
            return ret;

        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static String removeNonDigits(final String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        return str.replaceAll("\\D+", "");
    }

    public String getNumber() {
        return number;
    }

    public String getVerboseName() {
        return verboseName;
    }

    @Override
    public String toString() {
        return "Track{" +
                "number='" + number + '\'' +
                ", verboseName='" + verboseName + '\'' +
                '}';
    }
}
