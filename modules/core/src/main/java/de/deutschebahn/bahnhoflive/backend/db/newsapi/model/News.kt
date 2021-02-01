/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.newsapi.model

import android.net.Uri
import android.util.Base64
import java.util.*

class News {

    var published = false

    lateinit var id: String

    lateinit var title: String

    var subtitle: String? = null

    lateinit var content: String

    var version = -1

    lateinit var startTimestamp: Date

    lateinit var endTimestamp: Date

    lateinit var createdAt: Date

    val decodedImage by lazy {
        image?.substringAfter(",")?.let {
            Base64.decode(it, Base64.DEFAULT)
        }?.also { image = null }?.takeUnless { it.isEmpty() }
    }

    var image: String? = null

    var updatedAt: Date? = null

    val modifiedAt get() = updatedAt ?: createdAt

    var optionalData: OptionalData? = null

    lateinit var group: Group

    val link get() = optionalData?.link?.ifBlank { null }

    val linkUri by lazy {
        try {
            link?.trim()?.let {
                Uri.parse(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    val isLinkNotBroke get() = link.isNullOrBlank() || linkUri != null

    fun isActiveAt(timestamp: Date) = startTimestamp.before(timestamp) && endTimestamp.after(timestamp)

    val groupSortKey
        get() = when (group.id) {
            2 -> 1
            3 -> 2
            1 -> 3
            else -> group.id
        }
}
