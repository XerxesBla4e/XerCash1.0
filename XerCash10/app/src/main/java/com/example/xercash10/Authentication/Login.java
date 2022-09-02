package com.example.xercash10.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.MainActivity;
import com.example.xercash10.Models.User;
import com.example.xercash10.R;
import com.example.xercash10.SessionManager;
import com.example.xercash10.webview.websiteActivity;
import android.view.WindowManager;

import java.sql.SQLException;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword;
    private String semail, spass;
    private TextView txtWarning, txtLicence, txtRegister;
    private Button btnLogin;
    private DatabaseHelper databaseHelper;
    private LoginUser loginUser;
    private DoesEmailExist doesEmailExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        initViews();

        btnLogin.setOnClickListener(this);
        txtRegister.setOnClickListener(this);
        txtLicence.setOnClickListener(this);
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTxtEmail);
        editTextPassword = findViewById(R.id.editTxtPassword);
        txtWarning = findViewById(R.id.txtWarning);
        txtLicence = findViewById(R.id.txtLicence);
        txtRegister = findViewById(R.id.txtregister);
        btnLogin = findViewById(R.id.btnlogin);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnlogin:
                initLogin();
                break;
            case R.id.txtLicence:
                Intent xer1 = new Intent(Login.this, websiteActivity.class);
                startActivity(xer1);
                break;
            case R.id.txtregister:
                Intent xer = new Intent(Login.this, Register.class);
                startActivity(xer);
                break;
        }
    }

    private void initLogin() {
        semail = editTextEmail.getText().toString();
        spass = editTextPassword.getText().toString();

        if (validFields()) {
            doesEmailExist = new DoesEmailExist();
            doesEmailExist.execute(semail);
        }
    }

    private boolean validFields() {
        semail = editTextEmail.getText().toString();
        spass = editTextPassword.getText().toString();

        if (!semail.equals("")) {
            if (!spass.equals("")) {
                txtWarning.setVisibility(View.GONE);
                return true;
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Please Enter Password");
                return false;
            }
        } else {
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Please enter Email");
            return false;
        }
    }

    private class DoesEmailExist extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            databaseHelper = new DatabaseHelper(getApplicationContext());
        }

        @SuppressLint("Range")
        @Override
        protected Boolean doInBackground(String... strings) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("users", new String[]{"_id", "email"}, "email=?",
                    new String[]{strings[0]}, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    if (cursor.getString(cursor.getColumnIndex("email")).equals(strings[0])) {
                        cursor.close();
                        db.close();
                        return true;
                    } else {
                        cursor.close();
                        db.close();
                        return false;
                    }
                } else {
                    cursor.close();
                    db.close();
                    return false;
                }
            } else {
                db.close();
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                loginUser = new LoginUser();
                loginUser.execute();

            } else {
                txtWarning.setVisibility(View.GONE);
                txtWarning.setText("User Doesn't Exist");
            }
        }
    }

    private class LoginUser extends AsyncTask<Void, Void, User> {

        private String semail;
        private String spass;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.semail = editTextEmail.getText().toString();
            this.spass = editTextPassword.getText().toString();
        }

        @SuppressLint("Range")
        @Override
        protected User doInBackground(Void... voids) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("users", null, "email=? AND password=?",
                    new String[]{semail, spass}, null, null, null);

            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    User user = new User();
                    user.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                    user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                    user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                    user.setFirst_name(cursor.getString(cursor.getColumnIndex("first_name")));
                    user.setLast_name(cursor.getString(cursor.getColumnIndex("last_name")));
                    user.setAddress(cursor.getString(cursor.getColumnIndex("image_url")));
                    user.setImage_url(cursor.getString(cursor.getColumnIndex("address")));
                    user.setRemained_amount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));

                    cursor.close();
                    db.close();
                    return user;
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
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if (null != user) {
                SessionManager sessionManager = new SessionManager(Login.this);
                sessionManager.addUser(user);

                Intent xer = new Intent(Login.this, MainActivity.class);
                xer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(xer);
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Your password is incorrect");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != doesEmailExist) {
            if (!doesEmailExist.isCancelled()) {
                doesEmailExist.cancel(true);
            }
        }
        
        if (null != loginUser) {
            if (!loginUser.isCancelled()) {
                loginUser.cancel(true);
            }
        }
    }
}