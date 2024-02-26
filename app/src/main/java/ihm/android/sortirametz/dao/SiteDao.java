package ihm.android.sortirametz.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ihm.android.sortirametz.entities.RawSiteEntity;
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
    void insertSites(RawSiteEntity site);

    /**
     * Récupération de tous les sites avec l'id de la catégorie seulement
     * @return la liste des sites
     */
    @Query("SELECT * FROM sites")
    List<RawSiteEntity> getAllRawSites();

    /**
     * Récupération de tous les sites avec leur catégorie
     */
    @Transaction
    @Query("SELECT * FROM SITES ORDER BY nom_site")
    List<SiteEntity> getAllSites();

    @Transaction
    @Query("SELECT * FROM SITES ORDER BY nom_site")
    LiveData<List<SiteEntity>> getAllSitesLiveData();
    /**
     * Suppression de la liste des sites
     */
    @Delete
    void deleteSites(List<RawSiteEntity> sites);

}
