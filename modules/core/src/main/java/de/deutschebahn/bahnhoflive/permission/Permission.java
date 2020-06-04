package de.deutschebahn.bahnhoflive.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission implements ActivityCompat.OnRequestPermissionsResultCallback {

    public final static Permission LOCATION = new Permission(Manifest.permission.ACCESS_FINE_LOCATION, 815);

    public interface Listener {
        void onPermissionChanged(Permission permission);
    }

    @NonNull
    public final String name;

    public final int requestCode;

    private Boolean granted = null;

    private final List<Listener> listeners = new ArrayList<>();

    private Permission(@NonNull String name, int requestCode) {
        this.name = name;
        this.requestCode = requestCode;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == this.requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (name.equals(permissions[i])) {
                    setGranted(grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }
        }
    }

    private void setGranted(boolean granted) {
        if (this.granted == null || this.granted != granted) {
            this.granted = granted;

            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onPermissionChanged(this);
        }
    }

    public void update(Context context) {
        setGranted(isGranted(context));
    }

    private boolean isGranted(Context context) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, name);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isGranted() {
        return granted != null && granted;
    }

    /**
     * When requesting a permission, the user may choose to permanently deny that permission.
     * The only way for us to get informed about that condition is this method. Unfortunately
     * this also returns true before the very first request of each permission.
     *
     * @param activity the current Activity
     * @return <code>true</code> if showing the permission request popup might fail
     */
    public boolean isPermanentlyDeniedOrFreshInstallation(Activity activity) {
        return !(ActivityCompat.shouldShowRequestPermissionRationale(activity, name)
                || isGranted(activity));
    }

    public void request(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{name},
                requestCode);
    }

}
