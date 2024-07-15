/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.wagenstand.istwr

import android.graphics.Color
import android.util.Log
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandDataMergeFactory
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTEUERWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTEUERWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKAUTOTRANSPORTWAGENREISEZUGWAGENBAUART
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKSTEUERWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKSTEUERWAGENERSTEZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKSTEUERWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKWAGENERSTEZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.DOPPELSTOCKWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.GEPAECKWAGEN
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.HALBSPEISEWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.HALBSPEISEWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.LIEGEWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.LIEGEWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.LOK
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.REISEZUGWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.REISEZUGWAGENERSTEZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.REISEZUGWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.SCHLAFWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.SCHLAFWAGENERSTEZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.SCHLAFWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.SPEISEWAGEN
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.STEUERWAGENERSTEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.STEUERWAGENERSTEZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.STEUERWAGENZWEITEKLASSE
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.TRIEBKOPF
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandAllFahrzeugData.Category.UNBEKANNT
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstInformationData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.FeatureStatus
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.Status
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.WaggonFeature
import de.deutschebahn.bahnhoflive.repository.trainformation.LegacyFeature
import de.deutschebahn.bahnhoflive.repository.trainformation.Train
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.repository.trainformation.Waggon
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.Locale

class RepositoryConverter {
    private val all = setOf(
        LOK,
        TRIEBKOPF,
        HALBSPEISEWAGENZWEITEKLASSE,
        HALBSPEISEWAGENERSTEKLASSE,
        SPEISEWAGEN,
        REISEZUGWAGENERSTEZWEITEKLASSE,
        REISEZUGWAGENZWEITEKLASSE,
        REISEZUGWAGENERSTEKLASSE,
        STEUERWAGENERSTEKLASSE,
        STEUERWAGENZWEITEKLASSE,
        DOPPELSTOCKSTEUERWAGENERSTEKLASSE,
        DOPPELSTOCKSTEUERWAGENZWEITEKLASSE,
        DOPPELSTOCKSTEUERWAGENERSTEZWEITEKLASSE,
        STEUERWAGENERSTEZWEITEKLASSE,
        DOPPELSTOCKWAGENERSTEZWEITEKLASSE,
        DOPPELSTOCKWAGENERSTEKLASSE,
        DOPPELSTOCKWAGENZWEITEKLASSE,
        DOPPELSTOCKAUTOTRANSPORTWAGENREISEZUGWAGENBAUART,
        SCHLAFWAGENERSTEKLASSE,
        SCHLAFWAGENERSTEZWEITEKLASSE,
        SCHLAFWAGENZWEITEKLASSE,
        LIEGEWAGENERSTEKLASSE,
        LIEGEWAGENZWEITEKLASSE,
        GEPAECKWAGEN,
        DOPPELSTEUERWAGENERSTEKLASSE,
        DOPPELSTEUERWAGENZWEITEKLASSE,
        UNBEKANNT
    )
    private val restaurantCategories = all.filter { it.contains("DININGCAR") }.toHashSet()
    private val multiClassCategories = all.filter { it.contains("FIRST_ECONOMY") || it.contains("HALF") }.toHashSet()
    private val firstClassCategories = all.filter { it.contains("FIRST") }.toHashSet()
    private val secondClassCategories = all.filter { it.contains("ECONOMY") }.toHashSet() + DOPPELSTOCKAUTOTRANSPORTWAGENREISEZUGWAGENBAUART
    private val nonWagonCategories = hashSetOf(LOK, TRIEBKOPF, UNBEKANNT)
    private val directionalEngineCategory = hashSetOf(TRIEBKOPF)
    private val undirectionalEngineCategory = hashSetOf(LOK)
    private val sleepingCategories = all.filter { it.contains("SLEEPER") || it.contains("COUCHETTE") }.toHashSet()
    private val luggageCategories = all.filter { it.contains("BAGGAGE") }.toHashSet()
    private val splitWaggonCategories = all.filter { it.contains("CONTROLCAR") }.toHashSet()

    private val COLOR_SECOND_CLASS = Color.rgb(0, 178, 27)
    private val COLOR_FIRST_CLASS = Color.rgb(255, 230, 13)
    private val COLOR_RESTAURANT = Color.rgb(255, 0, 0)
    private val COLOR_LUGGAGE = Color.rgb(153, 153, 153)
    private val COLOR_SLEEPING = Color.rgb(0, 115, 255)
    private val COLOR_MISC = Color.rgb(255, 97, 3)
    private val COLOR_NONE = Color.argb(0, 1, 1, 1)
    
