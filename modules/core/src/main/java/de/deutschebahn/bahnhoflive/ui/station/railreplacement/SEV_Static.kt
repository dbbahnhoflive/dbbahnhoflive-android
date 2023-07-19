package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import de.deutschebahn.bahnhoflive.repository.MergedStation
import java.util.GregorianCalendar


object SEV_Static {



class SEV_Item(
    val stationId:Int=0,
    val evaId:Int=0,
    val sev_evaId:Int=0,
    val sev_stationName:String="",
    val isInConstructionPhase1:Boolean=false,
    val isInConstructionPhase2:Boolean=false,
    val showCompanionAppLink:Boolean=false // ab 6.8.
)

private val sev_items = arrayOf<SEV_Item>(

    SEV_Item(6945, 8000260, 8089299, "Würzburg Busbahnhof",true,false, true), // Würzburg Hbf
    SEV_Item(5400, 8005198, 462702, "Rottendorf Bahnhof",true,false, false), // Rottendorf
    SEV_Item(1181, 8001421, 467125, "Dettelbach Bahnhof",true,false, false), // Dettelbach
    SEV_Item(927,  8001225, 8071313,"Buchbrunn-Mainstockheim Bahnhofstraße SEV",true,false, false), // Buchbrunn-Mainstockheim
    SEV_Item(3212, 8000479, 465514, "Kitzingen Bahnhof",true,false, false), // Kitzingen
    SEV_Item(3001, 8003081, 461619, "Iphofen Bahnhof",true,false, false), // Iphofen
    SEV_Item(3968, 8003876, 683142, "Markt Bibart Bahnhof SEV",true,false, false), // Markt Bibart
    SEV_Item(4443, 8004323, 683174, "Neustadt(Aisch) Busbahnhof",true,false, true), // Neustadt (Aisch,)
    SEV_Item(8195, 8004336, 679173, "Neustadt(Aisch) Bahnhofstraße",true,true, true), // Neustadt (Aisch,) Mitte
    SEV_Item(1591, 8001783, 460530, "Emskirchen Bahnhof SEV",false,true, true), // Emskirchen
    SEV_Item(2466, 8002517, 8071318, "Hagenbüchach Bahnhof",false,true, true), // Hagenbüchach
    SEV_Item(2466, 8002517, 8071319, "Hagenbüchach Bahnhof",false,true, true), // Hagenbüchach
    SEV_Item(5060, 8004901, 205271, "Puschendorf Feuerwehrhaus",false,true, false), // Puschendorf
    SEV_Item(5841, 8005557, 683449, "Siegelsdorf Bahnhof SEV",false,true, true), // Siegelsdorf
    SEV_Item(1988, 8002152, 676317, "Burgfarrnbach Regelsbacher Straße",false,true, true), // Fürth-Burgfarrnbach
    SEV_Item(1991, 8002155, 677263, "Fürth-Unterfürberg Ritter-v.-Aldebert-Straße",true,false, true), // Fürth-Unterfürberg
    SEV_Item(1984, 8000114, 682635, "Fürth(Bay) Hauptbahnhof",false,true, true), // Fürth(Bay)Hbf
    SEV_Item(4593, 8000284, 8071247, "Nürnberg Hbf (Eilgutstraße/Westausgang)", false, true, true) // Nürnberg Hbf
)


    fun containsStationId(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && sev_items.map { it.stationId }.contains(stationIdAsInt))
    }

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
                list.add(it.sev_evaId.toString())
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

    /**
     * checks if now >start and<=end
     *
     * year 2022...
     *
     * month: 1..12
     *
     * day: 1..31
     */
    private fun isActualDateInRange(startYear:Int, startMonth:Int, startDay:Int, endYear:Int, endMonth:Int, endDay:Int) : Boolean {

        val cal = GregorianCalendar.getInstance()
        val now = cal.timeInMillis

        cal.set(startYear, startMonth - 1, startDay, 0, 0, 0)
        val start = cal.timeInMillis

        cal.set(endYear, endMonth - 1, endDay, 23, 59, 59)
        val end = cal.timeInMillis

        val isInRange = now > start && now < end

        return isInRange
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
        cal.set(2022, 8 - 1, 6, 0, 0, 0)
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

    fun hasStationWebAppCompanionLink(stationId: String?): Boolean { // webApp DB Wegbegleitung
        // ab 6.8. bis 12.9.
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        if (isInConstructionPhase2()) {
            sev_items.forEach {
                if (it.stationId == stationIdAsInt)
                    return it.showCompanionAppLink
            }
        }
        return false
    }

    fun hasSEVStationWebAppCompanionLink(evaId: String?): Boolean { // webApp DB Wegbegleitung
        // ab 6.8. bis 12.9.
        val evaIdAsInt = evaId?.toIntOrNull() ?: 0
        if (isInConstructionPhase2()) {
            sev_items.forEach {
                if (it.sev_evaId == evaIdAsInt)
                    return it.showCompanionAppLink
            }
        }
        return false
    }

    fun hasStationArAppLink(stationId: String?): Boolean { // AR-App link to playstore
        // spezial, nur 2 Stationen:
        // Würzburg (6945) ab 29.5. und
        // Nürnberg (4593) ab 4.8.
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0

        when (stationIdAsInt) {
            6945 -> {
                // Würzburg ab 29.5.
                return isActualDateInRange(2023, 5, 29, 2023, 9, 12)
            }
            4953 -> {
                // Nürnberg ab 4.8.
                return isActualDateInRange(2023, 8, 4, 2023, 9, 12)

            }
            else -> return false
        }
    }

    fun isStationInConstructionPhase(station: MergedStation): Boolean {
        return isStationInConstructionPhase(station.id)
    }

    fun isReplacementStopFrom(evaId: String, evaIds: MutableList<String>): Boolean {

        if (!isInConstructionPhase1() && !isInConstructionPhase2()) return false

        for (item in sev_items) {
            if (item.sev_evaId.toString().equals(evaId))
                if (evaIds.contains(item.evaId.toString()) && isStationInConstructionPhase(item.stationId.toString()))
                    return true
        }
        return false
    }

    fun isReplacementStopFrom(evaId:String?): Boolean {

        if (!isInConstructionPhase1() && !isInConstructionPhase2() || evaId==null) return false

        for (item in sev_items) {
            if(evaId==item.sev_evaId.toString())
                return true
        }
        return false
    }

    fun getStationEvaIdByReplacementId(evaId:String?) : Triple<String,String,String?>? {

        if (evaId==null || (!isInConstructionPhase1() && !isInConstructionPhase2())) return null

        for (item in sev_items) {
            if (item.sev_evaId.toString() == evaId) {
                return Triple(item.evaId.toString(), item.sev_stationName, null)
            }
        }
        return null

    }
}
