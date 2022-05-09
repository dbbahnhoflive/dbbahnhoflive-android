/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search;

import static de.deutschebahn.bahnhoflive.util.ImeCloserKt.closeIme;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.IssueTracker;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.location.BaseLocationListener;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.repository.LoadingStatus;
import de.deutschebahn.bahnhoflive.ui.hub.LocationFragment;
import de.deutschebahn.bahnhoflive.view.BaseTextWatcher;
import de.deutschebahn.bahnhoflive.view.ConfirmationDialog;

public class StationSearchFragment extends Fragment {

    public static final int AUTO_SEARCH_DELAY = 750;
    public static final String TAG = StationSearchFragment.class.getSimpleName();
    public static final String ISSUE_CONTEXT_STATION_SEARCH = "station search";
    private StationSearchAdapter adapter;
    private EditText inputView;

    private Handler delayedAutoSearchHandler;
    private Runnable autoSearchRunnable = new Runnable() {
        @Override
        public void run() {
            performSearch();
        }
    };
    private TextView listHeadlineView;
    private RecyclerView recyclerView;
    private TextView noResultsView;
    private LocationFragment locationFragment;

    private final BaseLocationListener locationListener = new BaseLocationListener() {

        @Override
        public void onLocationChanged(Location location) {
        }
    };
    private View clearHistoryView;
    private final RecentSearchesStore recentSearchesStore = BaseApplication.get().getApplicationServices().getRecentSearchesStore();
    private View coordinatorLayout;

    private final QueryRecorder queryRecorder = new QueryRecorder();
    private ViewFlipper viewFlipper;
    private StationSearchViewModel stationSearchViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationSearchViewModel = new ViewModelProvider(this).get(StationSearchViewModel.class);

        delayedAutoSearchHandler = new Handler();

        final FragmentManager fragmentManager = getParentFragmentManager();
        locationFragment = LocationFragment.get(fragmentManager);
        locationFragment.addLocationListener(locationListener);

