package ihm.android.sortirametz.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import ihm.android.sortirametz.R;
import ihm.android.sortirametz.entities.EntityType;

public class CustomCursorAdapter extends CursorAdapter implements FilterQueryProvider {

    MatrixCursor cursor;

    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
        cursor = (MatrixCursor) c;
        setFilterQueryProvider(this);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(R.layout.searchable_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String itemName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        ((TextView) view.findViewById(R.id.nameFieldView)).setText(itemName);
        EntityType entityType = EntityType.values()[cursor.getInt(cursor.getColumnIndexOrThrow("entity_type"))];

        switch (entityType) {
            case Site:
                ((ImageView) view.findViewById(R.id.iconView)).setImageResource(R.drawable.location_icon);
                break;
            case Category:
                ((ImageView) view.findViewById(R.id.iconView)).setImageResource(R.drawable.categorie_icon);
                break;
        }
    }

    @Override
    public Cursor runQuery(CharSequence charSequence) {
        MatrixCursor filteredCursor = new MatrixCursor(cursor.getColumnNames());
        if (charSequence != null) {
            String constraintString = charSequence.toString().toLowerCase(Locale.getDefault());
            int id = 0;
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String itemName = cursor.getString(cursor.getColumnIndex("name"));
                int idOnTable = cursor.getInt(cursor.getColumnIndex("id_on_table"));
                EntityType entityType = EntityType.values()[cursor.getInt(cursor.getColumnIndexOrThrow("entity_type"))];
                if (itemName.toLowerCase(Locale.getDefault()).contains(constraintString)) {
                    filteredCursor.addRow(new Object[]{id++, itemName, idOnTable, entityType.ordinal()});
                }
            }
        }
        return filteredCursor;
    }
}
