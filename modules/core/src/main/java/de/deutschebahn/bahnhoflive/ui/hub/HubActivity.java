/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.deutschebahn.bahnhoflive.BaseActivity;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.IssueTracker;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.permission.Permission;
//import de.deutschebahn.bahnhoflive.push.FacilityFirebaseService;
import de.deutschebahn.bahnhoflive.ui.tutorial.TutorialFragment;

public class HubActivity extends BaseActivity implements TutorialFragment.Host {

    private TrackingManager trackingManager = new TrackingManager(this);

    boolean keep = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setTheme(R.style.Theme_App_Starting);
//            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
//
//            splashScreen.setKeepOnScreenCondition(() -> keep);
//            Handler handler = new Handler();
//            handler.postDelayed(() -> keep = false, 800L);
//
//        }else{
//            setTheme(R.style.App_Theme);
//        }
//        setTheme(R.style.App_Theme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hub);
/*
        final Intent iintent = getIntent();

        DebugX.Companion.logIntent(this.getLocalClassName(), iintent);

        if (iintent != null) {

            // starts from notification -> search Station and start StationActivity -> showElevators()
            // station needs to be found, because FCM-notification does not contain the position-data, needed for map
            Bundle bundle = iintent.getBundleExtra(FacilityFirebaseService.BUNDLE_NAME_FACILITY_MESSAGE);

            if (bundle != null) {

                final Integer stationNumber = bundle.getInt("stationNumber");
                final String stationName = bundle.getString("stationName");

                final Boolean mapconsent = bundle.getBoolean("mapconsent");

                get().getApplicationServices().getMapConsentRepository().getConsented().setValue(mapconsent);

                if (stationNumber != 0 && stationName != null) {

                    if (!stationName.trim().isEmpty()) {

                        final StationSearchViewModel stationSearchViewModel = new ViewModelProvider(this).get(StationSearchViewModel.class);
                        final SearchResultResource searchResource = stationSearchViewModel.getSearchResource();
                        searchResource.setQuery(stationName);
                        final Context ctx = this;

                        searchResource.getData().observe(this, stations -> {

                            if (stations != null && !stations.isEmpty()) {

                                final int size = stations.size();
                                Station station;

                                // find station by id
                                for (int i = 0; i < size; i++) {
                                    station = stations.get(i).getAsInternalStation();

                                    if (station != null) {
                                        if (station.getId().equalsIgnoreCase(stationNumber.toString())) {
                                            final Intent intent = StationActivity.createIntent(ctx, station, false);
                                            intent.putExtra("SHOW_ELEVATORS", "1");
                                            startActivity(intent);
                                            break;
                                        }

                                    }
                                }


                            }

                        });
                    }
                }
            }
        }
/* cr: todo

        if (!splashWasSeen) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final Fragment hubFragment = fragmentManager.findFragmentByTag("hub");
            if (hubFragment == null) {
                final Fragment splashFragment = fragmentManager.findFragmentByTag("splash");
                if (splashFragment == null) {
                    splashWasSeen = true;
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, new SplashFragment(), "splash")
                            .commit();
                }
            }
        } else
*/
            endSplash(); // show hubfragment


    }

    @Override
    protected void onStart() {
        super.onStart();

        trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.H0);
    }

    @Override
    protected void onResume() {
        IssueTracker.Companion.getInstance().setContext("station", null);

        super.onResume();

        trackingManager.collectLifecycleData(this);

        Log.d("cr", "onResume StartHub");
    }

    @Override
    protected void onPause() {
        super.onPause();
        trackingManager.pauseCollectingLifecycleData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permission.LOCATION.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static Intent createIntent(Context context) {
        final Intent intent = new Intent(context, HubActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public void endSplash() {
        if (TutorialFragment.isPending(this)) {
            switchFragment(new TutorialFragment(), "tutorial", R.string.title_tutorial);
        } else {
            switchFragment(HubFragment.Companion.createWithoutInitialPermissionRequest(), "hub", R.string.title_hub);
        }
    }


    public void switchFragment(Fragment nextFragment, String tag, @StringRes int title) {
        final FragmentManager fragmentManager = getSupportFragmentManager();

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Fragment previousFragment = fragmentManager.findFragmentById(R.id.fragment_container);

            if (previousFragment instanceof TransitionViewProvider) {
                final TransitionViewProvider transitionViewProvider = (TransitionViewProvider) previousFragment;

                final Fade exitTransition = new Fade();
                exitTransition.setDuration(500);
                previousFragment.setExitTransition(exitTransition);

                final Transition sharedElementEnterTransition = new ChangeBounds();
                sharedElementEnterTransition.setDuration(250);
                sharedElementEnterTransition.setStartDelay(250);
                nextFragment.setSharedElementEnterTransition(sharedElementEnterTransition);

                addSharedElement(fragmentTransaction, transitionViewProvider.getPinView());
                addSharedElement(fragmentTransaction, transitionViewProvider.getHomeLogoView());
            }
        }

        setTitle(title);
        getWindow().getDecorView().sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        fragmentTransaction
                .replace(R.id.fragment_container, nextFragment, tag)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void addSharedElement(FragmentTransaction fragmentTransaction, View view) {
        if (view != null) {
            fragmentTransaction.addSharedElement(view, view.getTransitionName());
        }
    }

    @Override
    public void onCloseTutorial(View view) {
        switchFragment(HubFragment.Companion.createWithoutInitialPermissionRequest(), "hub", R.string.title_hub);
    }

}
