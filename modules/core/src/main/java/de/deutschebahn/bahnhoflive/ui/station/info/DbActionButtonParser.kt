/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

class DbActionButtonParser {

    companion object {
        const val TAG_NAME = "dbactionbutton"
    }

    val pattern = Regex("<$TAG_NAME(?:\\s+(href|action)=\"(.*?)\")?\\s*>(.*?)</$TAG_NAME>")

    fun parse(input: String): List<StaticInfoDescriptionPart> {

        val result = mutableListOf<StaticInfoDescriptionPart>()

        var index = 0

        pattern.findAll(input).forEach { matchResult ->

            if (index < matchResult.range.first) {
                result += StaticInfoDescriptionPart(input.substring(index, matchResult.range.first))
            }

            with(matchResult.groupValues) {
                result += StaticInfoDescriptionPart(
                    if (size == 4) {
                        when (get(1)) {
                            "action" -> DbActionButton(DbActionButton.Type.ACTION, get(2), get(3))
                            "href" -> DbActionButton(DbActionButton.Type.HREF, get(2), get(3))
                            else -> DbActionButton(DbActionButton.Type.LEGACY, get(2), get(3))
                        }
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