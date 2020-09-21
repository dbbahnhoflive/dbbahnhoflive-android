/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search

import android.util.Log

class QueryRecorder {

    val queries = mutableListOf<String>()

    fun put(query: String) {
        query.trim().takeIf { candidate ->
            queries.none { knownQuery ->
                knownQuery.startsWith(candidate)
            }
        }?.let { newEntry ->
            queries.removeAll { knownQuery ->
                newEntry.startsWith(knownQuery)
            }

            queries.add(newEntry)

            Log.i(StationSearchFragment.TAG, concatenatedQueries)
        }
    }

    fun clear() {
        queries.clear()
    }

    val concatenatedQueries get() = queries.joinToString()
}