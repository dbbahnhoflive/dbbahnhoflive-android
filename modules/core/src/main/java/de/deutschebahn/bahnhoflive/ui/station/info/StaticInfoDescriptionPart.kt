/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

class StaticInfoDescriptionPart private constructor(
    val text: String? = null,
    val button: DbActionButton? = null
) {

    constructor(text: String) : this(text, null)
    constructor(button: DbActionButton) : this(null, button)
}