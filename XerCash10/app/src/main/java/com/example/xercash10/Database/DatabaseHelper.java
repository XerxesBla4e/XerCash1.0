package com.example.xercash10.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "xer_cash_db";
    private static final int DB_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate: started");
        String createUserTable = "CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT NOT NULL, " + "password TEXT NOT NULL, " +
                "first_name TEXT, last_name TEXT, address TEXT, image_url TEXT, remained_amount DOUBLE)";

        String createShoppingTable = "CREATE TABLE shopping (_id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER, " +
                "user_id INTEGER, transaction_id INTEGER, price DOUBLE, date DATE, description TEXT)";

        String createInvestmentTable = "CREATE TABLE investments (_id INTEGER PRIMARY KEY AUTOINCREMENT, amount DOUBLE, " +
                "monthly_roi DOUBLE, name TEXT, init_date DATE, finish_date DATE, user_id INTEGER, transaction_id INTEGER)";

        String createLoansTable = "CREATE TABLE loans (_id INTEGER PRIMARY KEY AUTOINCREMENT, init_date DATE, " +
                "finish_date DATE, init_amount DOUBLE, remained_amount DOUBLE, monthly_payment DOUBLE, monthly_roi DOUBLE, " +
                "name TEXT, user_id INTEGER, transaction_id INTEGER)";

        String createTransactionTable = "CREATE TABLE transactions (_id INTEGER PRIMARY KEY AUTOINCREMENT, amount DOUBLE, " +
                "date DATE, type TEXT, user_id INTEGER, recipient TEXT, description TEXT)";

        String createItemTable = "CREATE TABLE items (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, image_url TEXT, " +
                "description TEXT)";

        sqLiteDatabase.execSQL(createUserTable);
        sqLiteDatabase.execSQL(createShoppingTable);
        sqLiteDatabase.execSQL(createInvestmentTable);
        sqLiteDatabase.execSQL(createLoansTable);
        sqLiteDatabase.execSQL(createTransactionTable);
        sqLiteDatabase.execSQL(createItemTable);


        addInitItems(sqLiteDatabase);
        addInitTransaction(sqLiteDatabase);
        addTestProfit(sqLiteDatabase);

    }

    private void addTestProfit(SQLiteDatabase xe2){
        Log.d(TAG, "addTestProfit: started");
        ContentValues values1 = new ContentValues();
        values1.put("amount", 5000);
        values1.put("type", "profit");
        values1.put("date", "2022-06-03");
        values1.put("description", "monthly profit from weed");
        values1.put("user_id", 1);
        values1.put("recipient", "Nakawa Estate");

        xe2.insert("transactions",null,values1);

        ContentValues values2 = new ContentValues();
        values2.put("amount", 9000);
        values2.put("type", "profit");
        values2.put("date", "2022-05-09");
        values2.put("description", "monthly profit from weed");
        values2.put("user_id", 1);
        values2.put("recipient", "Kijagwa Estate");

        xe2.insert("transactions",null,values2);

        ContentValues values3 = new ContentValues();
        values3.put("amount", 6500);
        values3.put("type", "profit");
        values3.put("date", "2022-05-10");
        values3.put("description", "monthly profit from weed");
        values3.put("user_id", 1);
        values3.put("recipient", "Nakumati Estate");

        xe2.insert("transactions",null,values3);
    }
    private void addInitTransaction(SQLiteDatabase xe1) {
        Log.d(TAG, "addInitTransaction: started");
        ContentValues values = new ContentValues();
        values.put("_id", 0);
        values.put("amount", 1800000);
        values.put("date", "2022-06-06");
        values.put("type", "shopping");
        values.put("user_id", 1);
        values.put("description", "Gaming and Accesories");
        values.put("recipient", "OvalShop");

        xe1.insert("transactions", null, values);

    }

    private void addInitItems(SQLiteDatabase xe) {
        Log.d(TAG, "addInitItems: started");
        ContentValues values = new ContentValues();
        values.put("name", "PS5");
        values.put("image_url", "data:image/jpeg;base64,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");
        values.put("description", "Experience the thrill of the game");

        xe.insert("items", null, values);
    }

    private Cursor cursor(){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        sqLiteDatabase.execSQL("");

        if(cursor()!=null){

        }
        return cursor();
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
