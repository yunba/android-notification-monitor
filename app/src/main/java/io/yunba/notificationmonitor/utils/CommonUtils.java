package io.yunba.notificationmonitor.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.yunba.notificationmonitor.ILogger;
import io.yunba.notificationmonitor.R;

/**
 * Created by miao on 2017/2/24.
 */

public class CommonUtils {
    public static boolean hasPermission(Context context, String thePermission) {
        if (null == context || TextUtils.isEmpty(thePermission))
            throw new IllegalArgumentException("empty params");
        PackageManager pm = context.getPackageManager();
        if (pm.checkPermission(thePermission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private static boolean checkCanStoreDevice(Context context) {
        boolean perm = hasPermission(context, Manifest.permission.WRITE_SETTINGS);
        boolean extrernal = (isSdcardExist() && hasPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (!perm && !extrernal)
            return false;
        if (extrernal)
            return true;
        if (perm)
            return canReadAndWriteSettings(context);
        return true;

    }

    public static boolean isSdcardExist() {
        boolean ret = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        return ret;
    }

    private static boolean canReadAndWriteSettings(Context paramContext) {
        String str = "";
        try {
            str = Settings.System.getString(paramContext.getContentResolver(),
                    "IMEI");
            if (TextUtils.isEmpty(str))
                str = "";
            Settings.System.putString(paramContext.getContentResolver(),
                    "IMEI", str);
        } catch (Exception localException) {
            return false;
        }
        return true;
    }

    public static ArrayList<String> readLines(InputStream is) {
        ArrayList<String> data = new ArrayList<String>();

        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr, 2048);
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if ("".equals(line))
                    continue; // remove blank line
                data.add(line);
            }

            isr.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static int getColorFromLogLevel(Context context, int logLevel) {
        if (ILogger.danger == logLevel) {
            return context.getResources().getColor(R.color.yunba_danger);
        } else if (ILogger.primary == logLevel) {
            return context.getResources().getColor(R.color.yunba_primary);
        } else if (ILogger.success == logLevel) {
            return context.getResources().getColor(R.color.yunba_success);
        } else if (ILogger.waring == logLevel) {
            return context.getResources().getColor(R.color.yunba_warning);
        } else if (ILogger.info == logLevel) {
            return context.getResources().getColor(R.color.yunba_info);
        }
        return context.getResources().getColor(R.color.yunba_info);
    }
}
