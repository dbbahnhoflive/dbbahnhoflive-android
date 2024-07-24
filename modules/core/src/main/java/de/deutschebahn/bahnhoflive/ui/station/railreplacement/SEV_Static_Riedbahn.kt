package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.GroupId
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.Group
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlace
import de.deutschebahn.bahnhoflive.backend.db.ris.model.StopPlaceName
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.util.isActualDateInRange
import java.util.Calendar


object SEV_Static_Riedbahn {
// Juli-Dezember 2024
class EvItem(
    val evaId:Int=0,
    val stationName : String = "",  // SPNV-Halt = Schienenpersonennahverkehr
    val hasDbCompanion : Boolean=true
) {
    fun toStopPlace(stadaID: Int) : StopPlace {
        val stpName = StopPlaceName()
        stpName.nameLong = stationName

        val stp = StopPlace()
        stp.stationID = stadaID.toString()
        stp.evaNumber = evaId.toString()
        stp.availableTransports = listOf("BUS")
        stp.names = mapOf("DE" to stpName)

        return stp
    }
}
    private const val testIsDbCompanionServiceAvailable=false // todo: false in production
    private const val testIsInAnnouncementPhase=false // todo: false in production
    private const val testIsInConstructionPhase=false // todo: false in production

    // // cal-format: year (2024), month (1..12), day (1..31), hour (0..23), minute (0..59), second (0..59)
    private val startOfAnnouncementPhase = arrayOf(2024, 7, 8, 0, 0, 0) // dummy
    private val endOfAnnouncementPhase = arrayOf(2024, 7, 14, 23, 59, 59)

    private val startOfConstructionPhase =  arrayOf(2024, 7, 15,  0,0,0) // Wegbegleitung
    private val endOfConstructionPhase = arrayOf(2024, 12, 14,  23,59,59)

    private val startOfShowAdHocBox = arrayOf(2024, 7, 8, 0, 0, 0)
    private val endOfShowAdHocBox = arrayOf(2024, 7, 14, 23, 59, 59) // dummy

