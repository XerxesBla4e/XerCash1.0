package com.example.xercash10;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.xercash10.Database.DatabaseHelper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InvestmentWorker extends Worker {
    private DatabaseHelper databaseHelper;

    public InvestmentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        Data data = getInputData();
        double amount = data.getDouble("amount", 0.0);
        String recipient = data.getString("recipient");
        String description = data.getString("description");
        int user_id = data.getInt("user_id", -1);
        String type = "profit";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());

        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("recipient", recipient);
        values.put("description", description);
        values.put("user_id", user_id);
        values.put("type", type);
        values.put("date", date);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long id = db.insert("transactions", null, values);

        if (id != -1) {
            Cursor cursor = db.query(
                    "users", new String[]{"remained_amount"},
                    "_id=?", new String[]{String.valueOf(user_id)},
                    null, null, null
            );
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    double currentAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                    cursor.close();
                    ContentValues newValues = new ContentValues();
                    newValues.put("remained_amount", currentAmount - amount);
                    int updatetable = db.update("users", newValues, "_id=?",
                            new String[]{String.valueOf(user_id)});

                } else {
                    cursor.close();
                    db.close();
                    return Result.failure();
                }
            } else {
                db.close();
                return Result.failure();
            }
        }
        db.close();
        return Result.success();
    }
}
