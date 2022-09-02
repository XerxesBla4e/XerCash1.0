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
import android.provider.ContactsContract;
import android.view.MenuItem;

import com.example.xercash10.Adapters.InvestmentAdapter;
import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.Investment;
import com.example.xercash10.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class InvestmentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private InvestmentAdapter investmentAdapter;
    private DatabaseHelper databaseHelper;
    private GetInvestments getInvestments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);

        initViews();

        initBottomNavView();

        investmentAdapter = new InvestmentAdapter(this);
        recyclerView.setAdapter(investmentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        getInvestments = new GetInvestments();
        SessionManager sessionManager = new SessionManager(this);
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getInvestments.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != getInvestments) {
            if (!getInvestments.isCancelled()) {
                getInvestments.cancel(true);
            }
        }
    }

    private class GetInvestments extends AsyncTask<Integer, Void, ArrayList<Investment>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Investment> doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("investments", null, "user_id=?",
                    new String[]{String.valueOf(integers[0])}, null, null, "init_date DESC");

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    ArrayList<Investment> investments = new ArrayList<>();
                    do {
                        Investment investment = new Investment();
                        investment.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        investment.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                        investment.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                        investment.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                        investment.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                        investment.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                        investment.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                        investment.setName(cursor.getString(cursor.getColumnIndex("name")));

                        investments.add(investment);
                    } while (cursor.moveToNext());

                    cursor.close();
                    db.close();
                    return investments;
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
        protected void onPostExecute(ArrayList<Investment> investments) {
            super.onPostExecute(investments);
            if (null != investments) {
                investmentAdapter.setInvestments(investments);
            } else {
                investmentAdapter.setInvestments(new ArrayList<Investment>());
            }
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.investmentRecView);
        bottomNavigationView = findViewById(R.id.bottomNavView);
    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.item_investment);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_stats:
                        Intent x9 = new Intent(InvestmentActivity.this, StatsActivity.class);
                        x9.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x9);
                        break;
                    case R.id.item_transaction:
                        Intent x8 = new Intent(InvestmentActivity.this, TransactionActivity.class);
                        x8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x8);
                        break;
                    case R.id.item_loan:
                        Intent x6 = new Intent(InvestmentActivity.this,LoanActivity.class);
                        x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x6);
                        break;
                    case R.id.item_investment:
                        break;
                    case R.id.item_home:
                        Intent x2 = new Intent(InvestmentActivity.this, MainActivity.class);
                        x2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x2);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }
}