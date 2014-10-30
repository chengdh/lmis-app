package com.lmis.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

/**
 * Created by chengdh on 14-10-24.
 * 蓝牙相关工具类
 */
public class BlueTooth {
    /**
     * 获取蓝牙地址.
     *
     * @param devName the device name
     * @return the string
     */
    public static String getAddress(String devName) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String ret = null;
        for (BluetoothDevice dev : pairedDevices) {
            if (dev.getName().equals(devName)) {
                ret = dev.getAddress();
            }
        }
        return ret;
    }
}
