package com.lahm.library.AntiAospUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 功能简介：用于检测虚拟机的工具类
 * aosp:Android Open-Source Project 一般虚拟机都是基于这个开发的
 * 目前能检测到的模拟器有：MuMu模拟器、逍遥模拟器、蓝叠模拟器、夜神模拟器、雷电模拟器、480 * 800 分辨率的脚本
 */
public class AntiAospUtils {

    private static final String SCAN_DEVICE_TIME = "s_aosp_device_time";

    /**
     * 开始扫描设备信息
     *
     * @param accountId 账号，用于保存上一次扫描的时间，每隔 3 天才会扫描一次，如果扫描到模拟器就上报
     * @param contex
     */
    public static void startScanDeviceInfo(final String accountId, final Context contex) {
        startScanDeviceInfo(accountId, contex, null);
    }
//
//    public  static boolean checkDeviceInfo(Context context) {
//        ScanDevicePlanWrapper scanCpuInfo = new ScanDevicePlanWrapper(new ScanCpuInfo());
//        DeviceScanInfo deviceScanInfo = null;
//        try {
//            deviceScanInfo = scanCpuInfo.scanDevice(context);
//            // 判断是否可疑设备 或者是否模拟器设备，都需要上报
//            if (deviceScanInfo.isFaker() || deviceScanInfo.isBadDevice()) {
//                return  true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            return false;
//        }
//    }