    fun toTrainFormation(wagenstandIstInformationData: WagenstandIstInformationData) = wagenstandIstInformationData.run {

        val waggons = mutableListOf<Waggon>()
        val trains = mutableListOf<Train>()

//        val sections = halt.allSektor.map {
//            it.sektorbezeichnung
//        }.sorted()

        for (wagenstandFahrzeugData in allFahrzeuggruppe) {
            val number = wagenstandFahrzeugData.verkehrlichezugnummer
            val destinationStation = wagenstandFahrzeugData.zielbetriebsstellename

            val train = trains.lastOrNull()?.takeIf {
                it.number == number && it.destinationStation == destinationStation
            } ?: Train(
                number,
                zuggattung,
                destinationStation,
                WagenstandDataMergeFactory.extractSectionSpan(wagenstandFahrzeugData))
                .also {
                    trains += it
                }

            var front = true
            for (wagenstandAllFahrzeugData in wagenstandFahrzeugData.allFahrzeug) {
                waggons += if (wagenstandAllFahrzeugData.kategorie in splitWaggonCategories)
                    listOf(
                        createTerminator(train, wagenstandAllFahrzeugData, front),
                        createWaggon(train, wagenstandAllFahrzeugData, true, front)
                    ).let {
                        if (front) it else it.reversed()
                    }
                else
                    listOf(createWaggon(train, wagenstandAllFahrzeugData, front = front))

                front = false
            }
        }

        val reversed = waggons.firstOrNull()?.sections?.firstOrNull()?.let { firstSection ->
            waggons.lastOrNull()?.sections?.lastOrNull()?.let { lastSection ->
                if (Collator.getInstance(Locale.GERMAN).compare(firstSection, lastSection) > 0) {
                    waggons.reverse()
                    true
                } else false
            }
        } ?: false

        TrainFormation(
            waggons,
            trains,
            zuggattung,
            halt.abfahrtszeit.let {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY).parse(it)?.let { itDate ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(itDate)
                } ?: ""
            },
            halt.abfahrtszeit.let {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY).parse(it)?.let { itDate ->
                    SimpleDateFormat("HH:mm", Locale.GERMANY).format(itDate)
                } ?: ""
            },
            halt.gleisbezeichnung,
            reversed,
            allFahrzeuggruppe.first().verkehrlichezugnummer,
            true
        )
    }

    private fun createTerminator(train: Train, wagenstandAllFahrzeugData: WagenstandAllFahrzeugData, first: Boolean) = Waggon(
        train,
        false,
        emptyList(),
        emptyList(),
        "",
        false,
        listOf(wagenstandAllFahrzeugData.fahrzeugsektor),
        "",
        COLOR_MISC,
        COLOR_NONE,
        false,
        1,
        first,
        !first,
        false,
        wagenstandAllFahrzeugData.wagenordnungsnummer
    )

    private fun createWaggon(train: Train, wagenstandAllFahrzeugData: WagenstandAllFahrzeugData, half: Boolean = false, front: Boolean): Waggon {
        val kategorie = wagenstandAllFahrzeugData.kategorie
        return Waggon(
            train,
            kategorie in restaurantCategories,
            wagenstandAllFahrzeugData.allFahrzeugausstattung.mapNotNull {
                try {
                    val waggonFeature = WaggonFeature.valueOf(it.ausstattungsart)
                    val status = Status.valueOf(it.status)
                    FeatureStatus(waggonFeature, status)
                } catch (e: Exception) {
                   if(BuildConfig.DEBUG) {
                       Log.i(
                           TrainFormation::class.java.simpleName,
                           "waggon feature unusable: ${it.ausstattungsart}"
                       )
                   }
                   null
                }
            },
            listOfNotNull(kategorie.takeIf { it in restaurantCategories }?.let { LegacyFeature() }),
            "",
            kategorie in multiClassCategories,
            listOf(wagenstandAllFahrzeugData.fahrzeugsektor),
            when (kategorie) {
                in firstClassCategories -> "1"
                in secondClassCategories -> "2"
                else -> ""
            },
            when (kategorie) {
                in firstClassCategories -> COLOR_FIRST_CLASS
                in restaurantCategories -> COLOR_RESTAURANT
                in secondClassCategories -> COLOR_SECOND_CLASS
                in sleepingCategories -> COLOR_SLEEPING
                in luggageCategories -> COLOR_LUGGAGE
                else -> COLOR_MISC
            },
            when (kategorie) {
                in luggageCategories -> COLOR_LUGGAGE
                in sleepingCategories -> COLOR_SLEEPING
                in secondClassCategories -> COLOR_SECOND_CLASS
                in restaurantCategories -> COLOR_RESTAURANT
                else -> COLOR_NONE
            },
            kategorie !in nonWagonCategories,
            if (half || kategorie in directionalEngineCategory) 1 else 2,
            front && kategorie in directionalEngineCategory,
            !front && kategorie in directionalEngineCategory,
            kategorie in undirectionalEngineCategory,
            wagenstandAllFahrzeugData.wagenordnungsnummer
        )
    }
}

fun WagenstandIstResponseData.toTrainFormation() = RepositoryConverter().toTrainFormation(data.istformation)