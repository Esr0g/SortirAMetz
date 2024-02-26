package ihm.android.sortirametz.entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

public class SiteEntity implements SearchableItem {
    @Embedded
    private RawSiteEntity site;

    @Relation(
            parentColumn = "id_categorie",
            entityColumn = "id_categorie"
    )
    private CategorieEntity categorie;

    public SiteEntity()  {}

    @Ignore
    public SiteEntity(RawSiteEntity site, CategorieEntity categorie) {
        this.site = site;
        this.categorie = categorie;
    }

    public RawSiteEntity getSite() {
        return site;
    }

    public void setSite(RawSiteEntity site) {
        this.site = site;
    }

    public CategorieEntity getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieEntity categorie) {
        this.categorie = categorie;
    }

    @Override
    public String getNom() {
        return site.getNom();
    }

    @Override
    public int getId() {
        return site.getId();
    }

    @Override
    public EntityType getType() {
        return EntityType.Site;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + site.getId() + "," + site.getNom() + ","
                + site.getLatitude() + "," + site.getLongitude() + ","
                + site.getAdresse() + "," + categorie.toString() + "," + site.getResume() + "]";
    }
}
