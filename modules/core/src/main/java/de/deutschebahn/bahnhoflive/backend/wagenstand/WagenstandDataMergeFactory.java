/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData;
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandFahrzeugData;

/**
 * Use this class to set or merge the  Wagenstand.waggons depending on WagenstandIstInformationData
 *
 */

public class WagenstandDataMergeFactory {

    public static final Collator COLLATOR = Collator.getInstance(Locale.GERMAN);

    public static String extractSectionSpan(WagenstandFahrzeugData wagenstandFahrzeugData) {
        if (wagenstandFahrzeugData == null) {
            return "";
        }

        final List<WagenstandAllFahrzeugData> allFahrzeug = wagenstandFahrzeugData.allFahrzeug;
        if (allFahrzeug.isEmpty()) {
            return "";
        }

        final String firstSector = allFahrzeug.get(0).fahrzeugsektor;
        final String lastSector = allFahrzeug.get(allFahrzeug.size() - 1).fahrzeugsektor;

        if (firstSector.equals(lastSector)) {
            return firstSector;
        }

        return COLLATOR.compare(firstSector, lastSector) < 0
                ? String.format("%s-%s", firstSector, lastSector)
                : String.format("%s-%s", lastSector, firstSector);
    }

}
