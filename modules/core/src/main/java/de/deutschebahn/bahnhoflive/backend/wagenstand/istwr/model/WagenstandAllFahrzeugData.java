package de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model;

import java.util.List;

public class WagenstandAllFahrzeugData {

    public List<WagenstandAllFahrzeugausstattungData> allFahrzeugausstattung; // "allFahrzeugausstattung":[],
    public String kategorie;                //"kategorie":"STEUERWAGENERSTEKLASSE",
    public String fahrzeugnummer;           //"fahrzeugnummer":"938054110706",
    public String orientierung;             //"orientierung":"UNDEFINIERT",
    public String positioningruppe;         //"positioningruppe":"1",
    public String fahrzeugsektor;           //"fahrzeugsektor":"D",
    public String fahrzeugtyp;              //"fahrzeugtyp":"Apmzf",
    public String wagenordnungsnummer;      //"wagenordnungsnummer":"28",
    public WagenstandPositionAmhaltData positionamhalt;//"positionamhalt":{"endemeter":"237.2","endeprozent":"59","startmeter":"209.3","startprozent":"52"},
    public String status;                   //    "status":"OFFEN"
    public interface Category {

        String LOK = "LOK";

        String TRIEBKOPF = "TRIEBKOPF";
        String TRIEBWAGENBAUREIHE628928 = "TRIEBWAGENBAUREIHE628928";
        String STEUERWAGENERSTEKLASSE = "STEUERWAGENERSTEKLASSE";
        String STEUERWAGENZWEITEKLASSE = "STEUERWAGENZWEITEKLASSE";
        String DOPPELSTOCKSTEUERWAGENERSTEKLASSE = "DOPPELSTOCKSTEUERWAGENERSTEKLASSE";
        String DOPPELSTOCKSTEUERWAGENZWEITEKLASSE = "DOPPELSTOCKSTEUERWAGENZWEITEKLASSE";
        String DOPPELSTOCKSTEUERWAGENERSTEZWEITEKLASSE = "DOPPELSTOCKSTEUERWAGENERSTEZWEITEKLASSE";
        String STEUERWAGENERSTEZWEITEKLASSE = "STEUERWAGENERSTEZWEITEKLASSE";

        String REISEZUGWAGENERSTEZWEITEKLASSE = "REISEZUGWAGENERSTEZWEITEKLASSE";
        String REISEZUGWAGENZWEITEKLASSE = "REISEZUGWAGENZWEITEKLASSE";
        String REISEZUGWAGENERSTEKLASSE = "REISEZUGWAGENERSTEKLASSE";
        String DOPPELSTOCKWAGENERSTEZWEITEKLASSE = "DOPPELSTOCKWAGENERSTEZWEITEKLASSE";
        String DOPPELSTOCKWAGENERSTEKLASSE = "DOPPELSTOCKWAGENERSTEKLASSE";
        String DOPPELSTOCKWAGENZWEITEKLASSE = "DOPPELSTOCKWAGENZWEITEKLASSE";

        String SPEISEWAGEN = "SPEISEWAGEN";
        String HALBSPEISEWAGENZWEITEKLASSE = "HALBSPEISEWAGENZWEITEKLASSE";
        String HALBSPEISEWAGENERSTEKLASSE = "HALBSPEISEWAGENERSTEKLASSE";

        String SCHLAFWAGENERSTEKLASSE = "SCHLAFWAGENERSTEKLASSE";
        String SCHLAFWAGENERSTEZWEITEKLASSE = "SCHLAFWAGENERSTEZWEITEKLASSE";
        String SCHLAFWAGENZWEITEKLASSE = "SCHLAFWAGENZWEITEKLASSE";
        String LIEGEWAGENERSTEKLASSE = "LIEGEWAGENERSTEKLASSE";
        String LIEGEWAGENZWEITEKLASSE = "LIEGEWAGENZWEITEKLASSE";

        String HALBGEPAECKWAGENERSTEKLASSE = "HALBGEPAECKWAGENERSTEKLASSE";
        String HALBGEPAECKWAGENZWEITEKLASSE = "HALBGEPAECKWAGENZWEITEKLASSE";
        String GEPAECKWAGEN = "GEPAECKWAGEN";

        String DOPPELSTOCKAUTOTRANSPORTWAGENREISEZUGWAGENBAUART = "DOPPELSTOCKAUTOTRANSPORTWAGENREISEZUGWAGENBAUART";
    }
}
