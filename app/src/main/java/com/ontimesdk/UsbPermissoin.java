package com.ontimesdk;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import com.fgtit.device.UsbModule;

import java.util.HashMap;
import java.util.Iterator;

public class UsbPermissoin {

    private static final String TAG = UsbPermissoin.class.getName();
    private Context pContext;
    private UsbModule usbModule;
    private UsbPermissoinListener permissionListener;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.fgtit.device.USB_PERMISSION".equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra("permission", false)) {
                        if (device != null) {
                            UsbPermissoin.this.usbModule.CloseDevice();
                            if (UsbPermissoin.this.usbModule.OpenDevice() == 0) {
                                Log.d(TAG, "usbModule.OpenDevice() == 0");
                            } else {
                                Log.d(TAG, "Permission Allowed");
                                permissionListener.onPermissionAllow();
                            }
                        } else {
                            Log.d(TAG, "UsbDevice is null");
                        }
                    } else {
                        Log.d(TAG, "Permission Denied");
                        permissionListener.onPermissionDenied();
                        //UsbPermissoin.this.PostNclMsg(1, -2);
                    }
                }
            } else if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                UsbPermissoin.this.requestPermission();
            } else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                //UsbPermissoin.this.PostNclMsg(1, 2);
                //UsbPermissoin.this.CloseDevice();
            }

        }
    };


    private int mDeviceType = 0;
    private int mDeviceIO = 0;

    public UsbPermissoin(Context pContext, UsbPermissoinListener permissionListener) {
        this.pContext = pContext;
        this.mDeviceType = this.getDeviceType();
        this.usbModule = new UsbModule();
        this.mDeviceIO = this.getDeviceIO(this.mDeviceType);
        this.permissionListener = permissionListener;
        registerUsb();
    }

    public int getDeviceIO(int devType) {
        switch (devType) {
            case 16:
            case 17:
            case 18:
            case 32:
            case 48:
            case 82:
                return 1;
            case 64:
                return 2;
            case 65:
            case 66:
            case 80:
            case 81:
                return 3;
            default:
                return 0;
        }
    }

    public int getDeviceType() {
        String devname = Build.MODEL;
        String devid = Build.DEVICE;
        String devmodel = Build.DISPLAY;
        if (!devname.equals("FP08") && !devname.equals("FP-08") && !devname.equals("FP-08T") && !devname.equals("TIQ-805Q")) {
            if (!devname.equals("b82") && !devname.equals("FP07") && !devname.equals("FP-07")) {
                if (!devname.equals("FP06") && !devname.equals("FP-06")) {
                    if (!devname.equals("FT06") && !devname.equals("FT-06")) {
                        if (!devname.equals("mbk82_tb_kk") && !devname.equals("iMA122") && !devname.equals("iMA321") && !devname.equals("iMA322") && !devname.equals("BioMatch FM-01") && !devname.equals("FP05") && !devname.equals("FP-05") && !devname.equals("KT-7500")) {
                            return 0;
                        } else if (devmodel.indexOf("35SM") >= 0) {
                            return 17;
                        } else {
                            return devmodel.indexOf("80M") >= 0 ? 18 : 16;
                        }
                    } else {
                        return 32;
                    }
                } else {
                    return 48;
                }
            } else if (devname.equals("b82")) {
                return 64;
            } else if (devid.equals("b906")) {
                return 64;
            } else if (devmodel.indexOf("35SM") >= 0) {
                return 66;
            } else {
                return devmodel.indexOf("80M") >= 0 ? 65 : 64;
            }
        } else if (devname.equals("FP-08T")) {
            return 82;
        } else {
            return devmodel.indexOf("35SM") >= 0 ? 81 : 80;
        }
    }

    public void requestPermission() {
        if (mDeviceIO == 1) {
            permissionListener.onPermissionAllow();
        } else {
            UsbManager pmusbManager = (UsbManager) ((Activity) this.pContext).getSystemService(Context.USB_SERVICE);
            if (pmusbManager == null) {
                //this.PostNclMsg(1, -2);
            } else {
                UsbDevice pmusbDevice = null;
                HashMap<String, UsbDevice> devlist = pmusbManager.getDeviceList();
                Iterator deviter = devlist.values().iterator();

                while (deviter.hasNext()) {
                    UsbDevice tmpusbdev = (UsbDevice) deviter.next();
                    Log.i("xpb", "find=" + String.valueOf(tmpusbdev.getVendorId()));
                    if (tmpusbdev.getVendorId() == 1107 && tmpusbdev.getProductId() == 36869) {
                        Log.i("xpb", "usb=0x0453,0x9005");
                        pmusbDevice = tmpusbdev;
                        break;
                    }

                    if (tmpusbdev.getVendorId() == 8201 && tmpusbdev.getProductId() == 30264) {
                        Log.i("xpb", "usb=0x2009,0x7638");
                        pmusbDevice = tmpusbdev;
                        break;
                    }

                    if (tmpusbdev.getVendorId() == 8457 && tmpusbdev.getProductId() == 30264) {
                        Log.i("xpb", "usb=0x2109,0x7638");
                        pmusbDevice = tmpusbdev;
                        break;
                    }

                    if (tmpusbdev.getVendorId() == 1155 && tmpusbdev.getProductId() == 22304) {
                        Log.i("xpb", "usb=0x0483,0x5720");
                        pmusbDevice = tmpusbdev;
                        break;
                    }
                }

                if (pmusbDevice != null) {
                    if (!pmusbManager.hasPermission(pmusbDevice)) {
                        BroadcastReceiver var7 = this.mUsbReceiver;
                        synchronized (this.mUsbReceiver) {
                            pmusbManager.requestPermission(pmusbDevice, this.mPermissionIntent);
                        }
                    } else {
                        this.usbModule.CloseDevice();
                        if (this.usbModule.OpenDevice() == 0) {
                            Log.d(TAG, "usbModule.OpenDevice() == 0");
                        } else {
                            Log.d(TAG, "Permission Allowed");
                            permissionListener.onPermissionAllow();
                        }
                    }
                }
            }
        }
    }


    private IntentFilter filter;
    private PendingIntent mPermissionIntent;

    public void registerUsb() {
        if (this.mDeviceIO == 3) {
            this.mPermissionIntent = PendingIntent.getBroadcast(this.pContext, 0, new Intent("com.fgtit.device.USB_PERMISSION"), 0);
            this.filter = new IntentFilter("com.fgtit.device.USB_PERMISSION");
            this.filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
            this.filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
            this.pContext.registerReceiver(this.mUsbReceiver, this.filter);
        }

    }

    public interface UsbPermissoinListener {
        void onPermissionAllow();

        void onPermissionDenied();
    }
}
