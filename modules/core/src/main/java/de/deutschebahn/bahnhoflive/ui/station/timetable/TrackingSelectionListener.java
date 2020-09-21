/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import androidx.annotation.NonNull;

import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class TrackingSelectionListener implements SingleSelectionManager.Listener {
    private final TrackingManager trackingManager;

    public TrackingSelectionListener(@NonNull TrackingManager trackingManager) {
        this.trackingManager = trackingManager;
    }

    @Override
    public void onSelectionChanged(SingleSelectionManager selectionManager) {
        if (trackingManager == null || selectionManager.getSelection() == SingleSelectionManager.INVALID_SELECTION) {
            return;
        }

        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.Action.TAP, TrackingManager.UiElement.VERBINDUNG_AUSWAHL);
    }
}
