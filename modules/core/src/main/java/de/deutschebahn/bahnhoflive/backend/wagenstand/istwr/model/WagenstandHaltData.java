/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model;

import java.util.List;

public class WagenstandHaltData {

    public String abfahrtszeit;//"abfahrtszeit":"2017-01-13T10:24:00",
    public String ankunftszeit;//        "ankunftszeit":"2017-01-13T10:22:00",
    public String bahnhofsname;//        "bahnhofsname":"Berlin Südkreuz",
    public String evanummer;//        "evanummer":"8011113",
    public String gleisbezeichnung;//        "gleisbezeichnung":"8",
    public String haltid;//        "haltid":"BPAF",
    public String rl100;//        "rl100":"BPAF",
    public List<WagenstandAllSectorData> allSektor;
}
