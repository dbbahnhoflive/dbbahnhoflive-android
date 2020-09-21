/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;
import de.deutschebahn.bahnhoflive.view.BackHandlingFragment;
import de.deutschebahn.bahnhoflive.view.CompoundButtonChecker;
import de.deutschebahn.bahnhoflive.view.FullBottomSheetDialogFragment;

public class FilterFragment extends FullBottomSheetDialogFragment implements BackHandlingFragment {

    private RimapFilter rimapFilter;
    private RimapFilter.Category category;

    private CompoundButtonChecker globalSwitch;
    private ViewGroup contentContainer;
    private TextView titleView;

    private View.OnClickListener categoryOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final RimapFilter.Category category = (RimapFilter.Category) v.getTag();
            setCategory(category);
        }
    };

    private CompoundButton.OnCheckedChangeListener itemOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final Object tag = buttonView.getTag();
            if (tag instanceof RimapFilter.Item) {
                final RimapFilter.Item item = (RimapFilter.Item) tag;
                if (isChecked != item.getChecked()) {
                    item.setChecked(isChecked);

                    propagateFilterChanged();
                }
            }
        }
    };

    private void propagateFilterChanged() {
        getFilterFragmentHost().onFilterChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rimapFilter = getFilterFragmentHost().getFilter();
    }

    @Override
    public void onDestroy() {
        rimapFilter = null;

        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (category != null) {
            setCategory(null);

            return true;
        }

        return false;
    }

    public interface Host {
        void onDismissFilterFragment(FilterFragment filterFragment);

        RimapFilter getFilter();

        void onFilterChanged();
    }

    private Host getFilterFragmentHost() {
        return (Host) getHost();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_filter, container, false);

        view.findViewById(R.id.background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFilterFragmentHost().onDismissFilterFragment(FilterFragment.this);
            }
        });

        titleView = view.findViewById(R.id.title);

        globalSwitch = new CompoundButtonChecker(view.findViewById(R.id.global_switch), (buttonView, isChecked) -> {
            if (category == null) {
                rimapFilter.checkAllItems(isChecked);
            } else {
                category.checkAllItems(isChecked);

                final int childCount = contentContainer.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = contentContainer.getChildAt(i);
                    if (child instanceof CompoundButton) {
                        CompoundButton checkBox = (CompoundButton) child;
                        final Object tag = checkBox.getTag();
                        if (tag instanceof RimapFilter.Item) {
                            checkBox.setChecked(((RimapFilter.Item) tag).getChecked());
                        }
                    }
                }
            }

            propagateFilterChanged();
        });

        contentContainer = view.findViewById(R.id.content_container);

        return view;
    }

    @Override
    public void onDestroyView() {
        titleView = null;
        globalSwitch = null;
        contentContainer = null;

        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        populateView();
    }

    private void populateView() {
        contentContainer.removeAllViews();

        if (category == null) {
            titleView.setText(R.string.title_filter);
            globalSwitch.setChecked(rimapFilter.areAllItemsChecked());

            final List<RimapFilter.Category> categories = rimapFilter.getCategories();

            for (RimapFilter.Category category : categories) {
                contentContainer.addView(createCategoryView(contentContainer, category));
            }

        } else {
            titleView.setText(category.getAppcat());
            globalSwitch.setChecked(category.areAllItemsChecked());

            final List<RimapFilter.Item> items = category.getItems();

            for (RimapFilter.Item item : items) {
                contentContainer.addView(createItemView(contentContainer, item));
            }
        }
    }

    private View createItemView(ViewGroup parent, RimapFilter.Item item) {
        final LayoutInflater layoutInflater = getLayoutInflater();

        final View view = layoutInflater.inflate(R.layout.item_filter_item, parent, false);

        final CompoundButton checkBox = view.findViewById(R.id.checkbox);
        checkBox.setText(item.getTitle());

        checkBox.setTag(item);
        checkBox.setChecked(item.getChecked());
        checkBox.setOnCheckedChangeListener(itemOnCheckedChangeListener);

        return view;
    }

    private View createCategoryView(ViewGroup parent, RimapFilter.Category category) {
        final LayoutInflater layoutInflater = getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.item_filter_category, parent, false);

        final TextView textView = view.findViewById(R.id.text);
        textView.setText(category.getAppcat());

        view.setTag(category);

        view.setOnClickListener(categoryOnClickListener);

        return view;
    }

    private void setCategory(RimapFilter.Category category) {
        this.category = category;

        populateView();
    }

}
