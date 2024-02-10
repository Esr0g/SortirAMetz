package ihm.android.sortirametz;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;

import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.SiteEntity;
import ihm.android.sortirametz.utils.FeatureBuilder;
import ihm.android.sortirametz.utils.SitesRecyclerViewAdapter;

public class SitesFragment extends Fragment {

    private ArrayList<SiteEntity> sitesList = new ArrayList<>();
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

        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setOnClickListener(v -> {
            searchView.setIconified(false);
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new SitesRecyclerViewAdapter(getContext(), sitesList, site -> {
            // Ici il faut retrourner sur le fragment de la carte centrée sur le site et afficher
            // un icone pour le site en question
            MapFragment mapFragment = (MapFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("MapFragment");

            mapFragment.getMarkersHandler().removeAllMarkers();
            FeatureBuilder builder = new FeatureBuilder();
            Feature feature = builder.buildSite(site);

            mapFragment.getMarkersHandler().addMarker(feature);
            mapFragment.zoomOnSite(site);

            ((BottomNavigationView)requireActivity().findViewById(R.id.bottomNavigationView)).setSelectedItemId(R.id.mapMenuItem);
        });

        recyclerView.setAdapter(adapter);

        refresh();

        return view;

    }

    public void refresh() {
        sitesList.clear();
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        sitesList.addAll(db.siteDao().getAllSites());
        adapter.notifyDataSetChanged();
    }
}