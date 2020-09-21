/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model;

import java.util.List;

public class WagenstandIstInformationData {

    public String fahrtrichtung;
    public List<WagenstandFahrzeugData> allFahrzeuggruppe;
    public WagenstandHaltData halt;

    public String liniebezeichnung;//"liniebezeichnung":"",
    public String zuggattung;//       "zuggattung":"ICE",
    public String zugnummer;//        "zugnummer":"1616",
    public String serviceid;//        "serviceid":"6816026228",
    public String planstarttag;//        "planstarttag":"2017-01-13",
    public String fahrtid;//        "fahrtid":"",
    public boolean istplaninformation;//       "istplaninformation":false

}
