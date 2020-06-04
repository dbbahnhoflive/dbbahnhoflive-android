package de.deutschebahn.bahnhoflive.persistence

import android.content.Context
import androidx.lifecycle.MutableLiveData
import java.util.*

class RecentContentQueriesStore(context: Context) {

    companion object {
        const val HISTORY_LIMIT = 10
    }

    private val sharedPreferences = context.getSharedPreferences("recentContentQueries", Context.MODE_PRIVATE)

    private var list = LinkedList(
            sharedPreferences.getString("queryList", null)?.split('\n')?.take(HISTORY_LIMIT)
                    ?: emptyList())

    fun putQuery(query: String) {
        list.remove(query)
        list.add(0, query)
        while (list.size > HISTORY_LIMIT) {
            list.removeLast()
        }

        recentQueries.value = list

        if (list.isEmpty()) {
            sharedPreferences.edit().clear().commit()
        } else {
            sharedPreferences.edit()
                    .putString("queryList", list.joinToString("\n"))
                    .commit()
        }
    }

    fun clear() {
        list.clear()
        recentQueries.value = list
        sharedPreferences.edit().clear().commit()
    }

    val recentQueries = MutableLiveData<List<String>>().apply {
        value = list
    }

}