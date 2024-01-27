package com.example.android_laba5;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ListActivity implements MyListAdapter.EditButtonClickListener {

    public static final String DB_TABLE_NAME = "people";
    private static final String DB_COLUMN_ID = "id";
    private static final String DB_COLUMN_NAME = "name";

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Cursor cursor;
    private ArrayList<String> rows;

    private MyListAdapter adapter;
    private ArrayList<Integer> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database helper
        dbHelper = new DBHelper(this);

        // Get a writable database
        db = dbHelper.getWritableDatabase();

        // Create or open the database
        createTable();
        // insertSampleData();
        populateRowsArray();

        // Set up the list view adapter
        adapter = new MyListAdapter(this, rows, ids, this, dbHelper);
        ListView listView = getListView();
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            int selectedIndex = getIndex(selectedItem);
            Toast.makeText(getApplicationContext(), "Selected index: " + selectedIndex, Toast.LENGTH_SHORT).show();
        });
    }

    private void createTable() {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " +
                    DB_TABLE_NAME +
                    " (" + DB_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DB_COLUMN_NAME + " TEXT)");
        } catch (SQLException e) {
            Log.e("MainActivity", "Error creating table: " + e.getMessage());
        }
    }

    private void insertSampleData() {
        ContentValues values = new ContentValues();
        values.put("name", "John Doe");
        db.insert(DB_TABLE_NAME, null, values);

        values.clear();
        values.put("name", "Jane Smith");
        db.insert(DB_TABLE_NAME, null, values);

        values.clear();
        values.put("name", "Bob Johnson");
        db.insert(DB_TABLE_NAME, null, values);

        values.clear();
        values.put("name", "Alice Williams");
        db.insert(DB_TABLE_NAME, null, values);
    }

    private void populateRowsArray() {
        rows = new ArrayList<>();
        ids = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + DB_TABLE_NAME, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DB_COLUMN_ID));
                String row = cursor.getString(cursor.getColumnIndexOrThrow(DB_COLUMN_NAME));
                rows.add(row);
                ids.add(id);
            }
        } catch (SQLException e) {
            Log.e("MainActivity", "Error populating rows array: " + e.getMessage());
        }
    }

    private int getIndex(String name) {
        int index = -1;
        String[] columns = {DB_COLUMN_ID};
        String selection = DB_COLUMN_NAME + " = ?";
        String[] selectionArgs = {name};
        Cursor cursor = db.query(DB_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            index = cursor.getInt(cursor.getColumnIndexOrThrow(DB_COLUMN_ID));
        }
        cursor.close();
        return index;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the database and cursor
        if (cursor != null) {
            cursor.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        if (item.getItemId() == R.id.menu_delete) {
            // Get the name of the selected item
            String selectedItem = rows.get(position);

            // Get the ID of the selected item
            int selectedId = getIndex(selectedItem);

            // Delete the item from the database
            db.delete(DB_TABLE_NAME, DB_COLUMN_ID + "=" + selectedId, null);

            // Update the list view
            updateListView();
            return true;
        }

        if (item.getItemId() == R.id.menu_delete) {
            Toast.makeText(this, "you Edit item!", Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }

    public void updateListView() {
        // Repopulate the rows array
        populateRowsArray();

        // Notify the adapter of the changes
        adapter.clear();
        adapter.addAll(rows);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditButtonClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit item");

        final EditText input = new EditText(this);
        input.setText(rows.get(position));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newItem = input.getText().toString();
            int id = ids.get(position);
            dbHelper.updateRow(id, newItem);
            adapter.updateItem(position, newItem);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Set the position as the tag of the input field so we can retrieve it later
        input.setTag(position);

        builder.show();
    }

    // Helper class for managing the database
    public static class DBHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "my_db";
        private static final int DB_VERSION = 1;

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Do nothing, we create the table in MainActivity
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop the table if it exists
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);

            // Recreate the table
            onCreate(db);
        }

        public void updateRow(int id, String newItem) {
            ContentValues values = new ContentValues();
            values.put(DB_COLUMN_NAME, newItem);

            String selection = DB_COLUMN_ID + " LIKE ?";
            String[] selectionArgs = {String.valueOf(id)};

            getWritableDatabase().update(DB_TABLE_NAME, values, selection, selectionArgs);
        }
    }
}