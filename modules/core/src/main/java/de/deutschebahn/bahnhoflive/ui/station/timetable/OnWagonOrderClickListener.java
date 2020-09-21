/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;

public interface OnWagonOrderClickListener {
    void onWagonOrderClick(TrainInfo trainInfo, TrainEvent trainEvent);
}
