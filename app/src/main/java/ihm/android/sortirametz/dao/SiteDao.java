package ihm.android.sortirametz.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ihm.android.sortirametz.entities.SiteEntity;

/**
 * Dao pour les sites
 */
@Dao
public interface SiteDao {

    /**
     * Insertion d'un site
     * @param site
     */
    @Transaction
    @Insert
    void insertSites(SiteEntity site);

    /**
     * Récupération de tous les sites
     * @return la liste des sites
     */
    @Query("SELECT * FROM sites")
    List<SiteEntity> getAllSites();
}
