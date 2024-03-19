/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.Gson
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool


class RISStationsStopPlacesGroupRequest(
    listener: VolleyRestListener<Array<String>>, // EvaIds
    dbAuthorizationTool: DbAuthorizationTool,
    evaId: String,
    force: Boolean = false
) : RISStationsRequest<Array<String>>( //         "https://apis.deutschebahn.com/db/apis/ris-stations/v1/$urlSuffix",
    BuildUrlSuffix(evaId),
    dbAuthorizationTool,
    listener
) {
    init {
        setShouldCache(!force)
    }

    override fun getCountKey() = "RIS/stations/stop-places"

    inner class XGroup (
        val type : String,
        val members : List<String> )


    inner class XGroups (
        val groups :List<XGroup>
    )
    override fun parseNetworkResponse(response: NetworkResponse): Response<Array<String>> {
        super.parseNetworkResponse(response)

        return try {
            val json = response.data.decodeToString()

            val gson = Gson()
            val groups: XGroups = gson.fromJson(json, XGroups::class.java)

            val evaIds = groups.groups.flatMap {it.members}.toSet().toTypedArray()

            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.HOUR_IN_MILLISECONDS)

            Response.success(evaIds, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            Response.error(VolleyError(e))
        }
    }

    companion object {

        fun BuildUrlSuffix(evaId: String): String {
            var ret: String = "stop-places/" + evaId + "/groups"
            return ret
        }

    }
}