        adapter = new StationSearchAdapter(getActivity(), recentSearchesStore, queryRecorder::clear, this, new TrackingManager(), BaseApplication.get().getApplicationServices().getEvaIdsProvider());
    }

    @Override
    public void onStart() {
        super.onStart();

        locationFragment.acquireLocation(false);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_station_search, container, false);

        final TextView appTitleView = view.findViewById(R.id.app_title);
        appTitleView.setText(getText(R.string.rich_app_title));

        final TextView errorMessageView = view.findViewById(R.id.errorMessageDetail);
        errorMessageView.setText(Html.fromHtml(getString(R.string.error_detail_message_station_search)));

        view.findViewById(R.id.buttonRetry).setOnClickListener(v -> {
            performSearch();
        });

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(inflater.getContext(), DividerItemDecoration.VERTICAL));

        inputView = view.findViewById(R.id.input);

        inputView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                    case EditorInfo.IME_ACTION_NONE:
                        performSearch();
                        return true;
                }

                return false;
            }

        });

        inputView.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                primeAutoSearch();
            }
        });
        inputView.requestFocus();

        listHeadlineView = view.findViewById(R.id.list_headline);
        clearHistoryView = view.findViewById(R.id.clear_history);
        clearHistoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearHistory();
            }
        });

        final View progressIndicator = view.findViewById(R.id.progressIndicator);

        noResultsView = view.findViewById(R.id.no_results);
        noResultsView.setVisibility(View.GONE);

        coordinatorLayout = view.findViewById(R.id.coordinator);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clearSelection();
            }
        });

        viewFlipper = view.findViewById(R.id.viewFlipper);

        final SearchResultResource searchResource = stationSearchViewModel.getSearchResource();
        searchResource.getLoadingStatus().observe(getViewLifecycleOwner(), loadingStatus -> {
            progressIndicator.setVisibility(loadingStatus == LoadingStatus.BUSY ? View.VISIBLE : View.GONE);
        });

        searchResource.getError().observe(getViewLifecycleOwner(), reason -> {
            if (reason != null) {

                adapter.setDBError();

                showOrHideNoResultsView();

                final String query = searchResource.getQuery();

                final IssueTracker issueTracker = getIssueTracker();
                issueTracker.dispatchThrowable(new StationSearchException(reason.getMessage(), reason), "Failed station query: " + query);
            }
        });

        searchResource.getData().observe(getViewLifecycleOwner(), stations -> {
            adapter.setDBStations(stations);
            if (stations == null /* just to be sure */ || stations.isEmpty()) {
                //TODO: maybe transport query string through result
                final String query = searchResource.getQuery();
                if (query != null) {
                    queryRecorder.put(query);
                }
            }

            showOrHideNoResultsView();

        });

        return view;
    }

    private void onClearHistory() {
        if (adapter.getItemCount() > 0) {
            closeKeyboard();
            new ConfirmationDialog(coordinatorLayout, "Suchverlauf löschen?", v -> {
                recentSearchesStore.clear();
                adapter.showRecents();
            });
        }
    }

    private void closeKeyboard() {
        closeIme(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        recyclerView.setAdapter(null);
        recyclerView = null;

        inputView = null;
        listHeadlineView = null;
        noResultsView = null;
        viewFlipper = null;
    }

    private boolean primeAutoSearch() {
        cancelAutoSearch();
        return delayedAutoSearchHandler.postDelayed(autoSearchRunnable, AUTO_SEARCH_DELAY);
    }

    private void cancelAutoSearch() {
        delayedAutoSearchHandler.removeCallbacks(autoSearchRunnable);
    }

    @Override
    public void onStop() {
        cancelAutoSearch();
        locationFragment.removeLocationListener(locationListener);

        super.onStop();
    }

    private void performSearch() {
        cancelAutoSearch();

        final Editable text = inputView.getText();
        final SearchResultResource searchResource = stationSearchViewModel.getSearchResource();
        searchResource.setQuery(text.toString());
        final String query = searchResource.getQuery();

        viewFlipper.setDisplayedChild(0);

        if (query != null && query.length() > 1) {
            hideNoResultsView();
        } else {
            listHeadlineView.setText(R.string.search_history);
            clearHistoryView.setVisibility(View.VISIBLE);
            noResultsView.setVisibility(View.GONE);
            adapter.showRecents();
        }
    }

    public void showOrHideNoResultsView() {
        final ViewFlipper viewFlipper = this.viewFlipper;
        if (viewFlipper != null) {
            if (adapter.hasErrors()) {
                viewFlipper.setDisplayedChild(1);
            } else {
                viewFlipper.setDisplayedChild(0);
            }
        }

        if (adapter.getItemCount() == 0) {
            if (adapter.hasErrors()) {
                listHeadlineView.setText(R.string.error_data_unavailable);
                clearHistoryView.setVisibility(View.INVISIBLE);
                noResultsView.setVisibility(View.GONE);
            } else {
                listHeadlineView.setText(R.string.home_suggestionsTitleNoResult);
                clearHistoryView.setVisibility(View.INVISIBLE);
                noResultsView.setVisibility(View.VISIBLE);
            }
        } else {
            hideNoResultsView();
        }
    }

    public void hideNoResultsView() {
        if (getView() != null) {
            listHeadlineView.setText(R.string.search_results);
            clearHistoryView.setVisibility(View.INVISIBLE);
            noResultsView.setVisibility(View.GONE);
        }
    }

//    private void setDBStations(List<FavendoStation> searchResult) {
//        if (searchResult == null) {
//            listHeadlineView.setText(R.string.search_history);
//            noResultsView.setVisibility(View.GONE);
//        } else if (searchResult.isEmpty()) {
//            listHeadlineView.setText(R.string.home_suggestionsTitleNoResult);
//            noResultsView.setVisibility(View.VISIBLE);
//        } else {
//            hideNoResultsView();
//        }
//
//        newsAdapter.setDBStations(searchResult);
//    }

    @Override
    public void onDestroy() {
        locationFragment.removeLocationListener(locationListener);

        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        final String concatenatedQueries = queryRecorder.getConcatenatedQueries();
        if (!concatenatedQueries.isEmpty()) {
            final IssueTracker issueTracker = getIssueTracker();

            final HashMap<String, String> values = new HashMap<>();
            values.put("queries", concatenatedQueries);
            issueTracker.setContext(ISSUE_CONTEXT_STATION_SEARCH, values);

            issueTracker.dispatchThrowable(new StationSearchException(
                    "User left search after unsuccessful queries",
                    null
            ), "Unsuccessful queries: " + concatenatedQueries);

            issueTracker.setContext(ISSUE_CONTEXT_STATION_SEARCH, null);

            queryRecorder.clear();
        }
    }

    protected IssueTracker getIssueTracker() {
        return BaseApplication.get().getIssueTracker();
    }


}
