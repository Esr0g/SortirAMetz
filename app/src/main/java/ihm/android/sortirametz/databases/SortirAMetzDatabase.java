package ihm.android.sortirametz.databases;

import androidx.room.Database;
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
@Database(entities = {CategorieEntity.class, SiteEntity.class}, version = 2)
public abstract class SortirAMetzDatabase extends RoomDatabase {
    public abstract CategorieDao categorieDao();
    public abstract SiteDao siteDao();
}
