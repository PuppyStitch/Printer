package com.simcom.printer.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.simcom.printerlib.printview.BitmapPrintLine;
import com.simcom.printerlib.printview.MidTextPrintLine;
import com.simcom.printerlib.printview.PrintLine;
import com.simcom.printerlib.printview.PrinterLayout;
import com.simcom.printerlib.printview.TextPrintLine;
import com.simcom.printerlib.utils.DataUtils;
import com.simcom.printerlib.utils.QRCodeUtil;

import java.io.InputStream;

public class PrintUtil {

    public static byte[] bytes;

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String byteToHexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexCode[(b >> 4) & 0xF]);
        sb.append(hexCode[(b) & 0xF]);
        return sb.toString();
    }

    public static byte[] meiTuan(Context context, PrinterLayout.ViewToBitmapListener listener) {

        int maxSize = 24;
        int midSize = 16;
        int minSize = 10;

        PrinterLayout printerLayout = new PrinterLayout(context);

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

        MidTextPrintLine midTextPrintLine = new MidTextPrintLine(context);
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

        MidTextPrintLine midTextPrintLine1 = new MidTextPrintLine(context);
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

        MidTextPrintLine firstMTPL = new MidTextPrintLine(context);
        firstMTPL.getLeftTextView().setText("打包费");
        firstMTPL.getRightTextView().setText("3.0");
        firstMTPL.getLeftTextView().setTextSize(minSize);
        firstMTPL.getMidTextView().setTextSize(minSize);
        firstMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(firstMTPL);

        MidTextPrintLine secondMTPL = new MidTextPrintLine(context);
        secondMTPL.getLeftTextView().setText("配送费");
        secondMTPL.getRightTextView().setText("6.5");
        secondMTPL.getLeftTextView().setTextSize(minSize);
        secondMTPL.getMidTextView().setTextSize(minSize);
        secondMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(secondMTPL);

        MidTextPrintLine thirdMTPL = new MidTextPrintLine(context);
        thirdMTPL.getLeftTextView().setText("[减配送费4.0元]");
        thirdMTPL.getRightTextView().setText("-4.0");
        thirdMTPL.getLeftTextView().setTextSize(minSize);
        thirdMTPL.getMidTextView().setTextSize(minSize);
        thirdMTPL.getRightTextView().setTextSize(minSize);
        printerLayout.addView(thirdMTPL);

        MidTextPrintLine forthMTPL = new MidTextPrintLine(context);
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

        printerLayout.viewToBitmap(listener);

//        printerLayout.viewToBitmap(new PrinterLayout.ViewToBitmapListener() {
//            @Override
//            public void success(Bitmap bitmap) {
//
//                byte[] bs = DataUtils.sendBWImage(bitmap, context);
//
//                int w = bitmap.getWidth() / 8;
//                int h = bitmap.getHeight();
//
//                bytes = new byte[bs.length + 8];
////                bytes = bs;
//
//                bytes[0] = 0x1d;
//                bytes[1] = 0x76;
//                bytes[2] = 0x30;
//                bytes[3] = 0x00;
//                bytes[4] = (byte) (w % 256);
//                bytes[5] = (byte) (w / 256);
//                bytes[6] = (byte) (h % 256);
//                bytes[7] = (byte) (h / 256);
//
//                for (int i = 0; i < bs.length; i++) {
//                    bytes[i + 8] = bs[i];
//                }
//            }
//
//            @Override
//            public void failure() {
//
//            }
//        });

        //todo need to verify
        return bytes;
    }



//    public static byte[] draw2PxPoint(Bitmap bmp) {
//        //用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
//        //整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，5:5455,3,5447,4,5427
//        //但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
//        //所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
//        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
//        byte[] data = new byte[size];
//        int k = 0;
//        //设置行距为0的指令
//        data[k++] = 0x1B;
//        data[k++] = 0x33;
//        data[k++] = 0x00;
//        // 逐行打印
//        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
//            //打印图片的指令
//            data[k++] = 0x1B;
//            data[k++] = 0x2A;
//            data[k++] = 33;
//            data[k++] = (byte) (bmp.getWidth() % 256); //nL
//            data[k++] = (byte) (bmp.getWidth() / 256); //nH
//            //对于每一行，逐列打印
//            for (int i = 0; i < bmp.getWidth(); i++) {
//                //每一列24个像素点，分为3个字节存储
//                for (int m = 0; m < 3; m++) {
//                    //每个字节表示8个像素点，0表示白色，1表示黑色
//                    for (int n = 0; n < 8; n++) {
//                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
//                        data[k] += data[k] + b;
//                    }
//
//                    k++;
//                }
//            }
//            data[k++] = 10;//换行
//        }
//        //   long a=System.currentTimeMillis();
//        byte[] data1 = new byte[k];
//        System.arraycopy(data, 0, data1, 0, k);
//        // long b=System.currentTimeMillis();
//        //  System.out.println("结束字节:"+k+"---"+data.length+"耗时:"+(b-a));
//        return data1;
//    }
//
//    public static byte px2Byte(int x, int y, Bitmap bit) {
//        if (x < bit.getWidth() && y < bit.getHeight()) {
//            byte b;
//            int pixel = bit.getPixel(x, y);
//            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
//            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
//            int blue = pixel & 0x000000ff; // 取低两位
//            int gray = RGB2Gray(red, green, blue);
//            if (gray < 128) {
//                b = 1;
//            } else {
//                b = 0;
//            }
//            return b;
//        }
//        return 0;
//    }
//
//    /**
//     * 图片灰度的转化
//     */
//    private static int RGB2Gray(int r, int g, int b) {
//        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
//        return gray;
//    }
//
//
//    private byte[] readFileFromAssets(Context context, String groupPath, String filename) {
//        byte[] buffer = null;
//        AssetManager am = context.getAssets();
//
//        try {
//            InputStream inputStream = null;
//            if (groupPath != null) {
//                inputStream = am.open(groupPath + "/" + filename);
//            } else {
//                inputStream = am.open(filename);
//            }
//
//            int length = inputStream.available();
//            buffer = new byte[length];
//            inputStream.read(buffer);
//        } catch (Exception var7) {
//            var7.printStackTrace();
//        }
//
//        return buffer;
//    }



//    public synchronized void sendMessageToPoint(byte[] buffer) {
////        binding.message1.setEnabled(false);
//
//        byte[] bytes2 = new byte[bytes.length + 4];
//        int a = 0;
//        for (; a < bytes.length; a++) {
//            bytes2[a] = bytes[a];
//        }
//
//        bytes2[a++] = 0x1D;
//        bytes2[a++] = 0x56;
//        bytes2[a++] = 0x41;
//        bytes2[a++] = 0x00;
//
////        for (int a = 0; a < curTime; a++) {
////            byte[] bs = ;
////            int i = myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer.length, 0);
//        int i = myDeviceConnection.bulkTransfer(epBulkOut, bytes2, bytes2.length, 0);
//
//        System.out.println("result-->:::" + i);
//        if (i >= 0) {
//            //0 或者正数表示成功
//            System.out.println("发送成功");
////            mHandler.postDelayed(runnable, 1000);
//        } else {
//            System.out.println("发送失败");
//        }
////        }
//    }

}
