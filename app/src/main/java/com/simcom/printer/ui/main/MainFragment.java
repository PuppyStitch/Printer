package com.simcom.printer.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simcom.printer.PrintApplication;
import com.simcom.printer.SpinnerAdapter;
import com.simcom.printer.databinding.FragmentMainBinding;
import com.simcom.printer.poscommand.PrintCMD;
import com.simcom.printer.utils.Subcontract;
import com.simcom.printerlib.printview.PrinterLayout;
import com.simcom.printerlib.utils.DataUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private MainViewModel mViewModel;

    FragmentMainBinding binding;
    byte[] bytes;

    String[] ticketStr = new String[5];
    int spinnerPosition = 0;

    SpinnerAdapter ticketAdapter;


    public static MainFragment newInstance() {
        return new MainFragment();
    }

    private final int MSG_ENABLE_BTN = 0;
    private final int MSG_DISABLE_BTN = 1;
    private final int MSG_NO_PAPER = 2;
    private final int MSG_OVER_HEAT = 3;
    private final int MSG_SET_TIPS_INVISIBILITY = 4;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ENABLE_BTN:
                    enableBtn();
                    break;
                case MSG_DISABLE_BTN:
                    disableBtn();
                    break;
                case MSG_NO_PAPER:
                    // no paper
                    showToastTips("缺纸");
                    break;
                case MSG_OVER_HEAT:
                    // over heat
                    showToastTips("过热");
                    break;
                case MSG_SET_TIPS_INVISIBILITY:
                    hideToastTips();
                    break;
            }
        }
    };

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

        ticketStr[0] = "小票1";
        ticketStr[1] = "小票2";
        ticketStr[2] = "小票3";
        ticketStr[3] = "小票4";
        ticketStr[4] = "黑块";

        ticketAdapter = new SpinnerAdapter(getContext(), ticketStr);
        binding.spinnerTicket.setAdapter(ticketAdapter);
        binding.spinnerTicket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.btnPrintOnce.setOnClickListener(v -> {

            mHandler.sendEmptyMessage(MSG_DISABLE_BTN);
            PrinterLayout.ViewToBitmapListener listener = null;
            CustomCallable customCallable = new CustomCallable();
            FutureTask<Boolean> futureTask = new FutureTask<>(customCallable);
            Thread thread = new Thread(futureTask);

            bytes = DataUtils.readFileFromAssets(getContext(), null, getFileName());

            if (spinnerPosition == 4) {
                byte[] bs = new byte[10 * 240 * 72];
                for (int j = 0; j < 7 * 240 * 72; j++) {
                    bs[j] = (byte) 0xff;
                }
                bytes = bs;
            }

            thread.start();

//            if (bytes != null) {
//                thread.start();
//            } else {

//                listener = new PrinterLayout.ViewToBitmapListener() {
//                    @Override
//                    public void success(Bitmap bitmap) {
////                            bytes = DataUtils.sendBWImage(bitmap, getContext());
//                        bytes = DataUtils.readFileFromAssets(getContext(), null, "s05_sample4.bin");
//                        thread.start();
//                    }
//
//                    @Override
//                    public void failure() {
//
//                    }
//                };
//
//                PrintUtil.meiTuan(getContext(), listener);
//            }
        });

        binding.btnPrintContinue.setOnClickListener(v -> {
            CustomCallable customCallable = new CustomCallable();
            FutureTask<Boolean> futureTask = new FutureTask<>(customCallable);
            Thread thread = new Thread(futureTask);

            bytes = DataUtils.readFileFromAssets(getContext(), null, getFileName());

            if (spinnerPosition == 4) {
                byte[] bs = new byte[10 * 240 * 72];
                for (int j = 0; j < 7 * 240 * 72; j++) {
                    bs[j] = (byte) 0xff;
                }
                bytes = bs;
            }

            thread.start();
        });
    }

    private void disableBtn() {
        binding.btnPrintOnce.setEnabled(false);
    }

    private void enableBtn() {
        binding.btnPrintOnce.setEnabled(true);
    }

    private void showToastTips(String text) {
        binding.textResult.setVisibility(View.VISIBLE);
        binding.textResult.setText(text);
    }

    private void hideToastTips() {
        binding.textResult.setVisibility(View.GONE);
    }

    private String getFileName() {
        if (spinnerPosition == 0) {
            return "s05_sample1.bin";
        }
        if (spinnerPosition == 1) {
            return "s05_sample2.bin";
        }
        if (spinnerPosition == 2) {
            return "s05_sample3.bin";
        }

        return "s05_sample4.bin";
    }

    /**
        打印数据和查询指令建议这样发：
        1）将需要打印的图片按240点行分成多个段，一次发送一段
        2）每发送一段图片指令，后面跟一个查询指令：0x10 0x04 0x81
        3）读取一个字节，如果该字节不是0，表明打印机有异常状态，暂停发送图片数据，但继续周期性地发送查询指令，直到回复的一个字节变为0，再继续发送图片数据
    */


    class CustomCallable implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {
            int time;
            synchronized (this) {
                boolean isReceived;
                Subcontract subcontract = new Subcontract();
                subcontract.goSubcontract(bytes);
                try {

                    for (int i = 0; i < subcontract.packageCount; i++) {
                        sendMessageToPoint(subcontract.getBytes()[i]);
                        isReceived = false;
                        PrintCMD.isSecond = false;
                        time = 0;
                        while (time < 10000) {
                            if (readMessageFromPoint()) {
                                isReceived = true;
                                break;
                            } else {
                                Thread.sleep(50);
                                Log.e("readMsg", "send status request");
                                sendMessageToPoint(PrintCMD.queryStatus());
                                PrintCMD.isSecond = true;
                            }
                            Log.d(TAG, "" + time);
                            time++;
                        }

                        if (!isReceived) {
                            return false;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessageToPoint(PrintCMD.getPreInfo());
                Thread.sleep(50);
                sendMessageToPoint(PrintCMD.cutPaper());
            }
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_BTN, 1000);
            return true;
        }
    }


    public synchronized void sendMessageToPoint(byte[] buffer) throws InterruptedException {

        int i = PrintApplication.getInstance().getPrinterPort().sendMsg(buffer);
        System.out.println("send result-->:::" + i);
        Log.e(TAG, "sendMessageToPoint " + i);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private synchronized boolean readMessageFromPoint() throws InterruptedException {
        boolean isNormal = false;
        byte[] recvbuf = PrintApplication.getInstance().getPrinterPort().readMsg();
        Log.e(TAG, "Received " + byteToHexStr(recvbuf[0]));
        if (recvbuf[0] == 0) {
            isNormal = true;
            mHandler.sendEmptyMessage(MSG_SET_TIPS_INVISIBILITY);
        } else if ((recvbuf[0] & 0x01) != 0) {
            Log.e("readMsg", "no paper");
            mHandler.sendEmptyMessage(MSG_NO_PAPER);
        } else if ((recvbuf[0] & 0x04) != 0) {
            Log.e("readMsg", "over heat");
            mHandler.sendEmptyMessage(MSG_OVER_HEAT);
        }

        return isNormal;
    }

    public String byteToHexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexCode[(b >> 4) & 0xF]);
        sb.append(hexCode[(b) & 0xF]);
        return sb.toString();
    }

}