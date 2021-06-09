package de.deutschebahn.bahnhoflive.backend.ris.model

object RimapMessageCodes {

    val messages = mapOf(
        "80" to "Andere Reihenfolge der Wagen",
        "82" to "Mehrere Wagen fehlen",
        "83" to "Störung fahrzeuggebundene Einstiegshilfe",
        "85" to "Ein Wagen fehlt",
        "86" to "Gesamter Zug ohne Reservierung",
        "87" to "Einzelne Wagen ohne Reservierung",
        "90" to "Kein gastronomisches Angebot",
        "91" to "Fehlende Fahrradbeförderung",
        "92" to "Eingeschränkte Fahrradbeförderung",
        "93" to "Keine behindertengerechte Einrichtung",
        "95" to "Ohne behindertengerechtes WC"
    )

    val revocations = mapOf(
        "84" to setOf("80", "82", "85"),
        "88" to setOf(
            "80",
            "82",
            "83",
            "85",
            "86",
            "87",
            "90",
            "91",
            "92",
            "93",
            "94",
            "95",
            "96",
            "97",
            "98"
        ),
        "89" to setOf("86", "87")
    )

    fun getRevocationsOf(code: String) = revocations[code]?.toTypedArray() ?: emptyArray()

    fun isCodeRelevant(code: String) = messages.containsKey(code) || revocations.containsKey(code)

}