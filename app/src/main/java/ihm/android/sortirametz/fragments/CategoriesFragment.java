package ihm.android.sortirametz.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.databases.SortirAMetzDatabase;
import ihm.android.sortirametz.entities.CategorieEntity;
import ihm.android.sortirametz.utils.CateogriesRecyclerViewAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesFragment extends Fragment {

    private final ArrayList<CategorieEntity> categoriesList = new ArrayList<>();
    private final ArrayList<CategorieEntity> selectedCategories = new ArrayList<>();
    private CateogriesRecyclerViewAdapter adapter;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setEnabled(false);

        // Lorsqu'on appuie sur le bouton supprimer, on supprime les catégories sélectionnées
        deleteButton.setOnClickListener(v -> {
            SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());

            db.categorieDao().deleteCategories(selectedCategories);
            deleteButton.setEnabled(false);
        });

        // Lorsqu'on appuie sur le bouton créer, on ouvre une boite de dialogue pour créer une catégorie
        FloatingActionButton createButton = view.findViewById(R.id.createButton);
        createButton.setOnClickListener(v -> {
            showCreateCategoriePopup();
        });

        // Lorsque l'on appuie sur une checkbox, on ajoute ou supprime la catégorie de la liste des catégories sélectionnées
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new CateogriesRecyclerViewAdapter(getContext(), categoriesList, (categorie, isChecked) -> {

            if (isChecked) {
                selectedCategories.add(categorie);
            } else {
                selectedCategories.remove(categorie);
            }

            deleteButton.setEnabled(selectedCategories.size() > 0);

        });
        recyclerView.setAdapter(adapter);

        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        db.categorieDao().getAllCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            categoriesList.clear();
            categoriesList.addAll(categories);
            this.selectedCategories.clear();
            adapter.notifyDataSetChanged();
        });
    }


    /**
     * Affiche une boite de dialogue pour créer une catégorie
     */
    private void showCreateCategoriePopup() {
        // Popup Creation
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_new_categorie, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        // Quand on appuie sur le bouton cancel cela ferma le popup
        ImageButton cancelButton = popupView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        // Quand on appuie sur le bouton créer une catégorie, cela crée une catégorie
        Button createCategorieButton = popupView.findViewById(R.id.setSearchRadiusButton);
        createCategorieButton.setOnClickListener(v -> {
            EditText editNomCategoriePopup = popupView.findViewById(R.id.editNomCategoriePopup);

            if (editNomCategoriePopup.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
            } else {

                CategorieEntity categorie = new CategorieEntity();
                categorie.setNom(editNomCategoriePopup.getText().toString());

                SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
                db.categorieDao().insertCategories(categorie);

                popupWindow.dismiss();

                FloatingActionButton deleteButton = requireView().findViewById(R.id.deleteButton);
                deleteButton.setEnabled(false);
            }
        });

        // Show Popup
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
    }
}