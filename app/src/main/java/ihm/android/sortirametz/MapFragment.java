package ihm.android.sortirametz;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.number.Precision;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
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

import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.SiteEntity;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private Location lastKnownLocation;
    private LocationComponent locationComponent;
    private MapboxMap mapboxMap;
    private Button centerOnUserButton;

    public MapFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        if (savedInstanceState != null) {
            mapView.onCreate(savedInstanceState);
        }

        checkPermissons();

        centerOnUserButton = (Button) view.findViewById(R.id.centerOnUserButton);
        centerOnUserButton.setOnClickListener(v -> {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Objects.requireNonNull(locationComponent.getLastKnownLocation()).getLatitude(),
                                locationComponent.getLastKnownLocation().getLongitude()),
                        16f),
                    1500);
                v.setVisibility(View.INVISIBLE);
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

            // Affiche les points d'intérêt présent dans la base de données
            SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(getContext());
            List<SiteEntity> sites = db.siteDao().getAllSites();

            // Ici on récupère l'icône par défaut de Mapbox
            Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), com.mapbox.mapboxsdk.R.drawable.maplibre_marker_icon_default);
            style.addImage("marker-icon", markerBitmap);

            List<Feature> features = new ArrayList<>();

            for (SiteEntity site : sites) {
                Feature feature = Feature.fromGeometry(Point.fromLngLat(site.getLongitude(), site.getLatitude()));
                feature.addStringProperty("nom", site.getNom());

                features.add(feature);
            }

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

        // Lorsque la caméra n'est plus centrée sur l'utilisateur on affiche
        // un bouton pour recentrer la caméra
        mapboxMap.addOnCameraIdleListener(() -> {
            mapboxMap.getStyle(style -> {
                double precision = 0.00000000000001d;
                double cameraLatitude = Math.round(mapboxMap.getCameraPosition().target.getLatitude() * 10000000.0) / 10000000.0;
                double cameraLongitude = Math.round(mapboxMap.getCameraPosition().target.getLongitude() * 10000000.0) / 10000000.0;
                double userLatitude = Math.round(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude() * 10000000.0) / 10000000.0;
                double userLongitude = Math.round(mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude() * 10000000.0) / 10000000.0;

                if (cameraLatitude != userLatitude || cameraLongitude != userLongitude) {
                    centerOnUserButton.setVisibility(View.VISIBLE);
                }
            });
        });

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