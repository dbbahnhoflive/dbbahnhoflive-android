/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris

import androidx.core.text.isDigitsOnly
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.util.json.asJSONObjectSequence
import de.deutschebahn.bahnhoflive.util.json.toStringList
import org.json.JSONObject
import java.util.*

class RISPlatformsRequest(
    listener: VolleyRestListener<List<Platform>>,
    dbAuthorizationTool: DbAuthorizationTool,
    stadaId: String,
    force: Boolean = false,
    clientIdDbAuthorizationTool: DbAuthorizationTool?,
) : RISStationsRequest<List<Platform>>(
    "platforms/by-key?includeAccessibility=true&keyType=STADA&key=$stadaId",
    dbAuthorizationTool,
    listener
) {

    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "RIS/stations"

    override fun parseNetworkResponse(response: NetworkResponse): Response<List<Platform>> {
        super.parseNetworkResponse(response)

        return try {
            val platforms = response.data?.decodeToString()?.let { responseString ->
                JSONObject(responseString).optJSONArray("platforms")
                    ?.asJSONObjectSequence()
                    ?.filterNotNull()
                    ?.mapNotNull { platformJsonObject ->
                        platformJsonObject.takeUnless { it.has("parentPlatform") }
                            ?.optString("name")?.let { name ->
                                platformJsonObject.optJSONObject("accessibility")
                                    ?.let { accessibilityJsonObject ->
                                        Platform(
                                            name,
                                            AccessibilityFeature.VALUES.fold(
                                                EnumMap<AccessibilityFeature, AccessibilityStatus>(
                                                    AccessibilityFeature::class.java
                                                )
                                            ) { acc, accessibilityFeature ->
                                                acc[accessibilityFeature] =
                                                    accessibilityJsonObject.optString(
                                                        accessibilityFeature.tag
                                                    ).let {
                                                        try {
                                                            AccessibilityStatus.valueOf(it)
                                                        } catch (e: Exception) {
                                                            null
                                                        }
                                                    } ?: AccessibilityStatus.UNKNOWN
                                                acc
                                            },
                                            platformJsonObject.optJSONArray("linkedPlatforms")?.toStringList()?.filter { it.isDigitsOnly() }?.toMutableList(),
                                            platformJsonObject.optBoolean("headPlatform"),
                                            platformJsonObject.optDouble("start", -1.0),
                                            platformJsonObject.optDouble("end", 0.0),
                                            platformJsonObject.optDouble("length", 0.0)
                                        )
                                    }
                            }
                    }?.toList()
            } ?: emptyList()

            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)

            Response.success(platforms, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            Response.error(VolleyError(e))
        }
    }
}