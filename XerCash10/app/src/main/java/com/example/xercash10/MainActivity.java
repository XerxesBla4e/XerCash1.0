package com.example.xercash10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xercash10.Adapters.TransactionAdapter;
import com.example.xercash10.Authentication.Login;
import com.example.xercash10.Authentication.Register;
import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Dialogs.AddTransactionDialog;
import com.example.xercash10.Models.Shopping;
import com.example.xercash10.Models.Transaction;
import com.example.xercash10.Models.User;
import com.example.xercash10.webview.websiteActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView transactionRecView;
    private BarChart barChart;
    private LineChart lineChart;
    private FloatingActionButton floatingActionButton;
    private Toolbar toolbar;
    private TextView amount, welcome;
    private DatabaseHelper databaseHelper;
    private GetAccountAmount getAccountAmount;
    private GetTransaction getTransactions;
    private GetSpending getSpending;
    private static final String TAG = "MainActivity";

    private TransactionAdapter adapter;
    private GetProfit getProfit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initBottomNavView();

        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        databaseHelper = new DatabaseHelper(getApplicationContext());

        setupAmount();

        clickables();
        initTransactionRecView();
        initLineChart();
        initBarChart();
    }

    private void initBarChart() {
        getSpending = new GetSpending();
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getSpending.execute(user.get_id());
        }
    }

    private void initLineChart() {
        getProfit = new GetProfit();
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getProfit.execute(user.get_id());
        }
    }

    private void initTransactionRecView() {
        adapter = new TransactionAdapter();
        transactionRecView.setAdapter(adapter);
        transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        getTransactions();
    }

    private void getTransactions() {
        getTransactions = new GetTransaction();
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getTransactions.execute(user.get_id());
        }
    }

    private void clickables() {
        welcome.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupAmount();
        getTransactions();
        initLineChart();
        initBarChart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupAmount();
        getTransactions();
        initLineChart();
        initBarChart();
    }

    private void setupAmount() {
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getAccountAmount = new GetAccountAmount();
            getAccountAmount.execute(user.get_id());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtWelcome:
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("XerCash")
                                .setMessage("Created and Developed By XerxesCodes54")
                                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).setPositiveButton("Visit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent xer54 = new Intent(MainActivity.this, WebView.class);
                                startActivity(xer54);
                            }
                        });
                builder.show();
                break;
            case R.id.fbAddTransaction:
                AddTransactionDialog addTransactionDialog = new AddTransactionDialog();
                addTransactionDialog.show(getSupportFragmentManager(), "add transaction dialog");
                break;
            default:
        }
    }

    private class GetAccountAmount extends AsyncTask<Integer, Void, Double> {

        @Override
        protected Double doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("users",
                    new String[]{"remained_amount"}, "_id=?",
                    new String[]{String.valueOf(integers[0])}, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    double amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                    db.close();
                    return amount;
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
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);

            if (null != aDouble) {
                amount.setText(String.valueOf(aDouble) + "Shs");
            } else {
                amount.setText("0.0 Shs");
            }
        }
    }

    private class GetTransaction extends AsyncTask<Integer, Void, ArrayList<Transaction>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {

            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("transactions",
                    null, "user_id=?",
                    new String[]{String.valueOf(integers[0])}, null, null, "date DESC");

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Transaction> transactions = new ArrayList<>();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        Transaction transaction = new Transaction();
                        transaction.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        transaction.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                        transaction.setDate(cursor.getString(cursor.getColumnIndex("date")));
                        transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                        transaction.setRecipient(cursor.getString(cursor.getColumnIndex("recipient")));
                        transaction.setType(cursor.getString(cursor.getColumnIndex("type")));
                        transaction.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));

                        transactions.add(transaction);
                        cursor.moveToNext();
                    }
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
                adapter.setTransactions(transactions);
            } else {
                adapter.setTransactions(new ArrayList<Transaction>());
            }
        }
    }

    private class GetProfit extends AsyncTask<Integer, Void, ArrayList<Transaction>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("transactions", null,
                    "user_id=? AND type=?",
                    new String[]{String.valueOf(integers[0]), "profit"}, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Transaction> transactions =
                            new ArrayList<>();
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
                ArrayList<Entry> entries = new ArrayList<>();

                for (Transaction x : transactions) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(x.getDate());

                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        calendar.setTime(date);
                        int month = calendar.get(Calendar.MONTH) + 1;

                        if (calendar.get(Calendar.YEAR) == year) {
                            boolean doesMonthExist = false;

                            for (Entry x1 : entries) {
                                if (x1.getX() == month) {
                                    doesMonthExist = true;
                                } else {
                                    doesMonthExist = false;
                                }
                            }
                            if (!doesMonthExist) {
                                entries.add(new Entry(month, (float) x.getAmount()));
                            } else {
                                for (Entry e : entries) {
                                    if (e.getX() == month) {
                                        e.setY(e.getY() + (float) x.getAmount());
                                    }
                                }
                            }
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                LineDataSet dataSet = new LineDataSet(entries, "Profit chart");
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(Color.GREEN);
                LineData data = new LineData(dataSet);
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setSpaceMin(1);
                xAxis.setSpaceMax(1);
                xAxis.setAxisMaximum(12);
                xAxis.setEnabled(false);
                YAxis yAxis = lineChart.getAxisRight();
                yAxis.setEnabled(false);
                YAxis leftAxis = lineChart.getAxisLeft();
                leftAxis.setAxisMaximum(100);
                leftAxis.setAxisMinimum(10);
                leftAxis.setDrawGridLines(false);

                lineChart.setDescription(null);
                lineChart.animateY(2000);
                lineChart.setData(data);
                lineChart.invalidate();


            } else {
                Log.d(TAG, "onPostExecute: transaction array list was null");
            }
        }
    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.item_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_stats:
                        Intent x9 = new Intent(MainActivity.this, StatsActivity.class);
                        x9.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x9);
                        break;
                    case R.id.item_transaction:
                        Intent x6 = new Intent(MainActivity.this, TransactionActivity.class);
                        x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x6);
                        break;
                    case R.id.item_loan:
                        Intent x4 = new Intent(MainActivity.this, LoanActivity.class);
                        x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x4);
                        break;
                    case R.id.item_investment:
                        Intent x2 = new Intent(MainActivity.this, InvestmentActivity.class);
                        x2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x2);
                        break;
                    case R.id.item_home:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private class GetSpending extends AsyncTask<Integer, Void, ArrayList<Shopping>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Shopping> doInBackground(Integer... integers) {

            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("shopping", new String[]{"date", "price"}, "user_id=?",
                    new String[]{String.valueOf(integers[0])}, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Shopping> shoppings
                            = new ArrayList<>();
                    do {
                        Shopping shopping = new Shopping();
                        shopping.setDate(cursor.getString(cursor.getColumnIndex("date")));
                        shopping.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                        shoppings.add(shopping);
                    } while (cursor.moveToNext());
                    cursor.close();
                    db.close();
                    return shoppings;
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
        protected void onPostExecute(ArrayList<Shopping> shoppings) {
            super.onPostExecute(shoppings);

            if (null != shoppings) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (Shopping s : shoppings) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(s.getDate());
                        Calendar calendar = Calendar.getInstance();
                        int month = calendar.get(Calendar.MONTH) + 1;
                        calendar.setTime(date);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        if (calendar.get(Calendar.MONTH) + 1 == month) {
                            boolean doesDayExist = false;
                            for (BarEntry e : entries) {
                                if (e.getX() == day) {
                                    doesDayExist = true;
                                } else {
                                    doesDayExist = false;
                                }
                            }

                            if (!doesDayExist) {
                                entries.add(new BarEntry(day, (float) s.getPrice()));
                            } else {
                                for (BarEntry e : entries) {
                                    if (e.getX() == day) {
                                        e.setY(e.getY() + (float) s.getPrice());
                                    }
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Shopping Chart");
                dataSet.setColor(Color.RED);
                BarData data = new BarData(dataSet);

                barChart.getAxisRight().setEnabled(false);
                XAxis xAxis = barChart.getXAxis();
                xAxis.setAxisMaximum(31);
                xAxis.setEnabled(false);
                YAxis yAxis = barChart.getAxisLeft();
                yAxis.setAxisMaximum(50);
                yAxis.setAxisMinimum(10);
                yAxis.setDrawGridLines(false);
                barChart.setData(data);
                barChart.setDescription(null);
                barChart.invalidate();

            } else {

            }
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavView);
        amount = findViewById(R.id.txtAmount);
        welcome = findViewById(R.id.txtWelcome);
        transactionRecView = findViewById(R.id.transactionRecView);
        barChart = findViewById(R.id.dailySpentChart);
        lineChart = findViewById(R.id.profitChart);
        floatingActionButton = findViewById(R.id.fbAddTransaction);
        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != getTransactions) {
            if (!getTransactions.isCancelled()) {
                getTransactions.cancel(true);
            }
        }

        if (null != getAccountAmount) {
            if (!getAccountAmount.isCancelled()) {
                getAccountAmount.cancel(true);
            }
        }

        if (null != getProfit) {
            if (!getProfit.isCancelled()) {
                getProfit.cancel(true);
            }
        }

        if (null != getSpending) {
            if (!getSpending.isCancelled()) {
                getSpending.cancel(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_xer:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("XerxesCodes")
                        .setMessage("Developed by XerxesCodes54")
                        .setNegativeButton("visit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent xer3 = new Intent(MainActivity.this, websiteActivity.class);
                                startActivity(xer3);
                            }
                        }).setPositiveButton("Invite friends", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String message = "Hey,wsap check out this amazing app " +
                                        "\nto help you manage your sente.\nDownload here";

                                Intent xer4 = new Intent(Intent.ACTION_SEND);
                                xer4.putExtra(Intent.EXTRA_TEXT, message);
                                xer4.setType("text/plain");
                                Intent chooserIntent = Intent.createChooser(xer4, "Invite Via..");
                                startActivity(chooserIntent);
                            }
                        });
                builder.show();
                break;
            case R.id.menu_logout:
                User user = sessionManager.isUserLoggedIn();
                if (null != user) {
                    sessionManager.signOutUser();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}