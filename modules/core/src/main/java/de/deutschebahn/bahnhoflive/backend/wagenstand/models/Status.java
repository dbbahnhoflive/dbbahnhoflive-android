/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringRes;

import de.deutschebahn.bahnhoflive.R;

public enum Status implements Parcelable {
    DEFEKT(false, R.string.status_defective),          // Ausstattung ist defekt.
    OFFEN(true, R.string.status_open),           // Fahrzeug kann genutzt werden. / Ausstattung kann genutzt werden.
    GESCHLOSSEN(false, R.string.status_closed),     // Fahrzeug kann nicht von Reisenden genutzt werden (oft auch als "leer und verschlossen" bezeichnet). / Ausstattung geschlossen
    VERFUEGBAR(true, R.string.status_available),      // Ausstattung ist nutzbar.
    NICHTVERFUEGBAR(false, R.string.status_unavailable), // Ausstattung ist nicht nutzbar.
    RESERVIERT(false, R.string.status_reserved),      // Ausstattung ist reserviert und kann vom "normalen" Fahrgast nicht genutzt werden.
    NICHTBEDIENT(false, R.string.status_noservice),    // Fahrzeug ist offen, aber Bistro oder Restaurant sind unbedient.
    UNDEFINIERT(true),     // Der Status der Ausstattung konnte nicht bestimmt werden; grundsätzlich ist die Austattung im Fahrzeug aber vorhanden.

    AVAILABLE(true, R.string.status_available),
    NOT_AVAILABLE(false, R.string.status_unavailable),
    RESERVED(false, R.string.status_reserved),      // Ausstattung ist reserviert und kann vom "normalen" Fahrgast nicht genutzt werden.
    UNDEFINED(true),     // Der Status der Ausstattung konnte nicht bestimmt werden; grundsätzlich ist die Austattung im Fahrzeug aber vorhanden.
    OPEN(true, R.string.status_open),           // Fahrzeug kann genutzt werden. / Ausstattung kann genutzt werden.
    CLOSED(false, R.string.status_closed);     // Fahrzeug kann nicht von Reisenden genutzt werden (oft auch als "leer und verschlossen" bezeichnet). / Ausstattung geschlossen

    @StringRes
    public final int label;

    public final boolean available;

    Status(boolean available) {
        this(available, 0);
    }

    Status(boolean available, int label) {
        this.available = available;
        this.label = label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ordinal());
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return Status.values()[in.readInt()];
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };
}
