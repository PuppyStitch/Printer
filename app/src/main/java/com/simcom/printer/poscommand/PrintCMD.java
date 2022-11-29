package com.simcom.printer.poscommand;

public class PrintCMD {

    public static byte[] queryStatus() {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) 0x10;
        bytes[1] = (byte) 0x04;
        bytes[2] = (byte) 0x81;
        return bytes;
    }

    public static byte[] cutPaper() {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 0x1D;
        bytes[1] = (byte) 0x56;
        bytes[2] = (byte) 0x41;
        bytes[3] = (byte) 0x00;
        return bytes;


        //            bytes[j][240 * 72 + 8 + 4 - 4] = 0x1d;              // 切纸指令
//            bytes[j][240 * 72 + 8 + 4 - 3] = 0x76;
//            bytes[j][240 * 72 + 8 + 4 - 2] = 0x30;
//            bytes[j][240 * 72 + 8 + 4 - 1] = 0x00;
    }

}
