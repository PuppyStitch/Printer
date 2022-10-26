package com.simcom.printer.utils;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class USBUtil {

    private UsbManager usbManager;
    private Context mContext;

    private String ACTION_USB_PERMISSION = "action_usb_permission";

    private Map<String, UsbEndpoint> mUsbEndpointOutMap, mUsbEndpointInMap;
    private Map<String, UsbDeviceConnection> mUsbDeviceConnectionMap;

    private int SENDING_TIME_OUT = 2000;
    private int CODE_ERROR = 10000;
    private int CODE_SUCCESS = 10001;

    public void init(Context context) {
        mContext = context;
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mUsbEndpointOutMap = new HashMap();
        mUsbEndpointInMap = new HashMap();
    }

    public UsbDevice getDeviceByName(String deviceName) {
        HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceMap.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            if (deviceName.equals(device.getDeviceName())) {
                return device;
            }
        }
        return null;
    }

    public void applyPermission(UsbDevice usbDevice) {
        if (!usbManager.hasPermission(usbDevice)) {
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            mContext.registerReceiver(mUsbPermissionReceiver, filter);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
                        false);
                if (granted) {
                    usbDeviceInit(device);
                }
            }
        }
    };

    public void usbDeviceInit(UsbDevice device) {
        int interfaceCount = device.getInterfaceCount();
        UsbInterface usbInterface = null;
        for (int i = 0; i < interfaceCount; i++) {
            usbInterface = device.getInterface(i);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                break;
            }
        }
        if (usbInterface != null) {
            //获取UsbDeviceConnection
            UsbDeviceConnection connection = usbManager.openDevice(device);
            if (connection != null) {
                if (connection.claimInterface(usbInterface, true)) {
                    for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                        UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                        //类型为大块传输
                        if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                                mUsbEndpointOutMap.put(device.getDeviceName(), endpoint);
                            } else {
                                mUsbEndpointInMap.put(device.getDeviceName(), endpoint);
                            }
                        }
                    }
                }
            }
        }
    }

    public int bulk(String deviceName, ArrayList<Byte> command) {
        if (command != null && !command.isEmpty()) {
            UsbEndpoint endpointOut = mUsbEndpointOutMap.get(deviceName);
            UsbDeviceConnection connection = mUsbDeviceConnectionMap.get(deviceName);
            if (endpointOut == null || connection == null) {
                return CODE_ERROR;
            }
            byte[] data = new byte[command.size()];
            for (int i = 0; i < command.size(); i++) {
                data[i] = command.get(i);
            }
            int ret = connection.bulkTransfer(endpointOut, data, data.length, SENDING_TIME_OUT);
            if (ret >= 0) {
                return CODE_SUCCESS;
            }

            return CODE_ERROR;
        } else {
            return CODE_SUCCESS;
        }

    }

    public void registerStatusBroadReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        mContext.registerReceiver(statusReceiver, filter);
    }

    BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            //TODO 检查一个该device是否是我们的目标设备
//            if (!isTargetDevice(device)) {
//                return;
//            }
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

                usbDeviceInit(device);
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

                /*TODO 释放UsbInterface 关闭UsbDeviceConnection
                  mUsbDeviceConnection.releaseInterface(interface);
                  mUsbDeviceConnection.close();
                */
            }
        }
    };
}
