package ihm.android.sortirametz.utils;

import com.mapbox.geojson.Feature;

import ihm.android.sortirametz.entities.SiteEntity;

public class FeatureBuilder {
    private Feature feature;
    public FeatureBuilder() {
    }

    public FeatureBuilder initateFeature(double latitude, double longitude) {
        this.feature = Feature.fromGeometry(com.mapbox.geojson.Point.fromLngLat(longitude, latitude));
        return this;
    }

    public FeatureBuilder setNom(String nom) {
        this.feature.addStringProperty("nom", nom);
        return this;
    }

    public FeatureBuilder setCategorie(String categorie) {
        this.feature.addStringProperty("categorie", categorie);
        return this;
    }

    public FeatureBuilder setSiteId(int siteId) {
        this.feature.addNumberProperty("siteId", siteId);
        return this;
    }

    public FeatureBuilder setResume(String resume) {
        this.feature.addStringProperty("resume", resume);
        return this;
    }

    public Feature build() {
        return this.feature;
    }

    public Feature buildSite(SiteEntity site) {
        return initateFeature(site.getSite().getLatitude(), site.getSite().getLongitude())
                .setNom(site.getSite().getNom())
                .setCategorie(site.getCategorie().getNom())
                .setResume(site.getSite().getResume())
                .build();
    }
}