    /**
     * 开始扫描设备信息
     *
     * @param accountId          账号，用于保存上一次扫描的时间，每隔 3 天才会扫描一次，如果扫描到模拟器就上报
     * @param context
     * @param scanDeviceListener 扫描完成回调
     */
    public static void startScanDeviceInfo(final String accountId, final Context context, final ScanDeviceListener scanDeviceListener) {
        ScanDevicePlanWrapper scanScreenInfo = new ScanDevicePlanWrapper(new ScanScreenInfo());
        ScanDevicePlanWrapper scanAppInfo = new ScanDevicePlanWrapper(new ScanAppInfo());
        ScanDevicePlanWrapper scanCpuInfo = new ScanDevicePlanWrapper(new ScanCpuInfo());
        scanCpuInfo.setNextScanDevicePlanWrapper(scanAppInfo);
        scanAppInfo.setNextScanDevicePlanWrapper(scanScreenInfo);
        DeviceScanInfo deviceScanInfo = null;
        try {
            deviceScanInfo = scanCpuInfo.scanDevice(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断是否可疑设备 或者是否模拟器设备，都需要上报
        if (deviceScanInfo.isFaker() || deviceScanInfo.isBadDevice()) {
//            LogUploadUtil.postAospDeviceLog(deviceScanInfo);
        }
        if (scanDeviceListener != null) {
            scanDeviceListener.onComplete(deviceScanInfo);
        }
    }

    private static class ScanDevicePlanWrapper implements ScanDevicePlanAble {

        /**
         * 下一个扫描器
         */
        public ScanDevicePlanWrapper mNextScanDevicePlanWrapper;

        public ScanDevicePlanAble mScanDevicePlan;

        public ScanDevicePlanWrapper(ScanDevicePlanAble scanDevicePlan) {
            mScanDevicePlan = scanDevicePlan;
        }

        public void setNextScanDevicePlanWrapper(ScanDevicePlanWrapper nextScanDevicePlanWrapper) {
            mNextScanDevicePlanWrapper = nextScanDevicePlanWrapper;
        }

        @Override
        public DeviceScanInfo scanDevice(Context context) throws Exception {
            DeviceScanInfo nextDeviceScanInfo = null;
            if (mNextScanDevicePlanWrapper != null) {
                nextDeviceScanInfo = mNextScanDevicePlanWrapper.scanDevice(context);
                // 判断是否需要扫描
                if (!isAllScanInfo() && nextDeviceScanInfo.isBadDevice()) {
                    return nextDeviceScanInfo;
                }
            }
            DeviceScanInfo currentDeviceScanInfo = mScanDevicePlan.scanDevice(context);
            if (nextDeviceScanInfo != null) {
                // 如果其他扫描器扫描出来有用的信息就保存下来
                String scanInfoTemp = nextDeviceScanInfo.getScanInfo();
                currentDeviceScanInfo.setScanInfo(scanInfoTemp + "  ||  " + currentDeviceScanInfo.getScanInfo());
                if (nextDeviceScanInfo.isBadDevice()) {
                    // 发现模拟器
                    currentDeviceScanInfo.setBadDevice(true);
                } else if (nextDeviceScanInfo.isFaker()) {
                    // 发现疑似模拟器
                    currentDeviceScanInfo.setFaker(true);
                }
            }
            return currentDeviceScanInfo;
        }

        @Override
        public boolean isAllScanInfo() {
            return mScanDevicePlan.isAllScanInfo();
        }
    }

    private interface ScanDevicePlanAble {

        /**
         * 扫描设备
         *
         * @param context
         * @return
         * @throws Exception
         */
        public DeviceScanInfo scanDevice(Context context) throws Exception;

        /**
         * 是否需要完整的扫描信息，因为这边的扫描器是链式的
         * return true 的情况下，会将所有的链式扫描器跑一遍，为的是完整的模拟器信息
         * return false 的情况下，只要有其中一个扫描器扫描到信息，本扫描器将不扫描信息
         *
         * @return
         */
        public boolean isAllScanInfo();
    }

    /**
     * 扫描 cpu 的架构信息
     */
    private static class ScanCpuInfo implements ScanDevicePlanAble {

        @Override
        public DeviceScanInfo scanDevice(Context context) throws Exception {
            if (DeviceUtils.checkDeviceForumX86()) {
                return new DeviceScanInfo("scanCpuInfo：x86 == true", true);
            } else {
                return new DeviceScanInfo("scanCpuInfo：x86 == false", false);
            }
        }

        /**
         * cpu 架构信息不重要，如果之前其他扫描器已经扫描到了，这里就不需要工作
         *
         * @return
         */
        @Override
        public boolean isAllScanInfo() {
            return false;
        }
    }

    /**
     * 针对 app 做扫描
     * 模拟器的系统应用都有一个特点，就是它们的 nativeLibraryDir 最终目录都是 x86
     * MuMu模拟器、逍遥模拟器、蓝叠模拟器、夜神模拟器、雷电模拟器 都经过验证，无一例外
     * 针对这个漏洞进行检测，准确率会比较高
     */
    private static class ScanAppInfo implements ScanDevicePlanAble {

        /**
         * 模拟器身上的标记
         */
        private static final String BAD_TAG = "x86";

        // --------------------- 需要扫描的包名 ---------------------
        /**
         * 拨打电话
         */
        private final String CALL = "com.android.server.telecom";

        /**
         * 通讯录
         */
        private final String CONTACTS = "com.android.contacts";

        /**
         * 网页渲染器
         */
        private final String WEB_VIEW = "com.android.webview";

        /**
         * 系统设置
         */
        private final String SYSTEM_SETTING = "com.android.settings";

        /**
         * Android 默认的浏览器
         */
        private final String SYSTEM_BROWSER = "com.android.browser";

        /**
         * 需要扫描的应用包名
         */
        private final String[] ALL_SCAN_PACKAGE_INFO = new String[]{CALL, CONTACTS, WEB_VIEW, SYSTEM_SETTING, SYSTEM_BROWSER};

        @Override
        public DeviceScanInfo scanDevice(Context context) throws Exception {
            // 判断是否扫描成功过
            // 正常的手机不太可能一个应用都没有找到
            // 如果出现这种情况的话，一般只有俩种可能，1、系统没给权限（默认都是给的）2、被 Hook 了
            // 这种情况下需要考虑一下这个设备是否是有问题的了
            boolean isScanDeviceComplete = false;
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                return new DeviceScanInfo("scanPackageInfo：packageManager == null", false, isScanDeviceComplete);
            }
            for (String scanPackageInfo : ALL_SCAN_PACKAGE_INFO) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(scanPackageInfo, PackageManager.GET_ACTIVITIES);
                    if (packageInfo != null) {
                        String nativeLibraryDir = packageInfo.applicationInfo.nativeLibraryDir;
                        // 如果 nativeLibraryDir 没有获取到的话，非常可疑
                        if (nativeLibraryDir != null) {
                            isScanDeviceComplete = true;
                            if (nativeLibraryDir.contains(BAD_TAG)) {
                                return new DeviceScanInfo("scanPackageInfo：" + scanPackageInfo + "." + BAD_TAG, true);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return new DeviceScanInfo("scanPackageInfo：scanPackageInfo == null", false, !isScanDeviceComplete);
        }

        @Override
        public boolean isAllScanInfo() {
            return true;
        }
    }

    /**
     * 扫描屏幕的宽高与物理尺寸来区分模拟器
     * 目前发现针对的脚本，都需要限定屏幕的尺寸，就算他进行了 hook 也不太可能针对获取屏幕分辨率进行处理
     * 所以这里检测屏幕分辨率
     */
    private static class ScanScreenInfo implements ScanDevicePlanAble {

        // --------------------- 可疑的屏幕分辨率 ---------------------
        /**
         * 貌似脚本会固定这个宽高，先检测看看
         */
        private static final Integer SCREEN_WIDTH[] = new Integer[]{480, 540};
        private static final Integer SCREEN_HEIGHT[] =  new Integer[]{800, 960};

        @Override
        public DeviceScanInfo scanDevice(Context context) throws Exception {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            // 判断是否是可疑宽高
            if (Arrays.asList(SCREEN_HEIGHT).contains(screenHeight) && Arrays.asList(SCREEN_WIDTH).contains(screenWidth)) {
                // 计算屏幕物理尺寸
                double diagonalPixels = Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));
                double size = new BigDecimal(diagonalPixels / (160 * dm.density)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return new DeviceScanInfo("ScanScreenInfo：badPixel width-" + screenWidth + "、height-" + screenHeight + "-size：" + size, true);
            }
            return new DeviceScanInfo("ScanScreenInfo：normal《 " + "width-" + screenWidth + "、height-" + screenHeight + " 》", false);
        }

        @Override
        public boolean isAllScanInfo() {
            return true;
        }
    }

    /**
     * 扫描设备回调
     */
    public interface ScanDeviceListener {

        public void onComplete(DeviceScanInfo info);
    }

    /**
     * 扫描的结果信息
     */
    public static class DeviceScanInfo {
        /**
         * 扫描的结果
         */
        private String mScanInfo = "";

        /**
         * 是否模拟器
         */
        private boolean isBadDevice = false;

        /**
         * 是否是可疑的设备
         */
        private boolean isFaker = false;

        public DeviceScanInfo(String scanInfo, boolean isBadDevice, boolean isFaker) {
            mScanInfo = scanInfo;
            this.isBadDevice = isBadDevice;
            this.isFaker = isFaker;
        }

        public DeviceScanInfo(String scanInfo, boolean isBadDevice) {
            mScanInfo = scanInfo;
            this.isBadDevice = isBadDevice;
        }

        public String getScanInfo() {
            return mScanInfo;
        }

        public void setScanInfo(String scanInfo) {
            mScanInfo = scanInfo;
        }

        public boolean isBadDevice() {
            return isBadDevice;
        }

        public void setBadDevice(boolean badDevice) {
            isBadDevice = badDevice;
        }

        public boolean isFaker() {
            return isFaker;
        }

        public void setFaker(boolean faker) {
            isFaker = faker;
        }

        @Override
        public String toString() {
            if (isBadDevice) {
                return "BadDevice: " + isBadDevice + "。" + mScanInfo;
            }
            return "Faker: " + isFaker + "，" + mScanInfo;
        }
    }
}


