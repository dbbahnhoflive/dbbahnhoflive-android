package de.deutschebahn.bahnhoflive.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.permission.Permission;

public class StationSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_station_search);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permission.LOCATION.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, StationSearchActivity.class);
    }
}
