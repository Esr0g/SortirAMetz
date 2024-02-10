package ihm.android.sortirametz.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

public class SiteEntity {
    @Embedded
    private RawSiteEntity site;

    @Relation(
            parentColumn = "id_categorie",
            entityColumn = "id_categorie"
    )
    private CategorieEntity categorie;

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
}
