package com.bin.youwei.bluetoothchat.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.bin.youwei.bluetoothchat.Config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by BinYouWei on 2022/1/20
 * <p>
 * 蓝牙客户端连接
 */
public class BluetoothClientConnect extends Thread {
    private Context context;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    private boolean connected = false;
    private Object lock = new Object();

    //蓝牙连接回调接口
    private BluetoothConnectCallback.Client connectCallback;

    public BluetoothClientConnect(Context context, BluetoothDevice device, BluetoothConnectCallback.Client callback) {
        BluetoothSocket soc = null;
        try {
            this.context = context;
            bluetoothDevice = device;
            soc = bluetoothDevice.createRfcommSocketToServiceRecord(Config.BluetoothUUID);
            connectCallback = callback;
        } catch (Exception e) {
            e.printStackTrace();
        }
        bluetoothSocket = soc;
    }

    @Override
    public void run() {
        // 停止扫描蓝牙可加快连接速度
        Bluetooth.getInstance().cancelScanBluetooth(context);
        // 如果还在连接，则取消连接再连接蓝牙
        cancel();
        connect();
        if (connected) {
            if (connectCallback != null) {
                connectCallback.connectSuccess(bluetoothSocket);
            }
        }
    }

    public void connect() {
        try {
            try {
                bluetoothSocket.connect();
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception connectException) {
            connectException.printStackTrace();
            // 连接失败则关闭连接
            try {
                bluetoothSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 取消连接
     */
    public void cancel() {
        if (bluetoothSocket != null) {
            try {
                synchronized (lock) {
                    if (connected) {
                        bluetoothSocket.close();
                        connected = false;
                    }
                }
            } catch (IOException e) {
            }
        }
    }
}
