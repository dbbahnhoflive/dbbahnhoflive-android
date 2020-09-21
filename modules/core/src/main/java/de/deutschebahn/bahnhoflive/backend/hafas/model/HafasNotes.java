/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.hafas.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HafasNotes implements Parcelable {

    @SerializedName("Note")
    public List<HafasNote> notes;

    protected HafasNotes() {}

    protected HafasNotes(Parcel in) {
        notes = in.createTypedArrayList(HafasNote.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(notes);
    }

    public static final Creator<HafasNotes> CREATOR = new Creator<HafasNotes>() {
        @Override
        public HafasNotes createFromParcel(Parcel in) {
            return new HafasNotes(in);
        }

        @Override
        public HafasNotes[] newArray(int size) {
            return new HafasNotes[size];
        }
    };

    /**
     * Concats all note's message values.
     * @return A comma separated string
     */
    public String getDisplayMessages() {

        List<String> messagesList = new ArrayList<>();
        for (HafasNote note : notes) {
            messagesList.add(note.value);
        }
        return TextUtils.join(", ", messagesList);
    }

    @Override
    public String toString() {
        return "HafasNotes{" +
                "notesList=" + notes +
                '}';
    }
}
