package ihm.android.sortirametz.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

import ihm.android.sortirametz.entities.CategorieEntity;

/**
 * Dao pour les catégories
 */
@Dao
public interface CategorieDao {

    /**
     * Insertion d'une catégorie
     * @param categorie
     */
    @Insert
    void insertCategories(CategorieEntity categorie);


    /**
     * Récupération de toutes les catégories
     * @return liste des catégories
     */
    @Query("SELECT * FROM categories ORDER BY nom_categorie ASC")
    List<CategorieEntity> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY nom_categorie ASC")
    LiveData<List<CategorieEntity>> getAllCategoriesLiveData();

    @Delete
    void deleteCategories(ArrayList<CategorieEntity> selectedCategories);
}
