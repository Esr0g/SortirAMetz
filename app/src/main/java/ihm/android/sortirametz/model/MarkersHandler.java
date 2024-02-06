package ihm.android.sortirametz.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MarkersHandler extends ViewModel {

    private final MutableLiveData<List<Feature>> features;

    public MarkersHandler() {
        this.features = new MutableLiveData<>(new ArrayList<>());
    }

    public void addMarker(Feature feature) {
        List<Feature> tmpFeatures = this.features.getValue();
        Objects.requireNonNull(tmpFeatures).add(feature);
        this.features.setValue(tmpFeatures);
    }

    public void removeMarker(Feature feature) {
        List<Feature> tmpFeatures = this.features.getValue();
        Objects.requireNonNull(tmpFeatures).remove(feature);
        this.features.setValue(tmpFeatures);
    }

    public void removerAllMarkers() {
        this.features.setValue(new ArrayList<>());
    }

    public FeatureCollection getFeaturesCollection() {
        return FeatureCollection.fromFeatures(Objects.requireNonNull(features.getValue()));
    }
}
