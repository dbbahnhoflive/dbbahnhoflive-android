package de.deutschebahn.bahnhoflive.ui.station.parking

import android.content.Context
import android.text.Html
import android.text.TextUtils
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility
import de.deutschebahn.bahnhoflive.util.then
import java.util.*

internal abstract class DescriptionRenderer(protected val context: Context) {
    protected fun MutableList<String>.addDescriptionDetail(
        label: String,
        value: String?
    ) = apply {
        value.takeUnless { it.isNullOrBlank() }?.let { value ->
            add("<b>$label: </b>${TextUtils.htmlEncode(value.trim())}")
        }
    }

    fun render(parkingFacility: ParkingFacility): CharSequence {
        val rawText = compileRawText(parkingFacility)
        return Html.fromHtml(rawText)
    }

    protected open fun compileRawText(parkingFacility: ParkingFacility): String {
        val descriptionParts = addDetails(parkingFacility)
        return TextUtils.join("<br/><br/>", descriptionParts)
    }

    protected abstract fun addDetails(
        parkingFacility: ParkingFacility,
        descriptionParts: MutableList<String> = mutableListOf()
    ): MutableList<String>

    companion object {

        class BriefDescriptionRenderer(context: Context) : DescriptionRenderer(context) {
            override fun addDetails(
                parkingFacility: ParkingFacility,
                descriptionParts: MutableList<String>
            ) = descriptionParts.apply {
                addDescriptionDetail("Name", parkingFacility.name)
                addDescriptionDetail("Anschrift", parkingFacility.access)
                addDescriptionDetail("Öffnungszeiten", parkingFacility.openingHours)
                addDescriptionDetail("Frei parken", parkingFacility.freeParking)
                addDescriptionDetail("Maximale Parkdauer", parkingFacility.maxParkingTime)
                addDescriptionDetail("Nächster Bahnhofseingang", parkingFacility.distanceToStation)
            }
        }

        class DetailedDescriptionRenderer(context: Context) : DescriptionRenderer(context) {
            override fun addDetails(
                parkingFacility: ParkingFacility,
                descriptionParts: MutableList<String>
            ) = descriptionParts.apply {
                addDescriptionDetail("Zufahrt", parkingFacility.access)
                addDescriptionDetail("Zufahrt (Details)", parkingFacility.mainAccess)
                addDescriptionDetail("Zufahrt (Nachts)", parkingFacility.nightAccess)
                addDescriptionDetail("Öffnungszeiten", parkingFacility.openingHours)
                addDescriptionDetail("Maximale Parkdauer", parkingFacility.maxParkingTime)
                addDescriptionDetail("Nächster Bahnhofseingang", parkingFacility.distanceToStation)
                addDescriptionDetail(
                    "Ausstattung",
                    parkingFacility.featureTags.joinToString { context.getString(it.label) }
                        .ifBlank { null })
                addDescriptionDetail("Betreiber", parkingFacility.operator)
            }
        }

        class PriceDescriptionRenderer(context: Context) : DescriptionRenderer(context) {
            override fun addDetails(
                parkingFacility: ParkingFacility,
                descriptionParts: MutableList<String>
            ) = descriptionParts.apply {
                parkingFacility.prices.forEach { price ->

                    addDescriptionDetail(
                        sequenceOf(
                            sequenceOf(
                                price.group,
                                price.period
                            ).filterNotNull().joinToString(separator = " ")
                                .takeUnless { it.isBlank() },
                            with(price.duration) {
                                if (amount != null && timeUnit != null) {
                                    sequenceOf(
                                        context.resources.getQuantityString(
                                            timeUnit.quantityString,
                                            amount,
                                            amount
                                        ),
                                        discount then { "(Rabatt)" },
                                        vendingMachine then { "(am Automaten)" },
                                        longTerm then { "Langzeitparken" },
                                        reservation then { "Reserviert" },
                                        unrecognizedTags?.let { "($it)" }
                                    ).filterNotNull().joinToString(" ")
                                } else raw
                            }
                        ).filterNotNull().joinToString(),
                        "%.2f €".format(Locale.GERMAN, price.price)
                    );
                }
            }

            override fun compileRawText(parkingFacility: ParkingFacility): String {
                return "Alle Angaben in EUR, inkl. MwSt.<br/>" + super.compileRawText(
                    parkingFacility
                )
            }
        }
    }
}