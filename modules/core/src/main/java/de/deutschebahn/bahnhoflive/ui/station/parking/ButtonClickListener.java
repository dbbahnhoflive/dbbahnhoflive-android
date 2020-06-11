package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.content.Context;

import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;

interface ButtonClickListener {
    void onButtonClick(Context context, ParkingFacility parkingFacility);
}
