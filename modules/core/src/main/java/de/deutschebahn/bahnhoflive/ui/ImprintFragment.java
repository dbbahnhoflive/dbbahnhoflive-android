package de.deutschebahn.bahnhoflive.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.deutschebahn.bahnhoflive.BuildConfig;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;

public class ImprintFragment extends Fragment {

    public static final String TAG = ImprintFragment.class.getSimpleName();
    private String title;
    private String url;

    private WebView webview;
    private ImageView headerIcon;
    private TrackingManager trackingManager = new TrackingManager();

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        setUIArguments(args);
    }

    public void setUIArguments(Bundle args) {
        title = args.getString(FragmentArgs.TITLE);
        url = args.getString(FragmentArgs.URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, container, false);

        if (savedInstanceState != null) {
            setUIArguments(savedInstanceState);
        }

        webview = v.findViewById(R.id.webview);
        headerIcon = v.findViewById(R.id.webview_icon);

        if (url.contains("datenschutz")) {
            headerIcon.setImageResource(R.drawable.legacy_datenschutz_dark);
        }

        webview.getSettings().setJavaScriptEnabled(true);

        WebViewClient mWebClient = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(getActivity(), mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    final PackageManager packageManager = getActivity().getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(i, 0);
                    if (list.size() == 0) {
                        return true;
                    }
                    startActivity(i);
                    return true;
                } else if ("app:lizenzen.html".equals(url)) {
                    final Intent intent = WebViewActivity.createIntent(getContext(), "lizenzen.html", "Lizenzen");
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("settings")) {

                    //FIXME url.contains() should not be used for these. Yes, maybe there are no 'normal' links containing these keywords, but still.
                    if (url.contains("location")) {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(i);
                    } else if (url.contains("bluetooth")) {
                        //
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(i);
                    } else if (url.contains("push")) {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivity(i);
                    } else if (url.contains("analytics")) {

                        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        final SharedPreferences.Editor editor = preferences.edit();
                        boolean trackingEnabled = preferences.getBoolean("tracking_enabled", true);

                        int alertMessage = R.string.settings_tracking_active_msg;
                        if (!trackingEnabled) {
                            alertMessage = R.string.settings_tracking_not_active_msg;
                        }

                        AlertDialog analyticsDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.settings_tracking_dlg_title)
                                .setMessage(alertMessage)
                                .setCancelable(true)
                                .setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.putBoolean("tracking_enabled", true);
                                        editor.commit();
                                        dialog.dismiss();

                                        trackingManager.setOptOut(false);
                                        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Action.TRACKING_ACTIVATE);

                                    }
                                }).setNeutralButton(R.string.dlg_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        trackingManager.track(TrackingManager.TYPE_ACTION, TrackingManager.Action.TRACKING_DEACTIVATE);

                                        editor.putBoolean("tracking_enabled", false);
                                        editor.commit();

                                        trackingManager.setOptOut(true);

                                        dialog.dismiss();
                                    }
                                }).
                                        setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();

                        analyticsDialog.show();
                    }

                    return true;

                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        };

        webview.setWebViewClient(mWebClient);
        webview.getSettings().setDefaultFontSize(14);

        InputStream in;
        try {
            in = getResources().getAssets().open(url);
            String imprint = getString(in).replaceAll("\\{APP_VERSION\\}", BuildConfig.VERSION_NAME);

            webview.loadDataWithBaseURL("nil://nil.nil", imprint
                    , "text/html", "UTF-8", null);

            webview.loadDataWithBaseURL("nil://nil.nil", imprint + "</body>", "text/html", "UTF-8", null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ToolbarViewHolder(v, title);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (url != null) {
            outState.putString(FragmentArgs.URL, url);
            outState.putString(FragmentArgs.TITLE, title);
        }

        super.onSaveInstanceState(outState);
    }

    public String getActionBarTitle() {
        return title;
    }

    public boolean isShowingActionBar() {
        return true;
    }

    private String getString(InputStream is) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    @NonNull
    public static ImprintFragment create(Bundle args) {
        final ImprintFragment frag = new ImprintFragment();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    public static Bundle createArgs(String url, String title) {
        final Bundle args = new Bundle();
        args.putString(FragmentArgs.URL, url);
        args.putString(FragmentArgs.TITLE, title);
        return args;
    }

    public static ImprintFragment create(String url, String title) {
        return create(createArgs(url, title));
    }
}
