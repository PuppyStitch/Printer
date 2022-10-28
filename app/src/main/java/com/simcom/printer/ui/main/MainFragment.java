package com.simcom.printer.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.simcom.printer.R;
import com.simcom.printer.databinding.FragmentMainBinding;
import com.simcom.printer.utils.USBUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private MainViewModel mViewModel;

    FragmentMainBinding binding;
    String printName = "/dev/bus/usb/005/084";
    byte[] bytes;

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
    private PendingIntent permissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbDevice device;

    String string="";

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getContext().registerReceiver(usbReceiver, filter);

        binding.message.setOnClickListener(v -> {
            enumeraterDevices();
            //3)查找设备接口
            getDeviceInterface();
            //4)获取设备endpoint
            assignEndpoint();
            //5)打开conn连接通道
            openDevice();
        });

        binding.message1.setOnClickListener(v -> {
            load();
            sendMessageToPoint(string.getBytes(StandardCharsets.UTF_8));
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    private void load() {
        byte[] bs = readFileFromAssets(getActivity(), null, "data.bin");
        int width = 48;
        int height = bs.length / 48;
        bytes = new byte[bs.length + 8];
        bytes[0] = 29;
        bytes[1] = 118;
        bytes[2] = 48;
        bytes[3] = 0;
        bytes[4] = 48;
        bytes[5] = 0;
        bytes[6] = (byte) (height % 256);
        bytes[6] = (byte) (height / 256);
    }

    private byte[] readFileFromAssets(Context context, String groupPath, String filename) {
        byte[] buffer = null;
        AssetManager am = context.getAssets();

        try {
            InputStream inputStream = null;
            if (groupPath != null) {
                inputStream = am.open(groupPath + "/" + filename);
            } else {
                inputStream = am.open(filename);
            }

            int length = inputStream.available();
            buffer = new byte[length];
            inputStream.read(buffer);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return buffer;
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
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

    /**
     * 枚举设备
     */
    public void enumeraterDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            System.out.println("result-->" + device.getVendorId() + ":" + device.getProductId());
            if (device.getVendorId() == 6645 && device.getProductId() == 12869) {
                myUsbDevice = device; // 获取USBDevice
                usbManager.requestPermission(myUsbDevice, permissionIntent);
            }
        }
    }

    private void getDeviceInterface() {
        if (myUsbDevice!=null) {
            System.out.println("result-->:"+myUsbDevice.getInterfaceCount());
            usbInterface = myUsbDevice.getInterface(0);
            System.out.println("result-->成功获得设备接口:" + usbInterface.getId());
        }
    }

    private void assignEndpoint() {
        if(usbInterface!=null){
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                switch (ep.getType()){
                    case UsbConstants.USB_ENDPOINT_XFER_BULK://块
                        if(UsbConstants.USB_DIR_OUT==ep.getDirection()){//输出
                            epBulkOut = ep;
                            System.out.println("result-->Find the BulkEndpointOut," + "index:" + i + "," + "使用端点号："+ epBulkOut.getEndpointNumber());
                        }else{
                            epBulkIn = ep;
                            System.out .println("result-->Find the BulkEndpointIn:" + "index:" + i+ "," + "使用端点号："+ epBulkIn.getEndpointNumber());
                        }
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_CONTROL://控制
                        epControl = ep;
                        System.out.println("result-->find the ControlEndPoint:" + "index:" + i+ "," + epControl.getEndpointNumber());
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_INT://中断
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//输出
                            epIntEndpointOut = ep;
                            System.out.println("result-->find the InterruptEndpointOut:" + "index:" + i + ","  + epIntEndpointOut.getEndpointNumber());
                        }
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                            epIntEndpointIn = ep;
                            System.out.println("result-->find the InterruptEndpointIn:" + "index:" + i + ","+ epIntEndpointIn.getEndpointNumber());
                        }
                        break;
                }
            }
        }
    }

    public void openDevice() {
        if(usbInterface!=null){//接口是否为null
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
            UsbDeviceConnection conn = null;
            if(usbManager.hasPermission(myUsbDevice)){
                //有权限，那么打开
                conn = usbManager.openDevice(myUsbDevice);
                if(null==conn){
                    Toast.makeText(getActivity(),"不能连接到设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                //打开设备
                if(conn.claimInterface(usbInterface,true)){
                    myDeviceConnection = conn;
                    // 到此你的android设备已经连上zigbee设备
                    System.out.println("result-->open设备成功！");
                } else {
                    System.out.println("result-->无法打开连接通道。");
                    Toast.makeText(getActivity(),"无法打开连接通道。",Toast.LENGTH_SHORT).show();
                    conn.close();
                }
            }else {
                Toast.makeText(getActivity(),"没有权限",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendMessageToPoint(byte[] buffer) {
        int i = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
        System.out.println("result-->:::"+i);
        if( i>= 0){
            //0 或者正数表示成功
            System.out.println("发送成功");
        } else {
            System.out.println("发送失败");
        }
    }

}