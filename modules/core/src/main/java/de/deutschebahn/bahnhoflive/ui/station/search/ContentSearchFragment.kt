/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.closeIme
import de.deutschebahn.bahnhoflive.view.ConfirmationDialog
import kotlinx.android.synthetic.main.fragment_content_search.view.*

class ContentSearchFragment : Fragment() {

    val viewModel: StationViewModel by activityViewModels<StationViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_content_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setOnClickListener {
            // dummy to prevent clicks being delegated to underlying views
        }

        viewModel.stationResource.data.observe(viewLifecycleOwner, Observer {
            view.stationTitle?.text = it?.title
        })

        val clearHistoryButton = view.clear_history
        clearHistoryButton?.setOnClickListener {
            view.content?.let { contentView ->
                ConfirmationDialog(contentView, "Suchverlauf löschen?", View.OnClickListener {
                    viewModel.clearSearchHistory()
                })
            }
        }

        viewModel.resultSetType.observe(viewLifecycleOwner, Observer {
            view.contentTitle?.setText(it.label)
            clearHistoryButton?.apply {
                visibility = if (it.showClearHistory) View.VISIBLE else View.GONE
            }
        })

        view.inputQuery?.let { searchView ->
            viewModel.contentQuery.observe(viewLifecycleOwner, Observer {
                searchView.setQuery(it.first, true)
            })

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = updateQuery(query)

                override fun onQueryTextChange(query: String?) = updateQuery(query)

                private fun updateQuery(query: String?): Boolean {
                    viewModel.contentQuery.value = query?.trim() to false
                    return true
                }
            })

            searchView.setOnSearchClickListener {
                viewModel.contentQuery.value = searchView.query?.toString()?.trim() to true
            }
        }

        view.recycler?.adapter = ContentSearchResultsAdapter(TrackingManager.fromActivity(activity))
            .also { adapter ->
                viewModel.contentSearchResults.observe(viewLifecycleOwner, Observer {
                    adapter.list = it
                    view.recycler?.fling(0, -1000)
                })
            }

    }

    override fun onStop() {
        context.closeIme()

        super.onStop()
    }
}