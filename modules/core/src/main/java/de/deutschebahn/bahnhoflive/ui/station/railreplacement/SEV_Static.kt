package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import java.util.GregorianCalendar


object SEV_Static {

class SEV_Item(
    var stationId:Int=0,
    var evaId:Int=0,
    var additionalStop_evaId:Int=0,
    var additionalStop_stationName:String=""
)

private val sev_items = arrayOf<SEV_Item>(

    SEV_Item(6945, 8000260, 8089299, "Würzburg Busbahnhof"), // Würzburg Hbf
    SEV_Item(6946, 8006582, 220219, "Würzburg Südbahnhof"), // Würzburg Südbahnhof
    SEV_Item(5400, 8005198, 462702, "Rottendorf Bahnhof"), // Rottendorf
    SEV_Item(1181, 8001421, 467125, "Dettelbach Bahnhof"), // Dettelbach
    SEV_Item(927,  8001225, 8071313,"Buchbrunn-Mainstockheim Bahnhofstraße SEV"), // Buchbrunn-Mainstockheim
    SEV_Item(3212, 8000479, 465514, "Kitzingen Bahnhof"), // Kitzingen
    SEV_Item(3001, 8003081, 461619, "Iphofen Bahnhof"), // Iphofen
    SEV_Item(3968, 8003876, 683142, "Markt Bibart Bahnhof SEV"), // Markt Bibart
    SEV_Item(4443, 8004323, 683174, "Neustadt(Aisch) Busbahnhof"), // Neustadt (Aisch,)
    SEV_Item(8195, 8004336, 679173, "Neustadt(Aisch) Bahnhofstraße"), // Neustadt (Aisch,) Mitte
    SEV_Item(1591, 8001783, 460530, "Emskirchen Bahnhof SEV"), // Emskirchen
    SEV_Item(2466, 8002517, 8071318, "Hagenbüchach Bahnhof"), // Hagenbüchach
    SEV_Item(2466, 8002517, 8071319, "Hagenbüchach Bahnhof"), // Hagenbüchach
    SEV_Item(5060, 8004901, 205271, "Puschendorf Feuerwehrhaus"), // Puschendorf
    SEV_Item(5841, 8005557, 683449, "Siegelsdorf Bahnhof SEV"), // Siegelsdorf
    SEV_Item(1988, 8002152, 676317, "Burgfarrnbach Regelsbacher Straße"), // Fürth-Burgfarrnbach
    SEV_Item(1991, 8002155, 677263, "Fürth-Unterfürberg Ritter-v.-Aldebert-Straße"), // Fürth-Unterfürberg
    SEV_Item(1984, 8000114, 682635, "Fürth(Bay) Hauptbahnhof"), // Fürth(Bay)Hbf
    SEV_Item(4593, 8000284, 8071247, "Nürnberg Hbf (Eilgutstraße/Westausgang)") // Nürnberg Hbf
)


    fun contains(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && sev_items.map { it.stationId }.contains(stationIdAsInt))
    }

//    fun getReplacementEvaId(_evaId:String) : String {
//        val evaIdAsIint = _evaId.toIntOrNull() ?: return _evaId
//        val ret : Int? = sev_items.find { it.evaId == evaIdAsIint }?.replacementStop_evaId1
//        return ret?.toString() ?: _evaId
//    }

    fun addEvaIds(stationId: String?, list: MutableList<String>) {
        if(!isInConstructionPhase()) return

        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        sev_items.forEach {
            if (it.stationId == stationIdAsInt)
                list.add(it.additionalStop_evaId.toString())
        }
    }


    fun isInAnnouncementPhase() : Boolean {
        val cal = GregorianCalendar.getInstance()
        val now = cal.timeInMillis

        // announcementPhase
        cal.set(2023, 5 - 1, 26, 20, 59, 59)
        val endAnnouncement = cal.timeInMillis
        val isInAnnouncementPhase = now <= endAnnouncement  // ab sofort bis 26.05.2023, 20:59 Uhr

        return isInAnnouncementPhase
    }

    fun isInConstructionPhase() : Boolean {

        val cal = GregorianCalendar.getInstance()
        val now = cal.timeInMillis

        // constructionPhase : 26.05.2023, 21:00 Uhr bis 11.09.2023, 23:59 Uhr
        cal.set(2023, 9 - 1, 11, 23, 59, 59)
        //        cal.set(2023, 9, 11, 23, 59, 59)
        val endConstruction = cal.timeInMillis

        // announcementPhase
        cal.set(2023, 5 - 1, 26, 20, 59, 59)
        val endAnnouncement = cal.timeInMillis

        val isInConstructionPhase = now > endAnnouncement && now < endConstruction

        return isInConstructionPhase
    }
}
