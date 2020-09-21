/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;

public class ExternalLinks {
    public static void openMonatskartekMail(Context context, ParkingFacility parkingFacility) {
        String subject = String.format("Antrag Monatskarte - %s", parkingFacility.getName());
        String recipient = "dauerparker@contipark.de";
        String text = "Sehr geehrte Damen und Herren,\n\nbitte senden Sie mir die benötigten\n" +
                "Unterlagen zur Beantragung einer Monatskarte für von Ihnen verwalteten\n" +
                "Parkraum zu.\n\nMit freundlichen Grüßen";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", recipient, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(emailIntent, "E-Mail senden..."));
    }

    public static void openReservation(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.parkplatz.kaufen"));
        context.startActivity(intent);
    }
}
