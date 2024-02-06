package ihm.android.sortirametz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mapbox.mapboxsdk.Mapbox;

import ihm.android.sortirametz.dao.CategorieDao;
import ihm.android.sortirametz.dao.SiteDao;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.SiteEntity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this);
        setContentView(R.layout.activity_main);

        setUpdatabase();

        // Permet de charger MapFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, MapFragment.class, null)
                    .commit();
        }




        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            FragmentManager fragmentManager = getSupportFragmentManager();
            MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentByTag("MapFragment");
            SitesFragment sitesFragment = (SitesFragment) fragmentManager.findFragmentByTag("SitesFragment");
            CategoriesFragment categoriesFragment = (CategoriesFragment) fragmentManager.findFragmentByTag("CategoriesFragment");

            if (mapFragment != null) transaction.hide(mapFragment);
            if (sitesFragment != null) transaction.hide(sitesFragment);
            if (categoriesFragment != null) transaction.hide(categoriesFragment);

            // Permet de changer de fragment en fonction de l'item du menu sélectionné
            int itemId = item.getItemId();
            if (itemId == R.id.mapMenuItem) {
                if (mapFragment == null) {
                    mapFragment = new MapFragment();
                            transaction.setReorderingAllowed(true)
                            .add(R.id.fragmentContainerView, mapFragment, "MapFragment");
                } else {
                            transaction.setReorderingAllowed(true)
                            .show(mapFragment);
                }
            } else if (itemId == R.id.listMenuItem) {
                if (sitesFragment == null) {
                    sitesFragment = new SitesFragment();
                            transaction.setReorderingAllowed(true)
                            .add(R.id.fragmentContainerView, sitesFragment, "SitesFragment");
                } else {
                            transaction.setReorderingAllowed(true)
                            .show(sitesFragment);
                }
            } else if (itemId == R.id.categoriesMenuItem) {
                if (categoriesFragment == null) {
                    categoriesFragment = new CategoriesFragment();
                            transaction.setReorderingAllowed(true)
                            .add(R.id.fragmentContainerView, categoriesFragment, "CategoriesFragment");
                } else {
                            transaction.setReorderingAllowed(true)
                            .show(categoriesFragment);
                }
            }

            transaction.commit();

            return true;
        });
    }

    private void setUpdatabase() {
        // Test sur la base de données
        SortirAMetzDatabase.deleteDatabase(this);
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(this);

        SiteDao siteDao = db.siteDao();
        CategorieDao categorieDao = db.categorieDao();

        CategorieEntity categorie1 = new CategorieEntity("Marché");
        CategorieEntity categorie2 = new CategorieEntity("Boulangerie");
        CategorieEntity categorie3 = new CategorieEntity("Autre");

        categorieDao.insertCategories(categorie1);
        categorieDao.insertCategories(categorie2);
        categorieDao.insertCategories(categorie3);

        SiteEntity site1 = new SiteEntity("Marché St Livier", 49.10058640470544, 6.171153485708678, "Rue Saint Livier 57000 Metz", 1, "Marché Saint-Livier");
        SiteEntity site2 = new SiteEntity("Boulangerie Pâtisserie \"Wozniak\"", 49.1001628326097, 6.170289309395522, "11 Rue Saint-Livier, 57000 Metz", 2, "Ceci est une boulangerie");
        SiteEntity site3 = new SiteEntity("Flair'Allure", 49.10020375455887, 6.170567197764935, "19 Rue Saint-Livier, 57000 Metz", 3, "S'occupe de iench");

        siteDao.insertSites(site1);
        siteDao.insertSites(site2);
        siteDao.insertSites(site3);

        Log.i("SortirAMetz", "Nombre de sites : " + siteDao.getAllSites());
    }
}