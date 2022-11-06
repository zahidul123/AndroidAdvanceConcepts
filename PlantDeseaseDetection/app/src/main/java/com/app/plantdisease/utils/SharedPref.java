package com.app.plantdisease.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", false);
    }

    public Boolean getIsJustify() {
        return sharedPreferences.getBoolean("justify", false);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public void setIsJustify(Boolean isJustify) {
        editor.putBoolean("justify", isJustify);
        editor.apply();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public Integer getFontSize() {
        return sharedPreferences.getInt("font_size", 2);
    }

    public void updateFontSize(int font_size) {
        editor.putInt("font_size", font_size);
        editor.apply();
    }

}
