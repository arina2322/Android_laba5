package com.example.android_laba5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MyListAdapter extends ArrayAdapter<String> {

    private final List<Integer> ids;
    private final EditButtonClickListener editButtonClickListener;
    private final List<String> items;
    private final Context context;
    private final MainActivity.DBHelper dbHelper;

    public MyListAdapter(Context context,
                         List<String> items,
                         List<Integer> ids,
                         EditButtonClickListener editButtonClickListener,
                         MainActivity.DBHelper dbHelper) {
        super(context, R.layout.list_item_layout, items);
        this.context = context;
        this.ids = ids;
        this.editButtonClickListener = editButtonClickListener;
        this.items = items;
        this.dbHelper = dbHelper; // Add this line
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Inflate the list item layout
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);

        // Get a reference to the TextView
        TextView textView = itemView.findViewById(R.id.text_view);

        // Set the text of the TextView to the corresponding item in the list
        textView.setText(getItem(position));

        // Get a reference to the "edit_button" button
        Button editButton = itemView.findViewById(R.id.edit_button);

        // Set an OnClickListener on the button
        editButton.setOnClickListener(v -> {
            // Invoke the onEditButtonClick() method of the EditButtonClickListener interface
            editButtonClickListener.onEditButtonClick(position);
        });

        return itemView;
    }

    public void updateItem(int position, String newItem) {
        int id = ids.get(position);
        items.set(position, newItem);
        dbHelper.updateRow(id, newItem);
        notifyDataSetChanged();
    }

    public interface EditButtonClickListener {
        void onEditButtonClick(int position);
    }
}

