package com.simcom.printer.database.printersp;

import android.content.Context;
import android.content.SharedPreferences;

public class PrinterSP {

    private static String FILENAME = "config";// 文件名称
    private static SharedPreferences mSharedPreferences = null;

    public static synchronized SharedPreferences getInstance(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getApplicationContext().getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    public static void putBoolean(String key, boolean value, Context context) {
        PrinterSP.getInstance(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defValue, Context context) {
        return PrinterSP.getInstance(context).getBoolean(key, defValue);
    }

    public static void putString(String key, String value, Context context) {
        PrinterSP.getInstance(context).edit().putString(key, value).apply();
    }

    public static String getString(String key, String defValue, Context context) {
        return PrinterSP.getInstance(context).getString(key, defValue);
    }

    public static void putInt(String key, int value, Context context) {
        PrinterSP.getInstance(context).edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int defValue, Context context) {
        return PrinterSP.getInstance(context).getInt(key, defValue);
    }

    public static void putLong(String key, long value, Context context) {
        PrinterSP.getInstance(context).edit().putLong(key, value).apply();
    }

    public static long getLong(String key, long defValue, Context context) {
        return PrinterSP.getInstance(context).getLong(key, defValue);
    }


}
