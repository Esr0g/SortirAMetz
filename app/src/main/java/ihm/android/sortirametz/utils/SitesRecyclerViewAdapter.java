package ihm.android.sortirametz.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.entities.SiteEntity;

public class SitesRecyclerViewAdapter extends RecyclerView.Adapter<SitesRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<SiteEntity> sitesList;
    private final Context context;
    private final ButtonShowOnMapListener buttonShowOnMapListener;
    private final CheckBoxListener checkBoxListener;

    public interface ButtonShowOnMapListener {
        void onButtonClick(SiteEntity site);
    }

    public interface CheckBoxListener {
        void onCheckBoxClick(SiteEntity site, boolean isChecked);
    }

    public SitesRecyclerViewAdapter(Context context,
                                    ArrayList<SiteEntity> sitesList,
                                    ButtonShowOnMapListener buttonShowOnMapListener,
                                    CheckBoxListener checkBoxListener) {
        this.sitesList = sitesList;
        this.context = context;
        this.buttonShowOnMapListener = buttonShowOnMapListener;
        this.checkBoxListener = checkBoxListener;
    }

    @NonNull
    @Override
    public SitesRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.site_recycler_view_row, parent, false);

        return new SitesRecyclerViewAdapter.ViewHolder(view, buttonShowOnMapListener, checkBoxListener);
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
        private final TextView adresseView;
        private final TextView categorieView;
        private final LinearLayout extraInfosLayout;
        private final TextView latitudeView;
        private final TextView longitudeView;
        private final TextView resumeView;
        private final Button showOnMapButton;
        private final CheckBox checkBox;

        private final ButtonShowOnMapListener buttonShowOnMapListener;
        private final CheckBoxListener checkBoxListener;


        public ViewHolder(@NonNull View itemView,
                          ButtonShowOnMapListener buttonShowOnMapListener,
                          CheckBoxListener checkBoxListener) {
            super(itemView);

            imageSiteView = itemView.findViewById(R.id.imageSiteView);
            nomSiteView = itemView.findViewById(R.id.nomCategorieView);
            adresseView = itemView.findViewById(R.id.adresseView);
            categorieView = itemView.findViewById(R.id.categorieView);
            extraInfosLayout = itemView.findViewById(R.id.extraInfosLayout);
            latitudeView = itemView.findViewById(R.id.latitudeView);
            longitudeView = itemView.findViewById(R.id.longitudeView);
            resumeView = itemView.findViewById(R.id.resumeView);
            showOnMapButton = itemView.findViewById(R.id.showOnMapButton);
            checkBox = itemView.findViewById(R.id.checkBox);

            this.buttonShowOnMapListener = buttonShowOnMapListener;
            this.checkBoxListener = checkBoxListener;

            itemView.setOnClickListener(v -> {
                boolean isExpanded = extraInfosLayout.getVisibility() == View.VISIBLE;
                extraInfosLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

                if (!isExpanded) {
                    // Animer l'expansion
                    expand(extraInfosLayout);
                } else {
                    // Animer le repli
                    collapse(extraInfosLayout);
                }
            });
        }

        public void bind(SiteEntity site) {
            imageSiteView.setImageResource(R.drawable.location_icon);
            nomSiteView.setText(site.getSite().getNom());
            adresseView.setText(site.getSite().getAdresse());
            categorieView.setText(site.getCategorie().getNom());
            latitudeView.setText(String.valueOf(site.getSite().getLatitude()));
            longitudeView.setText(String.valueOf(site.getSite().getLongitude()));
            resumeView.setText(site.getSite().getResume());

            checkBox.setChecked(false);

            showOnMapButton.setOnClickListener(v -> {
                buttonShowOnMapListener.onButtonClick(site);
            });

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkBoxListener.onCheckBoxClick(site, isChecked);
            });
        }

        private void expand(final View v) {
            int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
            int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
            final int targetHeight = v.getMeasuredHeight();

            v.getLayoutParams().height = 0;
            v.setVisibility(View.VISIBLE);
            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    v.getLayoutParams().height = interpolatedTime == 1
                            ? ViewGroup.LayoutParams.WRAP_CONTENT
                            : (int) (targetHeight * interpolatedTime);
                    v.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            animation.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 2);
            v.startAnimation(animation);
        }

        private void collapse(final View v) {
            final int initialHeight = v.getMeasuredHeight();

            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime == 1) {
                        v.setVisibility(View.GONE);
                    } else {
                        v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        v.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // Ralentir l'animation
            animation.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density) * 2);
            v.startAnimation(animation);
        }
    }
}
