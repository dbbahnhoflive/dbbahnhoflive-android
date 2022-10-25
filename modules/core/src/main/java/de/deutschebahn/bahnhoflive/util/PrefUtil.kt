/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import java.util.Collections

object PrefUtil {
    private const val SAVED_FACILITIES = "38"
    private const val FACILITIES_PUSH_ACTIVE = "39"
    const val FORMAT_ALARM = "alarm_%s"

    private fun getPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    /*
     *
     *
     * CACHING
     *
     *
     */
    @JvmStatic
    fun storeAlarmKey(key: String, context: Context) {
        val alarmKey = String.format(FORMAT_ALARM, key)
        getPrefs(context)
            .edit()
            .putString(alarmKey, key)
            .commit()
    }

    @JvmStatic
    fun cleanAlarmKey(key: String, context: Context) {
        val alarmKey = String.format(FORMAT_ALARM, key)
        getPrefs(context).edit().remove(alarmKey).commit()
    }

    @JvmStatic
    fun hasAlarmSet(key: String, context: Context): Boolean {
        val alarmKey = String.format(FORMAT_ALARM, key)
        return getPrefs(context).contains(alarmKey)
    }

    @JvmStatic
    fun getSavedFacilities(context: Context): MutableList<FacilityStatus> {
        val savedString = getPrefs(context).getString(SAVED_FACILITIES, "[]")
        return FacilityStatus.fromString(savedString)
    }

    fun getFacilityBookmarked(context: Context, equipmentNumber: Int): Boolean {
        val facilities: List<FacilityStatus> = getSavedFacilities(context)
        for (f in facilities) {
            if (f.equipmentNumber == equipmentNumber) {
                return f.isBookmarked
            }
        }
        return false
    }

    fun setFacilityBookmarked(context: Context, facility: FacilityStatus, isBookmarked: Boolean) {
        val savedFacilities = getSavedFacilities(context)
        for (fs in savedFacilities) {
            if (fs.equipmentNumber == facility.equipmentNumber) {
                //modifiy saved facility
                fs.isBookmarked = isBookmarked
                storeSavedFacilities(context, savedFacilities)
                return
            }
        }

        //add facility
        facility.isBookmarked = isBookmarked
        savedFacilities.add(facility)
        storeSavedFacilities(context, savedFacilities)
    }

    @JvmStatic
    fun storeSavedFacilities(context: Context, facilities: List<FacilityStatus>?) {
        //sort by station name and then by description
        Collections.sort(facilities) { f1, f2 ->
            val res = f1.stationName.compareTo(f2.stationName)
            if (res == 0) {
                f1.description.compareTo(f2.description)
            } else res
        }
        getPrefs(context)
            .edit()
            .putString(SAVED_FACILITIES, FacilityStatus.toString(facilities))
            .commit()
    }

    fun removeSavedFacilityStatus(context: Context, facilityStatus: FacilityStatus) {
        val savedFacilities = getSavedFacilities(context)
        for (fs in savedFacilities) {
            if (fs.equipmentNumber == facilityStatus.equipmentNumber) {
                savedFacilities.remove(fs)
                storeSavedFacilities(context, savedFacilities)
                return
            }
        }
    }


    fun getFacilityPushEnabled(context: Context, facility: FacilityStatus): Boolean {

        val savedFacilitie =
            getSavedFacilities(context).firstOrNull { it.equipmentNumber == facility.equipmentNumber }

        if(savedFacilitie!=null)  {
            return  savedFacilitie.canReceivePushMessages()
        }
        return false
    }

    fun getFacilityPushEnabled(context: Context, equipmentNumber: Int): Boolean {

        val savedFacilitie =
            getSavedFacilities(context).firstOrNull { it.equipmentNumber == equipmentNumber }

        if(savedFacilitie!=null)  {
            return  savedFacilitie.canReceivePushMessages()
        }
        return false
    }


    fun setFacilityPushEnabled(context: Context,  facility: FacilityStatus, enabled: Boolean) {

        val savedFacilities = getSavedFacilities(context)
        for (fs in savedFacilities) {
            if (fs.equipmentNumber == facility.equipmentNumber) {
                //modifiy saved facility
                fs.enableReceivePushMessages(enabled)
                storeSavedFacilities(context, savedFacilities)
                return
            }
        }

        //add facility
        facility.isBookmarked = enabled
        savedFacilities.add(facility)
        storeSavedFacilities(context, savedFacilities)
    }
}