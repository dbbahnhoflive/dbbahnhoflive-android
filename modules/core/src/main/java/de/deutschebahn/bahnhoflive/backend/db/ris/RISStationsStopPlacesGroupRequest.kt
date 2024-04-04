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
    buildUrlSuffix(evaId),
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

//            val evaIds = groups.groups.flatMap {it.members}.toSet().toTypedArray()
// Option 1.1 nur SALES verwenden, STATION raus
            val salesGroup = groups.groups.firstOrNull{it.type=="SALES"}

            val evaIds: Array<String> = if (salesGroup?.members != null) {
                salesGroup.members.toTypedArray()
            } else arrayOf()

            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.HOUR_IN_MILLISECONDS)

            Response.success(evaIds, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            Response.error(VolleyError(e))
        }
    }

    companion object {

        fun buildUrlSuffix(evaId: String): String {
            return "stop-places/" + evaId + "/groups"
        }

    }
}