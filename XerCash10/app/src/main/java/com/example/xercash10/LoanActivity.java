package com.example.xercash10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.xercash10.Adapters.LoanAdapter;
import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.Loan;
import com.example.xercash10.Models.Transaction;
import com.example.xercash10.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class LoanActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private LoanAdapter adapter;
    private DatabaseHelper databaseHelper;
    private GetLoans getLoans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);

        initViews();

        initBottomNavView();

        adapter = new LoanAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getLoans = new GetLoans();
            getLoans.execute(user.get_id());
        }
    }

    private class GetLoans extends AsyncTask<Integer, Void, ArrayList<Loan>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Loan> doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("loans", null,
                    "user_id=?", new String[]{String.valueOf(integers[0])}, null, null, "init_date DESC");

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
                adapter.setLoans(loans);
            } else {
                adapter.setLoans(new ArrayList<Loan>());
            }
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.loanRecView);
        bottomNavigationView = findViewById(R.id.bottomNavView);
    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.item_loan);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_stats:
                        Intent x0 = new Intent(LoanActivity.this, StatsActivity.class);
                        x0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x0);
                        break;
                    case R.id.item_transaction:
                        Intent x9 = new Intent(LoanActivity.this, TransactionActivity.class);
                        x9.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x9);
                        break;
                    case R.id.item_loan:
                        Intent x6 = new Intent(LoanActivity.this, LoanActivity.class);
                        x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x6);
                        break;
                    case R.id.item_investment:
                        Intent x2 = new Intent(LoanActivity.this, InvestmentActivity.class);
                        x2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x2);
                        break;
                    case R.id.item_home:
                        Intent x3 = new Intent(LoanActivity.this, MainActivity.class);
                        x3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x3);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != getLoans) {
            if (!getLoans.isCancelled()) {
                getLoans.cancel(true);
            }
        }
    }
}