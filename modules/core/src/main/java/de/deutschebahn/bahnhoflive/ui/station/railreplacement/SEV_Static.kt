package de.deutschebahn.bahnhoflive.ui.station.railreplacement

object SEV_Static {

    val stations = arrayOf(
        Pair(6945, 8000260), // Würzburg Hbf
        Pair(5400, 8005198), // Rottendorf
        Pair(1181, 8001421), // Dettelbach
        Pair(927, 8001225), // Buchbrunn-Mainstockheim
        Pair(3212, 8000479), // Kitzingen
        Pair(3001, 8003081), // Iphofen
        Pair(3968, 8003876), // Markt Bibart
        Pair(4443, 8004323), // Neustadt (Aisch)
        Pair(8195, 8004336), // Neustadt (Aisch) Mitte
        Pair(1591, 8001783), // Emskirchen
        Pair(2466, 8002517), // Hagenbüchach
        Pair(5060, 8004901), // Puschendorf
        Pair(5841, 8005557), // Siegelsdorf
        Pair(1988, 8002152), // Fürth-Burgfarrnbach
        Pair(1991, 8002155), // Fürth-Unterfürberg
        Pair(1984, 8000114), // Fürth(Bay)Hbf
        Pair(4593, 8000284) // Nürnberg Hbf
    )

    fun contains(stationId:String?) : Boolean {
        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && stations.map { it.first }.contains(stationIdAsInt))
    }

}