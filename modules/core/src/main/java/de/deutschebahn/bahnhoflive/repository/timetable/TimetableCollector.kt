/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.timetable

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class TimetableCollector {
    open val mergedTrainInfosObservable: Observable<Timetable> =
        Observable.error(Exception("Missing implementation"))
    val nextHourTrigger = PublishSubject.create<Boolean>()
    val refreshTrigger = BehaviorSubject.create<Boolean>()
    protected open val currentHourInput: Observable<Long> =
        Observable.just(System.currentTimeMillis())
    val evaIdsInput = BehaviorSubject.create<List<String>>()
    protected val refreshObservable = Observable.merge(
        refreshTrigger.filter { it },
        refreshTrigger.filter { !it }.take(1)
    )

}