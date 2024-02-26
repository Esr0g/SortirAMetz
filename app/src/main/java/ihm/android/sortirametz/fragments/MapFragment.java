package ihm.android.sortirametz.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ihm.android.sortirametz.BuildConfig;
import ihm.android.sortirametz.R;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.RawSiteEntity;
import ihm.android.sortirametz.entities.SearchableItem;
import ihm.android.sortirametz.entities.SiteEntity;
import ihm.android.sortirametz.model.MarkersHandler;
import ihm.android.sortirametz.model.Parameters;
import ihm.android.sortirametz.utils.CustomCursorAdapter;
import ihm.android.sortirametz.utils.FeatureBuilder;
import ihm.android.sortirametz.utils.SiteArrayAdapter;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private Location lastKnownLocation;
    private LocationComponent locationComponent;
    private MapboxMap mapboxMap;
    private Button centerOnUserButton;
    private MarkersHandler markersHandler;
    private Parameters parameters;

    public MapFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        markersHandler = new ViewModelProvider(this).get(MarkersHandler.class);
        parameters = new ViewModelProvider(this).get(Parameters.class);

        if (savedInstanceState != null) {
            mapView.onCreate(savedInstanceState);
        }

        checkPermissons();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        centerOnUserButton = (Button) view.findViewById(R.id.centerOnUserButton);
        centerOnUserButton.setOnClickListener(v -> {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Objects.requireNonNull(locationComponent.getLastKnownLocation()).getLatitude(),
                                locationComponent.getLastKnownLocation().getLongitude()),
                        16f),
                    1500);
                v.setVisibility(View.INVISIBLE);
        });

        SearchView searchView = (SearchView) view.findViewById(R.id.searchView);
        searchView.setOnClickListener(v -> {
            searchView.setIconified(false);
        });

    }

    /**
     * Vérifie si les permissions sont accordées, sinon les demande
     */
    private void checkPermissons() {
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            mapView.getMapAsync(this);
        } else {
            PermissionsManager permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(MapFragment.this.getContext(),
                            "Vous devez accepter les permissions pour utiliser l'application",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(MapFragment.this);
                    } else {
                        requireActivity().finish();
                    }
                }
            });

            permissionsManager.requestLocationPermissions(requireActivity());
        }
    }

    /**
     * Méthode appelée lorsque la carte est prête
     * @param mapboxMap Carte Mapbox
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.setStyle("https://api.maptiler.com/maps/streets-v2/style.json?key=" + BuildConfig.MAPTILER_API_KEY, style -> {
            locationComponent = mapboxMap.getLocationComponent();
            LocationComponentOptions locationComponentOptions = LocationComponentOptions.
                    builder(requireContext())
                    .accuracyAlpha(0)
                    .pulseEnabled(true)
                    .build();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    buildLocationComponentActivationOptions(this.mapboxMap.getStyle(), locationComponentOptions);
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.forceLocationUpdate(lastKnownLocation);

            // Positionner la caméra sur la position de l'utilisateur au démarrage aveec un zoom
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(Objects.requireNonNull(locationComponent.getLastKnownLocation()).getLatitude(), locationComponent.getLastKnownLocation().getLongitude()),
                    16f),
                    2500,
                    new MapboxMap.CancelableCallback() {


                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onFinish() {
                            Point position = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                                    locationComponent.getLastKnownLocation().getLatitude()); // Remplacez par vos coordonnées actuelles
                            GeoJsonSource geoJsonSource = new GeoJsonSource("source-id", position);
                            style.addSource(geoJsonSource);

                            double radiusInPixels = metersToPixelsAtZoom(50,
                                    Objects.requireNonNull(mapboxMap.getLocationComponent().getLastKnownLocation()),
                                    mapboxMap.getCameraPosition().zoom);

                            CircleLayer circleLayer = new CircleLayer("circle-layer-id", "source-id");
                            circleLayer.setProperties(
                                    PropertyFactory.circleRadius((float) radiusInPixels),
                                    PropertyFactory.circleColor(Color.parseColor("#FF0000")),
                                    PropertyFactory.visibility(Property.VISIBLE),
                                    PropertyFactory.circleOpacity(0.2f)
                            );

                            style.addLayer(circleLayer);
                            centerOnUserButton.setVisibility(View.INVISIBLE);
                        }
                    }
            );

            // Ici on récupère l'icône par défaut de Mapbox pour les POI
            Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(),
                    com.mapbox.mapboxsdk.R.drawable.maplibre_marker_icon_default);
            style.addImage("marker-icon", markerBitmap);
        });

        // Lorsque la caméra n'est plus centrée sur l'utilisateur on affiche
        // un bouton pour recentrer la caméra sur la position de l'utilisateur
        mapboxMap.addOnCameraIdleListener(() -> {
            mapboxMap.getStyle(style -> {
                double precision = 10000000.0;
                double cameraLatitude = Math.round(mapboxMap.getCameraPosition().target.getLatitude() * precision) / precision;
                double cameraLongitude = Math.round(mapboxMap.getCameraPosition().target.getLongitude() * precision) / precision;
                double userLatitude = Math.round(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude() * precision) / precision;
                double userLongitude = Math.round(mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude() * precision) / precision;

                if (cameraLatitude != userLatitude || cameraLongitude != userLongitude) {
                    centerOnUserButton.setVisibility(View.VISIBLE);
                }
            });
        });

        // On crée un ViewModel pour gérer les marqueurs
        markersHandler.getFeatures().observe(this, features -> {
            mapboxMap.getStyle(style -> {
                Log.i("MapFragment", features.size() + " features");
                style.removeLayer("sites-layer");
                style.removeSource("sites-source");

                style.addSource(new GeoJsonSource("sites-source", FeatureCollection.fromFeatures(features)));

                SymbolLayer sitesLayer = new SymbolLayer("sites-layer", "sites-source")
                        .withProperties(
                                PropertyFactory.iconImage("marker-icon"),
                                PropertyFactory.iconSize(1.4f),
                                PropertyFactory.textField("{nom}"),
                                PropertyFactory.iconAllowOverlap(true),
                                PropertyFactory.textAllowOverlap(true),
                                PropertyFactory.textOffset(new Float[] {0f, -2.5f})
                        );

                style.addLayer(sitesLayer);
            });
        });
        
        // On ajoute un listener pour les clics long sur la map. L'orsque l'utilisateur
        // reste appuié on Affiche un popup qui propose soit de faire une recherche soit
        // de créer un nouveau point d'intérêt aux coordonnées cliquées
        mapboxMap.addOnMapLongClickListener(point -> {
            showSelectionPopup(point);
            return true;
        });

        // On ajoute un listener pour les clicls sur l'enrenage qui ouvre un popup pour définir
        // le rayon de recherche
        ImageButton parametersButton = requireView().findViewById(R.id.parametersButton);
        parametersButton.setOnClickListener(v -> {
            showSetSearchRadiusPopup();
        });

        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        db.siteDao().getAllSitesLiveData().observe(this, sites -> {
            this.updateSearchCompletion();
        });
        db.categorieDao().getAllCategoriesLiveData().observe(this, categories -> {
            this.updateSearchCompletion();
        });

    }

    private void updateSearchCompletion() {
        SearchView searchView = requireView().findViewById(R.id.searchView);
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "nom_site"});
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        List<SearchableItem> searchableItemList = new ArrayList<>();
        searchableItemList.addAll(db.categorieDao().getAllCategories());
        searchableItemList.addAll(db.siteDao().getAllSites());

        for (SearchableItem item : searchableItemList) {
            cursor.addRow(new Object[]{item.getId(), item.getNom()});
        }

        CustomCursorAdapter adapter = new CustomCursorAdapter(requireContext(), cursor);
        searchView.setSuggestionsAdapter(adapter);
    }

    /**
     * Affiche un popup pour proposer à l'utilisateur de créer un nouveau point d'intérêt
     * ou de faire une recherche
     * @param point Coordonnées du point cliqué
     */
    private void showSelectionPopup(LatLng point) {
        // Popup Creation
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_selection, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        // Quand on appuie sur le bouton cancel cela ferma le popup
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        // Quand on appuie sur le bouton créer un site cela ouvre un popup pour créer un site
        Button createSiteButton = popupView.findViewById(R.id.createPOIButton);
        createSiteButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            showCreateSitePopup(point);
        });

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }

    /**
     * Affiche un popup pour créer un nouveau point d'intérêt
     * @param point Coordonnées du point cliqué
     */
    private void showCreateSitePopup(LatLng point) {
        // Popup Creation
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_new_site, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        TextView latitudeViewPopupCreate = popupView.findViewById(R.id.latitudeViewPopupCreate);
        TextView longitudeViewPopupCreate = popupView.findViewById(R.id.longitudeViewPopupCreate);
        latitudeViewPopupCreate.setText(String.valueOf(point.getLatitude()));
        longitudeViewPopupCreate.setText(String.valueOf(point.getLongitude()));

        // On récupère la liste des catégorie pour les afficher dans le spinner
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        List<CategorieEntity> categories = db.categorieDao().getAllCategories();
        SiteArrayAdapter adapter = new SiteArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories);
        Spinner spinnerCategoriePopup = popupView.findViewById(R.id.spinnerMetrique);
        spinnerCategoriePopup.setAdapter(adapter);


        // Quand on appuie sur le bouton cancel cela ferma le popup
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        // Quand on appuie sur le bouton créer un site cela crée un nouveau site
        Button createSiteButton = popupView.findViewById(R.id.setSearchRadiusButton);
        createSiteButton.setOnClickListener(v -> {
            EditText editNomSitePopup = popupView.findViewById(R.id.editNomCategoriePopup);
            EditText editAdresseSitePopup = popupView.findViewById(R.id.editAdresseSitePopup);
            EditText editResumeSitePopup = popupView.findViewById(R.id.editResumeSitePopup);

            if (editNomSitePopup.getText().toString().isEmpty() ||
                    editAdresseSitePopup.getText().toString().isEmpty() ||
                    editResumeSitePopup.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                int categorieId = ((CategorieEntity) spinnerCategoriePopup.getSelectedItem()).getId();
                String nomCategorie = ((CategorieEntity) spinnerCategoriePopup.getSelectedItem()).getNom();

                RawSiteEntity site = new RawSiteEntity();
                site.setNom(editNomSitePopup.getText().toString());
                site.setAdresse(editAdresseSitePopup.getText().toString());
                site.setResume(editResumeSitePopup.getText().toString());
                site.setLatitude(point.getLatitude());
                site.setLongitude(point.getLongitude());
                site.setIdCategorie(categorieId);
                db.siteDao().insertSites(site);
                
                markersHandler.removeAllMarkers();
                FeatureBuilder featureBuilder = new FeatureBuilder();
                SiteEntity siteEntity = new SiteEntity(site, new CategorieEntity(categorieId, nomCategorie));
                markersHandler.addMarker(featureBuilder.buildSiteFeature(siteEntity));
                zoomOnSite(siteEntity);
                popupWindow.dismiss();
            }
        });

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }

    /**
     * Affiche un popup pour définir le rayon de recherche
     */
    private void showSetSearchRadiusPopup() {
        // Popup Creation
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_parameters, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"m", "km"});
        Spinner spinnerMetrique = popupView.findViewById(R.id.spinnerMetrique);
        spinnerMetrique.setAdapter(adapter);

        EditText editRadius = popupView.findViewById(R.id.editRadius);
        editRadius.setText(String.valueOf(parameters.getSearchRadius().getValue()));

        // Quand on appuie sur le bouton cancel cela ferma le popup
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        // Quand on appuie sur le bouton cela applique le rayon de recherche
        Button setSearchRadiusButton = popupView.findViewById(R.id.setSearchRadiusButton);
        setSearchRadiusButton.setOnClickListener(v -> {

            String metrique = spinnerMetrique.getSelectedItem().toString();
            double searchRadius = Double.parseDouble(editRadius.getText().toString());

            if (metrique.equals("km")) {
                searchRadius *= 1000.;
                searchRadius = Math.round(searchRadius);
            } else {
                searchRadius = Math.round(searchRadius);
            }

            if (searchRadius < 10 || searchRadius > 1000000) {
                Toast.makeText(requireContext(), "Le rayon de recherche doit être compris entre 10 mètres et 10 000 km ", Toast.LENGTH_SHORT).show();
            } else {
                parameters.setSearchRadius((int) searchRadius);
                popupWindow.dismiss();
            }

        });

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }

    /**
     * Permet de construire les options d'activation du composant de localisation
     * @param style Style de la carte
     * @param locationComponentOptions Options du composant de localisation
     * @return Options d'activation du composant de localisation
     */
    private LocationComponentActivationOptions buildLocationComponentActivationOptions(
            Style style,
            LocationComponentOptions locationComponentOptions) {

        return LocationComponentActivationOptions.builder(requireContext(), style)
                .locationComponentOptions(locationComponentOptions)
                .useDefaultLocationEngine(true)
                .locationEngineRequest(
                        new LocationEngineRequest.Builder(750)
                                .setFastestInterval(750)
                                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                                .build()
                ).build();
    }

    /**
     * Permet de convertir des mètres en pixels en fonction du zoom sur la carte
     * @param meters Nombre de mètres
     * @param lastKnownLocation Dernière position connue
     * @param zoom Niveau de zoom
     * @return Nombre de pixels
     */
    private double metersToPixelsAtZoom(double meters, Location lastKnownLocation, double zoom) {
        double earthCircumference = 40075017; // Circonférence de la Terre en mètres
        double latitudeRadians = Math.toRadians(lastKnownLocation.getLatitude());
        double numberOfTiles = Math.pow(2, zoom);
        double metersPerPixel = Math.cos(latitudeRadians) * earthCircumference / (256 * numberOfTiles);
        return meters / metersPerPixel;
    }

    /**
     * Permet de zoomer sur un site
     * @param site Site à zoomer
     */
    public void zoomOnSite(SiteEntity site) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(site.getSite().getLatitude(), site.getSite().getLongitude()),
                        16f),
                1500);
    }

    public MarkersHandler getMarkersHandler() {
        return markersHandler;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}