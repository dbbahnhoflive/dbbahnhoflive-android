/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandAlarm;
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandAlarmManager;
import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandRequestManager;
import de.deutschebahn.bahnhoflive.push.NotificationChannelManager;
import de.deutschebahn.bahnhoflive.repository.MergedStation;
import de.deutschebahn.bahnhoflive.repository.Station;
import de.deutschebahn.bahnhoflive.repository.trainformation.Train;
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation;
import de.deutschebahn.bahnhoflive.repository.trainformation.Waggon;
import de.deutschebahn.bahnhoflive.ui.FragmentArgs;
import de.deutschebahn.bahnhoflive.ui.ToolbarViewHolder;
import de.deutschebahn.bahnhoflive.ui.WagenstandSectionIndicator;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.Track;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.util.AlertX;
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities;

public class WagenstandFragment extends Fragment implements View.OnLayoutChangeListener, WagenstandSectionIndicator.WagenstandSectionIndicatorListener,
        MapPresetProvider, VolleyRestListener<TrainFormation> {

    public static final String ARG_TRAIN_INFO = "trainInfo";
    public static final String ARG_TRAIN_EVENT = "trainEvent";
    private static final String TAG = WagenstandFragment.class.getSimpleName();

    private final static SimpleDateFormat FORMAT_UPDATED_TIME = new SimpleDateFormat("dd.MM.yyyy',' HH:mm", Locale.GERMANY);
    private final static SimpleDateFormat FORMATTERDATE = new SimpleDateFormat("yyyyMMdd", Locale.GERMANY);

    private ListView waggonListview;
    private WagenstandSectionIndicator sectionIndicator;

    @Nullable
    private TrainInfo trainInfo;
    private TrainFormation trainFormation;
    private String title;

    private String selectedWaggon;

    private TextView refreshTimestanp;
    private CompoundButton reminderCheckBox;
    private View refreshButton;
    private SwipeRefreshLayout refreshLayout;

    private String timestamp;
    private View reminderCheckboxContainer;

    private WagenstandAlarmManager wagenstandAlarmManager;

    private ViewGroup headerLayout;
    private LiveData<MergedStation> stationLiveData;
    private StationViewModel stationViewModel;
    private String titleDescription;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        setUIArguments(args);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        wagenstandAlarmManager = new WagenstandAlarmManager(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Bundle args = getArguments();
//
//        if(args!=null) {
//            TrainInfo trainInfo = args.getParcelable(ARG_TRAIN_INFO); // todo take date
//            if(trainInfo!=null && trainInfo.getDeparture()!=null) {
//             String sTime = trainInfo.getDeparture().getFormattedTime();
//             Log.d("cr", sTime);
//            }
//        }


        stationViewModel = new ViewModelProvider(getActivity()).get(StationViewModel.class);
        stationLiveData = stationViewModel.getStationResource().getData();
        stationLiveData.observe(this, new Observer<Station>() {
            @Override
            public void onChanged(@Nullable Station station) {
                reminderCheckBox.setEnabled(station != null);
                if (refreshLayout.isRefreshing()) {
                    doRefresh();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        wagenstandAlarmManager = null;

        super.onDetach();
    }

    public void setUIArguments(Bundle args) {
        this.title = args.getString(FragmentArgs.TITLE);
        this.titleDescription = args.getString(FragmentArgs.TITLE_DESCRIPTION);
        selectedWaggon = args.getString(FragmentArgs.WAGENSTAND_WAGGON);
        timestamp = args.getString(FragmentArgs.WAGENSTAND_TIMESTAMP, "");

        trainInfo = args.getParcelable(ARG_TRAIN_INFO);

        final TrainFormation trainFormation = args.getParcelable(FragmentArgs.TRAIN_FORMATION);
        if (trainFormation != null) {
            setTrainFormation(trainFormation);
            setTitle();
        }

    }

    private void setTrainFormation(TrainFormation trainFormation) {
        this.trainFormation = trainFormation;

        trainFormation.sortBySection();
    }

    public void setTitle() {
        try {
            if (trainInfo != null && trainInfo.getDeparture() != null) {
                this.title = "Wagenreihung " + String.format("%s | Gl. %s", trainInfo.getDeparture().getFormattedTime(), trainFormation.getPlatform());
                this.titleDescription = "Wagenreihung " + String.format("%s | Gleis %s", trainInfo.getDeparture().getFormattedTime(), trainFormation.getPlatform());
            } else {
                this.title = "Wagenreihung " + String.format("Gl. %s", trainFormation.getPlatform());
                this.titleDescription = "Wagenreihung " + String.format("Gleis %s", trainFormation.getPlatform());
            }
        } catch (Exception e) {

        }
    }

    public String getActionBarTitle() {
        return this.title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_wagenstand_detail, container, false);

        if (savedInstanceState != null) {
            setUIArguments(savedInstanceState);
        }

        new ToolbarViewHolder(v, getActionBarTitle()).setTitleContentDescription(titleDescription);

        waggonListview = v.findViewById(R.id.waggon_list);

        if (getResources().getBoolean(R.bool.isTablet)) {
            waggonListview.setDivider(null);
        } else {
            waggonListview.setDividerHeight(1);
        }

        waggonListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Object firstVisibleWaggonItem = waggonListview.getItemAtPosition(firstVisibleItem);
                if (firstVisibleWaggonItem instanceof Waggon) {
                    final List<String> sections = ((Waggon) firstVisibleWaggonItem).getSections();
                    String section = sections.get(sections.size() - 1);

                    sectionIndicator.setActiveWaggon(firstVisibleItem, section);
                }
            }
        });

        waggonListview.addOnLayoutChangeListener(this);

        // removed: "Daten laut Aushangplan". maybe check if never shown or only if data is from "soll" API

        headerLayout = v.findViewById(R.id.wagenstand_header_container);

        setWagenstandInformation();

        sectionIndicator = v.findViewById(R.id.wagenstand_section_indicator);
        sectionIndicator.setDelegate(this);

        // refresh
        refreshLayout = v.findViewById(R.id.wagenstand_swiperefresh);
        refreshButton = v.findViewById(R.id.wagenstand_refresh);
        refreshTimestanp = v.findViewById(R.id.wagenstand_refreshText);

        // reminder
        reminderCheckboxContainer = v.findViewById(R.id.reminder_checkbox_container);
        reminderCheckBox = v.findViewById(R.id.wagenstand_pushActivatedCheckBox);

        reminderCheckBox.setEnabled(true);

        // refresh on click
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doRefresh();
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });


        updateRefreshTimestamp(timestamp);
        updateReminderCheckBoxState(
                wagenstandAlarmManager
                        .isWagenstandAlarm(trainFormation.getTrainNumber(),
                                trainFormation.getTime())
        );

        reminderCheckboxContainer.setVisibility(isLiveWagenstand() ? View.VISIBLE : View.GONE);

        updateGui();

        return v;
    }

    private boolean isLiveWagenstand() {
        return trainFormation != null && trainFormation.isLive();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.selectedWaggon != null) {
            scrollToWaggon(this.selectedWaggon);
        }
    }

    public void didSelectSection(String section) {
        for (Waggon waggon : trainFormation.getWaggons()) {
            String lastSectionOfWaggon = waggon.getSections().get(waggon.getSections().size() - 1);
            if (lastSectionOfWaggon.equals(section)) {
                waggonListview.setSelection(trainFormation.getWaggons().indexOf(waggon) + 1);
                break;
            }
        }
    }

    public void scrollToWaggon(String waggonNumber) {
        if (trainFormation != null) {
            List<Waggon> waggons = trainFormation.getWaggons();
            for (int i = 0; i < waggons.size(); i++) {
                final Waggon waggon = waggons.get(i);
                if (waggonNumber.equals(waggon.getDisplayNumber())) {
                    waggonListview.setSelection(Math.min(i + 1, waggonListview.getCount() - 1));
                    return;
                }
            }
        }
    }

    @Override
    public void didSelectTypeOfWagon(String section) {
        //
    }

    @Override
    public void onLayoutChange(View v, int left, int top,
                               int right, int bottom,
                               int oldLeft, int oldTop,
                               int oldRight, int oldBottom) {

        waggonListview.post(new Runnable() {
            @Override
            public void run() {

                try {
                    DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
                    float dpHeight = displayMetrics.heightPixels;

                    // add extra space to make scrolling to the last section possible
                    ViewGroup.LayoutParams params = waggonListview.getLayoutParams();
                    params.height = params.height + (int) (dpHeight / 2);


                    waggonListview.setLayoutParams(params);
                    waggonListview.requestLayout();

                    if (getResources().getBoolean(R.bool.isTablet)) {
                        sectionIndicator.centerContent();
                    }
                }
                catch(Exception e) {
                    Log.e("WagenstandFragment", "Exception " + e.getMessage());
                }
            }
        });
        waggonListview.removeOnLayoutChangeListener(this);
    }

    public static WagenstandFragment create(String actionBarTitle, TrainFormation trainFormation,
                                            String waggon, String timestamp, TrainInfo trainInfo, TrainEvent trainEvent) {
        final Bundle args = new Bundle();

        args.putString(FragmentArgs.TITLE, actionBarTitle);
        // convert an array of Wagenstand objects back to JSON
        args.putParcelable(FragmentArgs.TRAIN_FORMATION, trainFormation);
        if (waggon != null) {
            args.putString(FragmentArgs.WAGENSTAND_WAGGON, waggon);
        }
        args.putString(FragmentArgs.WAGENSTAND_TIMESTAMP, timestamp);
        args.putString(FragmentArgs.TITLE, actionBarTitle);

        if (trainInfo != null && trainEvent != null) {
            args.putParcelable(ARG_TRAIN_INFO, trainInfo);
            args.putSerializable(ARG_TRAIN_EVENT, trainEvent);
        }

        final WagenstandFragment wagenstandFragment = new WagenstandFragment();

        wagenstandFragment.setArguments(args);

        return wagenstandFragment;
    }

    @Override
    public boolean prepareMapIntent(@NonNull Intent intent) {
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, new Track(trainFormation.getPlatform()));
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        final TrackingManager trackingManager = TrackingManager.fromActivity(getActivity());
        trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D3, TrackingManager.Entity.WAGENREIHUNG);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (trainFormation != null) {

            outState.putParcelable(FragmentArgs.TRAIN_FORMATION, trainFormation);
            outState.putString(FragmentArgs.TITLE, title);
            outState.putString(FragmentArgs.TITLE_DESCRIPTION, titleDescription);

            if (selectedWaggon != null) {
                outState.putString(FragmentArgs.WAGENSTAND_WAGGON, selectedWaggon);
            }
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Update the timestamp in the List-Header
     *
     * @param timestamp
     */
    private void updateRefreshTimestamp(String timestamp) {
        if (isLiveWagenstand()) {
            //2017-01-13T12:52:41.874
            Date d = new Date();
            // Set the timeStamp to use it for the Reminder Alarm
            timestamp = FORMAT_UPDATED_TIME.format(d);

            // Get the string combined with the timestamp
            String timestampString = getActivity()
                    .getResources()
                    .getString(R.string.wagenstand_last_updated, timestamp);

            refreshTimestanp.setText(Html.fromHtml(timestampString));
        } else {
            refreshTimestanp.setText(Html.fromHtml(getString(R.string.wagenstand_possibly_outdated)));
            refreshButton.setVisibility(View.GONE);
        }
    }

    private void updateReminderCheckBoxState(boolean isChecked) {

        Context ctx = this.getContext();

        reminderCheckBox.setOnCheckedChangeListener(null);
        reminderCheckBox.setChecked(isChecked);
        reminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                WagenstandAlarmManager.ValidationState validationState = wagenstandAlarmManager.getAlarmPermissionState();

                if (isChecked && validationState!= WagenstandAlarmManager.ValidationState.OK) {

                    if(validationState==WagenstandAlarmManager.ValidationState.NO_EXACT_ALARM_ALLOWED) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                            AlertX.Companion.execAlert(requireContext(), getString(R.string.wagenstand_no_result_headline),
                                    getString(R.string.notification_advice_systemsettings_reminders),
                                    AlertX.Companion.buttonNegative(),
                                    getString(R.string.settings), () -> {
                                        startActivity(new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + requireActivity().getPackageName())));
                                        return null;
                                    },
                                    getString(R.string.dlg_cancel), () -> {
                                        reminderCheckBox.setChecked(false);
                                        return null;
                                    },
                                    "", () -> null,
                                    "", (x) -> null,
                                    "",
                                    ""
                            );

                        } else {

                            AlertX.Companion.execAlert(requireContext(), getString(R.string.wagenstand_no_result_headline),
                                    getString(R.string.notification_advice_systemsettings_reminders),
                                    AlertX.Companion.buttonNegative(),
                                    getString(R.string.dlg_ok), () -> {
                                        reminderCheckBox.setChecked(false);
                                        return null;
                                    },
                                    "", () -> null,
                                    "", () -> null,
                                    "", (x) -> null,
                                    "",
                                    ""
                            );
                        }
                    } else if (validationState == WagenstandAlarmManager.ValidationState.NO_PERMISSION) {

                        AlertX.Companion.execAlert(ctx, getString(R.string.wagenstand_no_result_headline),
                                getString(R.string.notification_advice_systemsettings),
                                AlertX.Companion.buttonNegative(),
                                getString(R.string.settings), () -> {
                                    NotificationChannelManager.Companion.showNotificationSettingsDialog(ctx, null);
                                    return null;
                                },
                                getString(R.string.dlg_cancel), () -> {
                                    reminderCheckBox.setChecked(false);
                                    return null;
                                },
                                "", null,
                                "", null,
                                "",
                                ""
                        );

                    }

                } else {

                    final @NonNull String trainNumber = trainFormation.getTrainNumber();
//                    String time = trainFormation.getTime(); // fmt: hh:mm
                    if (trainInfo == null || trainInfo.getDeparture() == null)
                        return;

                    String time = trainInfo.getDeparture().getFormattedCorrectedTime();

                    StringBuilder trainLabel = new StringBuilder();
                    List<Train> trains = trainFormation.getTrains();
                    for (Train train : trains) {
                        trainLabel.append(String.format("%s %s nach %s ",
                                train.getType(),
                                train.getNumber(),
                                train.getDestinationStation()));
                    }

                    if (isChecked) {
                        final WagenstandAlarm wagenStandAlarm = new WagenstandAlarm(trainFormation, trainNumber, trainInfo,
                                time, trainLabel.toString(), timestamp, stationLiveData.getValue());

                        if (!wagenstandAlarmManager.isWagenstandAlarmInValidTimeRange(wagenStandAlarm)) {
                            reminderCheckBox.setChecked(false);

                            AlertX.Companion.execAlert(ctx, "Wagenreihungsplan",
                                    getString(R.string.alert_wagenstand_reminder_not_possible_msg),
                                    AlertX.Companion.buttonPositive(),
                                    getString(R.string.dlg_ok), null,
                                    "", null,
                                    "", null,
                                    "", null,
                                    "",
                                    ""
                            );

                        } else if (!wagenstandAlarmManager.addWagenstandAlarm(wagenStandAlarm)) {
                            Log.d("cr", "addWagenstandAlarm failed");
                    }
                } else {
                    wagenstandAlarmManager
                            .cancelWagenstandAlarm(trainNumber, time);
                    }
                }
            }
        });
    }

    /**
     * Updates the Gui
     */
    private void updateGui() {
        Log.d(TAG, "update Gui");

        setWagenstandInformation();

        waggonListview.setAdapter(new WagenstandAdapter(getActivity(), trainFormation));
        waggonListview.invalidate();

        sectionIndicator.setWagenstand(trainFormation);
        sectionIndicator.invalidate();
    }

    public void setWagenstandInformation() {
        headerLayout.removeAllViews();

        final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        for (Train train : trainFormation.getTrains()) {
            final View rowView = layoutInflater.inflate(R.layout.row_wagon_order_overview, headerLayout, false);
            final TextView nameView = rowView.findViewById(R.id.name);
            nameView.setText(String.format("%s %s", train.getType(), train.getNumber()));

            final TextView directionAndSectionsView = rowView.findViewById(R.id.direction_and_sections);
            final String htmlDirectionAndSection = String.format("<b>%s</b>%s", train.getDestinationStation(), formatSection(train));
            directionAndSectionsView.setText(Html.fromHtml(htmlDirectionAndSection));

            directionAndSectionsView.setContentDescription(AccessibilityUtilities.INSTANCE.convertTrackSpan(String.format("%s %s", train.getDestinationStation(), formatSection(train))));

            headerLayout.addView(rowView);
        }
    }

    private String formatSection(Train train) {
        final String sectionSpan = train.getSectionSpan();
        return TextUtils.isEmpty(sectionSpan) ? "" : " (" + sectionSpan + ")";
    }


    /**
     * refresh the Wagenstand from the ist api
     */
    private void doRefresh() {
        refreshLayout.setRefreshing(true);

        final Station station = stationLiveData.getValue();
        if (station == null) {
            return;
        }
        final EvaIds evaIds = station.getEvaIds();
        if (evaIds == null || !de.deutschebahn.bahnhoflive.util.Collections.hasContent(evaIds.getIds())) {
            return;
        }

        WagenstandRequestManager requestManager = new WagenstandRequestManager(this);
        requestManager.loadWagenstand(
                evaIds, trainFormation.getTrainNumber(), trainFormation.getCategory(), trainFormation.getDate(), trainFormation.getTime());

    }

    @Override
    public void onSuccess(TrainFormation payload) {
        refreshLayout.setRefreshing(false);

        try {
            setTrainFormation(payload);
            updateGui();

            updateRefreshTimestamp(null);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFail(VolleyError reason) {
        refreshLayout.setRefreshing(false);
        updateGui();
    }
}
