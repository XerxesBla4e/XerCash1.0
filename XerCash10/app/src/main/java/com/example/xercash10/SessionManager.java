package com.example.xercash10;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.xercash10.Authentication.Login;
import com.example.xercash10.Authentication.Register;
import com.example.xercash10.Models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class SessionManager {
    private Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String SESSION_STRING = "user_logged_in";

    public SessionManager(Context context) {
        this.context = context;
    }

    public void addUser(User user) {
        sharedPreferences = context.getSharedPreferences(SESSION_STRING, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        editor.putString("user", gson.toJson(user));
        editor.apply();
    }

    public User isUserLoggedIn() {
        sharedPreferences = context.getSharedPreferences(SESSION_STRING, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        return gson.fromJson(sharedPreferences.getString("user", null), type);


    }

    public void signOutUser() {
        sharedPreferences = context.getSharedPreferences(SESSION_STRING, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        editor.remove("user");
        editor.commit();

        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
