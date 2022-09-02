package com.example.xercash10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import android.widget.TextView;

import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddInvestment extends AppCompatActivity implements View.OnClickListener {
    private EditText edtTxtName, edtTxtInitAmount, edtTxtInitDate, edtTxtFinishDate, edtTxtMonthlyROI;
    private Button btnPickInitDate, btnPickFinishDate, btnAddInvestment;
    private TextView txtWarning;
    private Calendar initcalendar = Calendar.getInstance();
    private Calendar finishcalendar = Calendar.getInstance();
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private AddTransaction addTransaction;
    private AddInvestmentAsync addInvestmentasync;

    private DatePickerDialog.OnDateSetListener initDate =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    initcalendar.set(Calendar.YEAR, i);
                    initcalendar.set(Calendar.MONTH, i1);
                    initcalendar.set(Calendar.DAY_OF_MONTH, i2);
                    edtTxtInitDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(initcalendar.getTime()));

                }
            };

    private DatePickerDialog.OnDateSetListener finishDate =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    finishcalendar.set(Calendar.YEAR, i);
                    finishcalendar.set(Calendar.MONTH, i1);
                    finishcalendar.set(Calendar.DAY_OF_MONTH, i2);
                    edtTxtFinishDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(finishcalendar.getTime()));

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);

        initViews();

        sessionManager = new SessionManager(getApplicationContext());
        databaseHelper = new DatabaseHelper(getApplicationContext());
        setOnclickListener();
    }

    private void setOnclickListener() {
        btnAddInvestment.setOnClickListener(this);
        btnPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddInvestment.this,
                        initDate, initcalendar.get(Calendar.YEAR), initcalendar.get(Calendar.MONTH),
                        initcalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddInvestment.this,
                        finishDate, finishcalendar.get(Calendar.YEAR), finishcalendar.get(Calendar.MONTH),
                        finishcalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void initViews() {
        edtTxtName = findViewById(R.id.edtTxtInvestmentname);
        edtTxtInitAmount = findViewById(R.id.edtTxtinitamount);
        edtTxtInitDate = findViewById(R.id.edtTxtInitDate);
        edtTxtFinishDate = findViewById(R.id.edtTxtFinishDate);
        edtTxtMonthlyROI = findViewById(R.id.edtTxtMonthlyROI);

        btnPickInitDate = findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = findViewById(R.id.btnPickFinishDate);
        btnAddInvestment = findViewById(R.id.btnAddInvestment);

        txtWarning = findViewById(R.id.txtWarning);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddInvestment:
                if (validateFields()) {
                    txtWarning.setVisibility(View.GONE);
                    initAddingInvestment();
                } else {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please Fill all fields");
                }
                break;
            default:
                break;
        }
    }

    private void initAddingInvestment() {
        addTransaction = new AddTransaction();
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            addTransaction.execute(user.get_id());
        }

    }

    private boolean validateFields() {
        if (edtTxtName.getText().toString().equals("")) {
            return false;
        }
        if (edtTxtMonthlyROI.getText().toString().equals("")) {
            return false;
        }
        if (edtTxtInitAmount.getText().toString().equals("")) {
            return false;
        }
        if (edtTxtInitDate.getText().toString().equals("")) {
            return false;
        }
        if (edtTxtFinishDate.getText().toString().equals("")) {
            return false;
        }

        return true;
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Integer> {
        private String date, name;
        private double amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.date = edtTxtInitDate.getText().toString();
            this.name = edtTxtName.getText().toString();
            this.amount = -Double.parseDouble(edtTxtInitAmount.getText().toString());
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("amount", amount);
            values.put("recipient", name);
            values.put("date", date);
            values.put("description", "Initial amount for " + name + "investment");
            values.put("type", "investment");

            long id = db.insert("transactions",
                    null, values);
            return (int) id;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (null != integer) {
                addInvestmentasync = new AddInvestmentAsync();
                addInvestmentasync.execute(integer);
            }
        }
    }

    private class AddInvestmentAsync extends AsyncTask<Integer, Void, Void> {

        private int userId;
        private String initDate, finishDate, name;
        private double monthlyROI, amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.parseDouble(edtTxtInitAmount.getText().toString());
            this.monthlyROI = Double.parseDouble(edtTxtMonthlyROI.getText().toString());
            this.name = edtTxtName.getText().toString();
            this.initDate = edtTxtInitDate.getText().toString();
            this.finishDate = edtTxtFinishDate.getText().toString();
            User user = sessionManager.isUserLoggedIn();
            if (null != user) {
                this.userId = user.get_id();
            } else {
                this.userId = -1;
            }
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            if (userId != -1) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("init_date", initDate);
                values.put("finish_date", finishDate);
                values.put("amount", amount);
                values.put("monthly_roi", monthlyROI);
                values.put("user_id", userId);
                values.put("transaction_id", integers[0]);

                long id = db.insert("investments", null, values);
                if (id != -1) {
                    Cursor cursor = db.query("users",
                            new String[]{"remained_amount"}, "_id=?", new String[]{String.valueOf(userId)}, null, null, null, null);
                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            @SuppressLint("Range")
                            double currentAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                            cursor.close();
                            ContentValues newValues = new ContentValues();
                            newValues.put("remained_amount", currentAmount - amount);
                            int updatetable = db.update("users", newValues, "_id=?",
                                    new String[]{String.valueOf(userId)});

                        } else {
                            cursor.close();
                        }
                    }

                }
                db.close();
            } else {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date initDate = sdf.parse(edtTxtInitDate.getText().toString());
                calendar.setTime(initDate);
                int initMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
                Date finishDate = sdf.parse(edtTxtInitDate.getText().toString());
                calendar.setTime(finishDate);
                int finishMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                int difference = finishMonths - initMonths;

                int days = 0;

                for (int i = 0; i < difference; i++) {
                    days += exactDayCount();
                    Data data = new Data.Builder()
                            .putDouble(
                                    "amount", amount * monthlyROI / 100
                            )
                            .putString("description", "Profit for " + name)
                            .putInt("user_id", userId)
                            .putString("recipient", name)
                            .build();
                    Constraints constraints = new Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build();
                    OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(
                            InvestmentWorker.class
                    ).setInputData(data)
                            .setConstraints(constraints)
                            .setInitialDelay(days, TimeUnit.DAYS)
                            .addTag("profit")
                            .build();

                    WorkManager.getInstance(AddInvestment.this).enqueue(request);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            Intent xer5 = new Intent(AddInvestment.this, MainActivity.class);
            xer5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(xer5);
        }

        private int exactDayCount() {
            int days = 0;
            int monthArray31[] = {1, 3, 5, 7, 8, 10, 12};

            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            //check if leap year
            if (((currentYear % 4 == 0) && (currentYear % 100 != 0)) || (currentYear % 400 == 0)) {
                for (int m : monthArray31) {
                    if (currentMonth == m) {
                        days = 31;
                    } else if (currentMonth == 2) {
                        days = 29;
                    } else {
                        days = 30;
                    }
                }
            } else {
                for (int m : monthArray31) {
                    if (currentMonth == m) {
                        days = 31;
                    } else if (currentMonth == 2) {
                        days = 28;
                    } else {
                        days = 30;
                    }
                }
            }

            return days;
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

        if (null != addInvestmentasync) {
            if (!addInvestmentasync.isCancelled()) {
                addInvestmentasync.cancel(true);
            }
        }
    }
}