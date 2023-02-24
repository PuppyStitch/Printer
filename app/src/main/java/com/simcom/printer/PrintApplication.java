package com.simcom.printer;

import android.app.Activity;
import android.app.Application;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.simcom.printer.port.PrinterFinder;
import com.simcom.printer.port.PrinterPort;
import com.simcom.printer.port.UsbEndpointData;

public class PrintApplication extends Application implements Application.ActivityLifecycleCallbacks, PrinterPort.InterfaceListener {

    private static final String TAG = "PrintApplication";

    private PrinterFinder printerFinder = null;
    private PrinterPort printerPort = null;

    private UsbDevice usbDevice;
    private UsbDeviceConnection usbDeviceConnection;

    private static PrintApplication printApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        printApplication = this;
    }

    public static PrintApplication getInstance() {
        return printApplication;
    }

    public void init() {
        printerFinder = new PrinterFinder(this, this);
        printerFinder.registerBroadcast();
        printerFinder.enumerateDevices();
        UsbEndpointData usbEndpointData = printerFinder.assignEndpoint();
        usbDeviceConnection = printerFinder.openDevice();

        printerPort = new PrinterPort(this, usbDeviceConnection, usbEndpointData.getIn(),
                usbEndpointData.getOut());
    }



    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public PrinterPort getPrinterPort() {
        return printerPort;
    }

    @Override
    public void setConnection(UsbDeviceConnection connection) {
        printerPort.setConnection(connection);
    }

    @Override
    public void setUsbEndpointData(UsbEndpointData data) {
        printerPort.setUsbEndPoint(data);
    }
}
