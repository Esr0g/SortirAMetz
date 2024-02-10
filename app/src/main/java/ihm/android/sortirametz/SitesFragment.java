package ihm.android.sortirametz;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.RawSiteEntity;
import ihm.android.sortirametz.entities.SiteEntity;
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
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
            db.siteDao().deleteSites(sitesSelected);
            removeSiteFromRecyclerView(sitesSelected);
        });

        refresh();
    }

    public void removeSiteFromRecyclerView(List<RawSiteEntity> sitesSelected) {
        for (RawSiteEntity site : sitesSelected) {
            sitesList.removeIf(s -> s.getSite().getId() == site.getId());
        }

        sitesSelected.clear();
        adapter.notifyDataSetChanged();
    }

    public void refresh() {
        sitesList.clear();
        Log.i("SitesFragment", sitesList.size() + "");
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        sitesList.addAll(db.siteDao().getAllSites());
        Log.i("SitesFragment", sitesList.size() + "");
        Log.i("SitesFragment", db.siteDao().getAllSites().size() + "");
        adapter.notifyDataSetChanged();
    }
}