package com.ontimesdk;

import java.io.Serializable;

public class FingureModel implements Serializable {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getFingerInBytes() {
        return fingerInBytes;
    }

    public void setFingerInBytes(byte[] fingerInBytes) {
        this.fingerInBytes = fingerInBytes;
    }

    private String name;
    private byte[] fingerInBytes;
}
