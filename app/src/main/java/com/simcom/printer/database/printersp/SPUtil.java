package com.simcom.printer.database.printersp;

import android.content.Context;

public class SPUtil {

    private final static String PRE_PRINT_START_TIME = "pre_print_start_time";
    private final static String PRE_PRINT_LAST_TIME = "pre_print_last_time";

    public static void setPrintStartTime(Context context, long time) {
        PrinterSP.putLong(PRE_PRINT_START_TIME, time, context);
    }

    public static long getPrintStartTime(Context context) {
        return PrinterSP.getLong(PRE_PRINT_START_TIME, 0L, context);
    }

    public static void setPrePrintLastTime(Context context, long time) {
        PrinterSP.putLong(PRE_PRINT_LAST_TIME, time, context);
    }

    public static long getPrintLastTime(Context context) {
        return PrinterSP.getLong(PRE_PRINT_START_TIME, 0L, context);
    }

}
