package ihm.android.sortirametz.databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ihm.android.sortirametz.dao.CategorieDao;
import ihm.android.sortirametz.dao.SiteDao;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.SiteEntity;

/**
 * Base de données
 * Définit la configuration de la base de données et sert de point
 * d'accès principal de l'application aux données persistantes.
 */
@Database(entities = {CategorieEntity.class, SiteEntity.class}, version = 1)
public abstract class SortirAMetzDatabase extends RoomDatabase {

    private static SortirAMetzDatabase instance;
    public abstract CategorieDao categorieDao();
    public abstract SiteDao siteDao();

    public static SortirAMetzDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                    SortirAMetzDatabase.class,
                    "sortir_a_metz").allowMainThreadQueries().build();
        }
        return instance;
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase("sortir_a_metz");
    }

}
