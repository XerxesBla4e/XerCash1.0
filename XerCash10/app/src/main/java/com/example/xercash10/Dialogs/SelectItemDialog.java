package com.example.xercash10.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xercash10.Adapters.ItemsAdapter;
import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.Item;
import com.example.xercash10.R;

import java.util.ArrayList;

public class SelectItemDialog extends DialogFragment implements ItemsAdapter.GetItem {
    private EditText edtTxtItemName;
    private RecyclerView itemRecView;
    private ItemsAdapter.GetItem getItem;

    @Override
    public void OnGettingItemResult(Item item) {
        try {
            getItem = (ItemsAdapter.GetItem) getActivity();
            getItem.OnGettingItemResult(item);
            dismiss();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private ItemsAdapter adapter;
    private DatabaseHelper databaseHelper;
    private GetAllItems getAllItems;
    private SearchForItems searchForItems;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_item, null);

        edtTxtItemName = view.findViewById(R.id.edtTxtItemName);
        itemRecView = view.findViewById(R.id.itemRecView);
        adapter = new ItemsAdapter(getActivity(), this);
        itemRecView.setAdapter(adapter);
        itemRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        databaseHelper = new DatabaseHelper(getActivity());

        edtTxtItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchForItems = new SearchForItems();
                searchForItems.execute(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getAllItems = new GetAllItems();
        getAllItems.execute();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Select An Item");

        return builder.create();
    }

    private class GetAllItems extends AsyncTask<Void, Void, ArrayList<Item>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Item> doInBackground(Void... voids) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("items", null, null
                    , null, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Item> items =
                            new ArrayList<>();
                    do {
                        Item item = new Item();
                        item.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        item.setName(cursor.getString(cursor.getColumnIndex("name")));
                        item.setImage_url(cursor.getString(cursor.getColumnIndex("image_url")));
                        item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    } while (cursor.moveToNext());
                    cursor.close();
                    db.close();
                    return items;
                } else {
                    cursor.close();
                    db.close();
                    return null;
                }
            } else {
                db.close();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (null != items) {
                adapter.setItems(items);
            } else {
                adapter.setItems(new ArrayList<Item>());
            }
        }
    }

    private class SearchForItems extends AsyncTask<String, Void, ArrayList<Item>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Item> doInBackground(String... strings) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("items", null,
                    "name LIKE ?", new String[]{strings[0]}, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Item> items =
                            new ArrayList<>();
                    do {
                        Item item = new Item();
                        item.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        item.setName(cursor.getString(cursor.getColumnIndex("name")));
                        item.setImage_url(cursor.getString(cursor.getColumnIndex("image_url")));
                        item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    } while (cursor.moveToNext());
                    cursor.close();
                    db.close();
                    return items;
                } else {
                    cursor.close();
                    db.close();
                    return null;
                }
            } else {
                db.close();
                return null;
            }
        }

        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if (null != items) {
                adapter.setItems(items);
            } else {
                adapter.setItems(new ArrayList<Item>());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=getAllItems){
            if(!getAllItems.isCancelled()){
                getAllItems.cancel(true);
            }
        }

        if(null!=searchForItems){
            if(!searchForItems.isCancelled()){
                searchForItems.cancel(true);
            }
        }
    }
}
