package de.deutschebahn.bahnhoflive.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class WebViewActivity extends AppCompatActivity {

    public static final String ARG_IMPRINT_FRAGMENT_ARGS = ImprintFragment.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        installFragment(getSupportFragmentManager());
    }

    private void installFragment(FragmentManager fragmentManager) {
        final Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
        if (fragment instanceof ImprintFragment) {
            return;
        }

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, ImprintFragment.create(getIntent().getBundleExtra(ARG_IMPRINT_FRAGMENT_ARGS)))
                .commit();
    }

    public static Intent createIntent(Context context, String url, String title) {
        final Intent intent = new Intent(context, WebViewActivity.class);

        intent.putExtra(ARG_IMPRINT_FRAGMENT_ARGS, ImprintFragment.createArgs(url, title));

        return intent;
    }
}
