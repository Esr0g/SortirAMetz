package ihm.android.sortirametz.listener;

import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.entities.SiteEntity;

public interface CheckBoxListener<T> {
    void onCheckBoxClick(T entity, boolean isChecked);
}
