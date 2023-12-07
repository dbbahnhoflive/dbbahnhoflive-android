/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.hub;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME;
import static de.deutschebahn.bahnhoflive.BaseApplication.get;
import static de.deutschebahn.bahnhoflive.ui.accessibility.ContextXKt.isSpokenFeedbackAccessibilityEnabled;

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
import androidx.lifecycle.ViewModelProvider;

import de.deutschebahn.bahnhoflive.BaseActivity;
import de.deutschebahn.bahnhoflive.BuildConfig;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.IssueTracker;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo;
import de.deutschebahn.bahnhoflive.debug.BhfLiveUtilHandler;
import de.deutschebahn.bahnhoflive.permission.Permission;
import de.deutschebahn.bahnhoflive.push.FacilityFirebaseService;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.accessibility.ContextXKt;
import de.deutschebahn.bahnhoflive.ui.search.SearchResultResource;
import de.deutschebahn.bahnhoflive.ui.search.StationSearchViewModel;
import de.deutschebahn.bahnhoflive.ui.station.StationActivity;
import de.deutschebahn.bahnhoflive.ui.tutorial.TutorialFragment;
import de.deutschebahn.bahnhoflive.util.DebugX;
import de.deutschebahn.bahnhoflive.util.VersionManager;

import de.deutschebahn.bahnhoflive.backend.wagenstand.WagenstandAlarm;

public class HubActivity extends BaseActivity implements TutorialFragment.Host {

    private TrackingManager trackingManager = new TrackingManager(this);

    private Boolean checkSelectStationBundle(Intent appIntent) {

        if(appIntent.hasExtra(StationActivity.ARG_STATION_TO_NAVIGATE_BACK)) {
            return true;
        }

        return false;
    }

    private Boolean checkWagenstandNotificationBundle(Intent appIntent) {

        Bundle bundle = appIntent.getBundleExtra(WagenstandAlarm.DEFAULT_BUNDLE_NAME);

        if (bundle != null && (appIntent.getFlags() & (FLAG_ACTIVITY_CLEAR_TASK|FLAG_ACTIVITY_TASK_ON_HOME))!=0  ) {

            Log.d("cr", "wagenStand");
            final String  stationNumberAsString = bundle.getString("station");
            if(stationNumberAsString==null) return false;

            final int stationNumber = Integer.parseInt(stationNumberAsString);
            final String stationName = bundle.getString("stationName");

            if (stationNumber != 0 && stationName != null) {

                if (!stationName.trim().isEmpty()) {

                    final StationSearchViewModel stationSearchViewModel = new ViewModelProvider(this).get(StationSearchViewModel.class);
                    final SearchResultResource searchResource = stationSearchViewModel.getSearchResource();
                    searchResource.setQuery(stationName);
                    final Context ctx = this;

                    searchResource.getData().observe(this, stations -> {

                        if (stations != null && !stations.isEmpty()) {

                            final int size = stations.size();
                            InternalStation station;

                            // find station by id
                            for (int i = 0; i < size; i++) {
                                station = stations.get(i).getAsInternalStation();

                                if (station != null) {
                                    if (station.getId().equalsIgnoreCase(Integer.toString(stationNumber))) {
                                        final TrainInfo trainInfo = bundle.getParcelable("trainInfo");
                                        if(trainInfo!=null) {
                                            final Intent intent = StationActivity.createIntent(ctx, station, trainInfo);
                                            intent.putExtra("IS_NOTIFICATION", 1);
                                            startActivity(intent);
                                        }
                                        break;
                                    }

                                }
                            }


                        }

                    });

                }

                return true;

            }

        }

        return false;
    }

    private Boolean checkElevatorNotificationBundle(Intent appIntent)
    {
            // starts from notification -> search Station and start StationActivity -> showElevators()
            // station needs to be found, because FCM-notification does not contain the position-data, needed for map
            Bundle bundle = appIntent.getBundleExtra(FacilityFirebaseService.BUNDLE_NAME_FACILITY_MESSAGE);

            if (bundle != null && (appIntent.getFlags() & (FLAG_ACTIVITY_CLEAR_TASK|FLAG_ACTIVITY_TASK_ON_HOME))!=0  ) {

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
                                InternalStation station;

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

                    return true;
                }
            }
        }

        return false;
                    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("cr", "HubActivity.onCreate");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SplashScreen.installSplashScreen(this);
            } else
                setTheme(R.style.App_Theme); // todo: google will remove support for 4.4 in 2023...


        super.onCreate(savedInstanceState);

        final VersionManager versionManager = VersionManager.Companion.getInstance(this); // IMPORTANT: has to be done BEFORE Tracking is initialized
        final long version = versionManager.getActualVersion().asVersionLong();
        if(versionManager.isFreshInstallation())
            Log.d("cr", "FRESH");
        else
            Log.d("cr", "UPDATE");

        setContentView(R.layout.activity_hub);
        final Intent appIntent = getIntent();

        DebugX.Companion.logIntent(this.getLocalClassName(), appIntent);


        final String packageName = getPackageName();
        if(BuildConfig.DEBUG && !packageName.contains("community"))
          BhfLiveUtilHandler.Companion.init(this.getApplicationContext());

        if (appIntent != null ) {

            if(!checkWagenstandNotificationBundle(appIntent)) {
                if(!checkElevatorNotificationBundle(appIntent)) {
                  if(!checkSelectStationBundle(appIntent)) {

                  }
                }
            }

        }
/* cr: todo (old splashscreen (for Android 4.4), google will remove support for android 4.4 in 2023...

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
        if (TutorialFragment.isPending(this) && !ContextXKt.isTalkbackOrSelectToSpeakEnabled(this)) {
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
