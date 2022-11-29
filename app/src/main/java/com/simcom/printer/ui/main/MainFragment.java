package com.simcom.printer.ui.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simcom.printer.databinding.FragmentMainBinding;
import com.simcom.printer.poscommand.PrintCMD;
import com.simcom.printer.utils.PrintUtil;
import com.simcom.printer.utils.Subcontract;
import com.simcom.printerlib.printview.PrinterLayout;
import com.simcom.printerlib.utils.DataUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

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

    String string = "123454654\n" +
            "asdfadf \n" +
            "asdfas f asdf asdfkjasdl;fja \n";

    static final int ALIGN_LEFT = 0;
    static final int ALIGN_CENTER = 1;
    static final int ALIGN_RIGHT = 2;

    static final int DIFFUSE_DITHER = 0;
    static final int THRESHOLD_DITHER = 2;

    int DOTS_PER_LINE = 576;

//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            binding.message1.setEnabled(true);
//        }
//    };

//    Handler mHandler = new Handler();

    static Object lock = new Object();
    private static boolean isSendingData = false;

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


            enumerateDevices();
            //3)查找设备接口1
            getDeviceInterface();
            //4)获取设备endpoint
            assignEndpoint();
            //5)打开conn连接通道
//            sendMessageToPoint(new byte[]{0x1b, 0x61, 0x00});
            openDevice();
        });


        class MyThread extends Thread {
            @Override
            public void run() {

//                super.run();
                synchronized (lock) {
                    try {
                        sendMessageToPoint(bytes);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                sendMessageToPoint(bytes);
//            }
//        };

        binding.message1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.setEnabled(false);

                PrinterLayout.ViewToBitmapListener listener = null;

                CustomCallable customCallable = new CustomCallable();
                FutureTask<Boolean> futureTask = new FutureTask<>(customCallable);
                Thread thread = new Thread(futureTask);

                if (bytes != null) {
                    thread.start();
                } else {

                     listener = new PrinterLayout.ViewToBitmapListener() {
                        @Override
                        public void success(Bitmap bitmap) {
                            bytes = DataUtils.sendBWImage(bitmap, getContext());
                            thread.start();
                        }

                        @Override
                        public void failure() {

                        }
                    };

                    PrintUtil.meiTuan(getContext(), listener);
                }

//            openDevice();


                try {
                    v.setEnabled(futureTask.get());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

//                ExecutorService singleThread = Executors.newSingleThreadExecutor();
//
//                singleThread.submit(new CustomCallable());

//                singleThread.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        sendMessageToPoint(bytes);
//                    }
//                });

//            sendMessageToPoint(string.getBytes(StandardCharsets.UTF_8));
//            sendMessageToPoint(bytes);

//                MyThread newThread = new MyThread();
//                newThread.start();
//                thread.start();


            }
        });

        binding.message2.setOnClickListener(v -> {
//            byte[] bs = new byte[4];
//            bs[0] = 0x1D;
//            bs[1] = 0x56;
//            bs[2] = 0x41;
//            bs[3] = 0x00;
//            sendMessageToPoint(bs);
        });
    }

    class ResultCallable implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            return null;
        }
    }

    public class Lock {
        private boolean isLocked = false;

        public synchronized void lock() throws InterruptedException {
            while (isLocked) {
                wait();
            }
            isLocked = true;
        }

        public synchronized void unlock() {
            isLocked = false;
            notify();
        }
    }


//          打印数据和查询指令建议这样发：
//            1）将需要打印的图片按240点行分成多个段，一次发送一段
//            2）每发送一段图片指令，后面跟一个查询指令：0x10 0x04 0x81
//            3）读取一个字节，如果该字节不是0，表明打印机有异常状态，暂停发送图片数据，但继续周期性地发送查询指令，直到回复的一个字节变为0，再继续发送图片数据

    Lock myLock = new Lock();

    class CustomCallable implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {

            int time;

            synchronized (this) {

                boolean isReceived = true;
                Subcontract subcontract = new Subcontract();
                subcontract.goSubcontract(bytes);

                for (int i = 0; i < subcontract.packageCount; i++) {
                    if (isReceived) {
                        sendMessageToPoint(subcontract.getBytes()[i]);
                    } else {
                        Log.e(TAG, "never receive msg");
                        return false;
                    }

                    isReceived = false;
                    time = 0;

                    while (time < 10000) {
                        if (readMessageFromPoint()) {
                            isReceived = true;
                            break;
                        } else {
                            sendMessageToPoint(PrintCMD.queryStatus());
                            Thread.sleep(200);
                        }
                        time++;
                    }

                    if (!isReceived) {
                        return false;
                    }
                }
                sendMessageToPoint(PrintCMD.cutPaper());
            }
            return true;
        }
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
    public void enumerateDevices() {
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
                    Toast.makeText(getActivity(), "不能连接到设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                //打开设备
                if (conn.claimInterface(usbInterface, true)) {
                    myDeviceConnection = conn;
                    // 到此你的android设备已经连上zigbee设备
                    System.out.println("result-->open设备成功！");
                } else {
                    System.out.println("result-->无法打开连接通道。");
                    Toast.makeText(getActivity(), "无法打开连接通道。", Toast.LENGTH_SHORT).show();
                    conn.close();
                }
            } else {
                Toast.makeText(getActivity(), "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public synchronized void sendMessageToPoint(byte[] buffer) throws InterruptedException {
        myLock.lock();
        int i = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
        System.out.println("send result-->:::" + i);
        myLock.unlock();
    }

    private synchronized boolean readMessageFromPoint() throws InterruptedException {

        myLock.lock();

        boolean isNormal = false;

        int outMax = epBulkOut.getMaxPacketSize();

        int inMax = epBulkIn.getMaxPacketSize();

        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
        UsbRequest usbRequest = new UsbRequest();
        usbRequest.initialize(myDeviceConnection, epBulkIn);
        usbRequest.queue(byteBuffer, inMax);
        if (myDeviceConnection.requestWait() == usbRequest) {
            byte[] retData = byteBuffer.array();
            if (retData[0] == 0) {
                isNormal = true;
            } else if (retData[0] >> 7 == 1) {
                Log.e(TAG, "no paper");
            } else if (retData[0] >> 5 == 1) {
                Log.e(TAG, "over heat");
            }
            Log.e(TAG, "read " + retData[0]);
        }

        myLock.unlock();

        return isNormal;
    }

}