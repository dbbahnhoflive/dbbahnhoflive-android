/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

/**
 * Calls the specified function [block] with `this` value as its receiver only if it is not `null` and finally returns `null`.
 */
inline fun <T> T?.destroy(block: T.() -> Unit): T? {
    this?.block()
    return null
}
