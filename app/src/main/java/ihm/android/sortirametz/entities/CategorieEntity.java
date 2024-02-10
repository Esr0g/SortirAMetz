package ihm.android.sortirametz.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Représentation d'une catégorie au niveau de la base de donnée
 */
@Entity(tableName = "categories")
public class CategorieEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_categorie")
    private int id;

    @ColumnInfo(name = "nom_categorie")
    private String nom;



    // Constructeur vide important pour le bon fonctionnement de Room
    public CategorieEntity() {}

    @Ignore
    public CategorieEntity(String nom) {
        this.nom = nom;
    }

    @Ignore
    public CategorieEntity(int id, String nom) {
        this.nom = nom;
        this.id = id;
    }

    /**
     * Definition des getters et setters de chaque attributs (Important pour le bon fonctionnement
     * de Room)
     */

    public int getId() {
        return id;
    }

    public void setId(int newId) {
        id = newId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String newNom) {
        nom = newNom;
    }

    @NonNull
    @Override
    public String toString() {
        return  "[" + id + "," + nom + "]";
    }
}
