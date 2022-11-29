package com.simcom.printer.utils;

public class Subcontract {

    public int packageCount = 0;
    public int lastPackageLength = 0;

    private byte[][] bytes = new byte[1000][240 * 72 + 8 + 3];

    public void goSubcontract(byte[] bs) {
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

            bytes[j][240 * 72 + 8 + 3 - 3] = (byte) 0x10;
            bytes[j][240 * 72 + 8 + 3 - 2] = (byte) 0x04;
            bytes[j][240 * 72 + 8 + 3 - 1] = (byte) 0x81;
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
