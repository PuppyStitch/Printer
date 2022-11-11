package com.simcom.printer.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.simcom.printer.R;
import com.simcom.printer.databinding.FragmentMainBinding;
import com.simcom.printer.utils.USBUtil;
import com.simcom.printerlib.Printer;
import com.simcom.printerlib.printview.BitmapPrintLine;
import com.simcom.printerlib.printview.MidTextPrintLine;
import com.simcom.printerlib.printview.PrintLine;
import com.simcom.printerlib.printview.PrinterLayout;
import com.simcom.printerlib.printview.TextPrintLine;
import com.simcom.printerlib.utils.DataUtils;
import com.simcom.printerlib.utils.QRCodeUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Hashtable;
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

    String string = "123454654\n" +
            "asdfadf \n" +
            "asdfas f asdf asdfkjasdl;fja \n";

    static final int ALIGN_LEFT   = 0;
    static final int ALIGN_CENTER = 1;
    static final int ALIGN_RIGHT  = 2;

    static final int DIFFUSE_DITHER   = 0;
    static final int THRESHOLD_DITHER = 2;

    int DOTS_PER_LINE = 576;

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
            //3)查找设备接口1
            getDeviceInterface();
            //4)获取设备endpoint
            assignEndpoint();
            //5)打开conn连接通道
