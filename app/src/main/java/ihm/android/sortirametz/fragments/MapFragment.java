package ihm.android.sortirametz.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback;
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest;
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ihm.android.sortirametz.BuildConfig;
import ihm.android.sortirametz.R;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.EntityType;
import ihm.android.sortirametz.entities.RawSiteEntity;
import ihm.android.sortirametz.entities.SearchableItem;
import ihm.android.sortirametz.entities.SiteEntity;
import ihm.android.sortirametz.model.MarkersHandler;
import ihm.android.sortirametz.model.Parameters;
import ihm.android.sortirametz.utils.CustomCursorAdapter;
import ihm.android.sortirametz.utils.FeatureBuilder;
import ihm.android.sortirametz.utils.Pair;
import ihm.android.sortirametz.utils.SiteArrayAdapter;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private Location lastKnownLocation;
    private LocationComponent locationComponent;
    private MapboxMap mapboxMap;
    private Button centerOnUserButton;
    private MarkersHandler markersHandler;
    private Parameters parameters;
    private Pair<Integer, EntityType> currentSearchItem;
    private boolean searchFromLocation = true;
    private LatLng selectedPoint = null;

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
     *
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

            // A chaque fois que la localisation de l'utilisateur change et qu'une recherche est active
            // alors on met à jours les marqueurs
            locationComponent.getLocationEngine().requestLocationUpdates(
                    locationComponentActivationOptions.locationEngineRequest(),
                    new LocationEngineCallback<LocationEngineResult>() {
                        @Override
                        public void onSuccess(LocationEngineResult result) {
                            showSiteFromCategoryOnMap();
                        }

                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Ici on ne fait rien
                        }
                    }, requireActivity().getMainLooper());

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
                            centerOnUserButton.setVisibility(View.INVISIBLE);
                        }
                    }
            );

            // Ici on récupère l'icône par défaut de Mapbox pour les POI
            Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(),
                    com.mapbox.mapboxsdk.R.drawable.maplibre_marker_icon_default);
            style.addImage("marker-icon", markerBitmap);

            // Permet d'afficher un popup lors du click sur la position de l'utilisateur
            locationComponent.addOnLocationClickListener(() -> {
                showCreateSitePopup(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()));
            });
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
                                PropertyFactory.textOffset(new Float[]{0f, 1f})
                        );

                style.addLayer(sitesLayer);
            });
        });

        // On ajoute un listener pour les clics long sur la map. L'orsque l'utilisateur
        // reste appuié on Affiche un popup qui propose soit de faire une recherche soit
        // de créer un nouveau point d'intérêt aux coordonnées cliquées
        mapboxMap.addOnMapLongClickListener(point -> {
            showSelectionPopup(point);
            ((ImageButton) requireView().findViewById(R.id.cancelButton)).performClick();
            return true;
        });

        // On ajoute un listener pour les clicls sur l'enrenage qui ouvre un popup pour définir
        // le rayon de recherche
        ImageButton parametersButton = requireView().findViewById(R.id.parametersButton);
        parametersButton.setOnClickListener(v -> {
            showSetSearchRadiusPopup();
        });

        // Cette partie permet de Setup la complétion automatique de la barre de recherche
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        db.siteDao().getAllSitesLiveData().observe(this, sites -> {
            this.updateSearchCompletion();
        });
        db.categorieDao().getAllCategoriesLiveData().observe(this, categories -> {
            this.updateSearchCompletion();
        });

        SearchView searchView = requireView().findViewById(R.id.searchView);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                if (cursor != null && cursor.moveToPosition(i)) {
                    String selectedSuggestion = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    int entityId = cursor.getInt(cursor.getColumnIndexOrThrow("id_on_table"));
                    EntityType entityType = EntityType.values()[cursor.getInt(cursor.getColumnIndexOrThrow("entity_type"))];
                    currentSearchItem = new Pair<>(entityId, entityType);
                    searchView.setQuery(selectedSuggestion, true);
                }
                return false;
            }
        });

        // Quand la recherche est effectuée si c'et un site on l'affiche, sinon si c'est une catégorie
        // on affiche tous les point d'intéret dans le rayon défini dans les paramètres
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (currentSearchItem != null) {
                    switch (currentSearchItem.getSecond()) {
                        case Site:
                            SiteEntity siteEntity = db.siteDao().getById(currentSearchItem.getFirst());
                            FeatureBuilder featureBuilder = new FeatureBuilder();
                            Feature feature = featureBuilder.buildSiteFeature(siteEntity);
                            markersHandler.removeAllMarkers();
                            markersHandler.addMarker(feature);
                            zoomOnSite(siteEntity);
                            currentSearchItem = null;
                            selectedPoint = null;
                            searchFromLocation = true;
                        case Category:
                            showSiteFromCategoryOnMap();
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        // Un clic sur un marqueur ouvre un popup d'informations sur le marqueur cliqué
        mapboxMap.addOnMapClickListener(point -> {
            PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
            List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "sites-layer");

            if (features.size() > 0) {
                // Un ou plusieurs marqueurs ont été cliqués
                for (Feature feature : features) {
                    // Faites quelque chose avec chaque marqueur cliqué
                    int siteId = (int) Double.parseDouble(feature.getStringProperty("siteId"));
                    showPopuInfoSite(siteId);
                }
                return true;
            } else {
                return false;
            }
        });

    }

    /**
     * Permet d'afficher un popup lorsque l'utilisateur appuie sur un marqueur
     * @param siteId
     */
    private void showPopuInfoSite(int siteId) {
        // Popup Creation
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_show_site, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        // Quand on appuie sur le bouton cancel cela ferma le popup
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        SiteEntity siteEntity = db.siteDao().getById(siteId);

        ((TextView) popupView.findViewById(R.id.nameTextView)).setText(siteEntity.getNom());
        ((TextView) popupView.findViewById(R.id.addresseTextView)).setText(siteEntity.getSite().getAdresse());
        ((TextView) popupView.findViewById(R.id.categorieTextView)).setText(siteEntity.getCategorie().getNom());
        ((TextView) popupView.findViewById(R.id.resumeTextView)).setText(siteEntity.getSite().getResume());

        Location siteLocation = new Location("provider");
        siteLocation.setLongitude(siteEntity.getSite().getLongitude());
        siteLocation.setLatitude(siteEntity.getSite().getLatitude());
        double distance = siteLocation.distanceTo(mapboxMap.getLocationComponent().getLastKnownLocation());

        if (distance / 1000. > 1) {
            ((TextView) popupView.findViewById(R.id.distanceTextView)).setText(String.format("%.3f",distance/1000.) + " km");
        } else {
            ((TextView) popupView.findViewById(R.id.distanceTextView)).setText(((int)Math.round(distance)) + " m");
        }

        ((TextView) popupView.findViewById(R.id.latTexteView)).setText(String.format("%.6f", siteEntity.getSite().getLatitude()));
        ((TextView) popupView.findViewById(R.id.longTewtView)).setText(String.format("%.6f", siteEntity.getSite().getLongitude()));

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }

    // Cette fonction permet d'afficher un cercle ainsi que tous les POI dans un périmètre
    // défini dans les paramètres
    private void showSiteFromCategoryOnMap() {
        if (currentSearchItem != null) {
            SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
            List<SiteEntity> allSiteEntities = db.siteDao().getAllSitesOfCategoryId(currentSearchItem.getFirst());

            // On récupère que les site
            List<SiteEntity> filteredSiteEntities = allSiteEntities
                    .stream()
                    .filter(site -> {
                        Location siteLoctaion = new Location("provider");
                        siteLoctaion.setLatitude(site.getSite().getLatitude());
                        siteLoctaion.setLongitude(site.getSite().getLongitude());

                        float distance = 0f;
                        if (!this.searchFromLocation && this.selectedPoint != null) {
                            Location selectecdLocation = new Location("provider");
                            selectecdLocation.setLatitude(this.selectedPoint.getLatitude());
                            selectecdLocation.setLongitude(this.selectedPoint.getLongitude());

                            distance = selectecdLocation.distanceTo(siteLoctaion);
                        } else {

                            distance = mapboxMap.getLocationComponent().getLastKnownLocation().distanceTo(siteLoctaion);
                        }

                        return distance <= parameters.getSearchRadius().getValue();
                    }).collect(Collectors.toList());

            markersHandler.removeAllMarkers();
            FeatureBuilder featureBuilder = new FeatureBuilder();
            for (SiteEntity site : filteredSiteEntities) {
                markersHandler.addMarker(featureBuilder.buildSiteFeature(site));
            }

            showSearchZone();
        }
    }

    /**
     * Permet d'afficher la zone de recherche sous la forme d'un cercle
     */
    private void showSearchZone() {
        mapboxMap.getStyle(style -> {
            style.removeLayer("circle-layer-id");
            style.removeSource("source-id");

            Point position;

            if (!this.searchFromLocation && this.selectedPoint != null) {
                position = Point.fromLngLat(this.selectedPoint.getLongitude(), this.selectedPoint.getLatitude());
            } else {
                position = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                    locationComponent.getLastKnownLocation().getLatitude());
            }
            GeoJsonSource geoJsonSource = new GeoJsonSource("source-id", position);
            style.addSource(geoJsonSource);

            double radiusInPixels = calculatedRadius(parameters.getSearchRadius().getValue());

            CircleLayer circleLayer = new CircleLayer("circle-layer-id", "source-id");
            circleLayer.setProperties(
                    PropertyFactory.circleRadius((float) radiusInPixels),
                    PropertyFactory.circleColor(Color.parseColor("#379EF5")),
                    PropertyFactory.visibility(Property.VISIBLE),
                    PropertyFactory.circleOpacity(0.1f)
            );

            style.addLayer(circleLayer);
        });

        showSearchInfoPopup();
    }

    /**
     * Affiche un popup qui indique qu'une recherche est appliquée
     */
    private void showSearchInfoPopup() {
        requireView().findViewById(R.id.searchInfo).setVisibility(View.VISIBLE);
        String from = "";
        if (searchFromLocation) {
            from = "depuis votre position";
        } else {
            from = "depuis la position sélectionnée";
        }
        CharSequence sequence = ((SearchView) requireView().findViewById(R.id.searchView)).getQuery();
        ((TextView)requireView().findViewById(R.id.searchContentText)).setText("Recherche de '" + sequence + "' dans un rayon de " + parameters.getSearchRadius().getValue() + " mètres " + from);
        requireView().findViewById(R.id.cancelButton).setOnClickListener(v -> {
            requireView().findViewById(R.id.searchInfo).setVisibility(View.INVISIBLE);
            currentSearchItem = null;
            this.selectedPoint = null;
            this.searchFromLocation = true;
            markersHandler.removeAllMarkers();
            ((SearchView) requireView().findViewById(R.id.searchView)).setQuery("", false);
            ((SearchView) requireView().findViewById(R.id.searchView)).clearFocus();
            hideSearchZone();
        });
    }

    /**
     * Permet de supprimé la zone de recherche
     */
    private void hideSearchZone() {
        mapboxMap.getStyle(style -> {
            style.removeLayer("circle-layer-id");
            style.removeSource("source-id");
        });
    }

    /**
     * Permet de calculer le rayon du cercle en pixels en fonction du zoom sur la map
     * et de la distance indiquée dans les paramètres
     * @param meters
     * @return
     */
    private double calculatedRadius(double meters) {
        CameraPosition cameraPosition = mapboxMap.getCameraPosition();
        Projection projection = mapboxMap.getProjection();
        LatLng target = cameraPosition.target;

        double metersPerPixel = projection.getMetersPerPixelAtLatitude(target.getLatitude());

        return meters / metersPerPixel;
    }

    /**
     * Permet de configurer l'auto-complétion de la barre de recherche
     */
    private void updateSearchCompletion() {
        SearchView searchView = requireView().findViewById(R.id.searchView);
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "name", "id_on_table", "entity_type"});
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        List<SearchableItem> searchableItemList = new ArrayList<>();
        searchableItemList.addAll(db.categorieDao().getAllCategories());
        searchableItemList.addAll(db.siteDao().getAllSites());

        int id = 0;
        for (SearchableItem item : searchableItemList) {
            cursor.addRow(new Object[]{id++, item.getNom(), item.getId(), item.getType().ordinal()});
        }

        CustomCursorAdapter adapter = new CustomCursorAdapter(requireContext(), cursor);
        searchView.setSuggestionsAdapter(adapter);
    }

    /**
     * Affiche un popup pour proposer à l'utilisateur de créer un nouveau point d'intérêt
     * ou de faire une recherche
     *
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

        // Quand on appuie sur le bouton on faitune recherche à partir de la position choisie
        Button searchFromHereButton = popupView.findViewById(R.id.searchFromHereButton);
        searchFromHereButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            setUpSearchFromThisPoint(point);
        });

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }

    private void setUpSearchFromThisPoint(LatLng latLng) {
        mapboxMap.getStyle(style -> {
            style.removeLayer("sites-layer");
            style.removeSource("sites-source");

            Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());

            style.addSource(new GeoJsonSource("sites-source", Feature.fromGeometry(point)));

            SymbolLayer sitesLayer = new SymbolLayer("sites-layer", "sites-source")
                    .withProperties(
                            PropertyFactory.iconImage("marker-icon"),
                            PropertyFactory.iconSize(1.4f)
                    );

            style.addLayer(sitesLayer);

            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latLng.getLatitude(), latLng.getLongitude()),
                            16f),
                    1500);

            requireView().findViewById(R.id.searchView).requestFocus();

            this.searchFromLocation = false;
            this.selectedPoint = latLng;
        });
    }

    /**
     * Affiche un popup pour créer un nouveau point d'intérêt
     *
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
     *
     * @param style                    Style de la carte
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
     * Permet de zoomer sur un site
     *
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