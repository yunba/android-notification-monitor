package io.yunba.notificationmonitor.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class NotificationMonitor extends NotificationListenerService {
    private static final String TAG = "SevenNLS";
    private static final int EVENT_UPDATE_CURRENT_NOS = 0;
    public static final int EVENT_POST = 0x01;
    public static final int EVENT_REMOVE = 0x02;
    public static final String PAR_EVENT = "notification_event";
    public static final String PAR_EVENT_EXTRA = "notification_event_extra";
    public static final String PAR_NOTIFICATION_TITLE = "notification_title";
    public static final String PAT_NOTIFICATION_CONTENT = "notification_content";
    public static final String ACTION_NLS_CONTROL = "com.seven.notificationlistenerdemo.NLSCONTROL";
    public static List<StatusBarNotification[]> mCurrentNotifications = new ArrayList<StatusBarNotification[]>();
    public static int mCurrentNotificationsCounts = 0;
    public static StatusBarNotification mPostedNotification;
    public static StatusBarNotification mRemovedNotification;

    private Handler mMonitorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_UPDATE_CURRENT_NOS:
                    updateCurrentNotifications();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mMonitorHandler.sendMessage(mMonitorHandler.obtainMessage(EVENT_UPDATE_CURRENT_NOS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        updateCurrentNotifications();
        mPostedNotification = sbn;
        Intent intent = new Intent("io.yunba.notificationmonitor.NOTIFICATION_EVENT");
        intent.putExtra(PAR_EVENT, EVENT_POST);
        intent.putExtra(PAR_EVENT_EXTRA, sbn.getPackageName());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Bundle extras = sbn.getNotification().extras;
            intent.putExtra(PAR_NOTIFICATION_TITLE, extras.getString(Notification.EXTRA_TITLE));
        }
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        updateCurrentNotifications();
        mRemovedNotification = sbn;
    }

    private void updateCurrentNotifications() {
        try {
            StatusBarNotification[] activeNos = getActiveNotifications();
            if (mCurrentNotifications.size() == 0) {
                mCurrentNotifications.add(null);
            }
            mCurrentNotifications.set(0, activeNos);
            mCurrentNotificationsCounts = activeNos.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static StatusBarNotification[] getCurrentNotifications() {
        if (mCurrentNotifications.size() == 0) {
            return null;
        }
        return mCurrentNotifications.get(0);
    }
}
