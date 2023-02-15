package com.simcom.printer.utils;

import android.util.Log;

import com.simcom.printer.poscommand.PrintCMD;

public class UpgradeCon {

    public int packages = 1;

    byte[][] bs;

    public void go(byte[] bytes) {
        bs = new byte[1000][192];

        bs[0][0] = 0x01;
        bs[0][1] = 0x00;
        bs[0][2] = (byte) 0xff;

        bs[0][3] = (byte) (int) Integer.valueOf('a');
        bs[0][4] = (byte) (int) Integer.valueOf('p');
        bs[0][5] = (byte) (int) Integer.valueOf('p');
        bs[0][6] = (byte) (int) Integer.valueOf('.');
        bs[0][7] = (byte) (int) Integer.valueOf('b');
        bs[0][8] = (byte) (int) Integer.valueOf('i');
        bs[0][9] = (byte) (int) Integer.valueOf('n');

        // size
        int size = bytes.length;

        Log.e("chenxing size", size + "");

        String s = Integer.toString(size);
        byte[] s1 = s.getBytes();

        for (int i = 0; i < s1.length; i++) {
            bs[0][11 + i] = s1[i];
        }

        int m = PrintCMD.CRC16_ccitt(bs[0]);
        bs[0][131] = (byte) (m >> 8);
        bs[0][132] = (byte) (m & 0x00ff);

        int j = 3;
        for (int i = 0; i < bytes.length; i++) {
            if (i % 128 == 0 && i != 0) {
                packages++;
                j = 3;
            }
            bs[packages][j++] = bytes[i];
        }

        for (int i = 0; i < 128 - (size % 128); i++) {
            bs[packages][(size % 128) + 3 + i] = (byte) 0x1A;
        }

        for (int i = 1; i <= packages; i++) {
            bs[i][0] = (byte) 0x01;
            bs[i][1] = (byte) (i % 256);
            bs[i][2] = (byte) (255 - ((i % 256)));
            int CRC = PrintCMD.CRC16_ccitt(bs[i]);

            bs[i][131] = (byte) (CRC >> 8);
            bs[i][132] = (byte) (CRC & 0x00ff);
        }

        int a = 0;
        int b = a + 1;
    }

    public byte[][] getBs() {
        return bs;
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public String byteToHexStr(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(hexCode[(b >> 4) & 0xF]);
        sb.append(hexCode[(b) & 0xF]);
        return sb.toString();
    }

}
