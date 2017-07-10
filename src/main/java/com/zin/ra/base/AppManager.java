package com.zin.ra.base;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.zin.ra.processes.AndroidProcesses;
import com.zin.ra.processes.models.AndroidAppProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressLint("NewApi")
public class AppManager {
    public class AppInfo {
        public int versionCode = 0; // 名称
        public String appname = ""; // 包
        public String mainacname = ""; // 包
        public String packagename = "";
        public String versionName = ""; // 图标
        public String appClassName = ""; // 图标
        public Drawable appicon = null;
    }

    public static AppManager m_this = null;

    public HashMap<String, AppInfo> m_homeAppMap = null;
    public HashMap<String, AppInfo> m_browserAppMap = null;
    public HashMap<String, AppInfo> m_setupAppMap = null;
    public HashMap<String, String> m_blackAppMap = null;
    public ArrayList<String> m_updateUnllAppMap = null;

    public Context m_Context = null;

    public boolean m_IsInitBlack = false;


    private AppManager() {
        m_homeAppMap = new HashMap<String, AppInfo>();
        m_browserAppMap = new HashMap<String, AppInfo>();
        m_setupAppMap = new HashMap<String, AppInfo>();
        m_blackAppMap = new HashMap<String, String>();
        m_updateUnllAppMap = new ArrayList<String>();
    }

    public synchronized static AppManager getInstance() {
        if (m_this == null) {
            m_this = new AppManager();
        } else {

        }
        return m_this;
    }

    public synchronized static void deleteInstance() {
        if (m_this != null) {
            m_this.stop();
        } else {

        }
    }

    public void init(Context cx) {
        m_Context = cx;

        updateHomes();
        updateBrowser();
        UpdateAppList();
    }

    public void stop() {

    }

