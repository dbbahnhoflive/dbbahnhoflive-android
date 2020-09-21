/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model;

import java.util.List;

public class WagenstandFahrzeugData {

    public List<WagenstandAllFahrzeugData> allFahrzeug;
    public String fahrzeuggruppebezeichnung;        //"fahrzeuggruppebezeichnung":"E80 ICE1170",
    public String zielbetriebsstellename;           //"zielbetriebsstellename":"Hamburg-Altona",
    public String startbetriebsstellename;          //"startbetriebsstellename":"Saalfeld(Saale)",
    public String verkehrlichezugnummer;            //"verkehrlichezugnummer":"1616"

}
