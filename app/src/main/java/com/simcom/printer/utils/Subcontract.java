package com.simcom.printer.utils;

import android.util.Log;

public class Subcontract {

    private static final String TAG = "Subcontract";

    public int packageCount = 0;
    public int lastPackageLength = 0;

    private byte[][] bytes;

    private byte[][][] myBytes = new byte[1000][242][72];

    public void printLog(byte[] bytes) {

        Log.d(TAG, "starting print log");

        int[] arr = new int[8];

        byte bs;

        int j = 0;
        while (j < bytes.length) {
            bs = bytes[j];
            arr[0] = (bs & 0b10000000) == 0 ? 0 : 1;
            arr[1] = (bs & 0b01000000) == 0 ? 0 : 1;
            arr[2] = (bs & 0b00100000) == 0 ? 0 : 1;
            arr[3] = (bs & 0b00010000) == 0 ? 0 : 1;
            arr[4] = (bs & 0b00001000) == 0 ? 0 : 1;
            arr[5] = (bs & 0b00000100) == 0 ? 0 : 1;
            arr[6] = (bs & 0b00000010) == 0 ? 0 : 1;
            arr[7] = (bs & 0b00000001) == 0 ? 0 : 1;

            for (int i1 = 0; i1 < 8; i1++) {
                if (arr[i1] == 1) {
                    System.out.print("■");
                } else {
                    System.out.print(" ");
                }
            }

            if ((j + 1) % 72 == 0) {
                System.out.print("\n");
            }
            j++;

//            }
        }
        System.out.print("\n");
    }

//        Log.d("my print", "print log");

    public void goSubcontract(byte[] bs) {
        bytes = new byte[1000][240 * 72 + 8 + 3];

        packageCount = 0;
        int index = 0;
        for (int i = 0; i < bs.length; i++) {
            if (i % (240 * 72) == 0 && i != 0) {
                index = 0;
                packageCount++;
            }
            bytes[packageCount][index + 8] = bs[i];
            index++;
        }
        if (bs.length % (240 * 72) != 0) {
            packageCount++;
        }

        for (int j = 0; j < packageCount; j++) {
            bytes[j][0] = 0x1d;                             // 打印指令
            bytes[j][1] = 0x76;
            bytes[j][2] = 0x30;
            bytes[j][3] = 0x00;
            bytes[j][4] = (byte) (72 & 0x00ff);            // 宽高信息
            bytes[j][5] = (byte) (72 >> 8);
            bytes[j][6] = (byte) (240 & 0x00ff);
            bytes[j][7] = (byte) (240 >> 8);

            bytes[j][240 * 72 + 8 + 3 - 3] = (byte) 0x10;           // 查询状态信息
            bytes[j][240 * 72 + 8 + 3 - 2] = (byte) 0x04;
            bytes[j][240 * 72 + 8 + 3 - 1] = (byte) 0x81;
        }

//        fire();
//        printLog();
    }

    private void fire() {
        int[] arr = new int[8];
        byte bs;
        for (int i = 0; i < packageCount; i++) {
            for (int j = 8; j < 240 * 72 + 8; j++) {
                bs = bytes[i][j];
                arr[0] = (bs & 0b10000000) == 0 ? 0 : 1;
                arr[1] = (bs & 0b01000000) == 0 ? 0 : 1;
                arr[2] = (bs & 0b00100000) == 0 ? 0 : 1;
                arr[3] = (bs & 0b00010000) == 0 ? 0 : 1;
                arr[4] = (bs & 0b00001000) == 0 ? 0 : 1;
                arr[5] = (bs & 0b00000100) == 0 ? 0 : 1;
                arr[6] = (bs & 0b00000010) == 0 ? 0 : 1;
                arr[7] = (bs & 0b00000001) == 0 ? 0 : 1;

                for (int i1 = 0; i1 < 8; i1++) {
                    if (arr[i1] == 1) {
                        System.out.print("■");
                    } else {
                        System.out.print(" ");
                    }
                }

                if ((j + 1) % 72 == 0) {
                    System.out.print("\n");
                }

            }
        }
    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public int getLastPackageLength() {
        return lastPackageLength;
    }

    public void setLastPackageLength(int lastPackageLength) {
        this.lastPackageLength = lastPackageLength;
    }

    public byte[][] getBytes() {
        return bytes;
    }

    public void setBytes(byte[][] bytes) {
        this.bytes = bytes;
    }

}
