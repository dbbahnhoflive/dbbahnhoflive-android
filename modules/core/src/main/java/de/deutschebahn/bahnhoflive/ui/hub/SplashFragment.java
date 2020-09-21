/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

import de.deutschebahn.bahnhoflive.R;

public class SplashFragment extends Fragment implements TransitionViewProvider {

    public static final int DURATION = 1000;

    private Timer timer;

    private View pinView;
    private View homeLogoView;
    private View backgroundView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_splash, container, false);

        pinView = view.findViewById(R.id.pin_icon);
        homeLogoView = view.findViewById(R.id.home_logo);
        backgroundView = view.findViewById(R.id.header_background);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final android.app.Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (isResumed()) {
                            if (activity instanceof HubActivity) {
                                ((HubActivity) activity).endSplash();
                            }
                        }
                    });
                }
            }
        }, DURATION);
    }

    @Override
    public void onPause() {
        timer.cancel();

        super.onPause();
    }

    @Override
    public View getPinView() {
        return pinView;
    }

    @Override
    public View getHomeLogoView() {
        return homeLogoView;
    }

    public View getBackgroundView() {
        return backgroundView;
    }
}
