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

public class AddLoanActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtTxtName, edtTxtInitAmount, edtTxtInitDate, edtTxtFinishDate, edtTxtMonthlyROI, edtTxtMonthlyPayment;
    private Button btnPickInitDate, btnPickFinishDate, btnAddLoan;
    private TextView txtWarning;
    private Calendar initcalendar = Calendar.getInstance();
    private Calendar finishcalendar = Calendar.getInstance();
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private AddTransaction addTransaction;
    private AddLoanAsync addLoanAsync;

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
        setContentView(R.layout.activity_add_loan);

        initViews();

        sessionManager = new SessionManager(getApplicationContext());
        databaseHelper = new DatabaseHelper(getApplicationContext());
        setOnclickListener();
    }

    private void setOnclickListener() {
        btnAddLoan.setOnClickListener(this);
        btnPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddLoanActivity.this,
                        initDate, initcalendar.get(Calendar.YEAR), initcalendar.get(Calendar.MONTH),
                        initcalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddLoanActivity.this,
                        finishDate, finishcalendar.get(Calendar.YEAR), finishcalendar.get(Calendar.MONTH),
                        finishcalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void initViews() {
        edtTxtName = findViewById(R.id.edtTxtLoanname2);
        edtTxtInitAmount = findViewById(R.id.edtTxtinitamount);
        edtTxtInitDate = findViewById(R.id.edtTxtInitDate);
        edtTxtFinishDate = findViewById(R.id.edtTxtFinishDate);
        edtTxtMonthlyROI = findViewById(R.id.edtTxtMonthlyROI);
        edtTxtMonthlyPayment = findViewById(R.id.edtTxtMonthlyPayment);

        btnPickInitDate = findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = findViewById(R.id.btnPickFinishDate);
        btnAddLoan = findViewById(R.id.btnAddLoan);

        txtWarning = findViewById(R.id.txtWarning);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddLoan:
                if (validateFields()) {
                    txtWarning.setVisibility(View.GONE);
                    initAddingLoan();
                } else {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please Fill all fields");
                }
                break;
            default:
                break;
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
        if (edtTxtMonthlyPayment.getText().toString().equals("")) {
            return false;
        }

        return true;
    }

    private void initAddingLoan() {
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());
        }
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Integer> {
        private double amount;
        private String date, name;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.parseDouble(edtTxtInitAmount.getText().toString());
            this.name = edtTxtName.getText().toString();
            this.date = edtTxtInitDate.getText().toString();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("amount", amount);
            values.put("recipient", name);
            values.put("date", date);
            values.put("user_id", integers[0]);
            values.put("description", "Received amount for" + name + "loan");
            values.put("type", "loan");

            long tran_id = db.insert("transactions", null, values);
            db.close();
            return (int) tran_id;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (null != integer) {
                addLoanAsync = new AddLoanAsync();
                addLoanAsync.execute(integer);
            }
        }
    }

    private class AddLoanAsync extends AsyncTask<Integer, Void, Integer> {
        private int userId;
        private String name, initDate, finishDate;
        private double initAmount, monthlyROI, monthlyPayment;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.name = edtTxtName.getText().toString();
            this.initDate = edtTxtInitDate.getText().toString();
            this.finishDate = edtTxtFinishDate.getText().toString();
            this.initAmount = Double.valueOf(edtTxtInitAmount.getText().toString());
            this.monthlyROI = Double.valueOf(edtTxtMonthlyROI.getText().toString());
            this.monthlyPayment = Double.valueOf(edtTxtMonthlyPayment.getText().toString());
            User user = sessionManager.isUserLoggedIn();
            if (null != user) {
                this.userId = user.get_id();
            } else {
                this.userId = -1;
            }
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            if (userId != -1) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("init_date", initDate);
                values.put("finish_date", finishDate);
                values.put("init_amount", initAmount);
                values.put("remained_amount", initAmount);
                values.put("monthly_roi", monthlyROI);
                values.put("monthly_payment", monthlyPayment);
                values.put("user_id", userId);
                values.put("transaction_id", integers[0]);

                long loanid = db.insert("loans", null, values);
                if (loanid != -1) {
                    Cursor cursor = db.query("users",
                            new String[]{"remained_amount"},
                            "_id=?", new String[]{String.valueOf(userId)}, null, null, null);
                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            @SuppressLint("Range")
                            double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                            ContentValues values1 = new ContentValues();
                            values1.put("remained_amount", currentRemainedAmount + initAmount);
                            db.update("users", values1, "_id=?",
                                    new String[]{String.valueOf(userId)});
                            cursor.close();
                            db.close();
                            return (int) loanid;
                        } else {
                            cursor.close();
                            db.close();
                            return null;
                        }
                    } else {
                        db.close();
                        return null;
                    }

                } else {
                    db.close();
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (null != integer) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date initDate = sdf.parse(this.initDate);
                    calendar.setTime(initDate);
                    int initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                    Date finishDate = sdf.parse(this.initDate);
                    calendar.setTime(finishDate);
                    int finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);


                    int months = finishMonth - initMonth;

                    int days = 0;
                    for (int i = 0; i < months; i++) {
                        days += exactDayCount();
                        Data data = new Data.Builder()
                                .putInt("loan_id", integer)
                                .putInt("user_id", userId)
                                .putDouble("monthly_payment", monthlyPayment)
                                .putString("name", name)
                                .build();
                        Constraints constraints = new Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .build();
                        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LoanWorker.class)
                                .setInputData(data)
                                .setConstraints(constraints)
                                .setInitialDelay(days, TimeUnit.DAYS)
                                .addTag("loan_payment")
                                .build();
                        WorkManager.getInstance(AddLoanActivity.this).enqueue(request);

                        Intent xer5 = new Intent(AddLoanActivity.this, MainActivity.class);
                        xer5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(xer5);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
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

        if (null != addLoanAsync) {
            if (!addLoanAsync.isCancelled()) {
                addLoanAsync.cancel(true);
            }
        }
    }
}