/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform;
import de.deutschebahn.bahnhoflive.backend.ris.model.RISTimetable;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainMovementInfo;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.accessibility.AccessibilityFeaturesResource;
import de.deutschebahn.bahnhoflive.repository.timetable.Constants;
import de.deutschebahn.bahnhoflive.repository.timetable.Timetable;
import de.deutschebahn.bahnhoflive.ui.ViewHolder;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.util.ListHelper;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;


class DbTimetableAdapter extends RecyclerView.Adapter<ViewHolder<?>> implements TrainEvent.Provider {

    private final static int ITEM_TYPE_HEADER = 0;
    private final static int ITEM_TYPE_CONTENT = 1;
    private static final int ITEM_TYPE_ERROR = 3;
    private static final int ITEM_TYPE_EMPTY = 4;
    private final SingleSelectionManager selectionManager;

    private List<TrainInfo> filteredTrainInfos;

    @NonNull
    private TrainEvent trainEvent = TrainEvent.DEPARTURE;

    @Nullable
    private Station station;
    private final FilterUI filterUI;
    private String trainCategory;
    @Nullable
    private String track;

    private Boolean hasMoreThan1Track=true;
    private Boolean hasMoreThan1TrainType=true;

    private final TrackingManager trackingManager;
    private final View.OnClickListener loadMoreListener;

    @NonNull
    private final List<String> tracks = new LinkedList<>();
    @NonNull
    private final List<String> trainCategories = new LinkedList<>();

    private Timetable timetable;

    private List<Platform> platforms = null;

    private final Function3<? super TrainInfo, ? super TrainEvent, ? super Integer, Unit> itemClickListener;

    DbTimetableAdapter(@Nullable Station station,
                       FilterUI filterUI,
                       @NonNull final TrackingManager trackingManager,
                       View.OnClickListener loadMoreListener, Function3<? super TrainInfo, ? super TrainEvent, ? super Integer, Unit> itemClickListener) {
        this.station = station;
        this.filterUI = filterUI;
        this.trackingManager = trackingManager;
        this.loadMoreListener = loadMoreListener;
        this.itemClickListener = itemClickListener;

        selectionManager = new SingleSelectionManager(this);
        SingleSelectionManager.type = "h2_departure";
        selectionManager.addListener(new TrackingSelectionListener(trackingManager));
    }

