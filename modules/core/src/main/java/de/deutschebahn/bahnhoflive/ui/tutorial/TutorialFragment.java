package de.deutschebahn.bahnhoflive.ui.tutorial;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.permission.Permission;
import de.deutschebahn.bahnhoflive.ui.hub.TransitionViewProvider;
import de.deutschebahn.bahnhoflive.view.BaseAnimatorListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import static android.content.Context.MODE_PRIVATE;

public class TutorialFragment extends Fragment implements TransitionViewProvider {

    public static final int ANIMATION_DURATION = 750;
    public static final String TAG = TutorialFragment.class.getSimpleName();
    public static final int IMAGE_SEQUENCE_PAUSE = 1500;
    public static final String STATE_PERMISSION_REQUEST_PENDING = "permissionRequestPending";
    public static final String TUTORIAL_PREFERENCES = "tutorial";
    public static final String PREFERENCE_PENDING = "pending_since_2.6.0";
    private TutorialPagerAdapter pagerAdapter;

    private boolean permissionRequestPending = false;


    private final View.OnClickListener onCloseClickListener = this::onCloseClicked;

    private ViewGroup imageFrame;

    private final BaseAnimatorListener dummyAnimatorListener = new BaseAnimatorListener();
    private final Subject<Runnable> accessibilityEventTriggerSubject = BehaviorSubject.create();
    private Disposable accessibilityEventTriggerSubscription;
    private ViewPager viewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            permissionRequestPending = savedInstanceState.getBoolean(STATE_PERMISSION_REQUEST_PENDING, false);
        }

        pagerAdapter = new TutorialPagerAdapter();

        final SharedPreferences sharedPreferences = getSharedPreferences(getActivity());
        if (isPending(sharedPreferences)) {
            sharedPreferences.edit()
                    .putBoolean(PREFERENCE_PENDING, false)
                    .commit();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        imageFrame = view.findViewById(R.id.image_frame2);

        viewPager = view.findViewById(R.id.pager);

        viewPager.setAdapter(pagerAdapter);

        final TabLayout tabLayout = view.findViewById(R.id.page_indicator);
        tabLayout.setupWithViewPager(viewPager);

        view.findViewById(R.id.btn_close).setOnClickListener(onCloseClickListener);

        view.setOnTouchListener((v, event) -> {
            return viewPager.onTouchEvent(event); // forward touch events to allow horizontal swipe anywhere
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        imageFrame = null;
        viewPager.setAdapter(null);
        viewPager = null;
    }

    public void onCloseClicked(View view) {
        final Object host = getActivity();
        if (host instanceof Host) {
            ((Host) host).onCloseTutorial(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        pagerAdapter.locationPermissionTutorialPage.onResume();

        accessibilityEventTriggerSubscription = accessibilityEventTriggerSubject
                .debounce(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(Runnable::run);
    }

    @Override
    public void onPause() {
        accessibilityEventTriggerSubscription.dispose();

        super.onPause();
    }

    @Override
    public View getPinView() {
        return getView().findViewById(R.id.pin_icon);
    }

    @Override
    public View getHomeLogoView() {
        return getView().findViewById(R.id.header_background);
    }

    public static boolean isPending(Context context) {
        final SharedPreferences tutorialPreferences = getSharedPreferences(context);

        return isPending(tutorialPreferences);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(TUTORIAL_PREFERENCES, MODE_PRIVATE);
    }

    private static boolean isPending(SharedPreferences tutorialPreferences) {
        return tutorialPreferences.getBoolean(PREFERENCE_PENDING, true);
    }

    private class TutorialPagerAdapter extends PagerAdapter {

        private final List<TutorialPage> pages;
        final LocationPermissionTutorialPage locationPermissionTutorialPage;

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (object instanceof TutorialPage) {
                final TutorialPage primaryTutorialPage = (TutorialPage) object;
                for (TutorialPage page : pages) {
                    if (page != primaryTutorialPage) {
                        page.setPrimary(false);
                    }
                }
                primaryTutorialPage.setPrimary(true);
            }
        }

        private TutorialPagerAdapter() {
            locationPermissionTutorialPage = new LocationPermissionTutorialPage(R.string.tutorial_title_location_permission, R.string.tutorial_text_location_permission, new ImageSequenceAnimation(
                    R.drawable.tutorial_screen_05_01,
                    R.drawable.tutorial_screen_05_02
            ));

            pages = Arrays.asList(
                    new TutorialPage(R.string.tutorial_title_overview, R.string.tutorial_text_overview),
                    new TutorialPage(R.string.tutorial_title_search, R.string.tutorial_text_search, new ImageSequenceAnimation(
                            R.drawable.tutorial_screen_02_01,
                            R.drawable.tutorial_screen_02_02
                    )),
                    new TutorialPage(R.string.tutorial_title_nearby, R.string.tutorial_text_nearby, new ImageSequenceAnimation(
                            R.drawable.tutorial_screen_04_01,
                            R.drawable.tutorial_screen_04_02
                    )),
                    locationPermissionTutorialPage,
                    new TutorialPage(R.string.tutorial_title_poi_search, R.string.tutorial_text_poi_search, new ImageSequenceAnimation(
                            R.drawable.tutorial_screen_06_01,
                            R.drawable.tutorial_screen_06_02
                    )),
                    new FinalTutorialPage(R.string.tutorial_title_finish, R.string.tutorial_text_finish, onCloseClickListener)
            );
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            final Object tag = view.getTag();
            return object == tag;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @NonNull
        @Override
        public TutorialPage instantiateItem(@NonNull ViewGroup container, int position) {
            final TutorialPage tutorialPage = pages.get(position);

            container.addView(tutorialPage.onShow(container));

            return tutorialPage;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            final TutorialPage tutorialPage = (TutorialPage) object;
            container.removeView(tutorialPage.onDestroy());
        }
    }

    private class Animation {

        public void start() {
            if (imageFrame == null) {
                return;
            }

            imageFrame.animate()
                    .setDuration(ANIMATION_DURATION)
                    .alpha(0f)
                    .setListener(new BaseAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            final ViewGroup imageFrame = TutorialFragment.this.imageFrame;
                            if (imageFrame != null && imageFrame.getAlpha() == 0) {
                                imageFrame.removeAllViews();
                            }
                        }
                    });
        }

        public void cancel() {
        }


    }

    private class TutorialPage {

        private final int title;
        private final int copy;
        private final Animation animation;

        protected View view;

        private boolean primary = false;

        TutorialPage(@StringRes int title, @StringRes int copy) {
            this(title, copy, new Animation());
        }

        TutorialPage(@StringRes int title, @StringRes int copy, Animation animation) {
            this.title = title;
            this.copy = copy;

            this.animation = animation;
        }

        public View onShow(ViewGroup container) {
            if (view == null) {
                view = onCreateView(container);
            }

            return view;
        }

        protected View onCreateView(ViewGroup container) {
            final LayoutInflater layoutInflater = LayoutInflater.from(container.getContext());
            final View view = layoutInflater.inflate(R.layout.tutorial_page, container, false);

            view.setTag(this);

            final CharSequence titleText = getText(title);
            final CharSequence copyText = getText(copy);

            setText(view, R.id.title, titleText);
            setText(view, R.id.text, copyText);

            view.setContentDescription(titleText + "\n" + copyText);

            return view;
        }

        public View onDestroy() {
            animation.cancel();
            return view;
        }

        protected void setText(View view, int id, CharSequence text) {
            setText(view, id, text, null);
        }

        protected void setText(View view, int id, CharSequence text, CharSequence contentDescription) {
            final TextView textView = view.findViewById(id);
            textView.setContentDescription(contentDescription);
            textView.setText(text);
        }

        void bindButton(View view, int text, int accessibilityText, View.OnClickListener onClickListener) {
            final View button = view.findViewById(R.id.button);
            setText(button, R.id.button_label, getText(text), getText(accessibilityText));
            button.setOnClickListener(onClickListener);
            button.setVisibility(View.VISIBLE);
        }

        void setPrimary(boolean primary) {
            if (this.primary != primary) {
                this.primary = primary;

                if (this.primary) {
                    animation.start();

                    accessibilityEventTriggerSubject.onNext(() ->
                            view.announceForAccessibility(view.getContentDescription())
                    );
                } else {
                    animation.cancel();
                }
            }
        }
    }

    public class FinalTutorialPage extends TutorialPage {

        private final int buttonLabel = R.string.tutorial_button_finish;

        private final View.OnClickListener onClickListener;

        FinalTutorialPage(@StringRes int title, @StringRes int copy, View.OnClickListener onClickListener) {
            super(title, copy);
            this.onClickListener = onClickListener;
        }

        @Override
        protected View onCreateView(ViewGroup container) {
            final View view = super.onCreateView(container);

            bindButton(view, buttonLabel, R.string.sr_tutorial_button_finish, onClickListener);

            return view;
        }

    }

    public class LocationPermissionTutorialPage extends TutorialPage implements View.OnClickListener, Permission.Listener {

        private final Permission locationPermission = Permission.LOCATION;

        LocationPermissionTutorialPage(@StringRes int title, @StringRes int copy, Animation animation) {
            super(title, copy, animation);
        }

        @Override
        public View onShow(ViewGroup container) {
            final View view = super.onShow(container);

            updateViews(view);

            locationPermission.removeListener(this);
            locationPermission.addListener(this);

            return view;
        }

        void updateViews(View view) {
            if (locationPermission.isGranted()) {
                view.findViewById(R.id.done_indicator).setVisibility(View.VISIBLE);
                view.findViewById(R.id.button).setVisibility(View.GONE);
            } else {
                bindButton(view, R.string.tutorial_button_location_permission, R.string.sr_tutorial_button_location_permission, this);
                view.findViewById(R.id.done_indicator).setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            final Activity activity = getActivity();

            permissionRequestPending = locationPermission.isPermanentlyDeniedOrFreshInstallation(activity);

            locationPermission.request(activity);
        }

        @Override
        public View onDestroy() {
            locationPermission.removeListener(this);
            return super.onDestroy();
        }

        @Override
        public void onPermissionChanged(Permission permission) {
            updateViews();
        }

        void updateViews() {
            if (view != null) {
                updateViews(view);
            }
        }

        void onResume() {
            final Activity activity = getActivity();

            locationPermission.update(activity);

            if (permissionRequestPending) {
                permissionRequestPending = false;

                if (locationPermission.isPermanentlyDeniedOrFreshInstallation(activity)) {
                    // At this point it is clear that we don't have a fresh installation but the user permanently denied the permission request.
                    startAppSystemSettingsActivity(activity); // He needs to manually change the permission now.
                }
            }

            updateViews();
        }
    }

    public void startAppSystemSettingsActivity(Activity activity) {
        Toast.makeText(activity, "Bitte aktivieren Sie die Berechtigung \"Standort\".", Toast.LENGTH_LONG).show();
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        startActivity(intent);
    }


    public interface Host {
        void onCloseTutorial(View view);
    }

    private class ImageSequenceAnimation extends Animation {
        @DrawableRes
        private final int[] images;

        private int sequenceStep = 0;

        private final List<ViewPropertyAnimator> animators = new ArrayList<>();

        ImageSequenceAnimation(@DrawableRes int... images) {
            this.images = images;
        }

        @Override
        public void start() {
            final ViewGroup imageFrame = TutorialFragment.this.imageFrame;

            if (imageFrame != null) {
                imageFrame.animate() // smoothly ensure visibility of frame, e.g. after canceling fade out
                        .setDuration(ANIMATION_DURATION)
                        .alpha(1f)
                        .setListener(dummyAnimatorListener /* Without this dummy, animation would stop prematurely. */);

                sequenceStep = 0;
                transitionToNextStep(imageFrame);
            }
        }

        @Override
        public void cancel() {
            for (ViewPropertyAnimator animator : animators) {
                animator.cancel();
            }
            animators.clear();
        }

        void transitionToNextStep(ViewGroup imageFrame) {
            final ImageView imageView = (ImageView) LayoutInflater.from(getActivity())
                    .inflate(R.layout.include_tutorial_image, imageFrame, false);

            imageView.setAlpha(0f);
            imageView.setImageResource(images[sequenceStep++]);
            imageFrame.addView(imageView);
            final ViewPropertyAnimator viewPropertyAnimator = imageView.animate();
            animators.add(viewPropertyAnimator);

            viewPropertyAnimator.setStartDelay(sequenceStep <= 1 ? 0 : IMAGE_SEQUENCE_PAUSE)
                    .setDuration(ANIMATION_DURATION)
                    .alpha(1f)
                    .setListener(new BaseAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            final ViewGroup imageFrame = TutorialFragment.this.imageFrame;

                            if (imageFrame != null && imageView.getAlpha() == 1.0) {
                                final int viewIndex = imageFrame.indexOfChild(imageView);
                                if (viewIndex > 0) {
                                    imageFrame.removeViews(0, viewIndex - 1);
                                }

                                if (sequenceStep < images.length) {
                                    transitionToNextStep(imageFrame);
                                }
                            }

                            animators.remove(viewPropertyAnimator);
                        }
                    });

        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_PERMISSION_REQUEST_PENDING, permissionRequestPending);
    }
}
