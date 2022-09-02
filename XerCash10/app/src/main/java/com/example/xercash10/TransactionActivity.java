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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.xercash10.Adapters.TransactionAdapter;
import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.Models.Transaction;
import com.example.xercash10.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioGroup rgType;
    private Button btnSearch;
    private EditText edtTxtMin;
    private RecyclerView transactionRecView;
    private TextView txtNoTransaction;
    public BottomNavigationView bottomNavigationView;
    private TransactionAdapter adapter;
    private DatabaseHelper databaseHelper;
    private GetTransactions getTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initViews();
        initBottomNavView();

        adapter = new TransactionAdapter();

        transactionRecView.setAdapter(adapter);
        transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);
        initSearch();

        clickables();
    }

    private void clickables() {
        btnSearch.setOnClickListener(this);
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                initSearch();
            }
        });
    }

    private void initSearch() {
        SessionManager sessionManager = new SessionManager(this);
        User user = sessionManager.isUserLoggedIn();
        if (null != user) {
            getTransactions = new GetTransactions();
            getTransactions.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != getTransactions) {
            if (!getTransactions.isCancelled()) {
                getTransactions.cancel(true);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSearch:
                initSearch();
                break;
            default:
                break;
        }
    }

    private class GetTransactions extends AsyncTask<Integer, Void, ArrayList<Transaction>> {

        private String type = "all";
        private double min = 0.0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.min = Double.valueOf(edtTxtMin.getText().toString());
            switch (rgType.getCheckedRadioButtonId()) {
                case R.id.rbInvestment:
                    type = "investment";
                    break;
                case R.id.rbLoan:
                    type = "loan";
                    break;
                case R.id.rbLoanPayment:
                    type = "loan_payment";
                    break;
                case R.id.rbProfit:
                    type = "profit";
                    break;
                case R.id.rbShopping:
                    type = "shopping";
                    break;
                case R.id.rbSend:
                    type = "send";
                    break;
                case R.id.rbReceive:
                    type = "receive";
                    break;
                default:
                    type = "all";
                    break;
            }
        }

        @SuppressLint("Range")
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor;

            if (type.equals("all")) {
                cursor = db.query("transactions", null, "user_id=?",
                        new String[]{String.valueOf(integers[0])}, null, null, "date DESC");
            } else {
                cursor = db.query("transactions", null,
                        "type=? AND user_id=?", new String[]{type, String.valueOf(integers[0])}, null,
                        null, "date DESC");
            }

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

                        double absAmount = transaction.getAmount();
                        if (absAmount < 0) {
                            absAmount = -absAmount;
                        }

                        if (absAmount > this.min) {
                            transactions.add(transaction);
                        }
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
                txtNoTransaction.setVisibility(View.GONE);
                adapter.setTransactions(transactions);
            } else {
                txtNoTransaction.setVisibility(View.VISIBLE);
                adapter.setTransactions(new ArrayList<Transaction>());
            }
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.botttomNavView1);
        transactionRecView = findViewById(R.id.transactionRecView);
        rgType = findViewById(R.id.rgType);
        btnSearch = findViewById(R.id.btnSearch);
        edtTxtMin = findViewById(R.id.edtTxtMin);
        txtNoTransaction = findViewById(R.id.txtNoTransaction);

    }

    private void initBottomNavView() {
        bottomNavigationView.setSelectedItemId(R.id.item_transaction);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_stats:
                        Intent x5 = new Intent(TransactionActivity.this, StatsActivity.class);
                        x5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x5);
                        break;
                    case R.id.item_transaction:
                        break;
                    case R.id.item_loan:
                        Intent x4 = new Intent(TransactionActivity.this, LoanActivity.class);
                        x4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x4);
                        break;
                    case R.id.item_investment:
                        Intent x2 = new Intent(TransactionActivity.this, InvestmentActivity.class);
                        x2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x2);
                        break;
                    case R.id.item_home:
                        Intent x6 = new Intent(TransactionActivity.this, MainActivity.class);
                        x6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(x6);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }
}