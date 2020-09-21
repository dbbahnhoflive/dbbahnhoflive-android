/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

class TripleSCenter {

    var identifier: Int = -1

    var name: String? = null

    var publicPhoneNumber: String? = null
    var publicFaxNumber: String? = null
    var internalPhoneNumber: String? = null
    var internalFaxNumber: String? = null

    var address: Address? = null

}