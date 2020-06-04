package de.deutschebahn.bahnhoflive.ui.station.shop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.ui.RecyclerFragment;
import de.deutschebahn.bahnhoflive.ui.map.Content;
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager;
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider;
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;

public class ShopListFragment extends RecyclerFragment<ShopAdapter>
        implements MapPresetProvider {

    public static final String TAG = ShopListFragment.class.getSimpleName();
    public static final String ARG_CATEGORY = "category";

    private ShopCategory category;
    private ShopAdapter adapter;
    private StationViewModel stationViewModel;
    private MutableLiveData<ShopCategory> selectedShopCategory;

    public ShopListFragment() {
        super(R.layout.fragment_recycler_linear);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        category = (ShopCategory) args.getSerializable(ARG_CATEGORY);
        if (category != null) {
            titleResourceLiveData.setValue(category.label);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        adapter = new ShopAdapter(category);
        setAdapter(adapter);

        stationViewModel = ViewModelProviders.of(getActivity()).get(StationViewModel.class);

        selectedShopCategory = stationViewModel.getSelectedShopCategory();
        selectedShopCategory.observe(this, shopCategory -> {
            if (shopCategory != null) {
                if (shopCategory != this.category) {
                    popBackStack();
                } else {
                    selectedShopCategory.setValue(null);
                }
            }
        });

        stationViewModel.getSelectedNews().observe(this, news -> {
                    if (news != null) {
                        popBackStack();
                    }
                }
        );
    }

    private void popBackStack() {
        final HistoryFragment historyFragment = HistoryFragment.parentOf(this);

        if (historyFragment != null) {
            historyFragment.getChildFragmentManager().popBackStack();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        TrackingManager.fromActivity(getActivity()).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.tagFromArguments(getArguments()));
    }

    private void updateAdapter(List<Shop> shops) {
        getAdapter().setData(shops);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LiveData<CategorizedShops> shopsLiveData = stationViewModel.getShopsResource().getData();
        shopsLiveData.observe(getViewLifecycleOwner(), new Observer<CategorizedShops>() {
            @Override
            public void onChanged(@Nullable CategorizedShops categorizedShops) {
                if (categorizedShops != null) {
                    updateAdapter(categorizedShops.getShops().get(category));
                }
            }
        });

        Transformations.switchMap(shopsLiveData, shops -> {
            if (shops != null) {
                return Transformations.switchMap(selectedShopCategory, selectedShopCategoryValue -> {
                    if (selectedShopCategoryValue == null) {
                        return stationViewModel.getSelectedShop();
                    }
                    return null;
                });
            }
            return null;
        }).observe(getViewLifecycleOwner(), shop -> {
            if (shop != null) {
                final int selectedItemIndex = adapter.setSelectedItem(shop);
                stationViewModel.getSelectedShop().setValue(null);
                getRecyclerView().scrollToPosition(selectedItemIndex);
            }
        });

    }

    public static ShopListFragment create(ShopCategory category, String trackingTag) {
        final ShopListFragment fragment = new ShopListFragment();

        fragment.setArguments(createArguments(category, trackingTag));

        return fragment;
    }

    @NonNull
    public static Bundle createArguments(ShopCategory simplifiedRimapCategory, String trackingTag) {
        final Bundle bundle = new Bundle();

        bundle.putSerializable(ARG_CATEGORY, simplifiedRimapCategory);
        TrackingManager.putTrackingTag(bundle, trackingTag);

        return bundle;
    }

    @Override
    public boolean prepareMapIntent(Intent intent) {
        final Shop selectedItem = getAdapter().getSelectedItem();
        if (selectedItem instanceof RimapShop) { //FIXME hand over shop to map without backend dependency
            InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, selectedItem.getRimapPOI());
        }
        return true;
    }

}
