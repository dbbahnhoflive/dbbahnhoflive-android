/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.timetable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment;

public class FilterDialogFragment extends FullBottomSheetDialogFragment {

    public interface Consumer {
        void setFilter(String trainCategory, String track);
    }

    private static final String ARG_TRAIN_CATEGORY = "trainCategory";
    private static final String ARG_TRACK = "track";
    private static final String ARG_TRAIN_CATEGORIES = "trainCategories";
    private static final String ARG_TRACKS = "tracks";

    private String[] trainCategories;
    private String[] tracks;

    private NumberPicker picker;
    private boolean tracksMode = false;

    private String trackFilter;
    private String trainCategoryFilter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        trainCategories = arguments.getStringArray(ARG_TRAIN_CATEGORIES);
        trainCategoryFilter = arguments.getString(ARG_TRAIN_CATEGORY);
        tracks = arguments.getStringArray(ARG_TRACKS);
        trackFilter = arguments.getString(ARG_TRACK);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_timetable_filter, container, false);

        view.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.button_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Fragment fragment = getParentFragment();
                if (fragment instanceof Consumer) {
                    ((Consumer) fragment).setFilter(trainCategoryFilter, trackFilter);
                }

                dismiss();
            }
        });

        new TwoAlternateButtonsViewHolder(view, R.id.button_train_category, R.id.button_platform, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.button_train_category) {
                    showTrainCategories();
                } else if (id == R.id.button_platform) {
                    showTracks();
                }
            }
        });


        picker = view.findViewById(R.id.picker);
        picker.setMinValue(0);

        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (tracksMode) {
                    if (newVal == 0) {
                        trackFilter = null;
                    } else {
                        trackFilter = tracks[newVal];
                    }
                } else {
                    if (newVal == 0) {
                        trainCategoryFilter = null;
                    } else {
                        trainCategoryFilter = trainCategories[newVal];
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showTrainCategories();
    }

    private void showTracks() {
        tracksMode = true;
        setPickerContent(tracks, trackFilter);
    }

    private void showTrainCategories() {
        tracksMode = false;
        setPickerContent(trainCategories, trainCategoryFilter);
    }

    private void setPickerContent(String[] contents, String currentFilter) {
        picker.setMaxValue(0);
        picker.setDisplayedValues(contents);
        picker.setMaxValue(contents.length - 1);

        if (currentFilter != null) {
            final int index = Arrays.asList(contents).indexOf(currentFilter);
            if (index >= 0) {
                picker.setValue(index);
            }
        }
    }

    public static FilterDialogFragment create(String[] trainCategories, String trainCategory, String[] tracks, String track) {
        final FilterDialogFragment filterDialogFragment = new FilterDialogFragment();
        final Bundle args = new Bundle();
        args.putStringArray(ARG_TRAIN_CATEGORIES, trainCategories);
        args.putString(ARG_TRAIN_CATEGORY, trainCategory);
        args.putStringArray(ARG_TRACKS, tracks);
        args.putString(ARG_TRACK, track);
        filterDialogFragment.setArguments(args);
        return filterDialogFragment;
    }
}
