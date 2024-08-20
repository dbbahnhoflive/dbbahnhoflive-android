package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugausstattungData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandDataData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandFahrzeugData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandHaltData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstInformationData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData
import java.io.ByteArrayInputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class RisDepartureWagenstand {

    inner class PlatformAccessibility {
        val platformSign: String = "" // "AVAILABLE",
        val stairsMarking: String = "" //  "AVAILABLE",
        val standardPlatformHeight: String = "" //  "AVAILABLE",
        val tactileGuidingStrips: String = "" //  "AVAILABLE",
        val tactileHandrailLabel: String = "" //  "AVAILABLE",
        val tactilePlatformAccess: String = "" //  "NOT_AVAILABLE"
    }

    inner class PlatformSector {
        val name: String = ""
        val start:Double=0.0
        val end:Double=0.0
        val cubePosition:Double=0.0
    }


    inner class PlatformDetails {
        val name : String = ""
        val start : Double = 0.0
        val end : Double = 0.0
        val accessibility : PlatformAccessibility = PlatformAccessibility()
        val sectors : Array<PlatformSector> = arrayOf()
    }

    inner class Platform {
        val details : PlatformDetails = PlatformDetails()
        val platform: String = ""
        val platformSchedule : String =""
    }

    inner class JourneyRelation {
        val startAdministrationID: String = ""
        val startCategory: String = ""
        val startEvaNumber: String = ""
        val startJourneyNumber: String = ""
        val startTime: String = ""
    }

    inner class Station {
        val evaNumber: String = ""
        val name: String = ""
    }



    inner class Amenity {
        val amount:Int=0
        val status:String=""
        val type:String=""
    }

    inner class PlatformPosition {
        val start : Double = 0.0
        val end : Double = 0.0
        val sector : String = ""
    }


    inner class VehicleType {
        val category: String="" // CONTROLCAR_FIRST_CLASS",
        val constructionType: String="" // "Apmzf",
        val hasEconomyClass: Boolean=false //  false,
        val hasFirstClass: Boolean=false //  true
    }

    inner class Vehicle { // Waggon
        val vehicleID:String=""
        val type : VehicleType = VehicleType()
        val wagonIdentificationNumber:Int=0
        val status:String=""
        val orientation: String = ""
        val platformPosition = PlatformPosition()
        val amenities : Array<Amenity> = arrayOf()
    }

    inner class Group { // Zug
        val name: String = ""
        val journeyID: String = ""
        val destination: Station = Station()
        val journeyRelation: JourneyRelation = JourneyRelation()
        val vehicles : Array<Vehicle> = arrayOf()
    }

    val departureID: String = ""
    val journeyID: String = ""
    val groups: Array<Group> = arrayOf()
    val platform : Platform = Platform()
    val sequenceStatus: String = "" // "DIFFERS_FROM_SCHEDULE"

}


open class RISTransportsCoreRequest<T>(
    urlSuffix: String,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<T>
) : DbRequest<T>(
    Method.GET,
    "${BuildConfig.RIS_TRANSPORTS_BASE_URL}$urlSuffix",
    dbAuthorizationTool,
    restListener,
    "db-api-key"
) {
    init {
        retryPolicy = DefaultRetryPolicy(30000, 1, 1.0f)
    }
    override fun getCountKey(): String = "ris-transports"
}


class RISTransportsRequest(
    parameters: Parameters,
    dbAuthorizationTool: DbAuthorizationTool,
    restListener: VolleyRestListener<WagenstandIstResponseData>
) : RISTransportsCoreRequest<WagenstandIstResponseData>(
    "vehicle-sequences/departures/unmatched?${parameters.toUrlParameters()}",
    dbAuthorizationTool,
    restListener

) {

   // https://apis.deutschebahn.com/db/apis/ris-transports/v3/
    // vehicle-sequences/departures/unmatched?
    // includeAmenities=true
    // &date=2024-07-11
    // &category=ICE
    // &journeyNumber=650
    // &evaNumber=8011160
    // &includeOccupancy=NONE
    // &includePosition=true

    class Parameters(
        val evaId: String,
        val trainNumber: String,
        val trainCategory: String,
        val date: String,
    ) {
        fun toUrlParameters() = listOfNotNull(
            "includeAmenities" to "true",
            "date" to date,
            "category" to trainCategory,
            trainNumber.let { "journeyNumber" to trainNumber },
            evaId.let { "evaNumber" to evaId },
            "includeOccupancy" to "NONE",
            "includePosition" to "true"
        ).joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<WagenstandIstResponseData> {
        super.parseNetworkResponse(response)

        return kotlin.runCatching {
            val departureMatches = Gson().fromJson(
                ByteArrayInputStream(response.data).reader(),
                RisDepartureWagenstand::class.java
            )

            val  data = WagenstandIstResponseData()
            data.data = WagenstandDataData()
            data.data.istformation = WagenstandIstInformationData()
            data.data.istformation.let {
                it.allFahrzeuggruppe = mutableListOf()
                it.zuggattung = departureMatches.groups.firstOrNull()?.journeyRelation?.startCategory?:""
                it.halt = WagenstandHaltData()

                it.halt.abfahrtszeit = departureMatches.groups.firstOrNull()?.journeyRelation?.startTime?:""
                it.halt.gleisbezeichnung = departureMatches.platform.platform
            }

            departureMatches.groups.forEach { itGroup ->

                run {

                    val wagenstandFahrzeugData = WagenstandFahrzeugData()
                    wagenstandFahrzeugData.allFahrzeug = mutableListOf()
                    wagenstandFahrzeugData.zielbetriebsstellename = itGroup.destination.name
                    wagenstandFahrzeugData.verkehrlichezugnummer = itGroup.journeyRelation.startJourneyNumber

                    itGroup.vehicles.forEach { itVehicle ->
                        run {
                            val wagenstandAllFahrzeugData = WagenstandAllFahrzeugData()

                            wagenstandAllFahrzeugData.let {
                                it.allFahrzeugausstattung = mutableListOf()
                                it.status = itVehicle.status
                                it.kategorie = itVehicle.type.category
                                it.fahrzeugsektor = itVehicle.platformPosition.sector
                                if(it.fahrzeugsektor==null)
                                    it.fahrzeugsektor=""
                                it.wagenordnungsnummer = itVehicle.wagonIdentificationNumber.toString()
                            }

                            itVehicle.amenities.forEach { itAmenity ->
                                run {
                                    val wagenstandAllFahrzeugausstattungData =
                                        WagenstandAllFahrzeugausstattungData()

                                    wagenstandAllFahrzeugausstattungData.let {
                                        it.status = itAmenity.status
                                        it.anzahl = itAmenity.amount.toString()
                                        it.ausstattungsart = itAmenity.type
                                    }

                                    wagenstandAllFahrzeugData.allFahrzeugausstattung.add(
                                        wagenstandAllFahrzeugausstattungData
                                    )
                                }

                            }

                            wagenstandFahrzeugData.allFahrzeug.add(wagenstandAllFahrzeugData)
                        }

                    }

                    data.data.istformation.allFahrzeuggruppe.add(wagenstandFahrzeugData)

                }

            }


            Response.success(
                data, //departureMatches,
                ForcedCacheEntryFactory(TimeUnit.HOURS.toMillis(2).toInt()).createCacheEntry(
                    response
                )
            )
        }.getOrElse {
            Response.error(DetailedVolleyError(this, it))
        }
    }

//    override fun parseNetworkError(volleyError: VolleyError): VolleyError {
////        logVolleyResponseError(this, url, volleyError)
//        return super.parseNetworkError(volleyError)
//    }

}