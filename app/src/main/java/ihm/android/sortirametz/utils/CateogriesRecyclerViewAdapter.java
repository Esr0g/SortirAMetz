package ihm.android.sortirametz.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.entities.CategorieEntity;

public class CateogriesRecyclerViewAdapter extends RecyclerView.Adapter<CateogriesRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<CategorieEntity> cateogriesList;
    private final Context context;

    public CateogriesRecyclerViewAdapter(Context context, ArrayList<CategorieEntity> cateogriesList) {
        this.cateogriesList = cateogriesList;
        this.context = context;
    }

    @NonNull
    @Override
    public CateogriesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_recycler_view_row, parent, false);

        return new CateogriesRecyclerViewAdapter.ViewHolder(view);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nomCategorieView = itemView.findViewById(R.id.nomCategorieView);
        }

        public void bind(CategorieEntity categorie) {
            nomCategorieView.setText(categorie.getNom());
        }
    }
}
