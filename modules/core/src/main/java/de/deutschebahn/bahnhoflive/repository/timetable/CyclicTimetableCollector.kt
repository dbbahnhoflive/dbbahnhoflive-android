package de.deutschebahn.bahnhoflive.repository.timetable

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasTimetable
import de.deutschebahn.bahnhoflive.util.GeneralPurposeMillisecondsTimer

interface OnStartStopCyclicLoadingOfTimetableListener {
    fun onStartStopCyclicLoading(
        timetableCollector: TimetableCollector?,
        selectedHafasTimetable: HafasTimetable?,
        selection: Int
    )
}

class CyclicTimetableCollector(val owner: LifecycleOwner) {

    // only 1 of them can be not null
    private var selectedDbTimetableController: TimetableCollector? = null
    private var selectedHafasTimetable: HafasTimetable? = null

    private val timerCounter: GeneralPurposeMillisecondsTimer = GeneralPurposeMillisecondsTimer()

    inner class TheObserver(private val timerCounter: GeneralPurposeMillisecondsTimer) :
        DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) {
            timerCounter.restartTimer()
            super.onResume(owner)
        }

        override fun onPause(owner: LifecycleOwner) {
            timerCounter.cancelTimer()
            super.onPause(owner)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            timerCounter.cancelTimer()
            super.onDestroy(owner)
        }
    }

    init {
        owner.lifecycle.addObserver(TheObserver(timerCounter))

        timerCounter.startTimer(
            mainThreadAction = null,
//            {
//                selectedHafasTimetable?.refreshTimetable() // Hafas-Abfragen kosten Geld !!!
//            },
            intervalMilliSeconds = Constants.TIMETABLE_REFRESH_INTERVAL_MILLISECONDS,
            startDelayMilliSeconds = 200L,
            backgroundThreadAction = null,
//            {
//                selectedDbTimetableController?.refresh(false) // todo: with ticket !
//            }
        )
    }

    fun changeTimetableSource(
        timetableController: TimetableCollector?,
        hafasTimetable: HafasTimetable?,
        adapter: RecyclerView.Adapter<*>,
        selected: Int
    ) {
        selectedDbTimetableController = timetableController
        selectedHafasTimetable = hafasTimetable

        selectedDbTimetableController?.let {
            timerCounter.restartTimer()
            // new DB-timetable ?
            it.refresh(false) // once at start
            it.timetableUpdateAsLiveData.observe(owner) { itTimetable ->
                if(selected>=0)
                    adapter.notifyItemChanged(selected, itTimetable)
                else {
                     try {
                         adapter.notifyDataSetChanged()
                     }
                     catch (e : Exception ) {

                    }
                }
            }
        } ?: // new Hafas-timetable ?
        selectedHafasTimetable?.let {
            it.refreshTimetable() // once at start
            timerCounter.restartTimer()
            // LiveData wird beim Laden bedient und in ReducedHafasDeparturesViewHolder verarbeitet
        } ?: timerCounter.cancelTimer()

    }

}