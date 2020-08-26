package de.deutschebahn.bahnhoflive.ui.search;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import java.util.List;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.IssueTracker;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.SingleRequestRestListener;
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.StopPlace;
import de.deutschebahn.bahnhoflive.location.BaseLocationListener;
import de.deutschebahn.bahnhoflive.persistence.RecentSearchesStore;
import de.deutschebahn.bahnhoflive.ui.hub.LocationFragment;
import de.deutschebahn.bahnhoflive.util.Cancellable;
import de.deutschebahn.bahnhoflive.view.BaseTextWatcher;
import de.deutschebahn.bahnhoflive.view.ConfirmationDialog;

import static de.deutschebahn.bahnhoflive.util.ImeCloserKt.closeIme;

public class StationSearchFragment extends Fragment {

    public static final int AUTO_SEARCH_DELAY = 750;
    public static final String ORIGIN_SEARCH = "search";
    public static final String TAG = StationSearchFragment.class.getSimpleName();
    private StationSearchAdapter adapter;
    private EditText inputView;
    private Cancellable runningStationLookupRequest;

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

    private Location location;

    private final BaseLocationListener locationListener = new BaseLocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            StationSearchFragment.this.location = location;
        }
    };
    private View clearHistoryView;
    private RecentSearchesStore recentSearchesStore;
    private View coordinatorLayout;

    private final QueryRecorder queryRecorder = new QueryRecorder();
    private ProgressBar progressIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        delayedAutoSearchHandler = new Handler();

        final FragmentManager fragmentManager = getParentFragmentManager();
        locationFragment = LocationFragment.get(fragmentManager);
        locationFragment.addLocationListener(locationListener);

        recentSearchesStore = new RecentSearchesStore(getActivity());
        adapter = new StationSearchAdapter(getActivity(), recentSearchesStore, queryRecorder::clear, this, new TrackingManager());
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

        listHeadlineView = view.findViewById(R.id.list_headline);
        clearHistoryView = view.findViewById(R.id.clear_history);
        clearHistoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearHistory();
            }
        });

        progressIndicator = view.findViewById(R.id.progressIndicator);

        noResultsView = view.findViewById(R.id.no_results);
        noResultsView.setVisibility(View.GONE);

        coordinatorLayout = view.findViewById(R.id.coordinator);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clearSelection();
            }
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
        recyclerView.setAdapter(null);
        recyclerView = null;

        inputView = null;
        listHeadlineView = null;
        noResultsView = null;

        super.onDestroyView();
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
        final String query = text.toString().trim();

        if (runningStationLookupRequest != null) {
            runningStationLookupRequest.cancel();
        }

        if (query.length() > 1) {
            final Location location = this.location;

            hideNoResultsView();
            final ProgressBar progressIndicator = this.progressIndicator;
            if (progressIndicator != null) {
                progressIndicator.setVisibility(View.VISIBLE);
            }

            final BaseApplication baseApplication = BaseApplication.get();

            runningStationLookupRequest = baseApplication.getRepositories().getStationRepository().queryStations(
                    new SingleRequestRestListener<List<StopPlace>>() {
                        @Override
                        public void onSuccess(@NonNull List<StopPlace> stations) {
                            super.onSuccess(stations);

                            adapter.setDBStations(stations);
                                    if (stations == null /* just to be sure */ || stations.isEmpty()) {
                                        queryRecorder.put(query);
                                    }

                                    showOrHideNoResultsView();
                                }

                                @Override
                                public void onFail(VolleyError reason) {
                                    super.onFail(reason);

                                    adapter.setDBError();

                                    showOrHideNoResultsView();

                                    final IssueTracker issueTracker = getIssueTracker();
                                    issueTracker.log("Failed station query: " + query);
                                    issueTracker.dispatchThrowable(new StationSearchException(reason.getMessage(), reason));
                                }

                                @Override
                                protected void onRequestFinished(Request<List<StopPlace>> request) {
                                    if (request == runningStationLookupRequest) {
                                        runningStationLookupRequest = null;
                                    }

                                    showOrHideNoResultsView();
                                }
                            }, query, null, false, 25, 10000, true, true, false);
        } else {
            listHeadlineView.setText(R.string.search_history);
            clearHistoryView.setVisibility(View.VISIBLE);
            noResultsView.setVisibility(View.GONE);
            adapter.showRecents();
        }
    }

    public void showOrHideNoResultsView() {
        if (adapter.getItemCount() == 0) {
            if (adapter.hasErrors()) {
                listHeadlineView.setText(R.string.error_data_unavailable);
                clearHistoryView.setVisibility(View.INVISIBLE);
                noResultsView.setText(R.string.home_suggestionsMessageError);
            } else {
                listHeadlineView.setText(R.string.home_suggestionsTitleNoResult);
                clearHistoryView.setVisibility(View.INVISIBLE);
                noResultsView.setText(R.string.home_suggestionsMessageNoResult);
            }
            noResultsView.setVisibility(View.VISIBLE);
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
            getIssueTracker().log("Unsuccessful queries: " + queryRecorder.getConcatenatedQueries());
            issueTracker.dispatchThrowable(new StationSearchException(
                    "User left search after unsuccessful queries",
                    null
            ));
            queryRecorder.clear();
        }
    }

    protected IssueTracker getIssueTracker() {
        return BaseApplication.get().getIssueTracker();
    }


}
