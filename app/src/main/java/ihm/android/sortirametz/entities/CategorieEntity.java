package ihm.android.sortirametz.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Représentation d'une catégorie au niveau de la base de donnée
 */
@Entity(tableName = "categories")
public class CategorieEntity implements SearchableItem {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_categorie")
    private int id;

    @ColumnInfo(name = "nom_categorie", collate = ColumnInfo.NOCASE)
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

    @Override
    public EntityType getType() {
        return EntityType.Category;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CategorieEntity other = (CategorieEntity) obj;
        return this.nom.equals(other.nom) && this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id + nom);
    }

}
