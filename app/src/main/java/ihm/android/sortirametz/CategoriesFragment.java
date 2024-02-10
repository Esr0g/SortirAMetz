package ihm.android.sortirametz;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new CateogriesRecyclerViewAdapter(getContext(), categoriesList);
        recyclerView.setAdapter(adapter);

        refresh();
    }

    /**
     * On récupère les catégories depuis la base de données et on les affiche
     */
    public void refresh() {
        categoriesList.clear();
        SortirAMetzDatabase db = SortirAMetzDatabase.getInstance(requireContext());
        categoriesList.addAll(db.categorieDao().getAllCategories());
        adapter.notifyDataSetChanged();
    }
}