    @Override
    public ViewHolder<?> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_HEADER:
                return createHeaderViewHolder(parent);
            case ITEM_TYPE_EMPTY:
                return createEmptyMessageViewHolder(parent);
            default:
                return new TrainInfoViewHolder(parent,
                        this,
                        station,
                        selectionManager,
                        (trainInfo, integer) ->
                        itemClickListener.invoke(trainInfo, trainEvent, integer)
                );
        }

    }

    private TimetableTrailingItemViewHolder createEmptyMessageViewHolder(ViewGroup parent) {
        return new TimetableTrailingItemViewHolder(parent, loadMoreListener);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder<?> holder) {
        if (holder instanceof TrainInfoViewHolder) {
            ((TrainInfoViewHolder) holder).stopObservingItem();
        }
    }

    private ViewHolder<TrainInfo> createHeaderViewHolder(ViewGroup parent) {
        return new TimetableHeaderViewHolder(parent, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterOptions();
            }
        });
    }

    private void showFilterOptions() {
        filterUI.onShowFilter(trainCategories.toArray(new String[0]), trainCategory, tracks.toArray(new String[0]), track);
    }

    public void setTrainCategoryFilter(String trainCategory) {
        this.trainCategory = trainCategory;

        notifyItemChanged(0);

        applyFilters();
    }

    public void setFilter(@Nullable String track) {
        this.track = track;

        notifyItemChanged(0);

        applyFilters();
    }

    public void setTimetable(@NonNull Timetable timetable) {
        this.timetable = timetable;

        if (!applyFilters()) {
            // fallback old solution (todo: check if necessaray)
        trainCategories.clear();
        trainCategories.addAll(RISTimetable.getTrainCategories(
                timetable.getTrainInfos()));

        tracks.clear();
        tracks.addAll(RISTimetable.getTracksForFilter(getSelectedTrainInfos()));
        }

        hasMoreThan1Track = RISTimetable.hasMoreThan1Platform(getSelectedTrainInfos());
        hasMoreThan1TrainType = RISTimetable.hasMoreThan1TrainCategory( timetable.getTrainInfos());

    }

    public void setPlatforms(@NonNull List<Platform> platforms) {
        this.platforms = platforms;
        notifyDataSetChanged();
    }

    @Nullable
    public Track getCurrentTrack() {
        final TrainMovementInfo selectedItem = getSelectedItem();
        if (selectedItem != null) {
            final String strippedActualPlatform = selectedItem.getPurePlatform();
            if (strippedActualPlatform != null) {
                return new Track(strippedActualPlatform);
            }
        }

        if (track != null) {
            return new Track(track);
        }

        final Timetable timetable = this.timetable;
        if (timetable == null) {
            return null;
        }

        final LinkedList<String> tracks = RISTimetable.getTracks(timetable.getTrainInfos());
        return tracks.size() > 0 ? new Track(tracks.get(0)) : null;
    }

    public int setSelectedItem(TrainInfo trainInfo) {
        setTrainCategoryFilter(null);

        final int targetIndex = filteredTrainInfos.indexOf(trainInfo) + 1;

        if (targetIndex >= 0) {
            selectionManager.setSelection(targetIndex);
        }
        return targetIndex;
    }

    public void setStation(Station station) {
        if (this.station == null && station != null) {
            this.station = station;
            notifyDataSetChanged();
        }
    }

    public interface FilterUI {
        void onShowFilter(
                String[] trainCategories, String trainCategory,
                String[] tracks, String track);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder<?> holder, int position) {
        if (position == 0) {
            holder.bind(null);
        } else if (filteredTrainInfos != null && position <= filteredTrainInfos.size() && holder instanceof TrainInfoViewHolder) {
            final TrainInfoViewHolder trainInfoViewHolder = (TrainInfoViewHolder) holder;
            trainInfoViewHolder.setStation(station);
            if(platforms!=null)
              trainInfoViewHolder.setPlatforms(platforms);
            trainInfoViewHolder.bind(filteredTrainInfos.get(position - 1));
        } else if (holder instanceof TimetableTrailingItemViewHolder) {
            if (timetable == null) {
                return;
            }
            final long endTime = timetable.getEndTime();
            final boolean isMayLoadMore = timetable.getDuration() <= Constants.HOUR_LIMIT;
            ((TimetableTrailingItemViewHolder) holder)
                    .bind(new FilterSummary(track, trainCategory, trainEvent, filteredTrainInfos == null ? 0 : filteredTrainInfos.size(), endTime, isMayLoadMore));
        }
    }

    @Override
    public int getItemCount() {
        return filteredTrainInfos == null ? 0 : filteredTrainInfos.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ITEM_TYPE_HEADER :
                filteredTrainInfos == null ? ITEM_TYPE_ERROR :
                        position > filteredTrainInfos.size() ? ITEM_TYPE_EMPTY :
                                ITEM_TYPE_CONTENT;
    }

    private void addPlatformAndTrainCategoryToUiFilter(@NonNull TrainInfo trainInfo,
                                                       @Nullable TrainMovementInfo trainMovementInfo)
    {
     if(trainMovementInfo!=null) {
         final String platformRaw = trainMovementInfo.getPlatform();
         String platform = ""; //""k.A.";

         try {
           int value = Integer.parseInt(platformRaw.replaceAll("[^0-9]", "").trim());
           platform = Integer.toString(value);
         }
         catch(Exception e) {
             platform = platformRaw;
         }

         ListHelper.addToStringList(tracks, platform, false, true );

         final String category = trainInfo.getTrainCategory();
         ListHelper.addToStringList(trainCategories,category, false, true );
     }
    }

    public boolean applyFilters() { // nach Gleis (track,platform) und/oder Zugtyp(category) filtern
        selectionManager.clearSelection();

        final List<TrainInfo> selectedTrainInfos = getSelectedTrainInfos(); // alle im Zeitbereich (akt. zeit bis (+ n Stunden))

        // fuer ui Filter bzw. FilterDialogFragment
        trainCategories.clear();
        tracks.clear();
        for (TrainInfo trainInfo : selectedTrainInfos) {
            if (this.trainEvent == TrainEvent.DEPARTURE)
                addPlatformAndTrainCategoryToUiFilter(trainInfo, trainInfo.getDeparture());
            else
                addPlatformAndTrainCategoryToUiFilter(trainInfo, trainInfo.getArrival());
        }

        if (selectedTrainInfos != null) {

            final ArrayList<TrainInfo> filteredTrainInfos = new ArrayList<>();
            for (TrainInfo selectedTrainInfo : selectedTrainInfos) {
                if (trainCategory != null && !trainCategory.equals(selectedTrainInfo.getTrainCategory())) {
                    continue;
                }

                if (track != null && !track.equals(trainEvent.movementRetriever.getTrainMovementInfo(selectedTrainInfo).getPurePlatform())) {
                    continue;
                }

                filteredTrainInfos.add(selectedTrainInfo);

            }

            this.filteredTrainInfos = filteredTrainInfos;


        }

        trainCategories.add(0, "Alle");
        tracks.add(0, "Alle");

        notifyDataSetChanged();

        return selectedTrainInfos!=null;
    }

    private List<TrainInfo> getSelectedTrainInfos() {
        return timetable == null ? Collections.emptyList() : trainEvent.isDeparture ? timetable.getDepartures() : timetable.getArrivals();
    }

    private class TimetableHeaderViewHolder extends ViewHolder<TrainInfo> implements View.OnClickListener {

        private final View filterButton;

        private final View.OnClickListener onFilterClickListener;
        private final TwoAlternateButtonsViewHolder twoAlternateButtonsViewHolder;

        public TimetableHeaderViewHolder(ViewGroup parent, View.OnClickListener onFilterClickListener) {
            super(parent, R.layout.header_timetable_db);
            this.onFilterClickListener = onFilterClickListener;

            twoAlternateButtonsViewHolder = new TwoAlternateButtonsViewHolder(itemView, R.id.departure, R.id.arrival, this);

            filterButton = itemView.findViewById(R.id.filter);
            filterButton.setOnClickListener(this);
        }

        @Override
        protected void onBind(@Nullable TrainInfo item) {
            switch (trainEvent) {
                case DEPARTURE:
                    twoAlternateButtonsViewHolder.checkLeftButton();
                    break;
                case ARRIVAL:
                    twoAlternateButtonsViewHolder.checkRightButton();
                    break;
            }
            filterButton.setVisibility(hasMoreThan1Track || hasMoreThan1TrainType
                    ? View.VISIBLE : View.INVISIBLE);
            filterButton.setSelected(trainCategory != null || track != null);
        }

        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == R.id.departure) {
                setTrainEvent(TrainEvent.DEPARTURE);
                if (trackingManager != null) {
                    trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.Action.TAP, TrackingManager.UiElement.TOGGLE_ABFAHRT);
                }
            } else if (id == R.id.arrival) {
                setTrainEvent(TrainEvent.ARRIVAL);
                if (trackingManager != null) {
                    trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.Action.TAP, TrackingManager.UiElement.TOGGLE_ANKUNFT);
                }
            } else if (id == R.id.filter) {
                onFilterClickListener.onClick(v);
                if (trackingManager != null) {
                    trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Screen.H2, TrackingManager.Action.TAP, TrackingManager.UiElement.FILTER_BUTTON);
                }
            }
        }

    }

    private void setTrainEvent(@NonNull TrainEvent trainEvent) {
        this.trainEvent = trainEvent;
        applyFilters();
    }

    public void setArrivals(boolean arrivals) {
        if (trainEvent.isDeparture == arrivals) {
            setTrainEvent(arrivals ? TrainEvent.ARRIVAL : TrainEvent.DEPARTURE);
        }
    }

    public TrainMovementInfo getSelectedItem() {
        final TrainInfo selectedItem = selectionManager.getSelectedItem(filteredTrainInfos, 1);

        return selectedItem == null ? null : trainEvent.movementRetriever.getTrainMovementInfo(selectedItem);
    }

    @NonNull
    public TrainEvent getTrainEvent() {
        return trainEvent;
    }

}
