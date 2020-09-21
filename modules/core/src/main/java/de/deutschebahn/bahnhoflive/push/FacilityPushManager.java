/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.push;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus;
import de.deutschebahn.bahnhoflive.util.PrefUtil;

public class FacilityPushManager {
    private static FacilityPushManager ourInstance = new FacilityPushManager();
    private final String TOPIC_PATH = "/topics/F";

    public static FacilityPushManager getInstance() {
        return ourInstance;
    }

    private FacilityPushManager() {
    }

    public boolean isGlobalPushActive(Context context){
        return PrefUtil.getFacilityPushActivated(context);
    }

    public void setGlobalPushActive(Context context, boolean isChecked){
        boolean changed = isGlobalPushActive(context) != isChecked;
        if(changed){
            PrefUtil.setFacilityGlobalPushActivated(context, isChecked);
            List<FacilityStatus> facilities = PrefUtil.getSavedFacilities(context);
            for(FacilityStatus facility : facilities){
                if(facility.isSubscribed()){
                    if(isChecked) {
                        subscribeFirebase(facility);
                    } else {
                        unsubscribeFirebase(facility);
                    }
                }
            }
        }
    }

    public void removeFavorite(Context context, FacilityStatus facilityStatus){
        PrefUtil.removeSavedFacilityStatus(context, facilityStatus);
        if(isGlobalPushActive(context)){
            unsubscribeFirebase(facilityStatus);
        }
    }

    public void removeAll(Context context){
        PrefUtil.storeSavedFacilities(context, new ArrayList<FacilityStatus>());
    }

    public boolean getPushStatus(Context context, int equipmentNumber){
        return PrefUtil.getFacilitySubscribed(context, equipmentNumber);
    }

    public void setPushStatus(Context context, FacilityStatus facilityStatus, boolean isChecked){
        PrefUtil.setFacilitySubcribed(context, facilityStatus, isChecked);
        if (isChecked) {
            if(isGlobalPushActive(context)){
                //just subscribe this
                subscribeFirebase(facilityStatus);
            } else {
                //activating global push will subscribe all (including our new facility)
                setGlobalPushActive(context, true);
            }
        } else {
            if(isGlobalPushActive(context)){
                unsubscribeFirebase(facilityStatus);
            }

            removeFavorite(context, facilityStatus);
        }
    }


    private void subscribeFirebase(FacilityStatus f) {
        //FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_PATH+f.getEquipmentNumber());
    }

    private void unsubscribeFirebase(FacilityStatus f) {
        //unsubscribeFirebase(f.getEquipmentNumber());
    }

    public void unsubscribeFirebase(int equipmentNumber){
        //FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_PATH+equipmentNumber);
    }

}
