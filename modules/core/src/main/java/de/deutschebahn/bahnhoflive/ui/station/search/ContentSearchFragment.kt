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
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentContentSearchBinding
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.hideKeyboard
import de.deutschebahn.bahnhoflive.view.ConfirmationDialog

class ContentSearchFragment : Fragment() {

    val viewModel: StationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        FragmentContentSearchBinding.inflate(inflater, container, false).apply {
            root.setOnClickListener {
                // dummy to prevent clicks being delegated to underlying views
            }

            viewModel.stationResource.data.observe(viewLifecycleOwner) {
                stationTitle.text = it?.title
                stationTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }

            val clearHistoryButton = clearHistory
            clearHistoryButton.setOnClickListener {
                ConfirmationDialog(content, "Suchverlauf löschen?") {
                    viewModel.clearSearchHistory()
                }
            }

            viewModel.resultSetType.observe(viewLifecycleOwner) {
                contentTitle.setText(it.label)
                clearHistoryButton.apply {
                    visibility = if (it.showClearHistory) View.VISIBLE else View.GONE
                }
            }

            inputQuery.let { searchView ->
//                searchView.requestFocus()
                viewModel.contentQuery.observe(viewLifecycleOwner) {
                    searchView.setQuery(it.first, true)
                }

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

            recycler.adapter =
                ContentSearchResultsAdapter(TrackingManager.fromActivity(activity))
                    .also { adapter ->
                        viewModel.contentSearchResults.observe(viewLifecycleOwner) {
                            adapter.list = it
                            recycler.fling(0, -1000)
                        }
                    }

        }.root

    override fun onStop() {
        hideKeyboard()
        super.onStop()
    }
}