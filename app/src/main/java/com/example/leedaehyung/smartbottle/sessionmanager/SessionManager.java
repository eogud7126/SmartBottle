package com.example.leedaehyung.smartbottle.sessionmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.se.omapi.Session;

/**
 * Created by Lee DaeHyung on 2019-05-17.
 */

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String REFNAME = "Session";
    private static final String KEY_SESSION = "session";
    private Context context;
    private int Mode =0;

    public SessionManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(REFNAME,Mode);
        editor = sharedPreferences.edit();
    }

    public void createSession(String Sessionval) {
        editor.putString(KEY_SESSION, Sessionval);
        editor.commit();
    }

    public String getse() {
        String result = sharedPreferences.getString(KEY_SESSION, "");
        return result;
    }
}