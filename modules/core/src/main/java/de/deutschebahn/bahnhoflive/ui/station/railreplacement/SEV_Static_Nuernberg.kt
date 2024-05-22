package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.util.isActualDateInRange


object SEV_Static_Nuernberg {

class SEV_Item(
    val stationId:Int=0,
    val evaId:Int=0,
    val sev_evaId:Int=0,
    val sev_Name:String="",
    val isInBauPhase1:Boolean=false,
    val isInBauPhase2:Boolean=false,
    val showDbCompanionAdHocBox:Boolean=false
)

    // // cal-format: year (2024), month (1..12), day (1..31), hour (0..23), minute (0..59), second (0..59)
    private val startOfAnnouncementPhase = arrayOf(2024, 7, 8, 0, 0, 0) // dummy
    private val endOfAnnouncementPhase = arrayOf(2024, 7, 14, 23, 59, 59)

    private val startOfConstructionPhase1 =  arrayOf(2024, 7, 15,  0,0,0) // Wegbegleitung
    private val endOfConstructionPhase1 = arrayOf(2024, 12, 14,  23,59,59)

    private val startOfConstructionPhase2 =  arrayOf(2023, 1, 1,  0,0,0)
    private val endOfConstructionPhase2 = arrayOf(2023, 8, 1,  23,59,59)

    // AR-companion (ohne Funktion)
    private val startOfShowArCompanion_2545 = arrayOf(2023, 1, 1,  0,0,0)
    private val endOfShowArCompanion_2545 = arrayOf(2023, 8, 1,  23,59,59)

    private val startOfShowAdHocBox = arrayOf(2024, 7, 8, 0, 0, 0) // dummy
    private val endOfShowAdHocBox = arrayOf(2034, 12, 31, 23, 59, 59)

    private val sev_items = arrayOf<SEV_Item>(

//    SEV_Item(2545, 8000152, 8089299, "Hannover",true,true, true), // Test Hannover
     SEV_Item(1866, 8000105, 8098105, "Frankfurt (Main) Hbf",true,false, true), // Test Hannover

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
              if(it.isInBauPhase1 && isInConstructionPhase1 ||
                  it.isInBauPhase2 && isInConstructionPhase2
                      )
                list.add(it.sev_evaId.toString())
            }
        }
    }

    fun isInAnnouncementPhase() : Boolean {
        return isActualDateInRange(startOfAnnouncementPhase, endOfAnnouncementPhase)
    }

    fun isInConstructionPhase1() : Boolean {
        return isActualDateInRange(startOfConstructionPhase1, endOfConstructionPhase1)
    }

    fun isInConstructionPhase2() : Boolean {
        return isActualDateInRange(startOfConstructionPhase2, endOfConstructionPhase2)
    }

    // Box auch anzeigen wenn Bauphase noch nicht begonnen hat !
    // siehe NewsAdapter,  "Ankündigung Ersatzverkehr"
    fun shouldShowAdhocBox() : Boolean {
        return isActualDateInRange(startOfShowAdHocBox, endOfShowAdHocBox)
    }



    fun isStationInConstructionPhase(stationId: String?) : Boolean {

        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        val isInConstructionPhase1 = isInConstructionPhase1()
        val isInConstructionPhase2 = isInConstructionPhase2()

        sev_items.forEach {
            if (it.stationId == stationIdAsInt) {
                if(it.isInBauPhase1 && isInConstructionPhase1 ||
                    it.isInBauPhase2 && isInConstructionPhase2
                )
                    return true
            }
        }

        return false
    }

    fun isStationInConstructionPhase(station: MergedStation): Boolean {
        return isStationInConstructionPhase(station.id)
    }


    fun isStationSEV(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
            sev_items.forEach {
                if (it.stationId == stationIdAsInt)
                    return true
            }
        return false
    }


    fun hasSEVStationWebAppCompanionLink(evaId: String?): Boolean {
        val evaIdAsInt = evaId?.toIntOrNull() ?: 0
        if (isInConstructionPhase1()) {
            sev_items.forEach {
                if (it.sev_evaId == evaIdAsInt)
                    return it.showDbCompanionAdHocBox
            }
        }
        return false
    }

    // drin lassen, falls es wieder rein kommt
    fun hasStationArAppLink(stationId: String?): Boolean { // AR-App link to playstore
//        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
//
//        when (stationIdAsInt) {
//            2545 -> {
//                // Hannover nur zum testen
//                return isActualDateInRange(startOfShowArCompanion_2545, endOfShowArCompanion_2545)
//            }
//            else -> return false
//        }

        return false
    }


    fun isReplacementStopFrom(evaId: String, evaIds: MutableList<String>): Boolean {

        if (!isInConstructionPhase1() && !isInConstructionPhase2()) return false

        for (item in sev_items) {
            if (item.sev_evaId.toString() == evaId)
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
                return Triple(item.evaId.toString(), item.sev_Name, null)
            }
        }
        return null

    }


}
