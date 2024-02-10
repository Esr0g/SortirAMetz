package ihm.android.sortirametz.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ihm.android.sortirametz.entities.CategorieEntity;

public class SiteArrayAdapter extends ArrayAdapter<CategorieEntity> {

    public SiteArrayAdapter(@NonNull Context context, int resource, @NonNull List<CategorieEntity> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        CategorieEntity item = getItem(position);

        if (item != null) {
            textView.setText(item.getNom());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        CategorieEntity item = getItem(position);

        if (item != null) {
            textView.setText(item.getNom());
        }
        return convertView;
    }
}
