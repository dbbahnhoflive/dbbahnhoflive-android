package de.deutschebahn.bahnhoflive.repository.locker

import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class LockersViewModel {

    val lockerResource =
        LockerResource()

    val categorizedLockersLiveData: LiveData<List<UiLocker>> =

        lockerResource.data.map { itLockerList ->

            var l: MutableList<UiLocker> = mutableListOf<UiLocker>()

            itLockerList?.forEach { itLocker ->

                val uiLocker = UiLocker(itLocker)
                var fieldfound =
                    l.find { (it.lockerType == uiLocker.lockerType) && (it.isShortTimeLocker == uiLocker.isShortTimeLocker) }

                if (fieldfound != null)
                    fieldfound.amount += uiLocker.amount
                else
                    l.add(uiLocker)

            }

            l

        }


}


