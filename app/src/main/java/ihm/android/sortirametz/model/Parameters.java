package ihm.android.sortirametz.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Cette classe permet de stocker les paramètres de recherche de l'utilisateur.
 */
public class Parameters extends ViewModel {

    /**
     * Le rayon de recherche en mètres.
     */
    private final MutableLiveData<Integer> searchRadius;
    /**
     * Inique si la recherche est active : si oui on affiche le cercle, sinon on le cache.
     */
    private final MutableLiveData<Boolean> searchActivated;

    /**
     * La recherche est sur position de l'utilisateur sinon sur un marker.
     */
    private final MutableLiveData<Boolean> fromSelfPosition;

    public Parameters() {
        this.searchRadius = new MutableLiveData<>(500);
        this.searchActivated = new MutableLiveData<>(false);
        this.fromSelfPosition = new MutableLiveData<>(true);
    }

    public MutableLiveData<Integer> getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(int searchRadius) {
        this.searchRadius.setValue(searchRadius);
    }

    public MutableLiveData<Boolean> getSearchActivated() {
        return searchActivated;
    }

    public void setSearchActivated(boolean searchActivated) {
        this.searchActivated.setValue(searchActivated);
    }

    public MutableLiveData<Boolean> getFromSelfPosition() {
        return fromSelfPosition;
    }

    public void setFromSelfPosition(boolean fromSelfPosition) {
        this.fromSelfPosition.setValue(fromSelfPosition);
    }
}
