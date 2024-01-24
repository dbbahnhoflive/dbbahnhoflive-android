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
import de.deutschebahn.bahnhoflive.backend.db.ris.model.PlatformWithLevelAndLinkedPlatforms
import de.deutschebahn.bahnhoflive.backend.db.ris.model.PlatformWithLevelAndLinkedPlatformsComparator
import de.deutschebahn.bahnhoflive.backend.db.ris.model.combineToSet
import de.deutschebahn.bahnhoflive.backend.db.ris.model.containsPlatform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.getLevel
import de.deutschebahn.bahnhoflive.backend.db.ris.model.getPlatformWithMostLinkedPlatforms
import de.deutschebahn.bahnhoflive.backend.db.ris.model.removeNotExistingLinkedPlatforms
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeature
import de.deutschebahn.bahnhoflive.util.DebugX
import de.deutschebahn.bahnhoflive.util.json.asJSONObjectSequence
import de.deutschebahn.bahnhoflive.util.json.toStringList
import org.json.JSONObject
import java.util.EnumMap
import java.util.EnumSet


class RISPlatformsRequestResponseParser {

    fun parse(jsonString: String?) : List<Platform> {

        val platforms = kotlin.runCatching {

            jsonString?.let { responseString ->
                JSONObject(responseString).optJSONArray("platforms")
                    ?.asJSONObjectSequence()
                    ?.filterNotNull()
                    ?.mapNotNull { platformJsonObject ->
                        platformJsonObject.takeUnless { it.has("parentPlatform") }
                            ?.optString("name")?.let { name ->

                                val accessibilityJson =
                                    platformJsonObject.optJSONObject("accessibility")

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
        }
        return platforms.getOrElse { emptyList() }
    }


    // level, platformname, linked platforms
    fun getLinkedPlatforms(allPlatforms: List<Platform>, linkedPlatforms:MutableList<Platform>): MutableList<PlatformWithLevelAndLinkedPlatforms> {

        val reducedList: MutableList<Platform> =
            allPlatforms.filter { it.hasLinkedPlatforms }.toMutableList()

        // falls Gleise mit gleichem Namen vorhanden sind, das Gleis mit den meisten linked Gleisen nehmen
        reducedList.forEach { itLoopItem ->
            if (!linkedPlatforms.containsPlatform(itLoopItem.name))
                reducedList.getPlatformWithMostLinkedPlatforms(itLoopItem.name)?.let {
                    linkedPlatforms.add(it)
                }
        }

//                   aus Gleisname und Linked-Gleisen ein SET bilden:
        val linkedSetList0: MutableList<PlatformWithLevelAndLinkedPlatforms> = mutableListOf()
        linkedPlatforms.forEach { itLoopItem ->
            linkedSetList0.add(
                PlatformWithLevelAndLinkedPlatforms(
                    linkedPlatforms.getLevel(itLoopItem.name),
                    itLoopItem.name,
                    itLoopItem.combineToSet(true)
                )
            )
        }


        val linkedSetList: MutableList<PlatformWithLevelAndLinkedPlatforms> = mutableListOf()

        // Nur Gleise, die in allen linked-Gleisen uebereinstimmen, werden als "am gleichen Bahnsteig" angesehen
        linkedPlatforms.forEach { itLoopItem ->

            val nItems =
                linkedSetList0.count { itLoopItem.combineToSet(true) == it.linkedPlatforms }

            if (nItems > 1) {
                if (linkedSetList.find { itLoopItem.combineToSet() == it.linkedPlatforms } == null) {
                    linkedSetList.add(
                        PlatformWithLevelAndLinkedPlatforms(
                            linkedPlatforms.getLevel(itLoopItem.name),
                            itLoopItem.name,
                            itLoopItem.combineToSet(true)
                        )
                    )
                }
            } else {
                if (linkedSetList.find { itLoopItem.combineToSet(false) == it.linkedPlatforms } == null) {
                    linkedSetList.add(
                        PlatformWithLevelAndLinkedPlatforms(
                            linkedPlatforms.getLevel(itLoopItem.name),
                            itLoopItem.name,
                            itLoopItem.combineToSet(false)
                        )
                    )
                }
            }
        }

        linkedPlatforms.removeNotExistingLinkedPlatforms()

        // ggf. Gleise, die einen eigenen set haben aus den anderen sets entfernen
        linkedSetList.forEach { itSet ->
            val iter = itSet.linkedPlatforms.iterator()
            while (iter.hasNext()) {
                val item = iter.next()
                if (linkedSetList.find { itSet != it && it.platformName == item } != null) {
                    iter.remove()
                } else // linkedPlatforms, die nicht ex. aus set entfernen
                    if (linkedPlatforms.find { it.name == item } == null) {
                        iter.remove()
                    }
            }
        }

        // linked Gleise, die nicht auf dem geichen Stockwerk sind sonderbehandeln
        linkedSetList.forEach { itSet ->
            val iter = itSet.linkedPlatforms.iterator()
            while (iter.hasNext()) {
                val item = iter.next()
                if(reducedList.getLevel(item)!=itSet.level)
                    iter.remove()
            }
        }

        // gg. Gleise, die in einem anderen set sind, entfernen
         linkedSetList.forEach { itSet ->
            val iter = itSet.linkedPlatforms.iterator()
            while (iter.hasNext()) {
                val item = iter.next()
                val found = linkedSetList.find { itSet != it && it.linkedPlatforms.contains(item) }
                if(found!=null) {
                    iter.remove()
                }
            }
        }

        linkedSetList.sortWith(PlatformWithLevelAndLinkedPlatformsComparator())

        return linkedSetList
    }




}


class RISPlatformsRequest(
    listener: VolleyRestListener<List<Platform>>,
    dbAuthorizationTool: DbAuthorizationTool,
    stadaId: String,
    force: Boolean = false,
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

            val jsonString = response.data?.decodeToString()

            val platforms = RISPlatformsRequestResponseParser().parse(jsonString)

            val forcedCacheEntryFactory =
                ForcedCacheEntryFactory(ForcedCacheEntryFactory.DAY_IN_MILLISECONDS)

            Response.success(platforms, forcedCacheEntryFactory.createCacheEntry(response))
        } catch (e: Exception) {
            DebugX.logVolleyResponseException(this, url, e)
            Response.error(VolleyError(e))
        }
    }

}