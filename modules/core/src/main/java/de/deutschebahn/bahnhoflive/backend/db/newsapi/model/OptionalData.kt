/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.newsapi.model

class OptionalData {

    var link: String? = null
        set(value) {
            field = value?.takeUnless { it.isBlank() }
        }

}