package de.deutschebahn.bahnhoflive.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.backend.hafas.model.HafasStation;
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds;
import de.deutschebahn.bahnhoflive.repository.InternalStation;
import de.deutschebahn.bahnhoflive.ui.StationWrapper;

public class FavoriteStationsStore<T> {

    public static final Comparator<StationWrapper> TIMESTAMP_COMPARATOR = new Comparator<StationWrapper>() {
        @Override
        public int compare(StationWrapper o1, StationWrapper o2) {
            final long difference = getTimestamp(o2) - getTimestamp(o1);
            return difference > Integer.MAX_VALUE ? Integer.MAX_VALUE
                    : difference < Integer.MIN_VALUE ? Integer.MIN_VALUE
                    : (int) difference;
        }

        public long getTimestamp(StationWrapper item) {
            return item.getFavoriteTimestamp();
        }
    };
    public static final String TAG = FavoriteStationsStore.class.getSimpleName();

    public void clear() {
        timestampPreferences.edit().clear().commit();
        dataPreferences.edit().clear().commit();
    }

    public void adopt(final List<StationWrapper<T>> stationWrappers) {
        if (stationWrappers == null || stationWrappers.isEmpty()) {
            return;
        }

        final Gson gson = this.gson;
        final SharedPreferences.Editor dataEditor = dataPreferences.edit();
        final SharedPreferences.Editor timestampEditor = timestampPreferences.edit();

        for (StationWrapper<T> stationWrapper : stationWrappers) {
            dataEditor
                    .putString(itemAdapter.getId(stationWrapper.getWrappedStation()), gson.toJson(stationWrapper.getWrappedStation()));
            timestampEditor
                    .putLong(itemAdapter.getId(stationWrapper.getWrappedStation()), stationWrapper.getFavoriteTimestamp());
        }

        dataEditor.commit();
        timestampEditor.commit();
    }

    public interface ItemAdapter<T> {
        String getId(T item);

        Class<T> getItemClass();

        StationWrapper<T> createStationWrapper(T station, long timestamp, FavoriteStationsStore<T> favoriteStationsStore);
    }

    private final SharedPreferences dataPreferences;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(EvaIds.class, new EvaIdsJsonDeserializer())
            .create();
    private final SharedPreferences timestampPreferences;

    private final ItemAdapter<T> itemAdapter;

    public FavoriteStationsStore(Context context, final String preferenceName, ItemAdapter<T> itemAdapter) {
        dataPreferences = context.getSharedPreferences("favorite_" + preferenceName + ".pref", Context.MODE_PRIVATE);
        timestampPreferences = context.getSharedPreferences("favorite_" + preferenceName + "_timestamps.pref", Context.MODE_PRIVATE);
        this.itemAdapter = itemAdapter;
    }

    public void add(T station) {
        final Gson gson = this.gson;
        dataPreferences.edit()
                .putString(itemAdapter.getId(station), gson.toJson(station))
                .commit();
        timestampPreferences.edit()
                .putLong(itemAdapter.getId(station), System.currentTimeMillis())
                .commit();
    }

    public void remove(T station) {
        remove(itemAdapter.getId(station));
    }

    public void remove(String id) {
        dataPreferences.edit()
                .remove(id)
                .commit();
        timestampPreferences.edit()
                .remove(id)
                .commit();
    }


    public boolean isFavorite(T station) {
        return isFavorite(itemAdapter.getId(station));
    }

    public boolean isFavorite(String id) {
        return dataPreferences.contains(id);
    }

    public List<StationWrapper<T>> getAll() {
        final Map<String, ?> all = dataPreferences.getAll();
        final ArrayList<StationWrapper<T>> stations = new ArrayList<>(all.size());

        for (Object value : all.values()) {
            try {

                final T station = unmarshall(String.valueOf(value));
                stations.add(itemAdapter.createStationWrapper(station, timestampPreferences.getLong(itemAdapter.getId(station), 0), this));
            } catch (RuntimeException e) {
                Log.w(TAG, "Unmarshall failed", e);
            }
        }

        Collections.sort(stations, TIMESTAMP_COMPARATOR);

        return stations;
    }

    private T unmarshall(String jsonString) {
        return gson.fromJson(jsonString, itemAdapter.getItemClass());
    }

    public static List<StationWrapper> getFavoriteStations(Context context) {
        final ArrayList<StationWrapper> stationWrappers = new ArrayList<>();

        final FavoriteStationsStore<InternalStation> favoriteDbStationsStore = BaseApplication.get().getApplicationServices().getFavoriteDbStationStore();
        stationWrappers.addAll(favoriteDbStationsStore.getAll());

        final FavoriteStationsStore<HafasStation> favoriteHaFavoriteStationsStore = BaseApplication.get().getApplicationServices().getFavoriteHafasStationsStore();
        stationWrappers.addAll(favoriteHaFavoriteStationsStore.getAll());

        Collections.sort(stationWrappers, TIMESTAMP_COMPARATOR);

        return stationWrappers;
    }

}
