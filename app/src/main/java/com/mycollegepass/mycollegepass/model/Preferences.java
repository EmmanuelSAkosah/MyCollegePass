package com.mycollegepass.mycollegepass.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    private SharedPreferences mPreferences;
    static final String PREF_LOGGEDIN_USER_EMAIL = "logged_in_email";
    static final String PREF_USER_LOGGEDIN_STATUS = "logged_in_status";

    public Preferences(Context context){
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setLoggedInUserEmail(String email)
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_LOGGEDIN_USER_EMAIL, email);
        editor.commit();
    }

    public String getLoggedInEmailUser()
    {
        return mPreferences.getString(PREF_LOGGEDIN_USER_EMAIL, "");
    }

    public void setUserLoggedInStatus(boolean status)
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(PREF_USER_LOGGEDIN_STATUS, status);
        editor.commit();
    }

    public boolean getUserLoggedInStatus()
    {
        return mPreferences.getBoolean(PREF_USER_LOGGEDIN_STATUS, false);
    }

    public void clearLoggedInEmailAddress()
    {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(PREF_LOGGEDIN_USER_EMAIL);
        editor.remove(PREF_USER_LOGGEDIN_STATUS);
        editor.commit();
    }
}

