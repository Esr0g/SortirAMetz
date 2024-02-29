package ihm.android.sortirametz.listener;

import ihm.android.sortirametz.entities.SiteEntity;

public interface ButtonsSiteRecyclerListener {
    void onButtonShowOnMapClicked(SiteEntity site);
    void onButtonUpdateClicked(SiteEntity site);
}
