/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.RecyclerView;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager;
import de.deutschebahn.bahnhoflive.tutorial.TutorialPreferenceStore;
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment;
import de.deutschebahn.bahnhoflive.ui.hub.StationImageResolver;
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker;
import de.deutschebahn.bahnhoflive.view.SectionAdapter;
import de.deutschebahn.bahnhoflive.view.SelectableItemViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class SettingsFragment extends RecyclerFragment<SectionAdapter> {

    public SettingsFragment() {
        super(R.layout.fragment_recycler_linear);

        setTitle(R.string.settings);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof StationActivity) {

            final Station station = ((StationActivity) activity).getStation();

            final SingleSelectionManager selectionManager = null; // new SingleSelectionManager(null);

            final FavoritesAdapter favoritesAdapter = new FavoritesAdapter(InternalStation.of(station),
                    BaseApplication.get().getApplicationServices().getFavoriteDbStationStore(), selectionManager, new StationImageResolver(getActivity()), BaseApplication.get().getApplicationServices().getEvaIdsProvider());
            final TutorialAdapter tutorialAdapter = new TutorialAdapter(selectionManager);
            final PushAdapter pushAdapter = new PushAdapter(selectionManager);

            final SectionAdapter adapter = new SectionAdapter(
                    new SectionAdapter.Section(
                            favoritesAdapter, 1, "Favoriten verwalten"),
                    new SectionAdapter.Section(
                            tutorialAdapter, 1, activity.getText(R.string.settings_manage_notifications)
                    ),
                    new SectionAdapter.Section(
                            pushAdapter, 1, "") // no title, so it appears under the last


            );
//            if(selectionManager!=null)
//              selectionManager.setAdapter(adapter);

            setAdapter(adapter);
            TrackingManager.fromActivity(getActivity()).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D2, TrackingManager.Entity.EINSTELLUNGEN);
        }
    }

    private class TutorialSettingItemViewHolder extends SelectableItemViewHolder implements CompoundButton.OnCheckedChangeListener {

        private final CompoundButtonChecker toggleView;

        public TutorialSettingItemViewHolder(ViewGroup parent, SingleSelectionManager selectionManager) {
            super(parent, R.layout.card_expandable_setting_tutorial, selectionManager);

            toggleView = new CompoundButtonChecker(itemView.findViewById(R.id.show_tips_switch), this);
        }

        @Override
        protected void onBind(Object item) {
            super.onBind(item);

            final TutorialManager manager = TutorialManager.getInstance(getActivity());

            toggleView.setChecked(
                    manager.doesUserWantToSeeTutorials());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TutorialPreferenceStore.getInstance(getActivity()).setUserWantsTutorials(isChecked);
        }
    }

    private class TutorialAdapter extends RecyclerView.Adapter<TutorialSettingItemViewHolder> {
        private final SingleSelectionManager selectionManager;

        public TutorialAdapter(SingleSelectionManager selectionManager) {
            this.selectionManager = selectionManager;
        }

        @Override
        public TutorialSettingItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TutorialSettingItemViewHolder(parent, selectionManager);
        }

        @Override
        public void onBindViewHolder(TutorialSettingItemViewHolder holder, int position) {
            holder.bind(null);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }


    private class PushSettingItemViewHolder extends SelectableItemViewHolder implements CompoundButton.OnCheckedChangeListener {

        private final CompoundButtonChecker toggleView;

        public PushSettingItemViewHolder(ViewGroup parent, SingleSelectionManager selectionManager) {
            super(parent, R.layout.card_expandable_setting_push, selectionManager);

            toggleView = new CompoundButtonChecker(itemView.findViewById(R.id.enable_push), this);
        }

        @Override
        protected void onBind(Object item) {
            super.onBind(item);
// todo
//            final TutorialManager manager = TutorialManager.getInstance(getActivity());
//
//            toggleView.setChecked(
//                    manager.doesUserWantToSeeTutorials());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           // todo
        }
    }

    private class PushAdapter extends RecyclerView.Adapter<PushSettingItemViewHolder> {
        private final SingleSelectionManager selectionManager;

        public PushAdapter(SingleSelectionManager selectionManager) {
            this.selectionManager = selectionManager;
        }

        @Override
        public PushSettingItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PushSettingItemViewHolder(parent, selectionManager);
        }

        @Override
        public void onBindViewHolder(PushSettingItemViewHolder holder, int position) {
            holder.bind(null);
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
