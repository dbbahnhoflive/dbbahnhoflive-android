/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.elevators;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import de.deutschebahn.bahnhoflive.R;

public class OnDeleteAllFacilityStatusSubscriptionsClickListener implements View.OnClickListener {
    private final DialogInterface.OnClickListener yesClickListener;
    private final Context context;

    public OnDeleteAllFacilityStatusSubscriptionsClickListener(Context context, DialogInterface.OnClickListener yesClickListener) {
        this.yesClickListener = yesClickListener;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.facility_deleteall_alert_title)
                .setMessage(R.string.facility_deleteall_alert_text)
                .setNeutralButton(R.string.facility_deleteall_alert_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.facility_deleteall_alert_yes, yesClickListener)
                .setCancelable(true);

        dialog.show();

    }
}
