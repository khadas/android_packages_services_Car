/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.trust;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.android.car.Utils;
import com.android.internal.annotations.GuardedBy;

import java.io.PrintWriter;

/**
 * A service that interacts with the Trust Agent {@link CarBleTrustAgent} and a comms (BLE) service
 * {@link CarTrustAgentBleManager} to receive the necessary credentials to authenticate
 * an Android user.
 */
public class CarTrustAgentUnlockService {
    private static final String TAG = "CarTrustAgentUnlock";
    private final CarTrustedDeviceService mTrustedDeviceService;
    private final CarTrustAgentBleManager mCarTrustAgentBleManager;
    private CarTrustAgentUnlockDelegate mUnlockDelegate;
    // Locks
    private final Object mTokenLock = new Object();
    private final Object mHandleLock = new Object();
    private final Object mDeviceLock = new Object();

    @GuardedBy("mTokenLock")
    private byte[] mUnlockToken;
    @GuardedBy("mHandleLock")
    private byte[] mUnlockHandle;
    @GuardedBy("mDeviceLock")
    private BluetoothDevice mRemoteUnlockDevice;

    CarTrustAgentUnlockService(CarTrustedDeviceService service,
            CarTrustAgentBleManager bleService) {
        mTrustedDeviceService = service;
        mCarTrustAgentBleManager = bleService;
    }

    /**
     * The interface that an unlock delegate has to implement to get the auth credentials from
     * the unlock service.
     */
    interface CarTrustAgentUnlockDelegate {
        /**
         * Called when the Unlock service has the auth credentials to pass.
         *
         * @param user   user being authorized
         * @param token  escrow token for the user
         * @param handle the handle corresponding to the escrow token
         */
        void onUnlockDataReceived(int user, byte[] token, long handle);
    }

    /**
     * Set a delegate that implements {@link CarTrustAgentUnlockDelegate}. The delegate will be
     * handed the auth related data (token and handle) when it is received from the remote
     * trusted device. The delegate is expected to use that to authorize the user.
     */
    void setUnlockRequestDelegate(CarTrustAgentUnlockDelegate delegate) {
        mUnlockDelegate = delegate;
    }

    /**
     * Start Unlock Advertising
     */
    void startUnlockAdvertising() {
        mTrustedDeviceService.getCarTrustAgentEnrollmentService().stopEnrollmentAdvertising();
        stopUnlockAdvertising();
        mCarTrustAgentBleManager.startUnlockAdvertising();
    }

    /**
     * Stop unlock advertising
     */
    void stopUnlockAdvertising() {
        mCarTrustAgentBleManager.stopUnlockAdvertising();
        // Also disconnect from the peer.
        if (mRemoteUnlockDevice != null) {
            mCarTrustAgentBleManager.disconnectRemoteDevice(mRemoteUnlockDevice);
        }
    }

    void init() {
        mCarTrustAgentBleManager.setupUnlockBleServer();
    }

    void release() {
        synchronized (mDeviceLock) {
            mRemoteUnlockDevice = null;
        }
    }

    void onRemoteDeviceConnected(BluetoothDevice device) {
        synchronized (mDeviceLock) {
            if (mRemoteUnlockDevice != null) {
                // TBD, return when this is encountered?
                Log.e(TAG, "Unexpected: Cannot connect to another device when already connected");
            }
            mRemoteUnlockDevice = device;
        }
    }

    void onRemoteDeviceDisconnected(BluetoothDevice device) {
        // sanity checking
        if (!device.equals(mRemoteUnlockDevice) && device.getAddress() != null) {
            Log.e(TAG, "Disconnected from an unknown device:" + device.getAddress());
        }
        synchronized (mDeviceLock) {
            mRemoteUnlockDevice = null;
        }
    }

    void onUnlockTokenReceived(byte[] value) {
        synchronized (mTokenLock) {
            mUnlockToken = value;
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unlock Token: " + mUnlockToken);
        }
        if (mUnlockToken == null || mUnlockHandle == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unlock Handle not available yet");
            }
            return;
        }
        if (mUnlockDelegate == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "No Unlock delegate");
            }
            return;
        }
        mUnlockDelegate.onUnlockDataReceived(
                mTrustedDeviceService.getUserHandleByTokenHandle(Utils.bytesToLong(mUnlockHandle)),
                mUnlockToken,
                Utils.bytesToLong(mUnlockHandle));

        synchronized (mTokenLock) {
            mUnlockToken = null;
        }
        synchronized (mHandleLock) {
            mUnlockHandle = null;
        }
    }

    void onUnlockHandleReceived(byte[] value) {
        synchronized (mHandleLock) {
            mUnlockHandle = value;
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unlock Handle: " + mUnlockHandle);
        }
        if (mUnlockToken == null || mUnlockHandle == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unlock Token not available yet");
            }
            return;
        }

        if (mUnlockDelegate == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "No Unlock delegate");
            }
            return;
        }
        mUnlockDelegate.onUnlockDataReceived(
                mTrustedDeviceService.getUserHandleByTokenHandle(Utils.bytesToLong(mUnlockHandle)),
                mUnlockToken,
                Utils.bytesToLong(mUnlockHandle));

        synchronized (mUnlockToken) {
            mUnlockToken = null;
        }
        synchronized (mHandleLock) {
            mUnlockHandle = null;
        }
    }

    void dump(PrintWriter writer) {
    }
}