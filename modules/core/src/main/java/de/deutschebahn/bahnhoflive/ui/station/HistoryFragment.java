/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import java.util.Objects;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter;

public class HistoryFragment extends Fragment implements MapPresetProvider {

    @RimapFilter.Preset
    private String defaultPreset;

    public void onShow() {

        View view = getView();
        if(view!=null) {
            TextView titleView = view.findViewById(R.id.screen_title);
            if(titleView!=null) {
                titleView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
        }

    }



    @Override
    public boolean prepareMapIntent(@NonNull Intent intent) {
        RimapFilter.putPreset(intent, defaultPreset);

        final FragmentManager childFragmentManager = getChildFragmentManager();
        final Fragment currentFragment = childFragmentManager.findFragmentById(getId());
        if (currentFragment instanceof MapPresetProvider) {
            return ((MapPresetProvider) currentFragment).prepareMapIntent(intent);
        }
        return false;
    }

    public void setDefaultMapFilterPreset(@RimapFilter.Preset String preset) {
        this.defaultPreset = preset;
    }

    public void push(TransactionProcessor transactionProcessor) {
        push(transactionProcessor, null);
    }

    interface RootProvider {
        Fragment createRootFragment(HistoryFragment historyFragment);
    }

    public void initialize(Activity activity) {
//        if (getChildFragmentManager().findFragmentById(getId()) == null) {
//            final RootProvider rootProvider = (RootProvider) activity;
//            setRootFragment(rootProvider.createRootFragment(this));
//        }
        final FragmentManager childFragmentManager = getChildFragmentManager();
        Fragment fragment = childFragmentManager.findFragmentById(getId());
        final RootProvider rootProvider = (RootProvider) activity;

        if (fragment != null) {
            // works on Android 11
            String tg = fragment.getTag();
            if (Objects.equals(tg, "root")) {
                removeFragment(fragment);
                fragment = rootProvider.createRootFragment(this);
                setRootFragment(fragment);
            } else {
                removeFragment(fragment);
                getChildFragmentManager().beginTransaction()
                        .add(getId(), fragment, tg)
                        .commit();
            }


        } else {
            setRootFragment(rootProvider.createRootFragment(this));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        initialize(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public int push(final Fragment fragment) {
        return push(new DefaultTransactionProcessor(fragment, null), null);
    }

    public int push(TransactionProcessor transactionProcessor, String name) {
        final FragmentManager childFragmentManager = getChildFragmentManager();
        if (childFragmentManager.isStateSaved()) {
            return -1; // prevent crash, see https://jira.s-v.de/browse/BAHNHOFLIVE-1348
        }

        final FragmentTransaction transaction = childFragmentManager.beginTransaction()
                .addToBackStack(name);

        return transactionProcessor.process(transaction);
    }

    public boolean pop() {
        final FragmentManager childFragmentManager = getChildFragmentManager();

        final Fragment currentFragment = childFragmentManager.findFragmentById(getId());
        if (currentFragment != null) {
            final FragmentManager currentFragmentChildFragmentManager = currentFragment.getChildFragmentManager();
            if (canPop(currentFragmentChildFragmentManager)) {
                currentFragmentChildFragmentManager.popBackStack();
                return true;
            }
        }

        if (!canPop(childFragmentManager)) return false;

        childFragmentManager.popBackStack();
        return true;
    }

    private boolean canPop(FragmentManager fragmentManager) {
        return !fragmentManager.isStateSaved() && fragmentManager.getBackStackEntryCount() > 0;
    }

    public void popEntireHistory() {
        final FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            final FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStackImmediate(backStackEntry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public static HistoryFragment parentOf(Fragment fragment) {
        final Fragment parentFragment = fragment.getParentFragment();
        if (parentFragment instanceof HistoryFragment) {
            return (HistoryFragment) parentFragment;
        }

        if (parentFragment != null) {
            return parentOf(parentFragment);
        }

        throw new IllegalArgumentException("No ancestor HistoryFragment");
    }

    private void setRootFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .add(getId(), fragment, "root")
                .commit();
    }

    private void replaceRootFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(getId(), fragment, "root")
                .commit();
    }

    private void removeFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }

    public interface TransactionProcessor {
        int process(FragmentTransaction transaction);
    }

    public class DefaultTransactionProcessor implements TransactionProcessor {
        private final Fragment fragment;
        private final String tag;

        public DefaultTransactionProcessor(Fragment fragment, String tag) {
            this.fragment = fragment;
            this.tag = tag;
        }

        @Override
        public int process(FragmentTransaction transaction) {
            return transaction
                    .replace(getId(), fragment, tag)
                    .commit();
        }
    }
}
