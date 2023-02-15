package com.simcom.printer.port;

import android.hardware.usb.UsbEndpoint;

public class UsbEndpointData {

    private UsbEndpoint in, out;

    public UsbEndpoint getIn() {
        return in;
    }

    public void setIn(UsbEndpoint in) {
        this.in = in;
    }

    public UsbEndpoint getOut() {
        return out;
    }

    public void setOut(UsbEndpoint out) {
        this.out = out;
    }
}
