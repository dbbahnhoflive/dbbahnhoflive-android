/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.db.fasta2

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.deutschebahn.bahnhoflive.backend.DetailedVolleyError
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.DbRequest
import de.deutschebahn.bahnhoflive.backend.db.fasta2.FastaConstants.BASE_URL
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import java.util.*

class FacilityEquipmentStatusRequest(
    private val sourceFacilityStatuses: List<FacilityStatus>,
    authorizationTool: DbAuthorizationTool?,
    listener: VolleyRestListener<List<FacilityStatus>>
) : DbRequest<List<FacilityStatus>>(
    Method.GET,
    "${BASE_URL}facilities?equipmentnumbers=${
        sourceFacilityStatuses.map { it.equipmentNumber }.joinToString()
    }",
    authorizationTool,
    listener
) {

    override fun parseNetworkResponse(response: NetworkResponse) =
        kotlin.runCatching {
            super.parseNetworkResponse(response)

            Gson().fromJson<List<FacilityStatus>>(
                response.data.toString(Charsets.UTF_8),
                (object : TypeToken<List<FacilityStatus>>() {}).type
            )
        }.run {
            getOrNull()?.associateBy { it.equipmentNumber }?.let { facilityStatuses ->
                sourceFacilityStatuses.forEach { sourceFacilityStatus ->
                    facilityStatuses[sourceFacilityStatus.equipmentNumber]?.let { facilityStatus ->
                        // this happens on the main thread and thus doesn't need synchronization
                        // NOTE: server response does not contain the stationName and
                        // the stored facility item contains the subscribed status!
                        sourceFacilityStatus.description = facilityStatus.description
                        sourceFacilityStatus.latitude = facilityStatus.latitude
                        sourceFacilityStatus.longitude = facilityStatus.longitude
                        sourceFacilityStatus.type = facilityStatus.type
                        sourceFacilityStatus.state = facilityStatus.state
                    } ?: kotlin.run {
                        sourceFacilityStatus.state = ""
                    }
                }

                Response.success(
                    sourceFacilityStatuses,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            } ?: exceptionOrNull().let {
                Response.error(DetailedVolleyError(this@FacilityEquipmentStatusRequest, it))
            }
        }


    override fun getCountKey(): String? = null
}