//            sendMessageToPoint(new byte[]{0x1b, 0x61, 0x00});
        });

        binding.message1.setOnClickListener(v -> {

            openDevice();

            meituan();

//            sendMessageToPoint(string.getBytes(StandardCharsets.UTF_8));
            sendMessageToPoint(bytes);

            byte[] bs = new byte[4];
            bs[0] = 0x1D;
            bs[1] = 0x56;
            bs[2] = 0x41;
            bs[3] = 0x00;
            sendMessageToPoint(bs);

//            try {
//                Bitmap bitmap = QRCodeUtil.createQRCode("This is for testing", BarcodeFormat.QR_CODE,
//                        ErrorCorrectionLevel.M, 576);
//                sendMessageToPoint(draw2PxPoint(bitmap));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
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




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    StringBuilder orderContent = new StringBuilder();

    private void load() {



        Bitmap bm = QRCodeUtil.createQRCode("This is for sadfasdfa ", BarcodeFormat.QR_CODE,
                ErrorCorrectionLevel.M, 576);
        byte[] bs = DataUtils.sendBWImage(bm, getContext());

        int w = bm.getWidth() / 8;
        int h = bm.getHeight();

        bytes = new byte[bs.length + 8];

        bytes[0] = 0x1d;
        bytes[1] = 0x76;
        bytes[2] = 0x30;
        bytes[3] = 0x00;
        bytes[4] = (byte) (w % 256);
        bytes[5] = (byte) (w / 256);
        bytes[6] = (byte) (h % 256);
        bytes[7] = (byte) (h / 256);

        for (int i = 0; i < bs.length; i++) {
            bytes[i + 8] = bs[i];
        }

        //        cmd[4] = (byte) (width % 256);//计算xL
//        cmd[5] = (byte) (width / 256);//计算xH
//        cmd[6] = (byte) (height % 256);//计算yL
//        cmd[7] = (byte) (height / 256);//计算yH



        orderContent.append("1d763000");

        orderContent.append(String.format("%02x%02x", (w & 0xff), ((w >> 8) & 0xff)));
        orderContent.append(String.format("%02x%02x", (h & 0xff), ((h >> 8) & 0xff)));

        for (int i = 0; i < bs.length; i++) {
            orderContent.append(String.format("%02x", bs[i]));
        }

    }

    private void meituan() {

        int maxSize = 24;
        int midSize = 16;
        int minSize = 10;

        PrinterLayout printerLayout = new PrinterLayout(getContext());

        TextPrintLine headTitle = new TextPrintLine();
        headTitle.setSize(maxSize);
        headTitle.setPosition(PrintLine.LEFT);
        headTitle.setContent("商家小票");
        printerLayout.addText(headTitle);

        TextPrintLine toolLineTPL0 = new TextPrintLine();
        toolLineTPL0.setContent("- - - - - - - - - - - - - - - - - - - - - - -");
        toolLineTPL0.setPosition(PrintLine.CENTER);
        printerLayout.addText(toolLineTPL0);

        TextPrintLine headTPL = new TextPrintLine();
        headTPL.setBold(true);
        headTPL.setPosition(PrintLine.CENTER);
        headTPL.setContent("* #7 芯讯通外卖 *");
        headTPL.setSize(maxSize);
        printerLayout.addText(headTPL);

        TextPrintLine rest = new TextPrintLine();
        rest.setContent("琶洲村餐厅");
        rest.setPosition(PrintLine.CENTER);
        printerLayout.addText(rest);

        TextPrintLine secondTPL = new TextPrintLine();
        secondTPL.setContent("下单时间: 2022-06-30 21:13:56");
        secondTPL.setSize(minSize);
        printerLayout.addText(secondTPL);

        TextPrintLine toolLineTPL1 = new TextPrintLine();
        toolLineTPL1.setContent("* * * * * * * * * * * * * * * * *");
        printerLayout.addText(toolLineTPL1);

        TextPrintLine toolLineTPL = new TextPrintLine();
        toolLineTPL.setContent("- - - - - - - - - - - - 1号口袋 - - - - - - - - - - - -");
        toolLineTPL.setSize(midSize);
        toolLineTPL.setPosition(PrintLine.CENTER);
        printerLayout.addText(toolLineTPL);

        MidTextPrintLine midTextPrintLine = new MidTextPrintLine(getContext());
        midTextPrintLine.getLeftTextView().setText("黄瓜炒香肠");
        midTextPrintLine.getMidTextView().setText("* 1");
        midTextPrintLine.getMidTextView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        midTextPrintLine.getRightTextView().setText("5.0");
        midTextPrintLine.getRightTextView().setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        midTextPrintLine.getLeftTextView().setTextSize(minSize);
        midTextPrintLine.getMidTextView().setTextSize(minSize);
        midTextPrintLine.getRightTextView().setTextSize(minSize);

//        textView.setTextColor(-16777216);
//        textView.setBackgroundColor(0);

        midTextPrintLine.getLeftTextView().setTextColor(-16777216);
        printerLayout.addView(midTextPrintLine);

        MidTextPrintLine midTextPrintLine1 = new MidTextPrintLine(getContext());
        midTextPrintLine1.getLeftTextView().setText("青椒肉丝\n8.3折，原价78.00");
        midTextPrintLine1.getMidTextView().setText("\n* 1");
        midTextPrintLine1.getMidTextView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        midTextPrintLine1.getRightTextView().setText("65.0");
        midTextPrintLine1.getRightTextView().setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        midTextPrintLine1.getLeftTextView().setTextSize(minSize);
        midTextPrintLine1.getMidTextView().setTextSize(minSize);
        midTextPrintLine1.getRightTextView().setTextSize(minSize);
        printerLayout.addView(midTextPrintLine1);

        TextPrintLine toolLineTPL2 = new TextPrintLine();
        toolLineTPL2.setContent("- - - - - - - - - - 其 它 - - - - - - - - -");
        toolLineTPL2.setSize(midSize);
        toolLineTPL2.setPosition(PrintLine.CENTER);
        printerLayout.addText(toolLineTPL2);

        MidTextPrintLine firstMTPL = new MidTextPrintLine(getContext());
        firstMTPL.getLeftTextView().setText("打包费");
        firstMTPL.getRightTextView().setText("3.0");
        firstMTPL.getLeftTextView().setTextSize(minSize);
        firstMTPL.getMidTextView().setTextSize(minSize);
        firstMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(firstMTPL);

        MidTextPrintLine secondMTPL = new MidTextPrintLine(getContext());
        secondMTPL.getLeftTextView().setText("配送费");
        secondMTPL.getRightTextView().setText("6.5");
        secondMTPL.getLeftTextView().setTextSize(minSize);
        secondMTPL.getMidTextView().setTextSize(minSize);
        secondMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(secondMTPL);

        MidTextPrintLine thirdMTPL = new MidTextPrintLine(getContext());
        thirdMTPL.getLeftTextView().setText("[减配送费4.0元]");
        thirdMTPL.getRightTextView().setText("-4.0");
        thirdMTPL.getLeftTextView().setTextSize(minSize);
        thirdMTPL.getMidTextView().setTextSize(minSize);
        thirdMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(thirdMTPL);

        MidTextPrintLine forthMTPL = new MidTextPrintLine(getContext());
        forthMTPL.getLeftTextView().setText("[门店新客立减1.0元]");
        forthMTPL.getRightTextView().setText("-1.0");
        forthMTPL.getLeftTextView().setTextSize(minSize);
        forthMTPL.getMidTextView().setTextSize(minSize);
        forthMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(forthMTPL);

        TextPrintLine toolLineTPL21 = new TextPrintLine();
        toolLineTPL21.setContent("* * * * * * * * * * * * * * * * *");
        printerLayout.addText(toolLineTPL21);

        TextPrintLine original = new TextPrintLine();
        original.setContent("订单原价（含配送费和打包费）：92.5元");
        original.setSize(midSize);
        original.setPosition(TextPrintLine.RIGHT);
        printerLayout.addText(original);

        TextPrintLine pay = new TextPrintLine();
        pay.setContent("(用户在线支付) 74.5元");
        pay.setSize(midSize);
        pay.setPosition(TextPrintLine.RIGHT);
        printerLayout.addText(pay);

        TextPrintLine toolLineTPL01 = new TextPrintLine();
        toolLineTPL01.setContent("- - - - - - - - - - - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTPL01);

        TextPrintLine toolLineTPL4 = new TextPrintLine();
        toolLineTPL4.setContent("北京市朝阳区天通苑小区A17号楼3单元6楼15号");
        toolLineTPL4.setSize(maxSize);
        printerLayout.addText(toolLineTPL4);

        TextPrintLine toolLineTPL5 = new TextPrintLine();
        toolLineTPL5.setContent("顾客号码：12345678910");
        toolLineTPL5.setSize(minSize);
        printerLayout.addText(toolLineTPL5);

        TextPrintLine toolLineTPL7 = new TextPrintLine();
        toolLineTPL7.setContent("虚拟号码：12345678910转0130");
        toolLineTPL7.setSize(minSize);
        printerLayout.addText(toolLineTPL7);

        TextPrintLine toolLineTPL8 = new TextPrintLine();
        toolLineTPL8.setContent("备用号码：12345678910转0130");
        toolLineTPL8.setSize(minSize);
        printerLayout.addText(toolLineTPL8);

        Bitmap bm = QRCodeUtil.createQRCode("This is for testing", BarcodeFormat.QR_CODE,
                ErrorCorrectionLevel.M, 384);
        BitmapPrintLine bitmapPrintLine = new BitmapPrintLine(bm, PrintLine.CENTER);
        printerLayout.addBitmap(bitmapPrintLine);


        Bitmap bm1 = QRCodeUtil.getBarcodeBmp("This is for testing", BarcodeFormat.CODE_128,
                ErrorCorrectionLevel.M, 384, 100);
        BitmapPrintLine bitmapPrintLine1 = new BitmapPrintLine(bm1, PrintLine.CENTER);
        printerLayout.addBitmap(bitmapPrintLine1);

        TextPrintLine toolLineTPL9 = new TextPrintLine();
        toolLineTPL9.setContent("\n\n\n\n\n\n\n\n\n");
        toolLineTPL9.setSize(minSize);
        printerLayout.addText(toolLineTPL9);

        printerLayout.viewToBitmap(new PrinterLayout.ViewToBitmapListener() {
            @Override
            public void success(Bitmap bitmap) {

                byte[] bs = DataUtils.sendBWImage(bitmap, getContext());

                int w = bitmap.getWidth() / 8;
                int h = bitmap.getHeight();

                bytes = new byte[bs.length + 8];

                bytes[0] = 0x1d;
                bytes[1] = 0x76;
                bytes[2] = 0x30;
                bytes[3] = 0x00;
                bytes[4] = (byte) (w % 256);
                bytes[5] = (byte) (w / 256);
                bytes[6] = (byte) (h % 256);
                bytes[7] = (byte) (h / 256);

                for (int i = 0; i < bs.length; i++) {
                    bytes[i + 8] = bs[i];
                }
            }

            @Override
            public void failure() {

            }
        });

    }

//    // 使用光栅位图的打印方式
//    public byte[] printBitmap() throws Exception {
//        // GS v 0 m xL xH yL yH d1...dk
//        //规范化位图宽高
//        Bitmap bitmap = QRCodeUtil.createQRCode("This is for testing", BarcodeFormat.QR_CODE,
//                ErrorCorrectionLevel.M, 576);
//
//        int width = bitmap.getWidth() / 8;
//        int height = bitmap.getHeight();
//        byte[] cmd = new byte[width * height + 4 + 4];
//        cmd[0] = 29;
//        cmd[1] = 118;
//        cmd[2] = 48;
//        cmd[3] = 0;
//        cmd[4] = (byte) (width % 256);//计算xL
//        cmd[5] = (byte) (width / 256);//计算xH
//        cmd[6] = (byte) (height % 256);//计算yL
//        cmd[7] = (byte) (height / 256);//计算yH
//
//        int index = 8;
//        int temp = 0;
//        int part[] = new int[8];
//        for (int j = 0; j < bitmap.getHeight(); j++) {
//            for (int i = 0; i < bitmap.getWidth(); i += 8) {
//                //横向每8个像素点组成一个字节。
//                for (int k = 0; k < 8; k++) {
//                    int pixel = bitmap.getPixel(i + k, j);
//                    int grayPixle = grayPixle(pixel);
//                    if (grayPixle > 128) {
//                        //灰度值大于128位   白色 为第k位0不打印
//                        part[k] = 0;
//                    } else {
//                        part[k] = 1;
//                    }
//                }
//
//                //128千万不要写成2^7，^是异或操作符
//                temp = part[0] * 128 +
//                        part[1] * 64 +
//                        part[2] * 32 +
//                        part[3] * 16 +
//                        part[4] * 8 +
//                        part[5] * 4 +
//                        part[6] * 2 +
//                        part[7] * 1;
//                cmd[index++] = (byte) temp;
//            }
//        }
//
//        return cmd;
//    }

    public static byte[] draw2PxPoint(Bitmap bmp) {
        //用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        //整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，5:5455,3,5447,4,5427
        //但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        //所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
        byte[] data = new byte[size];
        int k = 0;
        //设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            //打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); //nL
            data[k++] = (byte) (bmp.getWidth() / 256); //nH
            //对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                //每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    //每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        data[k] += data[k] + b;
                    }

                    k++;
                }
            }
            data[k++] = 10;//换行
        }
        //   long a=System.currentTimeMillis();
        byte[] data1 = new byte[k];
        System.arraycopy(data, 0, data1, 0, k);
        // long b=System.currentTimeMillis();
        //  System.out.println("结束字节:"+k+"---"+data.length+"耗时:"+(b-a));
        return data1;
    }

    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    /**
     * 图片灰度的转化
     */
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
        return gray;
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

    public void sendMessageToPoint(byte[] buffer) {
        int i = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
        System.out.println("result-->:::" + i);
        if (i >= 0) {
            //0 或者正数表示成功
            System.out.println("发送成功");
        } else {
            System.out.println("发送失败");
        }
    }

}