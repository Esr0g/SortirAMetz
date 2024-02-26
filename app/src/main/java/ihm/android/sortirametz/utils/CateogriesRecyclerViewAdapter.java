package ihm.android.sortirametz.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.listener.CheckBoxListener;

public class CateogriesRecyclerViewAdapter extends RecyclerView.Adapter<CateogriesRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<CategorieEntity> cateogriesList;
    private final Context context;
    private final CheckBoxListener<CategorieEntity> checkBoxListener;

    public CateogriesRecyclerViewAdapter(Context context,
                                         ArrayList<CategorieEntity> cateogriesList,
                                         CheckBoxListener<CategorieEntity> checkBoxListener) {
        this.cateogriesList = cateogriesList;
        this.context = context;
        this.checkBoxListener = checkBoxListener;
    }

    @NonNull
    @Override
    public CateogriesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_recycler_view_row, parent, false);

        return new CateogriesRecyclerViewAdapter.ViewHolder(view, checkBoxListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CateogriesRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(cateogriesList.get(position));
    }

    @Override
    public int getItemCount() {
        return cateogriesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nomCategorieView;
        private final CheckBox checkBox;
        private final CheckBoxListener<CategorieEntity> checkBoxListener;

        public ViewHolder(@NonNull View itemView, CheckBoxListener<CategorieEntity> checkBoxListener) {
            super(itemView);

            nomCategorieView = itemView.findViewById(R.id.nomCategorieView);
            checkBox = itemView.findViewById(R.id.checkBox);
            this.checkBoxListener = checkBoxListener;
        }

        public void bind(CategorieEntity categorie) {
            nomCategorieView.setText(categorie.getNom());
            checkBox.setChecked(false);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkBoxListener.onCheckBoxClick(categorie, isChecked);
            });

            if (categorie.getId() == 1) {
                checkBox.setVisibility(View.INVISIBLE);
            } else {
                checkBox.setVisibility(View.VISIBLE);
            }
        }
    }
}
