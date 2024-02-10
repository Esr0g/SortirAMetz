package ihm.android.sortirametz.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


/**
 * Représentation d'un point d'intérêt au niveau de la base de donnée
 */
@Entity(tableName = "sites", foreignKeys = @ForeignKey(entity = CategorieEntity.class,
        parentColumns = "id_categorie",
        childColumns = "id_categorie",
        onDelete = ForeignKey.SET_NULL))
public class RawSiteEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_site")
    private int id;

    @ColumnInfo(name = "nom_site")
    private String nom;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "adresse")
    private String adresse;

    @ColumnInfo(name = "id_categorie")
    private int idCategorie;

    @ColumnInfo(name = "resume")
    private String resume;

    // Constructeur vide important pour le bon fonctionnement de Room
    public RawSiteEntity() {}

    public RawSiteEntity(String nom,
                         double latitude,
                         double longitude,
                         String adresse,
                         int idCategorie,
                         String resume) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adresse = adresse;
        this.idCategorie = idCategorie;
        this.resume = resume;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double newLatitude) {
        latitude = newLatitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double newLongitude) {
        longitude = newLongitude;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String newAdresse) {
        adresse = newAdresse;
    }

    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int newIdCategorie) {
        idCategorie = newIdCategorie;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String newResume) {
        resume = newResume;
    }

    @NonNull
    @Override
    public String toString() {
        return  "[" + id + "," + nom + ","
                + latitude + "," + longitude + ","
                + adresse + "," + idCategorie + "," + resume + "]";
    }

}
