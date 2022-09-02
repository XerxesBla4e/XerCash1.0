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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransferActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtTxtAmount, edtTxtDescription, edtTxtPerson, edtTxtDate;
    private TextView txtWarning;
    private Button btnPickDate, btnAdd;
    private RadioGroup rgType;
    private AddTransaction addTransaction;

    private Calendar calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener initDate =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    calendar.set(Calendar.YEAR, i);
                    calendar.set(Calendar.MONTH, i1);
                    calendar.set(Calendar.DAY_OF_MONTH, i2);
                    edtTxtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tranfer);

        initViews();

        initClickables();

    }

    private void initClickables() {
        btnAdd.setOnClickListener(this);
        btnPickDate.setOnClickListener(this);
    }

    private void pickDate() {
        new DatePickerDialog(TransferActivity.this,
                initDate, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void initViews() {
        edtTxtAmount = findViewById(R.id.edtTxtAmount);
        edtTxtDescription = findViewById(R.id.edtTxtDesc);
        edtTxtPerson = findViewById(R.id.edtTxtPerson);
        edtTxtDate = findViewById(R.id.edtTxtDate);

        txtWarning = findViewById(R.id.txtWarning);

        btnPickDate = findViewById(R.id.btnPickDate);
        btnAdd = findViewById(R.id.btnAdd);

        rgType = findViewById(R.id.rgType);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPickDate:
                pickDate();
                break;
            case R.id.btnAdd:
                if (validateFields()) {
                    txtWarning.setVisibility(View.GONE);
                    initAdding();
                } else {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please fill missing field(s)");
                }
                break;
            default:
                break;
        }
    }

    private void initAdding() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != addTransaction) {
            if (!addTransaction.isCancelled()) {
                addTransaction.cancel(true);
            }
        }
    }

    private boolean validateFields() {
        if (edtTxtAmount.getText().toString().equals("")) {
            return false;
        }
        if (edtTxtDate.getText().toString().equals("")) {
            return false;
        }
        if (edtTxtPerson.getText().toString().equals("")) {
            return false;
        }
        return true;
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Void> {
        private double amount;
        private String person, date, description, type;
        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.parseDouble(edtTxtAmount.getText().toString());
            this.person = edtTxtPerson.getText().toString();
            this.date = edtTxtDate.getText().toString();
            this.description = edtTxtDescription.getText().toString();

            int rbId = rgType.getCheckedRadioButtonId();
            switch (rbId) {
                case R.id.btnReceive:
                    type = "receive";
                    break;
                case R.id.btnSend:
                    type = "send";
                    amount = -amount;
                    break;
                default:
                    break;
            }
            databaseHelper = new DatabaseHelper(TransferActivity.this);
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("amount", this.amount);
            values.put("recipient", person);
            values.put("date", date);
            values.put("type", type);
            values.put("description", description);
            values.put("user_id", integers[0]);
            long id = db.insert("transactions", null, values);

            if (id != -1) {
                Cursor cursor = db.query("users",
                        new String[]{"remained_amount"}, "_id=?", new String[]{String.valueOf(integers[0])}, null, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range")
                        double currentAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        cursor.close();
                        ContentValues newValues = new ContentValues();
                        newValues.put("remained_amount", currentAmount + amount);
                        int updatetable = db.update("users", newValues, "_id=?",
                                new String[]{String.valueOf(integers[0])});
                        db.close();
                    } else {
                        cursor.close();
                        db.close();
                    }
                } else {
                    db.close();
                }
            } else {
                db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Intent x6 = new Intent(TransferActivity.this, MainActivity.class);
            x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(x6);
        }
    }

}