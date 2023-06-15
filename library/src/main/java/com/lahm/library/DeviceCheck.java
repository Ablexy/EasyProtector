package com.lahm.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class DeviceCheck {
  public static boolean checkFileLocation(Context paramContext) {
    try {
      String str = paramContext.getPackageName();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("/data/data/");
      stringBuilder.append(str);
      stringBuilder.append("/files");
      File file = new File(stringBuilder.toString());
      file.mkdirs();
      boolean bool = file.canWrite();
      return bool ^ true;
    } catch (Exception exception) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(exception);
      stringBuilder.append("");
      Log.e("losg_log", stringBuilder.toString());
      return false;
    } 
  }
  
  public static boolean checkIsFileHook() {
    try {
      Field field = Class.forName("libcore.io.Libcore").getDeclaredField("os");
      field.setAccessible(true);
      Object object = field.get((Object)null);
      if (object != null)
        return object.getClass().getName().startsWith("$Proxy"); 
    } catch (Exception exception) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(exception);
      stringBuilder.append("");
      Log.e("losg_log", stringBuilder.toString());
    } 
    return false;
  }
  
  public static boolean checkIsHandlerHook() {
    boolean bool = false;
    try {
      Handler handler = (Handler)getActivityThreadHandler();
      Field field = Handler.class.getDeclaredField("mCallback");
      field.setAccessible(true);
      Object object = field.get(handler);
      if (object != null)
        bool = true; 
      return bool;
    } catch (Exception exception) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Handler:");
      stringBuilder.append(exception);
      Log.e("losg_log", stringBuilder.toString());
      return false;
    } 
  }
  
  public static boolean checkIsLocationHook(Context paramContext) {
    try {
      LocationManager locationManager = (LocationManager)paramContext.getSystemService(Context.LOCATION_SERVICE);
      @SuppressLint("BlockedPrivateApi")
      Field field = locationManager.getClass().getDeclaredField("mService");
      field.setAccessible(true);
      return field.get(locationManager).getClass().getName().startsWith("$Proxy");
    } catch (Exception exception) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(exception);
      stringBuilder.append("");
      Log.e("losg_log", stringBuilder.toString());
      return false;
    } 
  }
  
  public static String cmd(String[] paramArrayOfString) {
    try {
      Process process = (new ProcessBuilder(paramArrayOfString)).start();
      StringBuilder stringBuilder = new StringBuilder();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
      while (true) {
        String str = bufferedReader.readLine();
        if (str != null) {
          stringBuilder.append(str);
          continue;
        } 
        bufferedReader.close();
        return stringBuilder.toString().toLowerCase();
      } 
    } catch (Exception exception) {
      return "";
    } 
  }
  
  public static boolean cpuCheck() {
    String str = readCupInfo();
    return (!str.contains("intel") && !str.contains("amd"));
  }
  
  private static Object getActivityThread() {
    try {
      Field field = Class.forName("android.app.ActivityThread").getDeclaredField("sCurrentActivityThread");
      field.setAccessible(true);
      return field.get((Object)null);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  private static Object getActivityThreadHandler() {
    try {
      Object object = getActivityThread();
      Field field = object.getClass().getDeclaredField("mH");
      field.setAccessible(true);
      return field.get(null);
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public static boolean hasLightSensor(Context paramContext) {
    return (((SensorManager)paramContext.getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(5) != null);
  }
  
  public static boolean hasRoot() {
    return cmd(new String[] { "which", "su" }).contains("/su");
  }
  
  private static String readCupInfo() {
    return cmd(new String[] { "/system/bin/cat", "/proc/cpuinfo" });
  }
}
