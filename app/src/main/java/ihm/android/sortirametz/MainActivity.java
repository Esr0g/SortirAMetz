package ihm.android.sortirametz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest;
import com.mapbox.mapboxsdk.location.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this);
        setContentView(R.layout.activity_main);

        // Permet de charger MapFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, MapFragment.class, null)
                    .commit();
        }
//        // Test sur la base de données
//        this.deleteDatabase("sortir_a_metz");
//        SortirAMetzDatabase db = Room.databaseBuilder(getApplicationContext(),
//                SortirAMetzDatabase.class,
//                "sortir_a_metz").allowMainThreadQueries().build();
//
//        SiteDao siteDao = db.siteDao();
//        CategorieDao categorieDao = db.categorieDao();
//
//        CategorieEntity categorie1 = new CategorieEntity("Categorie 1");
//        CategorieEntity categorie2 = new CategorieEntity("Categorie 2");
//
//        categorieDao.insertCategories(categorie1);
//        categorieDao.insertCategories(categorie2);
//
//        SiteEntity site1 = new SiteEntity("Site 1", 0.0, 0.0, "Adresse 1", 1, "Resume 1");
//        SiteEntity site2 = new SiteEntity("Site 2", 0.0, 0.0, "Adresse 2", 2, "Resume 2");
//        SiteEntity site3 = new SiteEntity("Site 3", 0.0, 0.0, "Adresse 3", 1, "Resume 3");
//        SiteEntity site4 = new SiteEntity("Site 4", 0.0, 0.0, "Adresse 4", 2, "Resume 4");
//
//        siteDao.insertSites(site1);
//        siteDao.insertSites(site2);
//        siteDao.insertSites(site3);
//        siteDao.insertSites(site4);
//
//        Log.i("SortirAMetz", "Nombre de sites : " + siteDao.getAllSites());
    }
}