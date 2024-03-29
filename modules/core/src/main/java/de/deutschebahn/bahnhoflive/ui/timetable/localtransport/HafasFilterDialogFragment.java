/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable.localtransport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.Arrays;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.station.timetable.TimetableTrailingItemViewHolder;
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment;

public class HafasFilterDialogFragment extends FullBottomSheetDialogFragment {


    public interface Consumer {
        void setFilter(@Nullable String trainCategory);
    }

    private static final String ARG_CURRENT = "current";
    private static final String ARG_OPTIONS = "options";
    private static final String ARG_END_TIME = "endTime";

    private String[] options;

    private NumberPicker picker;

    private String current;

    private long endTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        assert arguments != null;
        options = arguments.getStringArray(ARG_OPTIONS);
        current = arguments.getString(ARG_CURRENT);
        endTime = arguments.getLong(ARG_END_TIME);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_timetable_filter_local, container, false);

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
                    ((Consumer) fragment).setFilter(current);
                    dismiss();
                } else {
                    final Object host = getHost();
                    if (host instanceof Activity) {
                        final Intent data = new Intent();
                        data.putExtra("filter", current);
                        final Activity activity = (Activity) host;
                        activity.setResult(Activity.RESULT_OK, data);
                        activity.finish();
                    }

                }


            }
        });

        picker = view.findViewById(R.id.picker);
        picker.setMinValue(0);

        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal == 0) {
                    current = null;
                } else {
                    current = options[newVal];
                }
            }
        });

        final TextView contentHintTextView = view.findViewById(R.id.contentHint);
        if (endTime > 0) {
            final DateFormat timeFormat = TimetableTrailingItemViewHolder.Companion.getTimeFormat();
            final String formattedEndTime = timeFormat.format(endTime);
            final String tomorrowSuffix = DateUtils.isToday(endTime) ? "" : getString(R.string.timetable_trailer_tomorrow);

            contentHintTextView.setText(getString(R.string.template_filter_content_hint,
                    getString(R.string.filter_content_hint_local_transport),
                    formattedEndTime,
                    tomorrowSuffix));
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setPickerContent(options, current);
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

    public static HafasFilterDialogFragment create(long endTime, String trainCategory, String[] trainCategories) {
        final Bundle args = createArguments(endTime, trainCategory, trainCategories);

        return create(args);
    }

    @NonNull
    public static HafasFilterDialogFragment create(Bundle args) {
        final HafasFilterDialogFragment filterDialogFragment = new HafasFilterDialogFragment();
        filterDialogFragment.setArguments(args);
        return filterDialogFragment;
    }

    @NonNull
    public static Bundle createArguments(long endTime, String currentSelection, String... options) {
        final Bundle args = new Bundle();
        args.putStringArray(ARG_OPTIONS, options);
        args.putString(ARG_CURRENT, currentSelection);
        args.putLong(ARG_END_TIME, endTime);
        return args;
    }


}
