package com.example.xercash10;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xercash10.Adapters.ItemsAdapter;
import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Dialogs.SelectItemDialog;
import com.example.xercash10.Models.Item;
import com.example.xercash10.Models.User;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ShoppingActivity extends AppCompatActivity implements ItemsAdapter.GetItem, View.OnClickListener {

    private Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            edtTxtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

        }
    };
    private TextView txtWarning, txtItemName;
    private ImageView itemImg;
    private Button btnPickItem, btnPickDate, btnAdd;
    private EditText edtTxtDate, edtTxtDesc, edtTxtItemPrice, edtTxtStore;
    private RelativeLayout itemRelLayout;
    private Item selectedItem;
    private DatabaseHelper databaseHelper;
    private AddShopping addShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        initViews();

        initClickables();

    }

    private void initClickables() {
        btnPickDate.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnPickItem.setOnClickListener(this);
    }

    private void initViews() {
        txtWarning = findViewById(R.id.txtWarning);
        txtItemName = findViewById(R.id.txtItemName);
        itemImg = findViewById(R.id.itemImg);
        btnAdd = findViewById(R.id.btnAdd);
        btnPickDate = findViewById(R.id.btnPickDate1);
        btnPickItem = findViewById(R.id.btnPick);
        edtTxtDate = findViewById(R.id.edtTxtDate);
        edtTxtDesc = findViewById(R.id.edtTxtDesc);
        edtTxtItemPrice = findViewById(R.id.edtTxtPrice);
        edtTxtStore = findViewById(R.id.edtTxtStore);
        itemRelLayout = findViewById(R.id.investmentRelLayout);
    }

    @Override
    public void OnGettingItemResult(Item item) {
        selectedItem = item;
        itemRelLayout.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(item.getImage_url())
                .into(itemImg);
        txtItemName.setText(item.getName());
        edtTxtDesc.setText(item.getDescription());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPickDate1:
                pickDate();
                break;
            case R.id.btnPick:
                pickItem();
                break;
            case R.id.btnAdd:
                initAdd();
                break;
            default:
                break;
        }
    }

    private void initAdd() {
        if (null != selectedItem) {
            if (!edtTxtItemPrice.getText().toString().equals("")) {
                if (!edtTxtDate.getText().toString().equals("")) {
                    addShopping = new AddShopping();
                    addShopping.execute();
                } else {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please Select a date");
                }
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Please add price");
            }
        } else {
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Please Select an item");
        }
    }

    private void pickItem() {
        SelectItemDialog selectItemDialog = new SelectItemDialog();
        selectItemDialog.show(getSupportFragmentManager(), "select item dialog");
    }

    private void pickDate() {
        new DatePickerDialog(ShoppingActivity.this,
                dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private class AddShopping extends AsyncTask<Void, Void, Void> {
        private User loggedInUser;
        private String date;
        private double price;
        private String store;
        private String description;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SessionManager sessionManager = new SessionManager(ShoppingActivity.this);
            loggedInUser = sessionManager.isUserLoggedIn();
            this.date = edtTxtDate.getText().toString();
            this.price = -Double.valueOf(edtTxtItemPrice.getText().toString());
            this.store = edtTxtStore.getText().toString();
            this.description = edtTxtDesc.getText().toString();

            databaseHelper = new DatabaseHelper(ShoppingActivity.this);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("amount", price);
            contentValues.put("description", description);
            contentValues.put("user_id", loggedInUser.get_id());
            contentValues.put("type", "shopping");
            contentValues.put("date", date);
            contentValues.put("recipient", store);
            long id = db.insert("transactions", null, contentValues);

            ContentValues shoppingValues = new ContentValues();
            shoppingValues.put("item_id", selectedItem.get_id());
            shoppingValues.put("transaction_id", id);
            shoppingValues.put("user_id", loggedInUser.get_id());
            shoppingValues.put("price", price);
            shoppingValues.put("description", description);
            shoppingValues.put("date", date);
            long shopping_id = db.insert("shopping", null, shoppingValues);

            Cursor cursor = db.query("users",
                    new String[]{"remained_amount"},
                    "_id=?", new String[]{
                            String.valueOf(loggedInUser.get_id())
                    }, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    double remainedAmount = cursor.getDouble(cursor.getColumnIndex("reamined_amount"));
                    ContentValues amountValues = new ContentValues();
                    amountValues.put("remained_amount", remainedAmount - price);
                    db.update("users", amountValues, "_id=?",
                            new String[]{String.valueOf(loggedInUser.get_id())});

                }
                cursor.close();
            }
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Toast.makeText(ShoppingActivity.this, selectedItem.getName() + "added successfully", Toast.LENGTH_SHORT).show();
            Intent xer4 = new Intent(ShoppingActivity.this, MainActivity.class);
            xer4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(xer4);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != addShopping) {
            if (!addShopping.isCancelled()) {
                addShopping.cancel(true);
            }
        }
    }
}
