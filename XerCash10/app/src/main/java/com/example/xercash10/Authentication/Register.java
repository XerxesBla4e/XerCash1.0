package com.example.xercash10.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xercash10.Database.DatabaseHelper;
import com.example.xercash10.MainActivity;
import com.example.xercash10.Models.User;
import com.example.xercash10.R;
import com.example.xercash10.SessionManager;
import com.example.xercash10.webview.websiteActivity;


public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword, editTextAddress, editTextName;
    private String semail, spass, saddress, sname;
    private TextView txtWarning, txtlogin, txtLicence;
    private ImageView imageView;
    private Button btnRegister;
    Uri imguri;
    private DatabaseHelper databaseHelper;
    private DoesUserExist doesUserExist;
    private RegisterUser registerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        initViews();

        databaseHelper = new DatabaseHelper(getApplicationContext());

        btnRegister.setOnClickListener(this);
        imageView.setOnClickListener(this);
        txtlogin.setOnClickListener(this);
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.edttextemail);
        editTextPassword = findViewById(R.id.edttextpassword);
        editTextAddress = findViewById(R.id.edttextaddress);
        editTextName = findViewById(R.id.edttextname);

        txtLicence = findViewById(R.id.txtLicence);
        txtWarning = findViewById(R.id.txtWarning);
        txtlogin = findViewById(R.id.txtlogin);

        imageView = findViewById(R.id.logo);
        btnRegister = findViewById(R.id.btnregister);

        txtlogin = findViewById(R.id.txtlogin);
        txtLicence = findViewById(R.id.txtLicence);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnregister:
                initRegister();
                break;
            case R.id.logo:
                pickImage();
                break;
            case R.id.txtlogin:
                Intent xer = new Intent(Register.this, Login.class);
                startActivity(xer);
                break;
            case R.id.txtLicence:
                Intent xer1 = new Intent(Register.this, websiteActivity.class);
                startActivity(xer1);
                break;
        }

    }

    private void pickImage() {
        Intent xerxes = new Intent(Intent.ACTION_GET_CONTENT);
        startActivityForResult(xerxes, 454);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 454 && resultCode == RESULT_OK && data.getData() != null) {
            imguri = data.getData();
            imageView.setImageURI(imguri);
        }
    }

    private boolean validFields() {
        semail = editTextEmail.getText().toString();
        sname = editTextName.getText().toString();
        saddress = editTextAddress.getText().toString();
        spass = editTextPassword.getText().toString();

        if (semail.equals("") || sname.equals("") || saddress.equals("") || spass.equals("")) {
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Please Fill missing Field");
            return false;
        }

        txtWarning.setVisibility(View.GONE);
        return true;
    }

    private class DoesUserExist extends AsyncTask<String, Void, Boolean> {

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
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("User Email Already Exists");
            } else {
                txtWarning.setVisibility(View.GONE);
                registerUser = new RegisterUser();
                registerUser.execute();
            }
        }
    }

    private class RegisterUser extends AsyncTask<Void, Void, User> {
        private String semail;
        private String first_name;
        private String last_name;
        private String saddress;
        private String spass;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String semail = editTextEmail.getText().toString();
            String sname = editTextName.getText().toString();
            String saddress = editTextAddress.getText().toString();
            String spass = editTextPassword.getText().toString();

            this.semail = semail;
            this.spass = spass;
            this.saddress = saddress;

            String[] names = sname.split("");
            if (names.length >= 1) {
                this.first_name = names[0];
                for (int i = 1; i < names.length; i++) {
                    if (i > 1) {
                        last_name += " " + names[i];
                    } else {
                        last_name += names[i];
                    }
                }
            } else {
                this.first_name = names[0];
            }
        }

        @SuppressLint("Range")
        @Override
        protected User doInBackground(Void... voids) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("email", this.semail);
            contentValues.put("password", this.spass);
            contentValues.put("first_name", this.first_name);
            contentValues.put("last_name", this.last_name);
            contentValues.put("address", this.saddress);
            contentValues.put("image_url", String.valueOf(imguri));
            contentValues.put("remained_amount", 0.0);

            long userId = db.insert("users", null, contentValues);

            Cursor cursor = db.query("users", null,
                    "_id=?", new String[]{String.valueOf(userId)}, null, null, null);

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
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if (null != user) {
                Toast.makeText(Register.this, "User" + user.getEmail() + "registered successfully", Toast.LENGTH_SHORT).show();
                SessionManager sessionManager = new SessionManager(Register.this);
                sessionManager.addUser(user);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else {
                Toast.makeText(Register.this, "Oops!! failed to register", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initRegister() {
        semail = editTextEmail.getText().toString();
        sname = editTextName.getText().toString();
        saddress = editTextAddress.getText().toString();
        spass = editTextPassword.getText().toString();

        if (validFields()) {
            doesUserExist = new DoesUserExist();
            doesUserExist.execute(semail);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != doesUserExist) {
            if (!doesUserExist.isCancelled()) {
                doesUserExist.cancel(true);
            }
        }

        if (null != registerUser) {
            if (!registerUser.isCancelled()) {
                registerUser.cancel(true);
            }
        }
    }
}