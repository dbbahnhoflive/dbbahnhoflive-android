/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util

fun <T> Sequence<T>.append(nextSequence: Sequence<T>?) = nextSequence?.let { plus(it) } ?: this

fun <T> Sequence<T>.append(element: T?) = element?.let { plus(it) } ?: this
