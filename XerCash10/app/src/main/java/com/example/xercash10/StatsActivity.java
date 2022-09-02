package com.example.xercash10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.Loan;
import com.example.xercash10.Models.Transaction;
import com.example.xercash10.Models.User;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StatsActivity extends AppCompatActivity {
    private BarChart barChart;
    private PieChart pieChart;
    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private GetTransactions getTransactions;
    private GetLoans getLoans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        initViews();

        initBottomNavView();

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getTransactions = new GetTransactions();
            getTransactions.execute(user.get_id());
            getLoans = new GetLoans();
            getLoans.execute(user.get_id());
        }

    }

    private class GetTransactions extends AsyncTask<Integer, Void, ArrayList<Transaction>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("transactions", null, null, null, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Transaction> transactions = new ArrayList<>();
                    do {
                        Transaction transaction = new Transaction();
                        transaction.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        transaction.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                        transaction.setDate(cursor.getString(cursor.getColumnIndex("date")));
                        transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                        transaction.setRecipient(cursor.getString(cursor.getColumnIndex("recipient")));
                        transaction.setType(cursor.getString(cursor.getColumnIndex("type")));
                        transaction.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));

                        transactions.add(transaction);
                    } while (cursor.moveToNext());
                    cursor.close();
                    db.close();
                    return transactions;
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
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);

            if (null != transactions) {
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                ArrayList<BarEntry> entries = new ArrayList<>();
                for (Transaction t : transactions) {
                    try {
                        Date date = sdf.parse(t.getDate());
                        calendar.setTime(date);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);
                        int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;

                        if (month == currentMonth && year == currentYear) {
                            boolean dayExists = false;

                            for (BarEntry e : entries) {
                                if (e.getX() == day) {
                                    dayExists = true;
                                } else {
                                    dayExists = false;
                                }
                            }

                            if (dayExists) {
                                for (BarEntry e : entries) {
                                    if (e.getX() == day) {
                                        e.setY(e.getY() + (float) t.getAmount());
                                    }
                                }
                            } else {
                                entries.add(new BarEntry(day, (float) t.getAmount()));
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Account Activity");
                dataSet.setColor(Color.GREEN);
                BarData data = new BarData(dataSet);

                barChart.getAxisRight().setEnabled(false);
                XAxis xAxis = barChart.getXAxis();
                xAxis.setAxisMaximum(31);
                xAxis.setEnabled(false);
                YAxis yAxis = barChart.getAxisLeft();
                yAxis.setAxisMinimum(10);
                yAxis.setDrawGridLines(false);
                barChart.setData(data);
                Description description = new Description();
                description.setText("All of my transactions");
                description.setTextSize(12f);
                barChart.setDescription(description);
                barChart.invalidate();
            }
        }
    }

    private class GetLoans extends AsyncTask<Integer, Void, ArrayList<Loan>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Loan> doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("loans", null, null, null, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Loan> loans = new ArrayList<>();
                    do {
                        Loan loan = new Loan();
                        loan.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        loan.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                        loan.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                        loan.setName(cursor.getString(cursor.getColumnIndex("name")));
                        loan.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                        loan.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                        loan.setInit_amount(cursor.getDouble(cursor.getColumnIndex("init_amount")));
                        loan.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("roi")));
                        loan.setMonthly_payment(cursor.getDouble(cursor.getColumnIndex("monthly_payment")));
                        loan.setRemained_amount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));

                        loans.add(loan);
                    } while (cursor.moveToNext());
                    cursor.close();
                    db.close();
                    return loans;
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
        protected void onPostExecute(ArrayList<Loan> loans) {
            super.onPostExecute(loans);

            if (null != loans) {
                ArrayList<PieEntry> entries = new ArrayList<>();
                double totalLoansAmount = 0.0;
                double totalRemainedAmount = 0.0;

                for (Loan l : loans) {
                    totalLoansAmount += l.getInit_amount();
                    totalRemainedAmount += l.getRemained_amount();
                }
                entries.add(new PieEntry((float) totalLoansAmount, "Total Loans"));
                entries.add(new PieEntry((float) totalRemainedAmount, "Remained Loans"));
                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                dataSet.setSliceSpace(4f);
                PieData data = new PieData(dataSet);
                pieChart.setDrawHoleEnabled(false);
                pieChart.animateY(1500, Easing.EaseInOutBounce);
                pieChart.setData(data);
                pieChart.invalidate();
            }
        }
    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.item_stats);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_stats:
                        break;
                    case R.id.item_transaction:
                        Intent x6 = new Intent(StatsActivity.this, TransactionActivity.class);
                        x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x6);
                        break;
                    case R.id.item_loan:
                        Intent x4 = new Intent(StatsActivity.this, LoanActivity.class);
                        x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x4);
                        break;
                    case R.id.item_investment:
                        Intent x2 = new Intent(StatsActivity.this, InvestmentActivity.class);
                        x2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x2);
                        break;
                    case R.id.item_home:
                        Intent x8 = new Intent(StatsActivity.this, MainActivity.class);
                        x8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x8);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void initViews() {
        barChart = findViewById(R.id.barChartActivities);
        pieChart = findViewById(R.id.pieChartLoans);
        bottomNavigationView = findViewById(R.id.bottomNavView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != getTransactions) {
            if (!getTransactions.isCancelled()) {
                getTransactions.cancel(true);
            }
        }

        if (null != getLoans) {
            if (!getLoans.isCancelled()) {
                getLoans.cancel(true);
            }
        }
    }
}