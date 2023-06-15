package com.lahm.library;

import android.content.Context;
import android.util.Log;


public class HookUtil {
    public static boolean findHookStack() {
        try {
            throw new Exception("findhook");
        } catch (Exception e) {

            int zygoteInitCallCount = 0;
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if (stackTraceElement.getClassName().equals("com.android.internal.os.ZygoteInit")) {
                    zygoteInitCallCount++;
                    if (zygoteInitCallCount == 2) {
                        Log.wtf("HookDetection", "Substrate is active on the device.");
                        return true;
                    }
                }
                if (stackTraceElement.getClassName().equals("com.saurik.substrate.MS$2")
                        && stackTraceElement.getMethodName().equals("invoked")) {
                    Log.wtf("HookDetection", "A method on the stack trace has been hooked using Substrate.");
                    return true;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge")
                        && stackTraceElement.getMethodName().equals("main")) {
                    Log.wtf("HookDetection", "Xposed is active on the device.");
                    return true;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge")
                        && stackTraceElement.getMethodName().equals("handleHookedMethod")) {
                    Log.wtf("HookDetection", "A method on the stack trace has been hooked using Xposed.");
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean isHook(Context context) {
        String[] strs = new String[]{"com.android", "java.lang", "android", context.getPackageName()};
        try {
            throw new Exception("gg");
        } catch (Exception e) {
            for (int i1 = 0; i1 < e.getStackTrace().length; i1++) {
                for (int i = 0; i < strs.length; i++) {
                    if (e.getStackTrace()[i1].getClassName().startsWith(strs[i])) {
                        return true;
                    }

                }
            }

        }
        return false;
    }







}