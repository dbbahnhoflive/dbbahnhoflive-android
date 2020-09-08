package de.deutschebahn.bahnhoflive.ui.station.info

class StaticInfoDescriptionPart {
    val text: String?
    val button: DbActionButton?

    private constructor(text: String? = null, button: DbActionButton? = null) {
        this.text = text
        this.button = button
    }

    constructor(text: String) : this(text, null)
    constructor(button: DbActionButton) : this(null, button)
}