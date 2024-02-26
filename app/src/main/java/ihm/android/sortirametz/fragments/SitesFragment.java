package ihm.android.sortirametz.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Feature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.RawSiteEntity;
import ihm.android.sortirametz.entities.SiteEntity;
import ihm.android.sortirametz.listener.CheckBoxListener;
import ihm.android.sortirametz.utils.FeatureBuilder;
import ihm.android.sortirametz.utils.SitesRecyclerViewAdapter;

public class SitesFragment extends Fragment {

    private final ArrayList<SiteEntity> sitesList = new ArrayList<>();
    private final ArrayList<RawSiteEntity> sitesSelected = new ArrayList<>();
    private SitesRecyclerViewAdapter adapter;

    public SitesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sites, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setOnClickListener(v -> {
            searchView.setIconified(false);
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new SitesRecyclerViewAdapter(getContext(), sitesList, site -> {
            // Ici il faut retrourner sur le fragment de la carte centrée sur le site et afficher
            // un icone pour le site en question
            ((BottomNavigationView)requireActivity().findViewById(R.id.bottomNavigationView)).setSelectedItemId(R.id.mapMenuItem);
            MapFragment mapFragment = (MapFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("MapFragment");

            mapFragment.requireView().findViewById(R.id.cancelButton).performClick();
            mapFragment.getMarkersHandler().removeAllMarkers();
            FeatureBuilder builder = new FeatureBuilder();
            Feature feature = builder.buildSiteFeature(site);

            mapFragment.getMarkersHandler().addMarker(feature);
            mapFragment.zoomOnSite(site);


        }, (site, isChecked) -> {
            // Ici il faut ajouter ou retirer le site de la liste des sites sélectionnés

            if (isChecked) {
                sitesSelected.add(site.getSite());
            } else {
                sitesSelected.remove(site.getSite());
            }

            FloatingActionButton deleteButton = view.findViewById(R.id.deleteButton);
            deleteButton.setEnabled(sitesSelected.size() > 0);

        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setEnabled(false);
        deleteButton.setOnClickListener(v -> {
            SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
            db.siteDao().deleteSites(sitesSelected);
            deleteButton.setEnabled(false);
        });

        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        db.siteDao().getAllSitesLiveData().observe(getViewLifecycleOwner(), sites -> {
            sitesList.clear();
            sitesList.addAll(sites);
            sitesSelected.clear();
            adapter.notifyDataSetChanged();
        });
    }


}