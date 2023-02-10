package com.simcom.printer.utils;

import com.simcom.printer.poscommand.PrintCMD;

public class UpgradeCon {

    public int packages = 1;

    byte[][] bs;

    public void go(byte[] bytes) {
        bs = new byte[260][192];

        bs[0][0] = 0x01;
        bs[0][1] = 0x00;
        bs[0][2] = (byte) 0xff;
//        bs[0][3] = Byte.parseByte("a");
//        bs[0][4] = Byte.parseByte("p");
//        bs[0][5] = Byte.parseByte("p");
//        bs[0][6] = Byte.parseByte(".");
//        bs[0][7] = Byte.parseByte("b");
//        bs[0][8] = Byte.parseByte("i");
//        bs[0][9] = Byte.parseByte("n");

        bs[0][3] = (byte) Integer.parseInt("a");
        bs[0][4] = (byte) Integer.parseInt("p");
        bs[0][5] = (byte) Integer.parseInt("p");
        bs[0][6] = (byte) Integer.parseInt(".");
        bs[0][7] = (byte) Integer.parseInt("b");
        bs[0][8] = (byte) Integer.parseInt("i");
        bs[0][9] = (byte) Integer.parseInt("n");

        // size
        int size = bytes.length;

        String s = Integer.toString(size);
        byte[] s1 = s.getBytes();

        for (int i = 0; i < s1.length; i++) {
            bs[0][11 + i] = s1[i];
        }

        int m = PrintCMD.CRC16_ccitt(bs[0]);
        bs[0][131] = (byte) (m >> 8);
        bs[0][132] = (byte) (m & 0x00ff);

        int j = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (i % 128 == 0 && i != 0) {
                packages++;
                j = 3;
            }
            bs[packages][j++] = bytes[i];
        }

        for (int i = 0; i < packages; i++) {
            bs[packages][0] = (byte) 0x01;
            bs[packages][1] = (byte) i;
            bs[packages][2] = (byte) ((byte) 255 - i);
            int CRC = PrintCMD.CRC16_ccitt(bs[packages]);
            bs[packages][131] = (byte) (CRC >> 8);
            bs[packages][132] = (byte) (CRC & 0x00ff);
        }
    }

    public byte[][] getBs() {
        return bs;
    }

}
