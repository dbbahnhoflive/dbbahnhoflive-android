package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import de.deutschebahn.bahnhoflive.repository.MergedStation
import java.util.GregorianCalendar


object SEV_Static {

class SEV_Item(
    val stationId:Int=0,
    val evaId:Int=0,
    val additionalStop_evaId:Int=0,
    val additionalStop_stationName:String="",
    val isInConstructionPhase1:Boolean=false,
    val isInConstructionPhase2:Boolean=false
)

private val sev_items = arrayOf<SEV_Item>(

    SEV_Item(6945, 8000260, 8089299, "Würzburg Busbahnhof",true,false), // Würzburg Hbf
    SEV_Item(5400, 8005198, 462702, "Rottendorf Bahnhof",true,false), // Rottendorf
    SEV_Item(1181, 8001421, 467125, "Dettelbach Bahnhof",true,false), // Dettelbach
    SEV_Item(927,  8001225, 8071313,"Buchbrunn-Mainstockheim Bahnhofstraße SEV",true,false), // Buchbrunn-Mainstockheim
    SEV_Item(3212, 8000479, 465514, "Kitzingen Bahnhof",true,false), // Kitzingen
    SEV_Item(3001, 8003081, 461619, "Iphofen Bahnhof",true,false), // Iphofen
    SEV_Item(3968, 8003876, 683142, "Markt Bibart Bahnhof SEV",true,false), // Markt Bibart
    SEV_Item(4443, 8004323, 683174, "Neustadt(Aisch) Busbahnhof",true,false), // Neustadt (Aisch,)
    SEV_Item(8195, 8004336, 679173, "Neustadt(Aisch) Bahnhofstraße",true,true), // Neustadt (Aisch,) Mitte
    SEV_Item(1591, 8001783, 460530, "Emskirchen Bahnhof SEV",false,true), // Emskirchen
    SEV_Item(2466, 8002517, 8071318, "Hagenbüchach Bahnhof",false,true), // Hagenbüchach
    SEV_Item(2466, 8002517, 8071319, "Hagenbüchach Bahnhof",false,true), // Hagenbüchach
    SEV_Item(5060, 8004901, 205271, "Puschendorf Feuerwehrhaus",false,true), // Puschendorf
    SEV_Item(5841, 8005557, 683449, "Siegelsdorf Bahnhof SEV",false,true), // Siegelsdorf
    SEV_Item(1988, 8002152, 676317, "Burgfarrnbach Regelsbacher Straße",false,true), // Fürth-Burgfarrnbach
    SEV_Item(1991, 8002155, 677263, "Fürth-Unterfürberg Ritter-v.-Aldebert-Straße",true,false), // Fürth-Unterfürberg
    SEV_Item(1984, 8000114, 682635, "Fürth(Bay) Hauptbahnhof",false,true), // Fürth(Bay)Hbf
    SEV_Item(4593, 8000284, 8071247, "Nürnberg Hbf (Eilgutstraße/Westausgang)", false, true) // Nürnberg Hbf
)


    fun containsStationId(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && sev_items.map { it.stationId }.contains(stationIdAsInt))
    }

//    fun getReplacementEvaId(_evaId:String) : String {
//        val evaIdAsIint = _evaId.toIntOrNull() ?: return _evaId
//        val ret : Int? = sev_items.find { it.evaId == evaIdAsIint }?.replacementStop_evaId1
//        return ret?.toString() ?: _evaId
//    }

    fun addEvaIds(stationId: String?, list: MutableList<String>) {
        val isInConstructionPhase1 = isInConstructionPhase1()
        val isInConstructionPhase2 = isInConstructionPhase2()

        if(!isInConstructionPhase1 && !isInConstructionPhase2)
            return

        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        sev_items.forEach {
            if (it.stationId == stationIdAsInt) {
              if(it.isInConstructionPhase1 && isInConstructionPhase1 ||
                  it.isInConstructionPhase2 && isInConstructionPhase2
                      )
                list.add(it.additionalStop_evaId.toString())
            }
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

    fun isInConstructionPhase1() : Boolean {

        val cal = GregorianCalendar.getInstance()
        val now = cal.timeInMillis

        // start 26.5.23
        cal.set(2023, 5 - 1, 26, 0, 0, 0)
        val start = cal.timeInMillis

        // end : 6.08.2023
        cal.set(2023, 8 - 1, 6, 23, 59, 59)
        val end = cal.timeInMillis

        val isInConstructionPhase = now > start && now < end

        return isInConstructionPhase
    }

    fun isInConstructionPhase2() : Boolean {

        val cal = GregorianCalendar.getInstance()
        val now = cal.timeInMillis

        // start 6.8.23
        cal.set(2023, 6 - 1, 8, 0, 0, 0)
        val start = cal.timeInMillis

        // end : 12.09.2023
        cal.set(2023, 9 - 1, 12, 23, 59, 59)
        val end = cal.timeInMillis

        val isInConstructionPhase = now > start && now < end

        return isInConstructionPhase
    }

    fun isStationInConstructionPhase(stationId: String?) : Boolean {

        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        val isInConstructionPhase1 = isInConstructionPhase1()
        val isInConstructionPhase2 = isInConstructionPhase2()

        sev_items.forEach {
            if (it.stationId == stationIdAsInt) {
                if(it.isInConstructionPhase1 && isInConstructionPhase1 ||
                    it.isInConstructionPhase2 && isInConstructionPhase2
                )
                    return true
            }
        }

        return false
    }

    fun isStationInConstructionPhase(station:MergedStation) : Boolean {
        return isStationInConstructionPhase(station.id)
    }

    fun isReplacementStopFrom(evaId: String, evaIds: MutableList<String>): Boolean {

        if (!isInConstructionPhase1() && !isInConstructionPhase2()) return false

        for (item in sev_items) {
            if (item.additionalStop_evaId.toString().equals(evaId))
                if (evaIds.contains(item.evaId.toString()) && isStationInConstructionPhase(item.stationId.toString()))
                    return true
        }
        return false
    }

    fun isReplacementStopFrom(evaId:String?): Boolean {

        if (!isInConstructionPhase1() && !isInConstructionPhase2() || evaId==null) return false

        for (item in sev_items) {
            if(evaId==item.additionalStop_evaId.toString())
                return true
        }
        return false
    }

    fun getStationEvaIdByReplacementId(evaId:String?) : Triple<String,String,String?>? {

        if (evaId==null || (!isInConstructionPhase1() && !isInConstructionPhase2())) return null

        for (item in sev_items) {
            if (item.additionalStop_evaId.toString() == evaId) {
                return Triple(item.evaId.toString(), item.additionalStop_stationName, null)
            }
        }
        return null

    }
}
