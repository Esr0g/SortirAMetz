package ihm.android.sortirametz;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.SiteEntity;

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

        initsitesList();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new SitesRecyclerViewAdapter(getContext(), sitesList);
        recyclerView.setAdapter(adapter);

        return view;

    }

    private void initsitesList() {
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        sitesList.addAll(db.siteDao().getAllSites());
    }
}