package com.simcom.printer.port;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

public class PrinterFinder {

    private static final String TAG = "PrinterFinder";

    private IntentFilter intentFilter;
    private Context context;

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private PendingIntent permissionIntent;

    private UsbInterface usbInterface;
//    private UsbEndpoint epBulkOut;
//    private UsbEndpoint epBulkIn;
    private UsbEndpointData usbEndpointData;
    private UsbEndpoint epControl;
    private UsbEndpoint epIntEndpointOut;
    private UsbEndpoint epIntEndpointIn;

    private UsbDeviceConnection usbDeviceConnection;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private PrinterPort.InterfaceListener interfaceListener;

    public PrinterFinder(Context context, PrinterPort.InterfaceListener interfaceListener) {
        this.context = context;
        this.interfaceListener = interfaceListener;
        usbEndpointData = new UsbEndpointData();
    }

    public void registerBroadcast() {
        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            if (device.getVendorId() == 6645 && device.getProductId() == 12869) {
                                Log.i(TAG, "received permission callback");
                                getDeviceInterface();
                                assignEndpoint();

                                usbDeviceConnection = usbManager.openDevice(usbDevice);
                                if (usbDeviceConnection.claimInterface(usbInterface, true)) {
                                    Log.i(TAG, "open设备成功");
//                                    interfaceListener.setConnection(usbDeviceConnection);
                                } else {
                                    Log.i(TAG, "open设备失败");
                                }
                            }
                            //call method to set up device communication 打开设备
                            System.out.println("result-->" + device.getVendorId() + ":" + device.getProductId());
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                        System.out.println("result-->permission denied for device ");
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                System.out.println("result-->关闭");
            }
        }
    };

    public void enumerateDevices() {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            System.out.println("result-->" + device.getVendorId() + ":" + device.getProductId());
            if (device.getVendorId() == 6645 && device.getProductId() == 12869) {
                usbDevice = device;             // 获取USBDevice
//                usbManager.requestPermission(usbDevice, permissionIntent);
            }
        }
    }

    public void getDeviceInterface() {
        if (usbDevice != null) {
            System.out.println("result-->:" + usbDevice.getInterfaceCount());
            usbInterface = usbDevice.getInterface(0);
            System.out.println("result-->成功获得设备接口:" + usbInterface.getId());
        }
    }

    public UsbEndpointData assignEndpoint() {
        if (usbInterface != null) {
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                switch (ep.getType()) {
                    case UsbConstants.USB_ENDPOINT_XFER_BULK://块
                        if (UsbConstants.USB_DIR_OUT == ep.getDirection()) {//输出
//                            epBulkOut = ep;
                            usbEndpointData.setOut(ep);
                            System.out.println("result-->Find the BulkEndpointOut," + "index:" + i + "," + "使用端点号：" + ep.getEndpointNumber());
                        } else {
//                            epBulkIn = ep;
                            usbEndpointData.setIn(ep);
                            System.out.println("result-->Find the BulkEndpointIn:" + "index:" + i + "," + "使用端点号：" + ep.getEndpointNumber());
                        }
//                        interfaceListener.setUsbEndpointData(usbEndpointData);
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
        return usbEndpointData;
    }

    public UsbDeviceConnection openDevice() {
        if (usbInterface != null) {//接口是否为null
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
            UsbDeviceConnection conn = null;
            if (usbManager.hasPermission(usbDevice)) {
                //有权限，那么打开
                conn = usbManager.openDevice(usbDevice);
                if (null == conn) {
//                    Toast.makeText(getActivity(), "不能连接到设备", Toast.LENGTH_SHORT).show();
                    return null;
                }
                //打开设备
                if (conn.claimInterface(usbInterface, true)) {
                    usbDeviceConnection = conn;
                    System.out.println("result-->open设备成功！");
                    return usbDeviceConnection;
                    // 到此你的android设备已经连上zigbee设备
                } else {
                    System.out.println("result-->无法打开连接通道。");
//                    Toast.makeText(getActivity(), "无法打开连接通道。", Toast.LENGTH_SHORT).show();
                    conn.close();
                }
            } else {
//                Toast.makeText(getActivity(), "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
        return null;
    }

}
