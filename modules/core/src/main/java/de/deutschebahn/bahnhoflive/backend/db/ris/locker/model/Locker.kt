/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.db.ris.locker.model


class Locker {
    var amount: Int? = null
    var dimension: LockerDimension? = null
    var fee: LockerFee? = null
    var maxLeaseDuration: String? = null
    var paymentTypes: MutableList<String?>? = null
    var size: String = ""
}

