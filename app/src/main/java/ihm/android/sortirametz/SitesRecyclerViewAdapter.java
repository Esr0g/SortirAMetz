package ihm.android.sortirametz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ihm.android.sortirametz.entities.SiteEntity;

public class SitesRecyclerViewAdapter extends RecyclerView.Adapter<SitesRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<SiteEntity> sitesList;
    private final Context context;

    public SitesRecyclerViewAdapter(Context context, ArrayList<SiteEntity> sitesList) {
        this.sitesList = sitesList;
        this.context = context;
    }

    @NonNull
    @Override
    public SitesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.site_recycler_view_row, parent, false);

        return new SitesRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SitesRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(sitesList.get(position));
    }

    @Override
    public int getItemCount() {
        return sitesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageSiteView;
        private final TextView nomSiteView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageSiteView = itemView.findViewById(R.id.imageSiteView);
            nomSiteView = itemView.findViewById(R.id.nomSiteView);
        }

        public void bind(SiteEntity site) {
            imageSiteView.setImageResource(R.drawable.location_icon);
            nomSiteView.setText(site.getNom());
        }
    }
}
