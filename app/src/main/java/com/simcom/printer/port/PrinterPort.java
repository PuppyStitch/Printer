package com.simcom.printer.port;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

public class PrinterPort {

    private static final String TAG = "PrinterPort";

    private UsbDevice usbDevice;
    private UsbInterface usbInterface;
    private UsbEndpoint epBulkOut;
    private UsbEndpoint epBulkIn;
    private UsbEndpoint epIntEndpointOut;
    private UsbEndpoint epIntEndpointIn;

    private UsbDeviceConnection usbDeviceConnection;

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private Context context;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public PrinterPort(Context context, UsbDeviceConnection connection, UsbEndpoint inPoint,
                       UsbEndpoint outPoint) {
        this.context = context;
        usbDeviceConnection = connection;
        epBulkIn = inPoint;
        epBulkOut = outPoint;
    }

    public void setConnection(UsbDeviceConnection connection) {
        Log.d(TAG, "setConnection " + connection);
        usbDeviceConnection = connection;
    }

    public void setUsbEndPoint(UsbEndpointData point) {
        epBulkOut = point.getOut();
        epBulkIn = point.getIn();
    }

    public int sendMsg(byte[] data) {
        return usbDeviceConnection.bulkTransfer(epBulkOut, data, data.length, 0);
    }

    public byte[] readMsg() {
        byte[] rec = new byte[64];
        usbDeviceConnection.bulkTransfer(epBulkIn, rec, rec.length, 50);
        return rec;
    }

    private String byteToHexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexCode[(b >> 4) & 0xF]);
        sb.append(hexCode[(b) & 0xF]);
        return sb.toString();
    }

    public interface InterfaceListener {
        void setConnection(UsbDeviceConnection connection);
        void setUsbEndpointData(UsbEndpointData data);
    }

}
