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

public class LoanWorker extends Worker {
    private DatabaseHelper databaseHelper;

    public LoanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        int loanId = data.getInt("loan_id", -1);
        int userId = data.getInt("user_id", -1);
        double monthlyPayment = data.getDouble("monthly_payment", 0.0);
        String name = data.getString("name");
        if (loanId == -1 || userId == -1 || monthlyPayment == 0.0) {
            return Result.failure();
        }
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("amount", -monthlyPayment);
        contentValues.put("user_id", userId);
        contentValues.put("type", "loan_payment");
        contentValues.put("description", "Monthly payment for " + name + "loan");
        contentValues.put("recipient", name);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        contentValues.put("date", date);

        long trans_id = db.insert("transactions", null, contentValues);

        if (trans_id == -1) {
            return Result.failure();
        } else {
            Cursor cursor = db.query(
                    "users", new String[]{"remained_amount"},
                    "_id=?", new String[]{String.valueOf(userId)},
                    null, null, null
            );
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    double currentAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                    ContentValues newValues = new ContentValues();
                    newValues.put("remained_amount", currentAmount - monthlyPayment);
                    db.update("users", newValues, "_id=?",
                            new String[]{String.valueOf(userId)});
                    cursor.close();

                    Cursor cursor1 = db.query("loans",
                            new String[]{"remained_amount"},
                            "_id=?", new String[]{String.valueOf(loanId)},
                            null, null, null);
                    if (null != cursor1) {
                        if (cursor1.moveToFirst()) {
                            @SuppressLint("Range")
                            double currentLoanAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                            ContentValues newValues1 = new ContentValues();
                            newValues.put("remained_amount", currentLoanAmount - monthlyPayment);
                            db.update("loans", newValues1, "_id=?",
                                    new String[]{String.valueOf(userId)});
                            cursor1.close();
                            db.close();
                            return Result.success();
                        } else {
                            cursor1.close();
                            db.close();
                            return Result.failure();
                        }
                    } else {
                        db.close();
                        return Result.failure();
                    }

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
    }
}