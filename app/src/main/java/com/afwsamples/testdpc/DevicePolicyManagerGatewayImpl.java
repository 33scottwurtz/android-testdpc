/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.afwsamples.testdpc;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class DevicePolicyManagerGatewayImpl implements DevicePolicyManagerGateway {

    private static final String TAG = DevicePolicyManagerGatewayImpl.class.getSimpleName();

    private final DevicePolicyManager mDevicePolicyManager;
    private final UserManager mUserManager;
    private final ComponentName mAdminComponentName;

    public DevicePolicyManagerGatewayImpl(@NonNull Context context) {
        this(context.getSystemService(DevicePolicyManager.class),
                context.getSystemService(UserManager.class),
                DeviceAdminReceiver.getComponentName(context));
    }

    public DevicePolicyManagerGatewayImpl(@NonNull DevicePolicyManager dpm, @NonNull UserManager um,
            @NonNull ComponentName admin) {
        mDevicePolicyManager = dpm;
        mUserManager = um;
        mAdminComponentName = admin;

        Log.d(TAG, "constructor: admin=" + mAdminComponentName + ", dpm=" + dpm);
    }

    public static DevicePolicyManagerGatewayImpl forParentProfile(@NonNull Context context) {
        ComponentName admin = DeviceAdminReceiver.getComponentName(context);
        DevicePolicyManager dpm = context.getSystemService(DevicePolicyManager.class)
                .getParentProfileInstance(admin);
        UserManager um = context.getSystemService(UserManager.class);
        return new DevicePolicyManagerGatewayImpl(dpm, um, admin);
    }

    @Override
    public void createAndManageUser(String name, int flags, Consumer<UserHandle> onSuccess,
            Consumer<Exception> onError) {
        Log.d(TAG, "createAndManageUser(" + name + ", " + flags + ")");

        try {
            UserHandle userHandle = mDevicePolicyManager.createAndManageUser(mAdminComponentName,
                    name, mAdminComponentName, /* adminExtras= */ null, flags);
            if (userHandle != null) {
                onSuccess.accept(userHandle);
            } else {
                onError.accept(
                        new InvalidResultException("null",
                                "createAndManageUser(%s, %d)", name, flags));
            }
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void removeUser(long serialNumber, Consumer<Void> onSuccess,
            Consumer<Exception> onError) {
        Log.d(TAG, "removeUser(" + serialNumber + ")");
        UserHandle userHandle = mUserManager.getUserForSerialNumber(serialNumber);
        if (userHandle == null) {
            onError.accept(new InvalidResultException("null", "getUserForSerialNumber(%d)",
                    serialNumber));
            return;
        }
        removeUser(userHandle, onSuccess, onError);
    }

    @Override
    public void removeUser(UserHandle userHandle, Consumer<Void> onSuccess,
            Consumer<Exception> onError) {
        Log.d(TAG, "removeUser(" + userHandle + ")");

        try {
            boolean success = mDevicePolicyManager.removeUser(mAdminComponentName, userHandle);
            if (success) {
                onSuccess.accept(null);
            } else {
                onError.accept(new InvalidResultException("false", "removeUser(%s)", userHandle));
            }
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public Set<String> getUserRestrictions() {
        Log.d(TAG, "getUserRestrictions()");
        Bundle restrictions = mDevicePolicyManager.getUserRestrictions(mAdminComponentName);
        return restrictions.keySet().stream().filter(k -> restrictions.getBoolean(k))
                .collect(Collectors.toSet());
    }

    @Override
    public void setUserRestriction(String userRestriction, boolean enabled,
            Consumer<Void> onSuccess, Consumer<Exception> onError) {
        Log.d(TAG, "setUserRestriction(" + userRestriction + ", " + enabled + ")");

        try {
            if (enabled) {
                mDevicePolicyManager.addUserRestriction(mAdminComponentName, userRestriction);
            } else {
                mDevicePolicyManager.clearUserRestriction(mAdminComponentName, userRestriction);
            }
            onSuccess.accept(null);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void setUserRestriction(String userRestriction, boolean enabled) {
        String message = String.format("setUserRestriction(%s, %b)", userRestriction, enabled);
        setUserRestriction(userRestriction, enabled,
                (v) -> onSuccessLog(message),
                (e) -> onErrorLog(message));
    }

    @Override
    public boolean hasUserRestriction(String userRestriction) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "hasUserRestriction(" + userRestriction + ")");
        }
        return mUserManager.hasUserRestriction(userRestriction);
    }

    @Override
    public void lockNow(Consumer<Void> onSuccess, Consumer<Exception> onError) {
        Log.d(TAG, "lockNow()");

        try {
            mDevicePolicyManager.lockNow();
            onSuccess.accept(null);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void wipeData(int flags, Consumer<Void> onSuccess, Consumer<Exception> onError) {
        Log.d(TAG, "wipeData(" + flags + ")");

        try {
            mDevicePolicyManager.wipeData(flags);
            onSuccess.accept(null);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void requestBugreport(Consumer<Void> onSuccess, Consumer<Exception> onError) {
        Log.d(TAG, "requestBugreport(");

        try {
            boolean success = mDevicePolicyManager.requestBugreport(mAdminComponentName);
            if (success) {
                onSuccess.accept(null);
            } else {
                onError.accept(new FailedOperationException("requestBugreport()"));
            }
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void setNetworkLogging(boolean enabled, Consumer<Void> onSuccess,
            Consumer<Exception> onError) {
        Log.d(TAG, "setNetworkLogging(" + enabled + ")");

        try {
            mDevicePolicyManager.setNetworkLoggingEnabled(mAdminComponentName, enabled);
            onSuccess.accept(null);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void setNetworkLogging(boolean enabled) {
        String message = String.format("setNetworkLogging(%b)", enabled);
        setNetworkLogging(enabled,
                (v) -> onSuccessLog(message),
                (e) -> onErrorLog(message));
    }

    @Override
    public String toString() {
        return "DevicePolicyManagerGatewayImpl[" + mAdminComponentName + "]";
    }

    private void onSuccessLog(String template, Object... args) {
        Log.d(TAG, String.format(template, args) + " succeeded");
    }

    private void onErrorLog(String template, Object... args) {
        Log.d(TAG, String.format(template, args) + " failed");
    }

}
