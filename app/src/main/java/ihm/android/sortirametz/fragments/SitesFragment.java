package ihm.android.sortirametz.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.List;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.RawSiteEntity;
import ihm.android.sortirametz.entities.SiteEntity;
import ihm.android.sortirametz.listener.ButtonsSiteRecyclerListener;
import ihm.android.sortirametz.utils.FeatureBuilder;
import ihm.android.sortirametz.utils.SiteArrayAdapter;
import ihm.android.sortirametz.utils.SitesRecyclerViewAdapter;

public class SitesFragment extends Fragment implements ButtonsSiteRecyclerListener {

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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new SitesRecyclerViewAdapter(getContext(), sitesList, this, (site, isChecked) -> {
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


    @Override
    public void onButtonShowOnMapClicked(SiteEntity site) {
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

    }

    @Override
    public void onButtonUpdateClicked(SiteEntity site) {
        // Popup Creation
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_new_site, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        TextView latitudeViewPopupCreate = popupView.findViewById(R.id.latitudeViewPopupCreate);
        TextView longitudeViewPopupCreate = popupView.findViewById(R.id.longitudeViewPopupCreate);
        EditText editNomSitePopup = popupView.findViewById(R.id.editNomCategoriePopup);
        EditText editAdresseSitePopup = popupView.findViewById(R.id.editAdresseSitePopup);
        EditText editResumeSitePopup = popupView.findViewById(R.id.editResumeSitePopup);
        Button button = popupView.findViewById(R.id.setSearchRadiusButton);
        editNomSitePopup.setText(site.getNom());
        editAdresseSitePopup.setText(site.getSite().getAdresse());
        editResumeSitePopup.setText(site.getSite().getResume());
        latitudeViewPopupCreate.setText(String.valueOf(site.getSite().getLatitude()));
        longitudeViewPopupCreate.setText(String.valueOf(site.getSite().getLongitude()));
        button.setText("Mettre à jour");

        // On récupère la liste des catégorie pour les afficher dans le spinner
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        List<CategorieEntity> categories = db.categorieDao().getAllCategories();
        SiteArrayAdapter adapter = new SiteArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories);
        Spinner spinnerCategoriePopup = popupView.findViewById(R.id.spinnerMetrique);
        spinnerCategoriePopup.setAdapter(adapter);

        int index = categories.indexOf(site.getCategorie());
        spinnerCategoriePopup.setSelection(index);


        // Quand on appuie sur le bouton cancel cela ferma le popup
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        // Quand on appuie sur le bouton cela met à jour le site
        button.setOnClickListener(v -> {
            if (editNomSitePopup.getText().toString().isEmpty() ||
                    editAdresseSitePopup.getText().toString().isEmpty() ||
                    editResumeSitePopup.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                int categorieId = ((CategorieEntity) spinnerCategoriePopup.getSelectedItem()).getId();

                RawSiteEntity rawSite = new RawSiteEntity();
                rawSite.setId(site.getId());
                rawSite.setNom(editNomSitePopup.getText().toString());
                rawSite.setAdresse(editAdresseSitePopup.getText().toString());
                rawSite.setResume(editResumeSitePopup.getText().toString());
                rawSite.setLatitude(site.getSite().getLatitude());
                rawSite.setLongitude(site.getSite().getLongitude());
                rawSite.setIdCategorie(categorieId);
                db.siteDao().updateSite(rawSite);

                popupWindow.dismiss();
            }
        });

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }
}