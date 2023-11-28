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
    "platforms/by-key?includeAccessibility=true&includeSubPlatforms=false&keyType=STADA&key=$stadaId",
    dbAuthorizationTool,
    listener

//    object : VolleyRestListener<List<Platform>> {
//
//        @Synchronized
//        override fun onSuccess(payload: List<Platform>) {
//            listener.onSuccess(payload)
//        }
//
//        @Synchronized
//        override fun onFail(reason: VolleyError) {
//            // todo: stop requesting for a while
//            listener.onFail(reason)
//        }
//
//    }
) {

    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "RIS/stations"

    /**
     * deliver all platforms(multiple possible!) with a name, with ot without accessibility-information
     */
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

                                val accessibilityJson =  platformJsonObject.optJSONObject("accessibility")

                                name.takeIf { it.isNotEmpty() }.let {

                                    val emap = if (accessibilityJson != null) {
                                        AccessibilityFeature.VALUES.fold(
                                                EnumMap<AccessibilityFeature, AccessibilityStatus>(
                                                    AccessibilityFeature::class.java
                                                )
                                            ) { acc, accessibilityFeature ->
                                                acc[accessibilityFeature] =
                                                accessibilityJson.optString(
                                                        accessibilityFeature.tag
                                                    ).let {
                                                        try {
                                                            AccessibilityStatus.valueOf(it)
                                                        } catch (e: Exception) {
                                                            null
                                                        }
                                                    } ?: AccessibilityStatus.UNKNOWN
                                                acc
                                    }
                                    } else {
                                        EnumMap(
                                            EnumSet.allOf(AccessibilityFeature::class.java)
                                                .associateWith { AccessibilityStatus.UNKNOWN })
                                    }

                                Platform(
                                    name,
                                    emap,
                                    platformJsonObject.optJSONArray("linkedPlatforms")
                                        ?.toStringList()?.toMutableList(),
                                        platformJsonObject.optBoolean("headPlatform")
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