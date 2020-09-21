/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.search

class QueryPart(val phrase: String) {
    val predicate: (String) -> Boolean = if (phrase.length == 1 && !phrase[0].isDigit())
        { candidate ->
            candidate.startsWith(phrase, true)
        }
    else
        { candidate ->
            candidate.contains(phrase, true)
        }
}