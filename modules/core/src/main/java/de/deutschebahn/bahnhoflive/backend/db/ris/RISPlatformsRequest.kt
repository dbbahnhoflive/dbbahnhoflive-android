/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.model.AccessibilityStatus
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.util.json.asJSONObjectSequence
import org.json.JSONObject
import java.util.*

class RISPlatformsRequest(
    listener: VolleyRestListener<List<Platform>>,
    dbAuthorizationTool: DbAuthorizationTool,
    evaId: String,
    force: Boolean = false,
) : DbRequest<List<Platform>>(
    Method.GET,
    "https://gateway.businesshub.deutschebahn.com/ris-stations/v1/stop-places/$evaId/platforms" +
            "?includeAccessibility=true",
    dbAuthorizationTool,
    listener
) {

    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "RIS/stations"

    override fun parseNetworkResponse(response: NetworkResponse?): Response<List<Platform>> {
        super.parseNetworkResponse(response)

        return try {
            val platforms = response?.data?.decodeToString()?.let { responseString ->
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
                                                    )?.let {
                                                        try {
                                                            AccessibilityStatus.valueOf(it)
                                                        } catch (e: Exception) {
                                                            null
                                                        }
                                                    } ?: AccessibilityStatus.UNKNOWN
                                                acc
                                            }
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

    override fun getAuthorizationHeaderKey() = "db-api-key"

}