    private val evMap  : Map<Int, EvItem> = mapOf(

        66 to EvItem(8000506, "Alsheim"),
        488 to EvItem(8000031, "Bensheim"),
        489 to EvItem(8000877, "Bensheim-Auerbach"),
        614 to EvItem(8000503, "Biblis"),
        618 to EvItem(8000948, "Bickenbach (Bergstr)"),
        619 to EvItem(8000951, "Biebesheim"),
        716 to EvItem(8001032, "Bobenheim"),
        721 to EvItem(8001034, "Bobstadt"),
        739 to EvItem(8000359, "Bodenheim"),
        1002 to EvItem(8000360, "Bürstadt"),
        7177 to EvItem(8098360, "Bürstadt (Ried)"),
        1126 to EvItem(8000068, "Darmstadt Hbf"),
        1129 to EvItem(8001377, "Darmstadt Süd"),
        1131 to EvItem(8001379, "Darmstadt-Eberstadt"),
        8252 to EvItem(8001448, "Dienheim"),
        1848 to EvItem(8000332, "Frankenthal Hbf"),
        8210 to EvItem(8002025, "Frankenthal Süd"),
        7982 to EvItem(8070003, "Frankfurt am Main Flughafen Fernbahnhof"),
        1849 to EvItem(8070004, "Frankfurt (Main) Flughafen Regionalbahnhof"),
        1866 to EvItem(8098105, "Frankfurt (Main) Hbf"),
        1854 to EvItem(8002040, "Frankfurt am Main Stadion"),
        8268 to EvItem(8002060, "Frankfurt am Main Gateway Gardens"),
        1876 to EvItem(8002050, "Frankfurt (Main) Niederrad"),
        2097 to EvItem(8002249, "Gernsheim"),
        2299 to EvItem(8000136, "Groß Gerau"),
        2300 to EvItem(8002386, "Groß Gerau-Dornberg"),
        1278 to EvItem(8001511, "Groß Gerau-Dornheim"),
        2316 to EvItem(8002391, "Groß Rohrheim"),
        2419 to EvItem(8002474, "Guntersblum"),
        2471 to EvItem(8002498, "Hähnlein-Alsbach"),
        2362 to EvItem(8002430, "Heddesheim/Hirschberg"),
        2684 to EvItem(8002748, "Hemsbach"),
        2693 to EvItem(8002757, "Heppenheim (Bergstr)"),
        2826 to EvItem(8002934, "Hofheim (Ried)"),
        3490 to EvItem(8003489, "Ladenburg"),
        3500 to EvItem(8003503, "Lampertheim"),
        3524 to EvItem(8003523, "Langen (Hess)"),
        3578 to EvItem(8003571, "Laudenbach (Bergstr)"),
        3786 to EvItem(8003755, "Lorsch"),
        3837 to EvItem(8000236, "Ludwigshafen (Rhein) Hbf"),
        7385 to EvItem(8003759, "Ludwigshafen (Rhein) Mitte"),
        3839 to EvItem(8003766, "Ludwigshafen-Oggersheim"),
        3898 to EvItem(8000240, "Mainz Hbf"),
        3900 to EvItem(8003816, "Mainz Römisches Theater"),
        3905 to EvItem(8003819, "Mainz-Laubenheim"),
        3929 to EvItem(8006508, "Mannheim-Handelshafen", false),
        3925 to EvItem(8000244, "Mannheim Hbf"),
        3930 to EvItem(8003843, "Mannheim-Käfertal", false),
        3931 to EvItem(8006509, "Mannheim-Luzenberg"),
        3933 to EvItem(8006511, "Mannheim-Neckarstadt"),
        3936 to EvItem(8003848, "Mannheim-Waldhof"),
        4082 to EvItem(8004003, "Mettenheim"),
        4174 to EvItem(8004065, "Mörfelden"),
        4293 to EvItem(8004193, "Nackenheim"),
        4351 to EvItem(8004246, "Neu Isenburg"),
        4551 to EvItem(8004432, "Nierstein"),
        4772 to EvItem(8004680, "Oppenheim"),
        4808 to EvItem(8004714, "Osthofen"),
        8264 to EvItem(8004816, "Pfungstadt"),
        5272 to EvItem(8005089, "Riedrode"),
        2161 to EvItem(8000126, "Riedstadt-Goddelau"),
        3608 to EvItem(8003605, "Riedstadt-Wolfskehlen"),
        6035 to EvItem(8005740, "Stockstadt (Rhein)"),
        8290 to EvItem(8006283, "Weinheim-Sulzbach"),
        6503 to EvItem(8006175, "Walldorf (Hess)"),
        6622 to EvItem(8000377, "Weinheim (Bergstr) Hbf"),
        3873 to EvItem(8003792, "Weinheim-Lützelsachsen"),
        6759 to EvItem(8006421, "Wiesloch-Walldorf", false),
        6887 to EvItem(8000257, "Worms Hbf"),
        6999 to EvItem(8006648, "Zeppelinheim"),
        7075 to EvItem(8006687, "Zwingenberg (Bergstr)")
        )



    // taeglich 7-19 Uhr
    fun isCompanionServiceAvailable() : Boolean   {
        val c = Calendar.getInstance()

        val now = c.timeInMillis
        val day = c.get(Calendar.DAY_OF_WEEK)

        c.set(Calendar.MILLISECOND, 0)

        c.set(Calendar.HOUR_OF_DAY, 7)
        c.set(Calendar.MINUTE, 0)
        val today7_00h = c.timeInMillis

        c.set(Calendar.HOUR_OF_DAY, 19)
        c.set(Calendar.MINUTE, 0)
        val today19_00h = c.timeInMillis

//        return (((day >= Calendar.MONDAY && day <= Calendar.FRIDAY) && (now >= today7_00h && now < today19_00h)) || testIsDbCompanionServiceAvailable)
        return ( (now >= today7_00h && now < today19_00h) || testIsDbCompanionServiceAvailable)
    }

    fun containsStationId(stationId: String?): Boolean {
        if(!isInConstructionPhase())
          return false

        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && evMap.containsKey(stationIdAsInt))
    }


    fun isInAnnouncementPhase() : Boolean {
        return isActualDateInRange(startOfAnnouncementPhase, endOfAnnouncementPhase) || testIsInAnnouncementPhase
    }

    @JvmStatic
    fun isInConstructionPhase() : Boolean {
        return isActualDateInRange(startOfConstructionPhase, endOfConstructionPhase) || testIsInConstructionPhase
    }


    // "+++ Ersatzverkehr...+++  Box auch anzeigen wenn Bauphase noch nicht begonnen hat !
    // siehe NewsAdapter,  "Ankündigung Ersatzverkehr"
    private fun shouldShowAdhocBox() : Boolean {
        return isActualDateInRange(startOfShowAdHocBox, endOfShowAdHocBox)
    }


