package ihm.android.sortirametz.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import java.util.Locale;

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
        return layoutInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String itemName = cursor.getString(cursor.getColumnIndexOrThrow("nom_site"));
        ((TextView) view.findViewById(android.R.id.text1)).setText(itemName);
    }

    @Override
    public Cursor runQuery(CharSequence charSequence) {
        MatrixCursor filteredCursor = new MatrixCursor(cursor.getColumnNames());
        if (charSequence != null) {
            String constraintString = charSequence.toString().toLowerCase(Locale.getDefault());
            int id = 0;
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String itemName = cursor.getString(cursor.getColumnIndex("nom_site"));
                if (itemName.toLowerCase(Locale.getDefault()).contains(constraintString)) {
                    filteredCursor.addRow(new Object[]{id++, itemName});
                }
            }
        }
        return filteredCursor;
    }
}
