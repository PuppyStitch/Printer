package com.simcom.printer.ui.main;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.simcom.printer.R;
import com.simcom.printer.poscommand.PrintCMD;
import com.simcom.printer.utils.UpgradeCon;
import com.simcom.printerlib.utils.DataUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;

public class UpgradeActivity extends AppCompatActivity {

    private static final String TAG = "UpgradeActivity";

    private static final int CHOOSE_FILE_CODE = 1000;

    private byte[][] bytes = new byte[256][192];

    /**
     * 满足的设备
     */
    private UsbDevice myUsbDevice;

    /**
     * usb接口
     */
    private UsbInterface usbInterface;

    /**
     * 块输出端点
     */
    private UsbEndpoint epBulkOut;
    private UsbEndpoint epBulkIn;

    /**
     * 控制端点
     */
    private UsbEndpoint epControl;

    /**
     * 中断端点
     */
    private UsbEndpoint epIntEndpointOut;
    private UsbEndpoint epIntEndpointIn;

    /**
     * 连接
     */
    private UsbDeviceConnection myDeviceConnection;

    private UsbManager usbManager;
    private UsbDevice device;

    private Button upgrade;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        upgrade = findViewById(R.id.upgrade_btn);
        mContext = this;
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        enumerateDevices();
        //3)查找设备接口1
        getDeviceInterface();
        //4)获取设备endpoint
        assignEndpoint();

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    openDevice();

                    //sendMessageToPoint(PrintCMD.requestUpdateFW());         // 进入download mode
                    sendData(PrintCMD.requestUpdateFW());
                    Thread.sleep(200);
//                    sendMessageToPoint(PrintCMD.getFirstFrame());           // 发送program
                    sendData(PrintCMD.getFirstFrame());
                    UpgradeCon upgradeCon = new UpgradeCon();
                    upgradeCon.go(DataUtils.readFileFromAssets(mContext, null,
                            "s05.bin"));           // s05_1.2.1.bin
                    Log.d("SEND SIZE", upgradeCon.packages + "");
                    for (int i = 0; i <= upgradeCon.packages; i++) {
//                        sendMessageToPoint(upgradeCon.getBs()[i]);
                        Log.d("Send Index", i + "");
                        sendData(upgradeCon.getBs()[i]);
                        Thread.sleep(50);
                    }
                    Thread.sleep(200);
                    sendData(PrintCMD.getEndFrame());
//                    readMessageFromPoint();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // request permission and connect to device
    }

    @SuppressLint("NewApi")
    public void sendData(byte[] buffer) {
        byte[] receiveBuf = new byte[64];
        if (myDeviceConnection == null)
            return;
        int res = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
        Log.d("Sent", res + "");
        if (res >= 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int readRes = myDeviceConnection.bulkTransfer(epBulkIn, receiveBuf, 64, 50);
            Log.d("Read", readRes + ": " + byteToHexStr(receiveBuf[0]));
            Log.d("Received: ", byteToHexStr(receiveBuf[0]));
        } else {
            Log.d("Send", "send failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                Uri uri = data.getData();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleBytes(byte[] bytes) {

    }

    private void reset() {
        // 升级失败，重置变量

    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public String byteToHexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexCode[(b >> 4) & 0xF]);
        sb.append(hexCode[(b) & 0xF]);
        return sb.toString();
    }

    // 调用系统文件管理器，选择文件
    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Choose File"),
                    CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private byte[] getBytes(String path) {
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(new File(path).toPath());
        } catch (IOException | OutOfMemoryError | SecurityException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void enumerateDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            System.out.println("result-->" + device.getVendorId() + ":" + device.getProductId());
            if (device.getVendorId() == 6645 && device.getProductId() == 12869) {
                myUsbDevice = device; // 获取USBDevice
//                usbManager.requestPermission(myUsbDevice, permissionIntent);
            }
        }
    }

    private void getDeviceInterface() {
        if (myUsbDevice != null) {
            System.out.println("result-->:" + myUsbDevice.getInterfaceCount());
            usbInterface = myUsbDevice.getInterface(0);
            System.out.println("result-->成功获得设备接口:" + usbInterface.getId());
        }
    }

    private void assignEndpoint() {
        if (usbInterface != null) {
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                switch (ep.getType()) {
                    case UsbConstants.USB_ENDPOINT_XFER_BULK://块
                        if (UsbConstants.USB_DIR_OUT == ep.getDirection()) {//输出
                            epBulkOut = ep;
                            System.out.println("result-->Find the BulkEndpointOut," + "index:" + i + "," + "使用端点号：" + epBulkOut.getEndpointNumber());
                        } else {
                            epBulkIn = ep;
                            System.out.println("result-->Find the BulkEndpointIn:" + "index:" + i + "," + "使用端点号：" + epBulkIn.getEndpointNumber());
                        }
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_CONTROL://控制
                        epControl = ep;
                        System.out.println("result-->find the ControlEndPoint:" + "index:" + i + "," + epControl.getEndpointNumber());
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_INT://中断
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//输出
                            epIntEndpointOut = ep;
                            System.out.println("result-->find the InterruptEndpointOut:" + "index:" + i + "," + epIntEndpointOut.getEndpointNumber());
                        }
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                            epIntEndpointIn = ep;
                            System.out.println("result-->find the InterruptEndpointIn:" + "index:" + i + "," + epIntEndpointIn.getEndpointNumber());
                        }
                        break;
                }
            }
        }
    }

    public void openDevice() {
        if (usbInterface != null) {//接口是否为null
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
            UsbDeviceConnection conn = null;
            if (usbManager.hasPermission(myUsbDevice)) {
                //有权限，那么打开
                conn = usbManager.openDevice(myUsbDevice);
                if (null == conn) {
                    Toast.makeText(this, "不能连接到设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                //打开设备
                if (conn.claimInterface(usbInterface, true)) {
                    myDeviceConnection = conn;
                    // 到此你的android设备已经连上zigbee设备
                    System.out.println("result-->open设备成功！");
                } else {
                    System.out.println("result-->无法打开连接通道。");
                    Toast.makeText(this, "无法打开连接通道。", Toast.LENGTH_SHORT).show();
                    conn.close();
                }
            } else {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public synchronized void sendMessageToPoint(byte[] buffer) {
//        myLock.lock();
        int i = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
        System.out.println("send result-->:::" + i);
        Log.e(TAG, "sendMessageToPoint " + i);
//        myLock.unlock();
    }

}