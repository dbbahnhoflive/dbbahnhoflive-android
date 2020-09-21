/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.persistence;

import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;

public class LegacyHafasStationItemAdapter extends HafasStationItemAdapter {
    @Override
    public String getId(HafasStation item) {
        return item.id;
    }
}