//    private fun isStationInConstructionPhase(stationId: String?): Boolean {
//        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
//        val isInConstructionPhase = isInConstructionPhase()
//        return isInConstructionPhase && evMap[stationIdAsInt] !=null
//    }
//
//    fun isStationInConstructionPhase(station: MergedStation): Boolean {
//        return isStationInConstructionPhase(station.id)
//    }

    @JvmStatic
    fun hasStationDbCompanionByStationId(stationId: String?): Boolean {
        if(!isInConstructionPhase())
            return false

        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        val stat = evMap[stationIdAsInt]

        return stat!=null && stat.hasDbCompanion
    }

//    fun hasStationDbCompanionByEvaId(evaId: String?): Boolean {
//        val evaIdAsInt = evaId?.toIntOrNull() ?: 0
//        return evMap.filter { it.value.evaId==evaIdAsInt }.isNotEmpty()
//    }


//    fun isStationReplacementStopByStationID(stationId: String?): Boolean {
//        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
//        return evMap[stationIdAsInt] !=null
//    }

    fun isStationReplacementStopByEvaID(evaId: String?): Boolean {
        val evaIdAsInt = evaId?.toIntOrNull() ?: 0
        return evMap.filter { it.value.evaId==evaIdAsInt  }.isNotEmpty()
    }

    fun getSEV_News(stationId: String?) : List<News>? {

        if(containsStationId(stationId)) {
            val isInAnnouncementPhase = isInAnnouncementPhase()
            val isInConstructionPhase = isInConstructionPhase()
            val showAdhoc = shouldShowAdhocBox()

            if (isInAnnouncementPhase || isInConstructionPhase || showAdhoc) {

                // create content of adhoc box

                val l = mutableListOf<News>()
                val n = News()
                val g = Group()

                g.id = GroupId.REPLACEMENT_ANNOUNCEMENT.id
                g.title = BaseApplication.get() // ggf. animierte überschrift
                    .getString(if (isInAnnouncementPhase) R.string.nev_announcement_headline else R.string.nev_phase_headline)
                g.optionalData = BaseApplication.get()
                    .getString(if (isInAnnouncementPhase) R.string.sr_nev_announcement_headline else R.string.sr_nev_phase_headline)

                n.group = g

                n.id = "1"
                n.title = BaseApplication.get().getString(R.string.sev_date_range)
                n.titleForScreenReader = BaseApplication.get().getString(R.string.sr_sev_date_range)
                n.content = BaseApplication.get().getString(R.string.nev_copy)
                n.contentForScreenReader = BaseApplication.get().getString(R.string.sr_nev_copy)

                val cal = Calendar.getInstance()
                cal.add(Calendar.DATE, -1)
                n.startTimestamp = cal.time
                cal.add(Calendar.DATE, 1)
                n.endTimestamp = cal.time
                cal.add(Calendar.DATE, -10)
                n.createdAt = cal.time

                l.add(n)
                return l
            }

        }
        return null
    }

    fun getSEVStationNames() : List<String> {
        return evMap.map{it.value.stationName}.sortedBy { it }
    }

    fun findStations(searchTerm : String) : List<Pair<Int, EvItem>> {
        val lst : MutableList<Pair<Int, EvItem>> = mutableListOf()

        var sTerm = searchTerm
        if(searchTerm.contains("ss", true)) {
            sTerm = searchTerm.replace("ss", "ß", true)
        }
        if(searchTerm.contains("ue", true)) {
            sTerm = searchTerm.replace("ue", "ü", true)
        }
        if(searchTerm.contains("oe", true)) {
            sTerm = searchTerm.replace("oe", "ö", true)
        }
        if(searchTerm.contains("ae", true)) {
            sTerm = searchTerm.replace("ae", "ä", true)
        }

        evMap.forEach {
            if(it.value.stationName.contains(sTerm,true)) {
                lst.add(it.key to it.value)
            }
        }

        return lst
    }


    fun findStadaId(evaIds:EvaIds) : String? {
      var result:String?=null
      evMap.forEach { stadaId, item ->
          run {
              if (evaIds.ids.contains(item.evaId.toString())) {
                result = stadaId.toString()
                return@forEach
              }
          }
      }

      return result
    }

}
