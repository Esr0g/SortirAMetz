package ihm.android.sortirametz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;

import ihm.android.sortirametz.dao.CategorieDao;
import ihm.android.sortirametz.dao.SiteDao;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.SiteEntity;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ici le getInstance doit être appelé avant le setContentView
        Mapbox.getInstance(this);
        setContentView(R.layout.activity_main);
        // Init Mapbox


        // Init the MapView
        mapView = this.findViewById(R.id.mapView);

        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle(new Style.Builder().fromUri("https://api.maptiler.com/maps/streets-v2/style.json?key=" + BuildConfig.MAPTILER_API_KEY));
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(0.0, 0.0))
                    .zoom(1.0)
                    .build());
        });

        // Test sur la base de données
        this.deleteDatabase("sortir_a_metz");
        SortirAMetzDatabase db = Room.databaseBuilder(getApplicationContext(),
                SortirAMetzDatabase.class,
                "sortir_a_metz").allowMainThreadQueries().build();

        SiteDao siteDao = db.siteDao();
        CategorieDao categorieDao = db.categorieDao();

        CategorieEntity categorie1 = new CategorieEntity("Categorie 1");
        CategorieEntity categorie2 = new CategorieEntity("Categorie 2");

        categorieDao.insertCategories(categorie1);
        categorieDao.insertCategories(categorie2);

        SiteEntity site1 = new SiteEntity("Site 1", 0.0, 0.0, "Adresse 1", 1, "Resume 1");
        SiteEntity site2 = new SiteEntity("Site 2", 0.0, 0.0, "Adresse 2", 2, "Resume 2");
        SiteEntity site3 = new SiteEntity("Site 3", 0.0, 0.0, "Adresse 3", 1, "Resume 3");
        SiteEntity site4 = new SiteEntity("Site 4", 0.0, 0.0, "Adresse 4", 2, "Resume 4");

        siteDao.insertSites(site1);
        siteDao.insertSites(site2);
        siteDao.insertSites(site3);
        siteDao.insertSites(site4);

        Log.i("SortirAMetz", "Nombre de sites : " + siteDao.getAllSites());
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}