    public void updateHomes() {
        m_homeAppMap.clear();

        PackageManager packageManager = m_Context.getPackageManager();
        // 属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            AppInfo tmpInfo = new AppInfo();
            tmpInfo.packagename = ri.activityInfo.packageName;

            m_homeAppMap.put(ri.activityInfo.packageName, tmpInfo);
            // System.out.println("----pack" + ri.activityInfo.packageName);
        }
    }

    public void updateBrowser() {
        m_browserAppMap.clear();

        PackageManager packageManager = m_Context.getPackageManager();
        // 属性
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        Uri uri = Uri.parse("http://");
        intent.setDataAndType(uri, null);

        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
        for (ResolveInfo ri : resolveInfo) {
            AppInfo tmpInfo = new AppInfo();
            tmpInfo.packagename = ri.activityInfo.packageName;
            tmpInfo.mainacname = ri.activityInfo.name;

            m_browserAppMap.put(ri.activityInfo.packageName, tmpInfo);
        }
    }

    public HashMap<String, AppInfo> getSetupAppInfo() {
        HashMap<String, AppInfo> tmpMap = null;

        tmpMap = m_setupAppMap;

        return tmpMap;
    }

    public AppInfo getAppInfoByName(String sPackageName) {
        AppInfo tmpInfo = null;
        List<PackageInfo> packages = m_Context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (packageInfo.packageName.equals(sPackageName) == true) {
                tmpInfo = new AppInfo();
                tmpInfo.appname = packageInfo.applicationInfo.loadLabel(m_Context.getPackageManager()).toString();
                tmpInfo.packagename = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.appClassName = packageInfo.applicationInfo.className;
                tmpInfo.appicon = packageInfo.applicationInfo.loadIcon(m_Context.getPackageManager());
                break;
            }
        }

        return tmpInfo;
    }

    public Intent getAppInfo(String sPackageName) {
        Intent tmpInfo = null;

        // tmpInfo = m_Context.getPackageManager().getLaunchIntentForPackage(sPackageName);
        tmpInfo = GetIntent(sPackageName);

        return tmpInfo;
    }

    // 更新APP安装信息
    public ArrayList<AppInfo> UpdateAppList() {
        HashMap<String, AppInfo> tmpSetupAppMap = new HashMap<String, AppInfo>();
        ArrayList<AppInfo> tmpList = new ArrayList<AppInfo>();

        Iterator iter = m_setupAppMap.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry entry = (Entry) iter.next();
            String key = (String) entry.getKey();
            AppInfo val = (AppInfo) entry.getValue();
            tmpSetupAppMap.put(val.packagename, val);
        }

        m_setupAppMap.clear();

        List<PackageInfo> packages = m_Context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            AppInfo tmpInfo = new AppInfo();

            tmpInfo.appname = packageInfo.applicationInfo.loadLabel(m_Context.getPackageManager()).toString();

            tmpInfo.packagename = packageInfo.packageName;

            tmpInfo.versionName = packageInfo.versionName;
            tmpInfo.versionCode = packageInfo.versionCode;
            tmpInfo.appClassName = packageInfo.applicationInfo.className;

            tmpInfo.appicon = packageInfo.applicationInfo.loadIcon(m_Context.getPackageManager());

            m_setupAppMap.put(tmpInfo.packagename, tmpInfo);

            // 检查新增
            if (tmpSetupAppMap.get(tmpInfo.packagename) == null) {
                tmpList.add(tmpInfo);
            }
        }

        return tmpList;
    }

    // 判断是否桌面
    public boolean IsHomeApp(String sName) {
        boolean tmpRt = false;

        try {
            if (m_homeAppMap.get(sName) != null) {
                tmpRt = true;
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    // 判断是否浏览器
    public boolean IsBrowserApp(String sName) {
        boolean tmpRt = false;

        try {
            if (m_browserAppMap.get(sName) != null) {
                tmpRt = true;
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public AppInfo GetBrowserApp(String sName) {
        AppInfo tmpRt = null;

        try {
            if (sName.length() > 0) {
                tmpRt = m_browserAppMap.get(sName);
            } else {
                if (m_browserAppMap.size() > 0) {
                    Iterator iter = m_browserAppMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        HashMap.Entry entry = (Entry) iter.next();
                        String key = (String) entry.getKey();
                        tmpRt = (AppInfo) entry.getValue();
                        return tmpRt;
                    }
                }
            }

        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public boolean IsSystemApp(String sName) {
        boolean tmpRt = false;

        try {
            List<PackageInfo> packages = m_Context.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                if (sName.startsWith(packageInfo.packageName)) {
                    if (isSystemApp(packageInfo)) {
                        tmpRt = true;
                    }
                    return tmpRt;
                }
            }

            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                if (sName.startsWith(packageInfo.packageName)) {
                    if (isSystemApp(packageInfo)) {
                        tmpRt = true;
                    }
                    return tmpRt;
                }
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public boolean addBlackApp(String sName) {
        boolean tmpRt = false;

        try {
            if (m_blackAppMap.get(sName) == null) {
                m_blackAppMap.put(sName, sName);
                tmpRt = true;
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public boolean isAvilible(String packageName) {
        try {
            final PackageManager packageManager = m_Context.getPackageManager();
            // 获取所有已安装程序的包信息
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
            for (int i = 0; i < pinfo.size(); i++) {
                if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                    return true;
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return false;
    }

    public boolean updateBlackApp(ArrayList<String> sList) {
        boolean tmpRt = false;

        try {
            m_blackAppMap.clear();

            for (int i = 0; i < sList.size(); i++) {
                m_blackAppMap.put(sList.get(i), sList.get(i));
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public boolean updateUnllApp(ArrayList<String> sList) {
        boolean tmpRt = false;

        try {
            m_updateUnllAppMap.clear();

            for (int i = 0; i < sList.size(); i++) {
                m_updateUnllAppMap.add(sList.get(i));
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public boolean IsBlackApp(String sName) {
        boolean tmpRt = false;

        try {
            if (m_blackAppMap.get(sName) != null) {
                tmpRt = true;
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpRt;
    }

    public boolean IsCanShowAd(String sName) {
        boolean tmpRt = false;
        if (IsHomeApp(sName) == false && IsBlackApp(sName) == false && IsSystemApp(sName) == false) {
            return true;
        } else {
            return false;
        }
    }

    public String getCurrentPkgName(Context context) {
        String currentApp = null;

        // 根据SDK版本号来区分处理
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            // 需要system framework 目录权限
            final int PROCESS_STATE_TOP = 2;
            try {
                try {
                    List<AndroidAppProcess> tmpRun = AndroidProcesses.getRunningForegroundApps(context);
                    List<AndroidAppProcess> tmpRlt = new ArrayList<AndroidAppProcess>();

                    if (m_IsInitBlack == false) {
                        try {
                            m_blackAppMap.put(context.getPackageName(), context.getPackageName());
                            for (int i = 0; i < tmpRun.size(); i++) {
                                m_blackAppMap.put(tmpRun.get(i).getPackageName(), tmpRun.get(i).getPackageName());
                            }
                            m_IsInitBlack = true;
                        } catch (Throwable e) {
                            // TODO: handle exception
                        }
                    }


                    for (int i = 0; i < tmpRun.size(); i++) {
                        if (IsBlackApp(tmpRun.get(i).getPackageName()) == false) {
                            tmpRlt.add(tmpRun.get(i));
                        }
                    }
                    AndroidAppProcess tmpProcess = tmpRlt.get(0);

                    if (tmpRlt.size() > 1) {
                        Map<String, ProcessManager.Process> tmplist = ProcessManager.getRunningApps();

                        for (int i = 1; i < tmpRlt.size(); i++) {
                            try {
                                if (tmplist.get(tmpRlt.get(i).getPackageName()).systemTime < tmplist.get(tmpProcess.getPackageName()).systemTime) {
                                    tmpProcess = tmpRlt.get(i);
                                }
                            } catch (Throwable e) {
                                // TODO: handle exception
                            }

                        }

                    }
                    return tmpProcess.getPackageName();
                } catch (Throwable e) {
                    // TODO: handle exception
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
            return currentApp;
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> runningTaskInfos = am.getRunningTasks(1);
            ComponentName cn1 = runningTaskInfos.get(0).topActivity;
            currentApp = cn1.getPackageName();
        }

        return currentApp;
    }

    // 根据包名 启动APP
    public boolean StartAppByPackageNameInLock(String sPackageName) {
        boolean tmpRt = false;

        KeyguardManager mKeyguardManager;
        mKeyguardManager = (KeyguardManager) m_Context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isScreenOn = mKeyguardManager.inKeyguardRestrictedInputMode();//

        if (isScreenOn == true) {
            tmpRt = StartAppByPackageName(sPackageName);
        }

        return tmpRt;
    }

    public boolean IsinKeyguardRestrictedInputMode() {
        KeyguardManager mKeyguardManager;
        mKeyguardManager = (KeyguardManager) m_Context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isScreenOn = mKeyguardManager.inKeyguardRestrictedInputMode();//

        return isScreenOn;
    }

    public boolean StartAppByPackageName(String sPackageName) {
        boolean tmpRt = false;

        Intent tmpIt = GetIntent(sPackageName);
        if (tmpIt != null) {
            m_Context.startActivity(tmpIt);
            tmpRt = true;
        }

        if (tmpRt == false) {
            // 启动失败
        }

        return tmpRt;
    }

    // 根据包名获取指定应用
    public Intent GetIntent(String sPackageName) {
        Intent tmpRt = null;

        try {
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(sPackageName);

            PackageManager packageManager = m_Context.getPackageManager();
            List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
            if (apps.size() <= 0) {
                resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                resolveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resolveIntent.addCategory(Intent.CATEGORY_INFO);
                resolveIntent.setPackage(sPackageName);

                packageManager = m_Context.getPackageManager();
                apps = packageManager.queryIntentActivities(resolveIntent, 0);
            }

            if (apps.size() <= 0) {
                resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                resolveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                resolveIntent.addCategory(Intent.CATEGORY_DEFAULT);
                resolveIntent.setPackage(sPackageName);

                packageManager = m_Context.getPackageManager();
                apps = packageManager.queryIntentActivities(resolveIntent, 0);
            }

            if (apps.size() > 0) {
                ResolveInfo ri = apps.iterator().next();

                if (ri != null) {
                    String className = ri.activityInfo.name;

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClassName(sPackageName, className);
                    tmpRt = intent;
                }
            }
        } catch (Throwable e) {

        }

        return tmpRt;
    }

    public String getApkPachageName(String apkName) {
        String tmpNameString = "";

        try {
            PackageManager pm = m_Context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkName, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = null;
            if (info != null) {
                appInfo = info.applicationInfo;
                tmpNameString = appInfo.packageName;

            }
        } catch (Throwable e) {
            // TODO: handle exception
        }

        return tmpNameString;
    }

    /**
     * 判断是否为系统应用，是，返回true；不是返回false
     *
     * @param pInfo
     * @return
     */
    public static boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
}
