package com.lahm.easyprotector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lahm.library.DeviceCheck;

import java.util.Iterator;

import androidx.appcompat.app.AppCompatActivity;

public class VirtualActivity extends AppCompatActivity {
  public void checkCpu(View paramView) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("checkCpu是否异常:");
    stringBuilder.append(DeviceCheck.cpuCheck() ^ true);
    Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
  }
  
  public void checkDump(View paramView) {
    paramView = null;
    try {
      int j = 1 / 0;
    } catch (Exception exception) {
      StringBuilder stringBuilder2 = new StringBuilder();
      StackTraceElement[] arrayOfStackTraceElement = exception.getStackTrace();
      for (int i = 0; i < arrayOfStackTraceElement.length; i++) {
        String str = arrayOfStackTraceElement[i].toString();
        if (!str.contains("android") && !str.contains("java.lang") && !str.contains(getPackageName())) {
          stringBuilder2.append(str);
          stringBuilder2.append("\n");
        }
      }
      if (TextUtils.isEmpty(stringBuilder2.toString())) {
        Toast.makeText((Context) this, "未发现异常", Toast.LENGTH_SHORT).show();
        return;
      }
      StringBuilder stringBuilder1 = new StringBuilder();
      stringBuilder1.append("发现异常:\n");
      stringBuilder1.append(stringBuilder2.toString());
      Toast.makeText((Context) this, stringBuilder1.toString(), 0).show();
    }
  }
  
  public void checkFile(View paramView) {
    boolean bool;
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("FileHook:");
    if (DeviceCheck.checkIsFileHook() || DeviceCheck.checkFileLocation((Context)this)) {
      bool = true;
    } else {
      bool = false;
    } 
    stringBuilder.append(bool);
    Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
  }
  
  public void checkHandler(View paramView) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("handler:");
    stringBuilder.append(DeviceCheck.checkIsHandlerHook());
    Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
  }
  
  public void checkHook(View paramView) {
    boolean bool = DeviceCheck.checkIsLocationHook((Context)this);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("检测定位是否被修改结果:");
    stringBuilder.append(bool);
    Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
  }
  
  public void checkLocation(View paramView) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("checkLocation:");
    stringBuilder.append(DeviceCheck.checkIsLocationHook((Context)this));
    Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
  }
  
  public void checkRoot(View paramView) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("root:");
    stringBuilder.append(DeviceCheck.hasRoot());
    Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
  }
  
  public void checkVirLocal(View paramView) {
    try {
      if (Settings.Secure.getInt(getContentResolver(), "mock_location") == 1) {
        Toast.makeText((Context)this, "虚拟定位打开,请选关闭", 0).show();
        return;
      } 
      String str = findMockPermissionApps((Context)this);
      if (!TextUtils.isEmpty(str)) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("检测到虚拟定位软件:\n");
        stringBuilder.append(str);
        stringBuilder.append("\n请先卸载");
        Toast.makeText((Context)this, stringBuilder.toString(), 0).show();
        return;
      } 
    } catch (Exception exception) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(exception);
      stringBuilder.append("");
      Log.e("losg_log", stringBuilder.toString());
    } 
    Toast.makeText((Context)this, "未发现异常", 0).show();
  }
  
  public String findMockPermissionApps(Context paramContext) {
    String str2 = "";
    PackageManager packageManager = paramContext.getPackageManager();
    Iterator<ApplicationInfo> iterator = packageManager.getInstalledApplications(PackageManager.GET_META_DATA).iterator();
    String str1 = str2;
    while (iterator.hasNext()) {
      ApplicationInfo applicationInfo = iterator.next();
      str2 = str1;
      try {
        String[] arrayOfString = (packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS)).requestedPermissions;
        String str = str1;
        if (arrayOfString != null) {
          int i = 0;
          while (true) {
            str = str1;
            str2 = str1;
            if (i < arrayOfString.length) {
              str = str1;
              str2 = str1;
              if (arrayOfString[i].equals("android.permission.ACCESS_MOCK_LOCATION")) {
                str2 = str1;
                StringBuilder stringBuilder = new StringBuilder();
                str2 = str1;
                stringBuilder.append(str1);
                str2 = str1;
                stringBuilder.append(packageManager.getApplicationLabel(applicationInfo));
                str2 = str1;
                stringBuilder.append(",");
                str2 = str1;
                str = stringBuilder.toString();
              } 
              i++;
              str1 = str;
              continue;
            } 
            break;
          } 
        } 
        str1 = str;
      } catch (Exception exception) {
        str1 = str2;
      } 
    } 
    str2 = str1;
    if (!TextUtils.isEmpty(str1))
      str2 = str1.substring(0, str1.length() - 1); 
    return str2;
  }
  
  protected void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    setContentView(R.layout.activity_virtualmain);
  }
}
