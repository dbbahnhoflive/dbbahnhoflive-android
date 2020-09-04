package de.deutschebahn.bahnhoflive.ui.station.info

class DbActionButtonParser {

    companion object {
        const val TAG_NAME = "dbactionbutton"
    }

    val pattern = Regex("<$TAG_NAME\\s+(?:href=\"(.*?)\")?\\s*>(.*?)</$TAG_NAME>")

    fun parse(input: String): List<StaticInfoDescriptionPart> {

        val result = mutableListOf<StaticInfoDescriptionPart>()

        var index = 0

        pattern.findAll(input).forEach { matchResult ->

            if (index < matchResult.range.first) {
                result += StaticInfoDescriptionPart(input.substring(index, matchResult.range.first))
            }

            with(matchResult.groupValues) {
                result += StaticInfoDescriptionPart(
                    if (size == 3) {
                        DbActionButton(get(1), get(2))
                    } else {
                        DbActionButton()
                    }
                )
            }

            index = matchResult.range.last + 1;
        }

        if (index < input.length) {
            result += StaticInfoDescriptionPart(input.substring(index))
        }

        return result
    }
}